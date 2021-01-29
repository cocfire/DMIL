package com.routon.plcloud.device.api.controller.demo;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author FireWang
 * @date 2020/4/30 11:11
 */
@Controller
public class BeginController {

    /**
     * 初始化导航页面
     *
     * @return
     */
    @RequestMapping(value = "/begin")
    public String begininit() {
        //打开导航页
        return "thymeleaf/begin";
    }

}
