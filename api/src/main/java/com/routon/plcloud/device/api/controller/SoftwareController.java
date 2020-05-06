package com.routon.plcloud.device.api.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author FireWang
 * @date 2020/5/6 22:12
 */
@Controller
@RequestMapping(value = "/software")
public class SoftwareController {

    /* 软件管理页面初始化 */
    @RequestMapping(value = "/list")
    public String listinit(){

        return "thymeleaf/software";
    }

}
