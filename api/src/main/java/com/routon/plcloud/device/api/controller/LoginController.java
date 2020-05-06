package com.routon.plcloud.device.api.controller;

import com.alibaba.fastjson.JSONObject;
import com.routon.plcloud.device.api.Utils.EncodeUtils;
import com.routon.plcloud.device.api.config.RouteFilter;
import com.routon.plcloud.device.api.config.UserProfile;
import com.routon.plcloud.device.core.service.UserinfoService;
import com.routon.plcloud.device.data.entity.Userinfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * @author FireWang
 * @date 2020/4/27 16:09
 */
@Controller
public class LoginController {
    private Logger logger = LoggerFactory.getLogger(RouteFilter.class);

    @Autowired
    private EncodeUtils encodeUtil;

    @Autowired
    private UserinfoService userinfoService;

    @RequestMapping(value = "/login")
    public String logininit(){

        //登录页面初始化
        return "login";
    }

    @RequestMapping(value = "/dologin")
    @ResponseBody
    public JSONObject login(HttpServletRequest request, String account, String password){
        HttpSession session = request.getSession();
        JSONObject resJson = new JSONObject();
        String msg = "登录失败！";
        UserProfile userProfile = null;
        try {
            Userinfo admin = userinfoService.getadmin();
            if(admin != null){
                if(admin.getUsername().equals(account) && admin.getPassword().equals(encodeUtil.getPasswordMD5(password))){
                    userProfile = new UserProfile();
                    userProfile.setCurrentUserId(admin.getId());
                    userProfile.setCurrentUserLoginName(admin.getUsername());
                    userProfile.setCurrentUserstatus(1);
                    userProfile.setCurrentUserLoginIp(LoginController.getIpAddr(request));
                    msg = "success";
                } else {
                    msg = "登录失败：账号或密码错误！";
                }
            } else {
                msg = "登录失败：账号不存在！";
            }
            //登录信息加入session
            session.setAttribute("userProfile", userProfile);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        resJson.put("obj1", msg);
        return resJson;
    }

    /**
     * 获取ip
     */
    public static String getIpAddr(HttpServletRequest request) {
        String ip = request.getHeader(" x-forwarded-for ");
        if (ip == null || ip.length() == 0 || " unknown ".equalsIgnoreCase(ip)) {
            ip = request.getHeader(" Proxy-Client-IP ");
        }
        if (ip == null || ip.length() == 0 || " unknown ".equalsIgnoreCase(ip)) {
            ip = request.getHeader(" WL-Proxy-Client-IP ");
        }
        if (ip == null || ip.length() == 0 || " unknown ".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }


}
