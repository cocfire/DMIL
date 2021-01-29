package com.routon.plcloud.device.api.controller.commom;

import com.routon.plcloud.device.api.config.UserProfile;
import com.routon.plcloud.device.api.utils.PageUtil;
import com.routon.plcloud.device.core.service.UserinfoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Map;

/**
 * @author FireWang
 * @date 2020/11/10 15:32
 * 用户数据信息公共方法集合
 */
public class CommonForUser {
    private static Logger logger = LoggerFactory.getLogger(CommonForUser.class);

    @Autowired
    private static UserinfoService userinfoService;

    public static UserProfile getUserProfile(HttpServletRequest request) throws Exception {
        UserProfile userProfile = new UserProfile();
        try {
            //根据请求获取用户临时文件
            HttpSession session = request.getSession();
            userProfile = (UserProfile) session.getAttribute("userProfile");
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e.getMessage());
        }
        return userProfile;
    }

    /**
     * 根据请求加载用户信息及权限
     *
     * @param model
     * @param request
     * @return
     * @throws Exception
     */
    public static Model getUserinfo(Model model, HttpServletRequest request) throws Exception {
        try {
            UserProfile userProfile = getUserProfile(request);

            //加载当前用户信息
            model.addAttribute("currentUserId", userProfile.getCurrentUserId());
            model.addAttribute("currentUserName", userProfile.getCurrentUserLoginName());
            model.addAttribute("userPermission", userProfile.getUserPermission());

        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e.getMessage());
        }
        return model;
    }

    /**
     * 添加页面传值参数
     *
     * @param model
     * @param params
     * @return
     * @throws Exception
     */
    public static Model addParam(Model model, Map<String, Object> params) throws Exception {
        try {
            if (params != null) {
                for (Map.Entry<String, Object> entry : params.entrySet()) {
                    model.addAttribute(entry.getKey(), entry.getValue());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e.getMessage());
        }
        return model;
    }

    /**
     * 加载用户当前也秒格式信息
     *
     * @param model
     * @param currentPage
     * @return
     * @throws Exception
     */
    public static Model initPage(Model model, PageUtil currentPage) throws Exception {
        try {
            model.addAttribute("page", currentPage.getPage());
            model.addAttribute("pageSize", currentPage.getPageSize());
            model.addAttribute("maxPage", currentPage.getMaxPage());
            model.addAttribute("columns", currentPage.getColumns());
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e.getMessage());
        }
        return model;
    }

}
