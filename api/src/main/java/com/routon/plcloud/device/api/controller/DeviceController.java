package com.routon.plcloud.device.api.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author FireWang
 * @date 2020/4/29 22:12
 */
@Controller
@RequestMapping(value = "/device")
public class DeviceController {

    /* 初始化设备管理 */
    @RequestMapping(value = "/list")
    public String listinit() {

        return "thymeleaf/device";
    }

}
