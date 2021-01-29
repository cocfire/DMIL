package com.routon.plcloud.device.api.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.routon.plcloud.device.api.config.UserProfile;
import com.routon.plcloud.device.api.controller.commom.CommonForUser;
import com.routon.plcloud.device.api.utils.PageUtil;
import com.routon.plcloud.device.core.common.TreeBean;
import com.routon.plcloud.device.core.service.MenuService;
import com.routon.plcloud.device.core.service.SyslogService;
import com.routon.plcloud.device.core.service.UserinfoService;
import com.routon.plcloud.device.core.utils.ConvUtil;
import com.routon.plcloud.device.core.utils.EncodeUtils;
import com.routon.plcloud.device.data.entity.Menu;
import com.routon.plcloud.device.data.entity.Userinfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author FireWang
 * @date 2020/6/8 16:14
 * 用户管理
 */
@Controller
@RequestMapping(value = "/user")
public class UserController {
    private static Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private SyslogService syslogService;

    @Autowired
    private EncodeUtils encodeUtil;

    @Autowired
    private UserinfoService userinfoService;

    @Autowired
    private MenuService menuService;

    private String success = "success";

    /**
     * 初始化用户管理
     *
     * @param model
     * @param companyid
     * @param companyname
     * @param msg
     * @param username
     * @param phone
     * @param remark
     * @param status
     * @param page
     * @param pageSize
     * @param maxPage
     * @param columns
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/list")
    public String listinit(HttpServletRequest request, Model model, Integer companyid, String companyname, String msg,
                           String username, String phone, String remark, Integer status,
                           Integer page, Integer pageSize, Integer maxPage, Integer columns) throws Exception {
        try {
            //当前用户信息
            model = CommonForUser.getUserinfo(model, request);

            /* 获取软件树形分级目录 */
            List<TreeBean> companyTree = userinfoService.getCompanyTree();

            /* 添加查询条件 */
            Map<String, Object> paramMap = new HashMap<>(16);
            paramMap.put("username", username);
            paramMap.put("phone", phone);
            paramMap.put("company", companyid);
            paramMap.put("remark", remark);
            paramMap.put("a.status", status);

            /* 设置最大页码，翻页用 */
            columns = userinfoService.getMaxCount(paramMap);
            PageUtil currentPage = new PageUtil(page, pageSize, columns);
            model = CommonForUser.initPage(model, currentPage);

            /* 获取设备信息列表 */
            List<Userinfo> userlist = userinfoService.searchUserforpage(paramMap, currentPage.getPage(), currentPage.getPageSize());

            /* 设置返回页面的数据----分级目录数据 */
            //分级目录
            model.addAttribute("menuTreeBeans", JSONArray.toJSON(companyTree));
            //选中数据的项目
            model.addAttribute("company", companyid);

            //还原查询条件
            model.addAttribute("username", username);
            model.addAttribute("phone", phone);
            model.addAttribute("remark", remark);
            model.addAttribute("status", status);

            //数据列表
            model.addAttribute("datalist", userlist);

