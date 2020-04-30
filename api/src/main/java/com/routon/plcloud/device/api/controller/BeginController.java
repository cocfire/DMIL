package com.routon.plcloud.device.api.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;


/**
 * @author FireWang
 * @date 2020/4/30 11:11
 */
@Controller
public class BeginController {

    //初始化导航页面
    @RequestMapping(value = "/begin")
    public String begininit() {


        return "begin";
    }


}
