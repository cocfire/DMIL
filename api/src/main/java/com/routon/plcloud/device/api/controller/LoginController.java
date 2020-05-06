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
import java.util.Date;

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

    /* 登录页面初始化 */
    @RequestMapping(value = "/login")
    public String logininit(){
        return "login";
    }

    /* 执行登录 */
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

                    /* 更新登录信息 */
                    admin.setLastlogintime(new Date());
                    admin.setLastloginip(LoginController.getIpAddr(request));
                    if (userinfoService.saveadmin(admin)) {
                        msg = "success";
                    } else {
                        msg = "修改失败：数据异常！";
                    }
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
            resJson.put("obj1", e.getMessage());
            return resJson;
        }
        resJson.put("obj1", msg);
        return resJson;
    }

    /* 执行登出 */
    @RequestMapping(value = "/dologout")
    public String logout(HttpServletRequest request){
        HttpSession session = request.getSession();
        //使session失效，从而登出
        session.invalidate();
        return "login";
    }

    /* 修改密码 */
    @RequestMapping(value = "/changepwd")
    @ResponseBody
    public JSONObject changepwd(HttpServletRequest request, String oldPwd, String newPwd, String newPwdConfirm){
        HttpSession session = request.getSession();
        JSONObject resJson = new JSONObject();
        String msg = "修改失败：系统异常！";
        try {
            UserProfile userProfile = (UserProfile) session.getAttribute("userProfile" );
            Userinfo admin = userinfoService.getadmin();
            if (admin != null) {
                if (admin.getPassword().equals(encodeUtil.getPasswordMD5(oldPwd))) {
                    if (oldPwd.equals(newPwd)) {
                        msg = "修改失败：新密码不能与旧密码相同！";
                    } else if (newPwd.equals(newPwdConfirm)) {
                        admin.setPassword(encodeUtil.getPasswordMD5(newPwd));
                        if (userinfoService.saveadmin(admin)) {
                            msg = "success";
                            session.invalidate();
                        } else {
                            msg = "修改失败：数据异常！";
                        }
                    } else {
                        msg = "修改失败：确认密码与新密码不一致！";
                    }
                } else {
                    msg = "修改失败：旧密码错误！";
                }
            } else {
                msg = "修改失败：用户不存在！";
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
            resJson.put("obj1", msg);
            return resJson;
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
