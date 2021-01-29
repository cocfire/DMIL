package com.routon.plcloud.device.api.controller.demo;

import com.routon.plcloud.device.core.service.HelloService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author FireWang
 * @date 2020/4/26 17:15
 * 示例接口Controller代码
 * 关于@Controller 和@RestController：
 * 都可以在前端调通接口，但是二者的区别在于，当用前者的时候在方法上必须添加注解@ResponseBody；
 * 如果不添加@ResponseBody，就会报上面错误，因为当使用@Controller 注解时，spring默认方法返回的是view对象（页面）；
 * 而加上@ResponseBody，则方法返回的就是具体对象了。@RestController的作用就相当于@Controller+@ResponseBody的结合体。
 */
@RestController
public class HelloWorldController {
    @Autowired
    private HelloService helloService;

    /**
     * 展示数据库数据
     *
     * @return
     */
    @GetMapping("/hello")
    public String hello() {
        String msg = "Hello, SpringBoot!";
        String msgfromcore = helloService.sayhello();
        if (msgfromcore != null) {
            msg = msgfromcore;
        }
        return msg;
    }

    /**
     * 向数据库添加一条数据
     *
     * @return
     */
    @GetMapping("/addhello")
    public String addhello() {
        String msg = "Hello, SpringBoot!";
        String msgfromcore = helloService.addhello();
        if (msgfromcore != null) {
            msg = msgfromcore;
        }
        return msg;
    }

    /**
     * 清空示例表格数据
     *
     * @return
     */
    @GetMapping("/deletehello")
    public String deletehello() {
        String msg = "Hello, SpringBoot!";
        String msgfromcore = helloService.deletehello();
        if (msgfromcore != null) {
            msg = msgfromcore;
        }
        return msg;
    }
}
