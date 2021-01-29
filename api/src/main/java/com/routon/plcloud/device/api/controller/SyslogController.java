package com.routon.plcloud.device.api.controller;

import com.alibaba.fastjson.JSONArray;
import com.routon.plcloud.device.api.config.UserProfile;
import com.routon.plcloud.device.api.controller.commom.CommonForUser;
import com.routon.plcloud.device.api.utils.PageUtil;
import com.routon.plcloud.device.core.utils.ConvUtil;
import com.routon.plcloud.device.core.common.TreeBean;
import com.routon.plcloud.device.core.service.SyslogService;
import com.routon.plcloud.device.data.entity.Syslog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @ClassName:SyslogController
 * @Description:系统日志管理
 * @Author: fire
 * @Time: 2020-04-30 01:23
 **/
@Controller
@RequestMapping(value = "/syslog")
public class SyslogController {
    private Logger logger = LoggerFactory.getLogger(SyslogController.class);

    @Autowired
    private SyslogService syslogService;

    /**
     * 系统日志页面初始化
     *
     * @param request
     * @param model
     * @param type
     * @param start
     * @param end
     * @param loginfo
     * @param page
     * @param pageSize
     * @param maxPage
     * @param columns
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/list")
    public String listinit(HttpServletRequest request, Model model, Integer type, String start, String end, String loginfo,
                           Integer page, Integer pageSize, Integer maxPage, Integer columns) throws Exception {
        try {
            //当前用户信息
            UserProfile userProfile = CommonForUser.getUserProfile(request);
            model = CommonForUser.getUserinfo(model, request);

            /*获取软件树形分级目录*/
            List<TreeBean> syslogtree = syslogService.getSyslogTree(userProfile.getCurrentUserId());

            /*添加查询条件*/
            Map<String, Object> paramMap = new HashMap<>(16);
            if (ConvUtil.convToInt(type) != 0) {
                paramMap.put("type", type);
            }
            paramMap.put("start", start);
            paramMap.put("end", end);
            paramMap.put("loginfo", loginfo);
            if (!userProfile.isTheAdmin()) {
                paramMap.put("userid", userProfile.getCurrentUserId());
            }

            /*设置最大页码，翻页用*/
            columns = syslogService.getMaxCount(paramMap);
            PageUtil currentPage = new PageUtil(page, pageSize, columns);
            model = CommonForUser.initPage(model, currentPage);

            /* 获取软件信息列表 */
            List<Syslog> softwarelist = syslogService.searchsyslogforpage(paramMap, currentPage.getPage(), currentPage.getPageSize());

            /*设置返回页面的数据----分级目录数据*/
            //分级目录
            model.addAttribute("menuTreeBeans", JSONArray.toJSON(syslogtree));
            model.addAttribute("type", type);
            model.addAttribute("start", start);
            model.addAttribute("end", end);
            model.addAttribute("loginfo", loginfo);

            //数据列表
            model.addAttribute("datalist", softwarelist);

        } catch (Exception e) {
            logger.error(e.getMessage());
            e.printStackTrace();
        }

        return "thymeleaf/syslog";
    }
}