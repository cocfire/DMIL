package com.routon.plcloud.device.api.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @ClassName:
 * @Description:
 * @Author: fire
 * @Time: 2020-04-30 01:23
 **/
@Controller
@RequestMapping(value = "/syslog")
public class SyslogController {

    /* 系统日志页面初始化 */
    @RequestMapping(value = "/list")
    public String listinit(){

        return "thymeleaf/syslog";
    }
}