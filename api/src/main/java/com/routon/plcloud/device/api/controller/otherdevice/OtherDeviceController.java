package com.routon.plcloud.device.api.controller.otherdevice;

import com.alibaba.fastjson.JSONArray;
import com.routon.plcloud.device.api.config.UserProfile;
import com.routon.plcloud.device.api.controller.commom.CommonForUser;
import com.routon.plcloud.device.api.controller.DeviceController;
import com.routon.plcloud.device.api.controller.LoginController;
import com.routon.plcloud.device.api.utils.PageUtil;
import com.routon.plcloud.device.core.common.TreeBean;
import com.routon.plcloud.device.core.service.*;
import com.routon.plcloud.device.core.utils.ConvUtil;
import com.routon.plcloud.device.data.entity.Device;
import com.routon.plcloud.device.data.entity.Userinfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author FireWang
 * @date 2020/11/3 11:18
 */
@Controller
@RequestMapping(value = "/otherdevice")
public class OtherDeviceController {
    private static Logger logger = LoggerFactory.getLogger(DeviceController.class);

    @Autowired
    private DeviceService deviceService;

    @Autowired
    private UserinfoService userinfoService;


    /**
     * 初始化第三方设备管理
     *
     * @param model
     * @param projectid
     * @param companyid
     * @param msg
     * @param termsn
     * @param remark
     * @param terminfo
     * @param page
     * @param pageSize
     * @param maxPage
     * @param columns
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/list")
    public String listinit(HttpServletRequest request, Model model, Integer projectid, Integer companyid, String msg,
                           String termsn, Integer isgroup, Integer status, String remark, String terminfo,
                           Integer page, Integer pageSize, Integer maxPage, Integer columns) throws Exception {
        try {
            //当前用户信息
            UserProfile userProfile = CommonForUser.getUserProfile(request);
            model = CommonForUser.getUserinfo(model, request);

            //页面传值集合
            Map<String, Object> modelMap = new HashMap<>(16);
            modelMap.put("companyid", companyid);
            modelMap.put("projectid", projectid);
            modelMap.put("termsn", termsn);
            modelMap.put("isgroup", isgroup);
            modelMap.put("status", status);
            modelMap.put("terminfo", terminfo);
            modelMap.put("remark", remark);

            //查询框添加
            Map<String, Object> condition = new HashMap<>(16);

            //非管理员用户只能查看该账号下设备
            int searchId = 0;
            if (!userProfile.isTheAdmin()) {
                Userinfo user = userinfoService.getUserById(userProfile.getCurrentUserId());
                if (isgroup != null) {
                    if (isgroup == 0) {
                        projectid = 0;
                    } else if (isgroup == 1) {
                        projectid = 1;
                    } else {
                        projectid = null;
                    }
                }

                if (user != null) {
                    if (user.getCompany() != null) {
                        companyid = ConvUtil.convToInt(user.getCompany());

                        //查询框加入公司信息
                        condition.put("companyid", companyid);
                    } else {
                        companyid = 2;
                    }
                    searchId = companyid;
                } else {
                    //登录过程中用户被删除，返回登录页
                    return new LoginController().logout(request);
                }
            } else {
                if (isgroup != null) {
                    companyid = isgroup;
                    model.addAttribute("companyid", companyid);
                    projectid = null;
                }
            }

            /* 获取条件列表：设备类型列表termlist */
            List<Device> termlist = deviceService.searchOtherDeviceClounmByCondition("terminfo", condition);

            // 获取软件树形分级目录
            List<TreeBean> projecttree = deviceService.getDviceTree(companyid, searchId);

            /** 设置返回页面的数据----分级目录数据 */
            //分级目录
            model.addAttribute("menuTreeBeans", JSONArray.toJSON(projecttree));

            //添加查询条件
            Map<String, Object> paramMap = new HashMap<>(16);
            paramMap.put("companyid", companyid);
            paramMap.put("projectid", projectid);

            paramMap.put("termsn", termsn);
            paramMap.put("remark", remark);
            paramMap.put("terminfo", terminfo);
            paramMap.put("status", status);

            //获取最大数据条数并格式化翻页组件
            List<Device> fullList = deviceService.getOtherMaxList(paramMap);
            columns = fullList.size();
            PageUtil currentPage = new PageUtil(page, pageSize, columns);
            model = CommonForUser.initPage(model, currentPage);

            //获取设备信息列表
            List<Device> devicelist = deviceService.searchotherdeviceforpage(paramMap, currentPage.getPage(), currentPage.getPageSize());

            //还原页面传值状态
            model = CommonForUser.addParam(model, modelMap);

            //加入数据列表
            model.addAttribute("datalist", devicelist);
            model.addAttribute("termlist", termlist);

            //返回信息展示
            if (!"".equals(ConvUtil.convToStr(msg))) {
                model.addAttribute("msg", msg);
            }

        } catch (Exception e) {
            logger.error(e.getMessage());
            e.printStackTrace();
        }
        return "thymeleaf/otherdevice";
    }

}
