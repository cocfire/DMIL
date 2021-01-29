package com.routon.plcloud.device.api.controller;

import com.alibaba.fastjson.JSONObject;
import com.routon.plcloud.device.api.config.UserProfile;
import com.routon.plcloud.device.api.controller.commom.CommonForUser;
import com.routon.plcloud.device.core.service.DeviceService;
import com.routon.plcloud.device.core.service.MenuService;
import com.routon.plcloud.device.core.service.SyslogService;
import com.routon.plcloud.device.core.service.UserinfoService;
import com.routon.plcloud.device.core.utils.ConvUtil;
import com.routon.plcloud.device.data.entity.Menu;
import com.routon.plcloud.device.data.entity.Userinfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

/**
 * @author FireWang
 * @date 2020/6/9 13:43
 * 分组管理
 */
@Controller
@RequestMapping(value = "/menu")
public class MenuController {
    private static Logger logger = LoggerFactory.getLogger(MenuController.class);

    @Autowired
    private SyslogService syslogService;

    @Autowired
    private MenuService menuService;

    @Autowired
    private UserinfoService userinfoService;

    @Autowired
    private DeviceService deviceService;

    private String success = "success";

    /**
     * 添加菜单
     *
     * @param menuName
     * @param rank
     * @return
     */
    @RequestMapping(value = "/menuAdd")
    @ResponseBody
    public JSONObject menuAdd(HttpServletRequest request, String menuName, Integer rank) {
        JSONObject jsonMsg = new JSONObject();
        String msg = "success";
        try {
            UserProfile userProfile = CommonForUser.getUserProfile(request);
            if (rank == 1) {
                //检查菜单是否存在，若不存在则可插入
                if (menuService.getMenuByName(menuName.trim()) == null) {
                    Menu menu = new Menu();
                    menu.setName(menuName.trim());
                    menu.setRank(rank);
                    menuService.addMenu(menu);
                } else {
                    msg = "保存失败：公司已存在！";
                }

                //添加系统日志
                if (msg.equals(success)) {
                    Map<String, Object> paramMap = new HashMap<>(16);
                    paramMap.put("type", 40);
                    paramMap.put("loginfo", "用户(" + userProfile.getCurrentUserLoginName() + ")添加公司：" + menuName);
                    paramMap.put("userid", userProfile.getCurrentUserId());
                    paramMap.put("userip", userProfile.getCurrentUserLoginIp());
                    syslogService.addSyslog(paramMap);
                }
            } else {
                //添加分组
                Userinfo user = userinfoService.getUserById(userProfile.getCurrentUserId());
                if (user.getCompany() != null && user.getCompany() != null) {
                    //检查菜单是否存在，若不存在则可插入
                    if (menuService.getMenuByNameForPid(menuName.trim(), ConvUtil.convToInt(user.getCompany())) == null) {
                        Menu menu = new Menu();
                        menu.setName(menuName.trim());
                        menu.setRank(rank);
                        menu.setPid(ConvUtil.convToInt(user.getCompany()));
                        menuService.addMenu(menu);
                    } else {
                        msg = "保存失败：分组已存在！";
                    }
                } else {
                    msg = "保存失败：账号或账号公司已失效！";
                }

                //添加系统日志
                if (msg.equals(success)) {
                    Map<String, Object> paramMap = new HashMap<>(16);
                    paramMap.put("type", 42);
                    paramMap.put("loginfo", "用户(" + userProfile.getCurrentUserLoginName() + ")添加分组：" + menuName);
                    paramMap.put("userid", userProfile.getCurrentUserId());
                    paramMap.put("userip", userProfile.getCurrentUserLoginIp());
                    syslogService.addSyslog(paramMap);
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
     * 删除菜单
     *
     * @param menuId
     * @return
     */
    @RequestMapping(value = "/menuDel")
    @ResponseBody
    public JSONObject menuDel(HttpServletRequest request, Integer menuId) {
        JSONObject jsonMsg = new JSONObject();
        String msg = "success";
        try {
            UserProfile userProfile = CommonForUser.getUserProfile(request);
            //检查公司是否存在，若不存在则可插入
            Menu menu = menuService.getMenuById(menuId);
            if (menu == null) {
                msg = "删除失败：不存在！";
            } else {
                //删除菜单
                menuService.delMenuById(menuId);

                //删除设备关联的分组信息
                deviceService.delLinkedMenuId(menuId);

                //删除用户相关分组信息
                userinfoService.delLinkedMenuId(menuId);
            }

            //添加系统日志
            if (msg.equals(success)) {
                Map<String, Object> paramMap = new HashMap<>(16);
                if (ConvUtil.convToInt(menu.getRank()) == 1) {
                    paramMap.put("type", 41);
                    paramMap.put("loginfo", "用户(" + userProfile.getCurrentUserLoginName() + ")删除公司：" + menu.getName());
                } else {
                    paramMap.put("type", 43);
                    paramMap.put("loginfo", "用户(" + userProfile.getCurrentUserLoginName() + ")删除分组：" + menu.getName());
                }
                paramMap.put("userid", userProfile.getCurrentUserId());
                paramMap.put("userip", userProfile.getCurrentUserLoginIp());
                syslogService.addSyslog(paramMap);
            }
        } catch (Exception e) {
            e.printStackTrace();
            msg = "删除失败：系统异常！";
        }
        jsonMsg.put("obj1", msg);
        return jsonMsg;
    }

    /**
     * 获取菜单列表
     * @param rank
     * @param pid
     *
     * @return
     */
    @RequestMapping(value = "/getMenu")
    @ResponseBody
    public JSONObject getMenu(HttpServletRequest request, Integer rank, Integer pid) {
        JSONObject jsonMsg = new JSONObject();
        String msg = "success";
        try {
            UserProfile userProfile = CommonForUser.getUserProfile(request);
            if (rank == 1) {
                jsonMsg.put("obj", menuService.getMenuListByRank(1));
            } else {
                int adminId = 888;
                if (userProfile.getCurrentUserId() != adminId) {
                    Userinfo user = userinfoService.getUserById(userProfile.getCurrentUserId());
                    if (user != null && user.getCompany() != null) {
                        jsonMsg.put("obj", menuService.getMenuListByPid(ConvUtil.convToInt(user.getCompany())));
                    }
                } else {
                    if (pid != null) {
                        jsonMsg.put("obj", menuService.getMenuListByPid(pid));
                    } else {
                        jsonMsg.put("obj", menuService.getMenuListByRank(rank));
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            msg = "操作失败：系统异常！";
        }
        jsonMsg.put("obj1", msg);
        return jsonMsg;
    }
}
