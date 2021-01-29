package com.routon.plcloud.device.api.controller;

import com.alibaba.fastjson.JSONObject;
import com.routon.plcloud.device.api.config.ServerConfig;
import com.routon.plcloud.device.api.config.emq.EmqClient;
import com.routon.plcloud.device.api.config.UserProfile;
import com.routon.plcloud.device.core.utils.ConvUtil;
import com.routon.plcloud.device.core.utils.EncodeUtils;
import com.routon.plcloud.device.core.service.SyslogService;
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
import java.util.*;

/**
 * @author FireWang
 * @date 2020/4/27 16:09
 * 登录管理
 */
@Controller
public class LoginController {
    private Logger logger = LoggerFactory.getLogger(LoginController.class);

    @Autowired
    private EncodeUtils encodeUtil;

    @Autowired
    private UserinfoService userinfoService;

    @Autowired
    private SyslogService syslogService;

    @Autowired
    private EmqClient emqClient;

    @Autowired
    private ServerConfig serverConfig;

    private String success = "success";

    /**
     * 登录页面初始化
     *
     * @return
     */
    @RequestMapping(value = "/login" )
    public String logininit() {
        /*EMQ启动服务*/
        if (!emqClient.emqIsConnected()) {
            emqClient.emqBoot();
        }
        return "login";
    }

    /**
     * 执行登录
     *
     * @param request
     * @param account
     * @param password
     * @return
     */
    @RequestMapping(value = "/dologin" )
    @ResponseBody
    public JSONObject login(HttpServletRequest request, String account, String password, String phone) {
        HttpSession session = request.getSession();
        JSONObject resJson = new JSONObject();
        String msg = "success";
        try {
            Userinfo user;
            if (!"".equals(phone)) {
                user = userinfoService.getUserByPhone(phone);
            } else {
                user = userinfoService.getUserByUerName(account);
            }

            if (user == null) {
                msg = "登录失败：账号不存在！";
                resJson.put("obj1", msg);
                return resJson;
            }
            if (!user.getPassword().equals(encodeUtil.getPasswordMd5(password))) {
                msg = "登录失败：账号或密码错误！";
                resJson.put("obj1", msg);
                return resJson;
            }
            if (ConvUtil.convToInt(user.getStatus()) != 1) {
                msg = "登录失败：该账号已被禁用！";
                resJson.put("obj1", msg);
                return resJson;
            }
            //校验页面权限，没有权限直接拒绝访问，有权限则返回第一个页面序号
            String permission = ConvUtil.convToStr(user.getProject());
            if ("".equals(permission)) {
                msg = "登录失败：该账号没有权限！";
                resJson.put("obj1", msg);
                return resJson;
            } else {
                int admin = 888;
                if (ConvUtil.convToInt(user.getId()) != admin) {
                    String[] pageNums = permission.split(",");
                    resJson.put("pageOrder", ConvUtil.convToInt(pageNums[0]));
                } else {
                    resJson.put("pageOrder", 1);
                }
            }

            UserProfile userProfile = new UserProfile();
            userProfile.setCurrentUserId(user.getId());
            userProfile.setCurrentUserLoginName(user.getUsername());
            userProfile.setCurrentUserstatus(1);
            userProfile.setCurrentUserLoginIp(serverConfig.getIpAddr(request));
            userProfile.setCurrentUserCompany(user.getCompany());
            userProfile.setUserPermission(user.getProject());

            /* 更新登录信息 */
            user.setLastlogintime(new Date());
            user.setLastloginip(serverConfig.getIpAddr(request));
            if (userinfoService.saveAdmin(user)) {
                msg = "success";

                //添加系统日志
                Map<String, Object> paramMap = new HashMap<>(16);
                paramMap.put("type", 10);
                paramMap.put("loginfo", "用户(" + userProfile.getCurrentUserLoginName() + ")登录平台" );
                paramMap.put("userid", userProfile.getCurrentUserId());
                paramMap.put("userip", userProfile.getCurrentUserLoginIp());
                syslogService.addSyslog(paramMap);
            } else {
                logger.info("修改失败：更新用户数据异常！" );
            }

            //登录信息加入session
            session.setAttribute("userProfile", userProfile);

            resJson.put("obj1", msg);
            return resJson;
        } catch (Exception e) {
            logger.error(e.getMessage());
            e.printStackTrace();
            msg = "登录失败：系统异常！";
            resJson.put("obj1", msg);
            return resJson;
        }
    }

    /**
     * 执行登出
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/dologout" )
    public String logout(HttpServletRequest request) {
        HttpSession session = request.getSession();
        //使session失效，从而登出
        session.invalidate();
        return "login";
    }

    /**
     * 修改密码
     *
     * @param request
     * @param oldPwd
     * @param newPwd
     * @param newPwdConfirm
     * @return
     */
    @RequestMapping(value = "/changepwd" )
    @ResponseBody
    public JSONObject changepwd(HttpServletRequest request, String oldPwd, String newPwd, String newPwdConfirm) {
        HttpSession session = request.getSession();
        JSONObject resJson = new JSONObject();
        String msg = "success";
        try {
            UserProfile userProfile = (UserProfile) session.getAttribute("userProfile" );
            Userinfo user = userinfoService.getUserById(userProfile.getCurrentUserId());
            Map<String, Object> paramMap = new HashMap<>(16);
            if (user != null) {
                if (user.getPassword().equals(encodeUtil.getPasswordMd5(oldPwd))) {
                    if (oldPwd.equals(newPwd)) {
                        msg = "修改失败：新密码不能与旧密码相同！";
                    } else if (newPwd.equals(newPwdConfirm)) {
                        user.setPassword(encodeUtil.getPasswordMd5(newPwd));
                        if (userinfoService.saveAdmin(user)) {

                            //添加系统日志
                            paramMap.put("type", 11);
                            paramMap.put("loginfo", "用户(" + userProfile.getCurrentUserLoginName() + ")修改密码" );
                            paramMap.put("userid", userProfile.getCurrentUserId());
                            paramMap.put("userip", userProfile.getCurrentUserLoginIp());
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

            if (!msg.equals(success)) {
                paramMap.put("type", 11);
                paramMap.put("loginfo", "用户(" + userProfile.getCurrentUserLoginName() + ")修改密码" );
                paramMap.put("userid", userProfile.getCurrentUserId());
                paramMap.put("userip", userProfile.getCurrentUserLoginIp());
                paramMap.put("remark", msg);
            }
            syslogService.addSyslog(paramMap);
        } catch (Exception e) {
            logger.error(e.getMessage());
            e.printStackTrace();
            msg = "修改失败：系统异常！";
        }
        resJson.put("obj1", msg);
        return resJson;
    }
}
