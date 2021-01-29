package com.routon.plcloud.device.api.controller.vrclient;

import com.alibaba.fastjson.JSONObject;
import com.routon.plcloud.device.core.service.VrdeviceService;
import com.routon.plcloud.device.core.utils.ConvUtil;
import com.routon.plcloud.device.data.entity.Vrdevice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author FireWang
 * @date 2020/7/6 16:54
 * 该类用于管理虚拟EMQ客户端设备
 */
@Controller
@RequestMapping(value = "/vrdevice")
public class VrDeviceController {
    private static Logger logger = LoggerFactory.getLogger(VrDeviceController.class);

    @Autowired
    private VrdeviceService vrdeviceService;

    @Autowired
    private VrEmqUtils vrEmqUtils;

    /**
     * 虚拟设备通行验证页面
     *
     * @return
     */
    @RequestMapping(value = "/start")
    public String start() {
        return "vrdevice/start";
    }

    /**
     * 虚拟设备管理通行验证
     *
     * @param request
     * @param passport
     * @return
     */
    @RequestMapping(value = "/goPass")
    @ResponseBody
    public JSONObject goPass(HttpServletRequest request, String passport) {
        HttpSession session = request.getSession();
        JSONObject resJson = new JSONObject();
        String msg = "success";
        try {
            String pass = "vr888";
            int vruserCont = ConvUtil.convToInt(session.getAttribute("vruserCont"));
            int ten = 10;
            if (vruserCont >= ten) {
                msg = "通行证错误次数过多，暂时无法登录，请联系管理员！";
            } else if (passport.equals(pass)) {
                //通行信息加入session
                session.setAttribute("vruser", "used");
                logger.info("通行码正确，进入虚拟设备管理！");
            } else {
                session.setAttribute("vruserCont", ConvUtil.convToInt(session.getAttribute("vruserCont")) + 1);
                msg = "通行证错误，剩余" + (10 - ConvUtil.convToInt(session.getAttribute("vruserCont"))) + "次！";
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
            e.printStackTrace();
            msg = "进入失败：系统异常！";
        }
        resJson.put("obj1", msg);
        return resJson;
    }

    /**
     * 通行码是否有效
     *
     * @param request
     * @return
     */
    private boolean isPass(HttpServletRequest request) {
        try {
            HttpSession session = request.getSession();
            String vruser = ConvUtil.convToStr(session.getAttribute("vruser"));
            if ("".equals(vruser)) {
                return false;
            } else {
                return true;
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 执行登出
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/passout")
    public String logout(HttpServletRequest request) {
        HttpSession session = request.getSession();
        //这里不能使session失效
        session.setAttribute("vruser", "");
        session.setAttribute("vruserCont", 0);
        return "vrdevice/start";
    }

    /**
     * 初始化vr设备管理
     *
     * @param model
     * @param vrdeviceid
     * @param name
     * @param type
     * @param status
     * @param remark
     * @param page
     * @param pageSize
     * @param maxPage
     * @param columns
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/list")
    public String listinit(HttpServletRequest request, Model model,
                           String vrdeviceid, String name, Integer type, String remark, Integer status,
                           Integer page, Integer pageSize, Integer maxPage, Integer columns) throws Exception {
        try {
            if (!isPass(request)) {
                return "vrdevice/start";
            }

            //查询框添加
            Map<String, Object> condition = new HashMap<>(16);

            /* 添加查询条件 */
            Map<String, Object> paramMap = new HashMap<>(16);
            paramMap.put("vrdeviceid", vrdeviceid);
            paramMap.put("name", name);
            paramMap.put("type", type);
            paramMap.put("remark", remark);
            paramMap.put("status", status);

            /* 设置最大页码，翻页用 */
            if (ConvUtil.convToInt(pageSize) == 0) {
                pageSize = 10;
            }
            if (ConvUtil.convToInt(page) == 0) {
                page = 1;
            }
            columns = vrdeviceService.getMaxCount(paramMap);
            if (columns == 0) {
                maxPage = 1;
            } else {
                maxPage = (int) Math.ceil(columns / (double) pageSize);
            }

            /* 获取设备信息列表 */
            List<Vrdevice> devicelist = vrdeviceService.searchvrdeviceforpage(paramMap, page, pageSize);

            /** 设置返回页面的数据----分级目录数据 */

            //还原查询条件
            model.addAttribute("vrdeviceid", vrdeviceid);
            model.addAttribute("remark", remark);
            model.addAttribute("name", name);
            model.addAttribute("type", type);
            model.addAttribute("status", status);
            model.addAttribute("status", status);

            model.addAttribute("page", page);
            model.addAttribute("pageSize", pageSize);
            model.addAttribute("maxPage", maxPage);
            model.addAttribute("columns", columns);
            //数据列表
            model.addAttribute("datalist", devicelist);

        } catch (Exception e) {
            logger.error(e.getMessage());
            e.printStackTrace();
        }
        return "vrdevice/vrlist";
    }

    /**
     * 设备详情保存
     *
     * @param id
     * @param vrdeviceid
     * @param name
     * @param info
     * @param version
     * @return
     */
    @RequestMapping(value = "/saveinfo")
    @ResponseBody
    public JSONObject saveremark(HttpServletRequest request, Integer id, String vrdeviceid, String name, String info, String version) {
        JSONObject jsonMsg = new JSONObject();
        String msg = "success";
        try {
            if (!isPass(request)) {
                msg = "登录超时，请重新登录！";
            } else {
                boolean insertflag = false;
                Vrdevice vrdevice = vrdeviceService.getById(id);
                if (vrdevice == null) {
                    vrdevice = new Vrdevice();
                    vrdevice.setStatus(0);
                    vrdevice.setType("1");
                    vrdevice.setCreatetime(new Date());
                    vrdevice.setRemark("");
                    insertflag = true;

                }
                vrdevice.setVrdeviceid(vrdeviceid);
                vrdevice.setName(name);
                vrdevice.setInfo(info);
                vrdevice.setVersion(version);
                vrdevice.setUpdatetime(new Date());

                if (insertflag) {
                    vrdeviceService.insertVrDevice(vrdevice);
                } else {
                    vrdeviceService.update(vrdevice);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            msg = "系统异常！";
        }
        jsonMsg.put("obj1", msg);
        return jsonMsg;
    }

    /**
     * 设备详情查询
     *
     * @param id
     * @return
     */
    @RequestMapping(value = "/devicedetail")
    @ResponseBody
    public JSONObject devicedetail(HttpServletRequest request, Integer id) {
        JSONObject jsonMsg = new JSONObject();
        String msg = "success";
        try {
            if (!isPass(request)) {
                msg = "登录超时，请重新登录！";
            }
            Vrdevice vrdevice = vrdeviceService.getById(id);
            jsonMsg.put("obj2", vrdevice);
        } catch (Exception e) {
            e.printStackTrace();
            msg = "系统异常！";
        }
        jsonMsg.put("obj1", msg);
        return jsonMsg;
    }

    /**
     * 设备操作集锦
     *
     * @param request
     * @param idstr
     * @param opreation
     * @param topic
     * @param message
     * @return
     */
    @RequestMapping(value = "/doOpreation")
    @ResponseBody
    public JSONObject machinepubmsg(HttpServletRequest request, String idstr, Integer opreation, String topic, String message) {
        JSONObject jsonMsg = new JSONObject();
        JSONObject versioninfo = new JSONObject();
        String msg = "success";
        try {
            if (!isPass(request)) {
                msg = "登录超时，请重新登录！";
            } else {
                /** ------------ 根据操作类型opreation来执行操作 -------------*/
                JSONObject idmap = JSONObject.parseObject(idstr);
                for (Object value : idmap.values()) {
                    Vrdevice vrdevice = vrdeviceService.getById(ConvUtil.convToInt(value));
                    if (ConvUtil.convToInt(opreation) == 0) {
                        //离线操作
                        vrEmqUtils.clientOffline(ConvUtil.convToStr(vrdevice.getVrdeviceid()));
                    } else if (ConvUtil.convToInt(opreation) == 1) {
                        //上线操作
                        vrEmqUtils.clientOnline(ConvUtil.convToStr(vrdevice.getVrdeviceid()));
                    } else if (ConvUtil.convToInt(opreation) == 2) {
                        //订阅操作
                        vrEmqUtils.clientSub(ConvUtil.convToStr(vrdevice.getVrdeviceid()), ConvUtil.convToStr(topic));
                    } else if (ConvUtil.convToInt(opreation) == 1) {
                        //发布操作
                        vrEmqUtils.clientPub(ConvUtil.convToStr(vrdevice.getVrdeviceid()), ConvUtil.convToStr(topic), ConvUtil.convToStr(message));
                    } else {
                        msg = "操作类型异常！错误类型：" + opreation;
                    }
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
