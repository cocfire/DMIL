package com.routon.plcloud.device.api.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author FireWang
 * @date 2020/4/27 16:09
 */
@Controller
public class ThemleafController {
    @RequestMapping(value = "/index")
    public String toindex(){

        /* 这里返回页面路径 */
        return "/index";
    }
}
