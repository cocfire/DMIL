package com.routon.plcloud.device.api.controller.otherdevice;

import com.routon.plcloud.device.api.config.ServerConfig;
import com.routon.plcloud.device.core.service.DeviceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @ClassName MegViiController
 * @Description 旷视科技的设备接入
 * @Author shiquan
 * @Date 2020/5/8 9:23
 */
@Controller
@RequestMapping(value = "/megvii")
public class MegViiController {
    private Logger logger = LoggerFactory.getLogger(MegViiController.class);
    private static String termName = "iDR700-MGVI-";

    @Autowired
    private ServerConfig serverConfig;

    @Autowired
    private DeviceService deviceService;


}

























