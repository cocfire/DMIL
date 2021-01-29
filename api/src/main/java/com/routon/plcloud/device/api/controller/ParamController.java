package com.routon.plcloud.device.api.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.routon.plcloud.device.api.config.UserProfile;
import com.routon.plcloud.device.api.config.emq.EmqClient;
import com.routon.plcloud.device.api.controller.commom.CommonForDevice;
import com.routon.plcloud.device.api.controller.commom.CommonForUser;
import com.routon.plcloud.device.api.utils.PageUtil;
import com.routon.plcloud.device.core.common.TreeBean;
import com.routon.plcloud.device.core.service.*;
import com.routon.plcloud.device.core.utils.ConvUtil;
import com.routon.plcloud.device.data.entity.*;
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
 * 参数信息
 */
@Controller
@RequestMapping(value = "/param")
public class ParamController {
    private static Logger logger = LoggerFactory.getLogger(ParamController.class);

    @Autowired
    private EmqClient emqClient;

    @Autowired
    private OfflinemsgService offlinemsgService;

    @Autowired
    private UserinfoService userinfoService;

    @Autowired
    private ParaminfoService paraminfoService;

    @Autowired
    private DeviceService deviceService;

    @Autowired
    private SoftwareService softwareService;

    @Autowired
    private ConfigService configService;

    /**
     * 初始化设备管理
     *
     * @param model
     * @param projectid
     * @param companyid
     * @param msg
     * @param termsn
     * @param remark
     * @param termmodel
     * @param erpcode
     * @param isSet
     * @param page
     * @param pageSize
     * @param maxPage
     * @param columns
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/list")
    public String listinit(HttpServletRequest request, Model model, Integer projectid, Integer companyid, String msg,
                           String termsn, Integer isgroup, Integer status, String remark, String termmodel, String erpcode,
                           boolean isSet, Integer page, Integer pageSize, Integer maxPage, Integer columns) throws Exception {
        try {
            //当前用户信息
            UserProfile userProfile = CommonForUser.getUserProfile(request);
            model = CommonForUser.getUserinfo(model, request);

            //选中数据的公司
            model.addAttribute("companyid", companyid);
            model.addAttribute("projectid", projectid);

            //查询框添加，用于显示该公司可查条件
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

            /* 获取软件树形分级目录 */
            List<TreeBean> projecttree = deviceService.getDviceTree(companyid, searchId);

            /* 获取条件列表：目前有两个列表，termlist softwarelist */
            List<Device> termlist = deviceService.searchDeviceClounmByCondition("termmodel", condition);
            List<Software> softlist = softwareService.getSoftwareByCondition(condition);

            /* 添加查询条件 */
            Map<String, Object> paramMap = new HashMap<>(16);
            paramMap.put("companyid", companyid);
            paramMap.put("projectid", projectid);

            paramMap.put("a.termsn", termsn);
            paramMap.put("a.status", status);
            paramMap.put("a.remark", remark);
            paramMap.put("termmodel", termmodel);
            paramMap.put("b.erpcode", erpcode);
            paramMap.put("isSet", isSet);

            /* 设置最大页码，翻页用 */
            columns = paraminfoService.getMaxCount(paramMap);
            PageUtil currentPage = new PageUtil(page, pageSize, columns);
            model = CommonForUser.initPage(model, currentPage);

            /* 获取设备信息列表 */
            List<Paraminfo> paramlist = paraminfoService.searchparamforpage(paramMap, currentPage.getPage(), currentPage.getPageSize());

            /** 设置返回页面的数据----分级目录数据 */
            //分级目录
            model.addAttribute("menuTreeBeans", JSONArray.toJSON(projecttree));

            //还原查询条件
            model.addAttribute("termsn", termsn);
            model.addAttribute("status", status);
            model.addAttribute("isgroup", isgroup);

            model.addAttribute("remark", remark);
            model.addAttribute("termmodel", termmodel);
            model.addAttribute("erpcode", erpcode);
            model.addAttribute("termlist", termlist);
            model.addAttribute("softlist", softlist);
            model.addAttribute("isSet", isSet);

            //数据列表
            model.addAttribute("datalist", paramlist);

