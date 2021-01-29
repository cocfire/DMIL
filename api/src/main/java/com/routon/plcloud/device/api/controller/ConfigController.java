package com.routon.plcloud.device.api.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.routon.plcloud.device.api.config.UserProfile;
import com.routon.plcloud.device.api.controller.commom.CommonForUser;
import com.routon.plcloud.device.api.utils.PageUtil;
import com.routon.plcloud.device.core.common.TreeBean;
import com.routon.plcloud.device.core.service.ConfigService;
import com.routon.plcloud.device.core.utils.ConvUtil;
import com.routon.plcloud.device.data.entity.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author FireWang
 * @date 2020/12/22 16:05
 */
@Controller
@RequestMapping(value = "/config")
public class ConfigController {
    private static Logger logger = LoggerFactory.getLogger(ConfigController.class);

    @Autowired
    private ConfigService configService;

    /**
     * 系统日志页面初始化
     *
     * @param request
     * @param model
     * @param type
     * @param start
     * @param end
     * @param configinfo
     * @param name
     * @param configid
     * @param status
     * @param page
     * @param pageSize
     * @param maxPage
     * @param columns
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/list")
    public String listinit(HttpServletRequest request, Model model, Integer type, String start, String end,
                           String configinfo, String name, Integer configid, Integer status,
                           Integer page, Integer pageSize, Integer maxPage, Integer columns) throws Exception {
        try {
            //当前用户信息
            model = CommonForUser.getUserinfo(model, request);

            /*获取软件树形分级目录*/
            List<TreeBean> configtree = configService.getConfigTree();

            /*添加查询条件*/
            Map<String, Object> paramMap = new HashMap<>(16);
            if (ConvUtil.convToInt(type) != 0) {
                paramMap.put("type", type);
            }
            if (ConvUtil.convToInt(status) != 0) {
                paramMap.put("status", status);
            }
            paramMap.put("start", start);
            paramMap.put("end", end);
            paramMap.put("configinfo", "");
            paramMap.put("configid", configid);
            paramMap.put("name", name);

            /*设置最大页码，翻页用*/
            columns = configService.getMaxCount(paramMap);
            PageUtil currentPage = new PageUtil(page, pageSize, columns);
            model = CommonForUser.initPage(model, currentPage);

            /* 获取软件信息列表 */
            List<Config> datalist = configService.searchforpage(paramMap, currentPage.getPage(), currentPage.getPageSize());

            /*设置返回页面的数据----分级目录数据*/
            //分级目录
            model.addAttribute("menuTreeBeans", JSONArray.toJSON(configtree));
            model.addAttribute("start", start);
            model.addAttribute("end", end);
            model.addAttribute("type", type);
            model.addAttribute("configinfo", configinfo);
            model.addAttribute("name", name);
            model.addAttribute("configid", configid);
            model.addAttribute("status", status);

            //数据列表
            model.addAttribute("datalist", datalist);

        } catch (Exception e) {
            logger.error(e.getMessage());
            e.printStackTrace();
        }

        return "thymeleaf/config";
    }

    /**
     * 添加配置
     *
     * @param configcode
     * @param configname
     * @param configtype
     * @param configerp
     * @param configremark
     * @return
     */
    @RequestMapping(value = "/addconfig")
    @ResponseBody
    public JSONObject addconfig(HttpServletRequest request, Integer configcode, String configname,
                              Integer configtype, String configerp, String configremark) {
        JSONObject jsonMsg = new JSONObject();
        String msg = "success";
        try {
            boolean addflag = true;
            //检查用户必填
            if ("".equals(configcode) || "".equals(configname) || "".equals(configerp)) {
                msg = "保存失败：请填写完整带“*”号的信息！";
                addflag = false;
            }

            //判断是否存在
            Config rconfig = configService.getByConfigid(configcode);
            if (rconfig != null) {
                msg = "保存失败：该配置代码已存在！";
                addflag = false;
            }

            if (addflag) {
                Config config = new Config();
                config.setConfigid(configcode);
                config.setName(configname);
                config.setType(configtype);
                config.setConfiginfo(configerp);
                config.setRemark(configremark);
                config.setStatus(1);
                config.setCreatetime(new Date());
                config.setModifytime(new Date());
                configService.insert(config);
            }

        } catch (Exception e) {
            e.printStackTrace();
            msg = "保存失败：系统异常！";
        }
        jsonMsg.put("obj1", msg);
        return jsonMsg;
    }

    /**
     * 编辑配置
     *
     * @param id
     * @param status
     * @return
     */
    @RequestMapping(value = "/editconfig")
    @ResponseBody
    public JSONObject editconfig(HttpServletRequest request, Integer id, String status) {
        JSONObject jsonMsg = new JSONObject();
        String msg = "success";
        try {
            Config config = configService.getById(ConvUtil.convToInt(id));

            if (config != null) {
                if (!"".equals(ConvUtil.convToStr(status))) {
                    config.setStatus(ConvUtil.convToInt(status));
                    config.setModifytime(new Date());
                    configService.update(config);
                } else {
                    configService.delete(config);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            msg = "操作失败：系统异常！";
        }
        jsonMsg.put("obj1", msg);
        return jsonMsg;
    }

    /**
     * 设备参数检查
     *
     * @return
     */
    @RequestMapping(value = "/checkconfig")
    @ResponseBody
    public JSONObject checkconfig(HttpServletRequest request) {
        JSONObject jsonMsg = new JSONObject();
        String msg = "success";
        try {
            //当前用户信息
            UserProfile userProfile = CommonForUser.getUserProfile(request);
            Integer companyid = ConvUtil.convToInt(userProfile.getCurrentUserCompany());

            //获取所有的有效设备参数
            /* 添加查询条件 */
            Map<String, Object> configMap = new HashMap<>(16);
            configMap.put("type", 2);
            configMap.put("status", 1);
            List<Config> clist = configService.getMaxList(configMap);

            for (Config config : clist) {
                /* 添加查询条件 */
                Map<String, Object> paramMap = new HashMap<>(16);
                if (companyid != 0) {
                    paramMap.put("companyid", companyid);
                }
                paramMap.put("configid", config.getConfigid());
                paramMap.put("configinfo", config.getConfiginfo());
                paramMap.put("value", config.getRemark());

                //打开页面之前先查看设备是否具有默认值，没有则生成默认值，v1.4.4
                configService.checkDefultParam(paramMap);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        jsonMsg.put("obj1", msg);
        return jsonMsg;
    }

}
