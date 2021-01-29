package com.routon.plcloud.device.core.serviceimpl;

import com.routon.plcloud.device.core.service.HelloService;
import com.routon.plcloud.device.data.entity.Helloworld;
import com.routon.plcloud.device.data.mapper.HelloworldMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * @author FireWang
 * @date 2020/4/26 19:02
 */
@Service
public class HelloServiceImpl implements HelloService {

    @Autowired
    private HelloworldMapper helloworldMapper;

    @Override
    public String sayhello() {
        String what2see = "Hello, SpringBoot!(from core)";
        List<Map> saysomething = helloworldMapper.selectBySql("select * from helloworld");
        if (saysomething.size() > 0) {
            what2see = saysomething.toString();
        }
        return what2see;
    }

    @Override
    public String addhello() {
        String what2see = null;
        Helloworld hw = new Helloworld();
        hw.setApi("从这里插入数据");
        hw.setCore("从这里处理逻辑");
        hw.setData("从这里操作datsbass");
        int cunms = helloworldMapper.insertNew(hw);
        if (cunms > 0) {
            what2see = helloworldMapper.selectBySql("select * from helloworld").toString();
        }
        return what2see;
    }

    @Override
    public String deletehello() {
        String what2see = "数据库未执行删除操作！";
        int cunms = helloworldMapper.deleteall();
        if (cunms > 0) {
            what2see = "数据库已清空";
        }
        return what2see;
    }
}