            /*返回信息展示*/
            if (!"".equals(ConvUtil.convToStr(msg))) {
                model.addAttribute("msg", msg);
            }

        } catch (Exception e) {
            logger.error(e.getMessage());
            e.printStackTrace();
        }
        return "thymeleaf/param";
    }

    /**
     * 设备参数保存设置
     *
     * @param request
     * @param idstr
     * @return
     */
    @RequestMapping(value = "/pubparam")
    @ResponseBody
    public JSONObject parampub(HttpServletRequest request, String idstr,  String paramlist) {
        JSONObject jsonMsg = new JSONObject();
        JSONObject paraminfo = new JSONObject();
        paraminfo.put("type", "1");
        String msg = "success";
        try {
            //组织要发布的参数列表
            JSONArray pubList = new JSONArray();
            //获取设备信息
            JSONObject idmap = JSONObject.parseObject(idstr);
            //获取前台参数列表
            JSONArray paramList =  JSONArray.parseArray(paramlist);
            for (Object paramOb : paramList) {
                JSONObject param = JSONObject.parseObject(ConvUtil.convToStr(paramOb));
                int paramCode = ConvUtil.convToInt(param.get("paramCode"));
                String paramValue = ConvUtil.convToStr(param.get("paramValue"));
                //获取参数详情
                Config config = configService.getByConfigid(paramCode);

                //组织要发布的参数详情
                JSONObject paramtext = new JSONObject();
                paramtext.put("erpcode", config.getConfiginfo());
                paramtext.put("param_code", paramCode);
                paramtext.put("param_name", config.getName());
                paramtext.put("param_value",paramValue);
                pubList.add(paramtext);

                /** 保存设备参数信息 */
                for (Object value : idmap.values()) {
                    Device device = deviceService.getDeviceById(ConvUtil.convToInt(value));
                    //获取参数信息
                    Paraminfo dparam = paraminfoService.getByParamidAndSn(paramCode, device.getTermsn());
                    if (dparam != null) {
                        //存在，则在此处更更新
                        dparam.setParaminfo(paramValue);
                        dparam.setModifytime(new Date());
                        paraminfoService.update(dparam);
                    } else {
                        //不存在则新建
                        dparam = new Paraminfo();
                        //linkid储存设备运行软件对应的erp
                        dparam.setLinkid(ConvUtil.convToInt(config.getConfiginfo()));
                        dparam.setTermsn(device.getTermsn());
                        dparam.setParamid(paramValue);
                        dparam.setParaminfo("");
                        dparam.setCreatetime(new Date());
                        dparam.setModifytime(new Date());
                        dparam.setStatus(1);
                        paraminfoService.insert(dparam);
                    }
                }
            }
            paraminfo.put("param_list", pubList);

            /** 进行参数设置信息发布 */
            for (Object value : idmap.values()) {
                Device device = deviceService.getDeviceById(ConvUtil.convToInt(value));
                String topic = "param/"+ device.getTermsn();
                CommonForDevice.emqRetPub(emqClient, offlinemsgService, topic, paraminfo, device, "");
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
     * 设备参数详情
     *
     * @param request
     * @param id
     * @return
     */
    @RequestMapping(value = "/paramdetail")
    @ResponseBody
    public JSONObject paramdetail(HttpServletRequest request, Integer id) {
        JSONObject jsonMsg = new JSONObject();
        JSONObject paraminfo = new JSONObject();
        paraminfo.put("type", "2");
        String msg = "success";
        try {
            //获取设备信息
            Device device = deviceService.getDeviceById(ConvUtil.convToInt(id));
            //获取参数信息
            List<Config> configlist = configService.getVaildList(device.getTermsn());
            //组织要发布的参数列表
            JSONArray paramList = new JSONArray();
            for (Config config : configlist) {
                //组织要发布的参数详情
                JSONObject paramtext = new JSONObject();
                paramtext.put("erpcode", config.getConfiginfo());
                paramtext.put("param_code", config.getConfigid());
                paramtext.put("param_name", config.getName());
                paramList.add(paramtext);
            }
            paraminfo.put("param_list", paramList);

            /** 先发布查询，再返回数据 */
            String topic = "param/" + device.getTermsn();
            emqClient.pubTopic(topic, JSONObject.toJSONString(paraminfo));

            //获取设备参数信息
            List<Paraminfo> paramlist = paraminfoService.getVaildList(device.getTermsn());
            for (Paraminfo param : paramlist) {

                //按参数类型解析
                if (ConvUtil.convToInt(param.getParamid()) == 1100) {
                    jsonMsg.put("voiceOb", JSONArray.parse(param.getParaminfo()));
                }
                if (ConvUtil.convToInt(param.getParamid()) == 1101) {
                    jsonMsg.put("rebootOb", param.getParaminfo());
                }
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
