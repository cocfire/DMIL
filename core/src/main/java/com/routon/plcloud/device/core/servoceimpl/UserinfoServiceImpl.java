package com.routon.plcloud.device.core.servoceimpl;

import com.routon.plcloud.device.core.service.UserinfoService;
import com.routon.plcloud.device.data.entity.Userinfo;
import com.routon.plcloud.device.data.mapper.UserinfoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * @author FireWang
 * @date 2020/5/6 13:25
 */
@Service
public class UserinfoServiceImpl implements UserinfoService {
    @Autowired
    private UserinfoMapper userinfoMapper;

    @Override
    public Userinfo getadmin() {
        List<Userinfo> users = userinfoMapper.selectBySql("select * from userinfo where status = 1");
        Userinfo admin = new Userinfo();
        if (users.size() > 0) {
            admin = users.get(0);
        } else {
            admin = null;
        }
        return admin;
    }

    @Override
    public boolean saveadmin(Userinfo user) {
        boolean succ = false;
        user.setModifytime(new Date());
        int ns = userinfoMapper.uopdateBySql(user);
        if (ns > 0) {
            succ = true;
        }
        return succ;
    }


}
