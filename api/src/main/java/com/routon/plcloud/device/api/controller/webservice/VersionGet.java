package com.routon.plcloud.device.api.controller.webservice;

import com.alibaba.fastjson.JSONObject;
import com.routon.plcloud.device.api.config.ServerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author FireWang
 * @date 2020/12/22 15:06
 * 获取系统版信息
 */
@Controller
@RequestMapping(value = "/WebService/version")
public class VersionGet {
    private static Logger logger = LoggerFactory.getLogger(VersionGet.class);

    @Autowired
    private ServerConfig serverConfig;

    /**
     * 播放记录上报
     *
     * @return
     */
    @RequestMapping(value = "/getversion", method = RequestMethod.GET)
    public @ResponseBody JSONObject getversion() {
        //创建应答对象
        JSONObject versionOb = new JSONObject();
        int transCode = 1001, result = 0;
        String message = "SUCCESS";
        try {
            versionOb.put("version", serverConfig.getVersionInfo());

        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e.getMessage());
            result = -1;
            message = "无法获取版本信息！";
        }
        //组织应答报文
        versionOb.put("result", result);
        versionOb.put("message", message);

        return versionOb;
    }

}