            /*返回信息展示*/
            if (!"".equals(ConvUtil.convToStr(msg))) {
                model.addAttribute("msg", msg);
            }

        } catch (Exception e) {
            logger.error(e.getMessage());
            e.printStackTrace();
        }
        return "thymeleaf/user";
    }

    /**
     * 添加用户
     *
     * @param username
     * @param phone
     * @param newPwd
     * @param newPwdConfirm
     * @param realname
     * @param company
     * @param userpermission
     * @return
     */
    @RequestMapping(value = "/adduser")
    @ResponseBody
    public JSONObject adduser(HttpServletRequest request, String username, String phone, String newPwd, String newPwdConfirm,
                              String realname, Integer company, String userpermission) {
        UserProfile userProfile = (UserProfile) request.getSession().getAttribute("userProfile");
        JSONObject jsonMsg = new JSONObject();
        String msg = "success";
        try {
            boolean addflag = true;
            //检查用户必填
            if ("".equals(username) || "".equals(phone) || "".equals(newPwd)) {
                msg = "保存失败：请填写完整带“*”号的信息！";
                addflag = false;
            }

            //检查用户是否存在，若不存在则可插入
            if (userinfoService.getUserByUerName(username.trim()) != null) {
                msg = "保存失败：用户名已存在！";
                addflag = false;
            }
            if (userinfoService.getUserByPhone(phone.trim()) != null) {
                msg = "保存失败：手机号已存在！";
                addflag = false;
            }
            if (!newPwd.equals(newPwdConfirm)) {
                msg = "保存失败：登录密码与确认密码不一致！";
                addflag = false;
            }

            if (addflag) {
                Userinfo userinfo = new Userinfo();
                userinfo.setUsername(username);
                userinfo.setPhone(phone);
                userinfo.setPassword(encodeUtil.getPasswordMd5(newPwd));
                userinfo.setRealname(ConvUtil.convToStr(realname));
                userinfo.setCompany(ConvUtil.convToStr(company));
                userinfo.setProject(ConvUtil.convToStr(userpermission));

                if (!userinfoService.addUser(userinfo)) {
                    msg = "保存失败：系统异常！";
                } else {
                    //添加系统日志
                    if (msg.equals(success)) {
                        Map<String, Object> paramMap = new HashMap<>(16);
                        paramMap.put("type", 12);
                        paramMap.put("loginfo", "用户(" + userProfile.getCurrentUserLoginName() + ")添加用户：" + username);
                        paramMap.put("userid", userProfile.getCurrentUserId());
                        paramMap.put("userip", userProfile.getCurrentUserLoginIp());
                        syslogService.addSyslog(paramMap);
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            msg = "保存失败：系统异常！";
        }
        jsonMsg.put("obj1", msg);
        return jsonMsg;
    }


    /**
     * 编辑用户
     *
     * @param id
     * @param pwdReset
     * @param status
     * @param delflag
     * @return
     */
    @RequestMapping(value = "/edituser")
    @ResponseBody
    public JSONObject edituser(HttpServletRequest request, Integer id, String pwdReset, String status, String delflag) {
        UserProfile userProfile = (UserProfile) request.getSession().getAttribute("userProfile");
        JSONObject jsonMsg = new JSONObject();
        String msg = "操作成功！";
        try {
            if (userProfile.isTheAdmin()) {
                Userinfo user = userinfoService.getUserById(id);
                //检查用户是否存在
                if (user != null) {
                    if (!"".equals(ConvUtil.convToStr(pwdReset))) {
                        user.setPassword(encodeUtil.getPasswordMd5("plsy1234"));
                        userinfoService.saveAdmin(user);
                        msg = "密码已重置！";

                        //添加系统日志
                        Map<String, Object> paramMap = new HashMap<>(16);
                        paramMap.put("type", 14);
                        paramMap.put("loginfo", "用户(" + userProfile.getCurrentUserLoginName() + ")重置用户(" + user.getUsername() + ")密码：plsy1234");
                        paramMap.put("userid", userProfile.getCurrentUserId());
                        paramMap.put("userip", userProfile.getCurrentUserLoginIp());
                        syslogService.addSyslog(paramMap);
                    }
                    if (!"".equals(ConvUtil.convToStr(status))) {
                        if ("".equals(ConvUtil.convToStr(user.getCompany())) && ConvUtil.convToInt(status) == 1) {
                            msg = "启用失败：请先将用户分组到公司！";
                        } else {
                            user.setStatus(ConvUtil.convToInt(status));
                            userinfoService.saveAdmin(user);
                        }
                    }
                    if (!"".equals(ConvUtil.convToStr(delflag))) {
                        userinfoService.deleteUserById(id);

                        //添加系统日志
                        Map<String, Object> paramMap = new HashMap<>(16);
                        paramMap.put("type", 13);
                        paramMap.put("loginfo", "用户(" + userProfile.getCurrentUserLoginName() + ")删除用户：" + user.getUsername());
                        paramMap.put("userid", userProfile.getCurrentUserId());
                        paramMap.put("userip", userProfile.getCurrentUserLoginIp());
                        syslogService.addSyslog(paramMap);
                    }

                } else {
                    msg = "操作失败：未找到用户！";
                }
            } else {
                msg = "非管理员账号：没有操作权限！";
            }
        } catch (Exception e) {
            e.printStackTrace();
            msg = "操作失败：系统异常！";
        }
        jsonMsg.put("obj1", msg);
        return jsonMsg;
    }

    /**
     * 用户分组
     *
     * @param request
     * @param idstr
     * @param menuId
     * @return
     */
    @RequestMapping(value = "/userToCom")
    @ResponseBody
    public JSONObject deviceToGroup(HttpServletRequest request, String idstr, Integer menuId) {
        UserProfile userProfile = (UserProfile) request.getSession().getAttribute("userProfile");
        JSONObject jsonMsg = new JSONObject();
        String msg = "success";
        try {
            if (userProfile.isTheAdmin()) {
                //获取id列表
                JSONObject idmap = JSONObject.parseObject(idstr);

                //获取菜单信息
                Menu menu = menuService.getMenuById(menuId);

                //修改用户分组
                Userinfo user = new Userinfo();
                if (menu != null && ConvUtil.convToInt(menu.getRank()) == 1) {
                    for (Object value : idmap.values()) {
                        user = userinfoService.getUserById(ConvUtil.convToInt(value));
                        user.setCompany(menuId.toString());
                        userinfoService.saveAdmin(user);
                    }
                } else {
                    msg = "操作失败：公司不存在！";
                }
            } else {
                msg = "非管理员账号：没有操作权限！";
            }

        } catch (Exception e) {
            logger.error(e.getMessage());
            e.printStackTrace();
            msg = "分组失败：系统异常！";
        }
        jsonMsg.put("obj1", msg);
        return jsonMsg;
    }

    /**
     * 查询用户权限
     *
     * @param id
     * @return
     */
    @RequestMapping(value = "/getPermission")
    @ResponseBody
    public JSONObject getPermission(Integer id) {
        JSONObject jsonMsg = new JSONObject();
        String msg = "success";
        try {
            Userinfo user = userinfoService.getUserById(id);
            //检查用户是否存在
            if (user != null) {
                jsonMsg.put("permission", ConvUtil.convToStr(user.getProject()));
            } else {
                msg = "操作失败：未找到用户！";
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
            e.printStackTrace();
            msg = "系统异常！";
        }
        jsonMsg.put("obj1", msg);
        return jsonMsg;
    }

    /**
     * 保存用户权限
     *
     * @param id
     * @param permission
     * @return
     */
    @RequestMapping(value = "/savePermission")
    @ResponseBody
    public JSONObject savePermission(Integer id, String permission) {
        JSONObject jsonMsg = new JSONObject();
        String msg = "success";
        try {
            Userinfo user = userinfoService.getUserById(id);
            //检查用户是否存在
            if (user != null) {
                user.setProject(permission);
                userinfoService.saveAdmin(user);
            } else {
                msg = "操作失败：未找到用户！";
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
            e.printStackTrace();
            msg = "系统异常！";
        }
        jsonMsg.put("obj1", msg);
        return jsonMsg;
    }

}
