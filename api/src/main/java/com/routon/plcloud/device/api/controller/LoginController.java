package com.routon.plcloud.device.api.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author FireWang
 * @date 2020/4/27 16:09
 */
@Controller
public class LoginController {

    @RequestMapping(value = "/login")
    public String logininit(){

        //登录页面初始化
        return "login";
    }

    @RequestMapping(value = "/dologin")
    public String login(){
        System.out.println("登录请求。。。。。。。。。。。。。。。。");



        return "login";
    }


}
