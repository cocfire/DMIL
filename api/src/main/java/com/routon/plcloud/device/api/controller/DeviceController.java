package com.routon.plcloud.device.api.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.routon.plcloud.device.api.controller.commom.CommonForDevice;
import com.routon.plcloud.device.api.controller.commom.CommonForUser;
import com.routon.plcloud.device.api.utils.ExcelUtil;
import com.routon.plcloud.device.api.utils.FileUtil;
import com.routon.plcloud.device.api.utils.PageUtil;
import com.routon.plcloud.device.api.utils.PinyinUtil;
import com.routon.plcloud.device.api.config.emq.EmqClient;
import com.routon.plcloud.device.api.config.UserProfile;
import com.routon.plcloud.device.core.service.*;
import com.routon.plcloud.device.core.utils.ConvUtil;
import com.routon.plcloud.device.core.utils.PropertiesUtil;
import com.routon.plcloud.device.core.common.TreeBean;
import com.routon.plcloud.device.data.entity.*;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author FireWang
 * @date 2020/4/29 22:12
 * 设备管理
 */
@Controller
@RequestMapping(value = "/device")
public class DeviceController {
    private static Logger logger = LoggerFactory.getLogger(DeviceController.class);

    @Autowired
    private DeviceService deviceService;

    @Autowired
    private DeviceswService deviceswService;

    @Autowired
    private SoftwareService softwareService;

    @Autowired
    private OfflinemsgService offlinemsgService;

    @Autowired
    private MenuService menuService;

    @Autowired
    private SyslogService syslogService;

    @Autowired
    private UserinfoService userinfoService;

    @Autowired
    private EmqClient emqClient;

    private JSONObject versions = new JSONObject();
    private JSONObject nameinfo = new JSONObject();

    /**
     * 初始化设备管理
     *
     * @param model
     * @param projectid
     * @param companyid
     * @param msg
     * @param termsn
     * @param deviceid
     * @param remark
     * @param termmodel
     * @param erpcode
     * @param page
     * @param pageSize
     * @param maxPage
     * @param columns
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/list")
    public String listinit(HttpServletRequest request, Model model, Integer projectid, Integer companyid, String msg,
                           String termsn, String deviceid, String mac, String ipaddr, Integer isgroup, Integer status,
                           String addid, String zpid, String braid, String remark, String termmodel, String erpcode,
                           Integer page, Integer pageSize, Integer maxPage, Integer columns) throws Exception {
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

            paramMap.put("termsn", termsn);
            paramMap.put("a.deviceid", deviceid);
            paramMap.put("macaddress", mac);
            paramMap.put("lanip", ipaddr);
            paramMap.put("a.status", status);

            paramMap.put("udid", addid);
            paramMap.put("uuid", zpid);
            paramMap.put("barcode", braid);
            paramMap.put("a.remark", remark);
            paramMap.put("termmodel", termmodel);
            paramMap.put("b.erpcode", erpcode);

            /* 设置最大页码，翻页用 */
            columns = deviceService.getMaxCount(paramMap);
            PageUtil currentPage = new PageUtil(page, pageSize, columns);
            model = CommonForUser.initPage(model, currentPage);

            /* 获取设备信息列表 */
            List<Device> devicelist = deviceService.searchdeviceforpage(paramMap, currentPage.getPage(), currentPage.getPageSize());

            /** 设置返回页面的数据----分级目录数据 */
            //分级目录
            model.addAttribute("menuTreeBeans", JSONArray.toJSON(projecttree));

            //还原查询条件
            model.addAttribute("termsn", termsn);
            model.addAttribute("saveid", deviceid);
            model.addAttribute("mac", mac);
            model.addAttribute("ipaddr", ipaddr);
            model.addAttribute("status", status);
            model.addAttribute("isgroup", isgroup);

            model.addAttribute("addid", addid);
            model.addAttribute("zpid", zpid);
            model.addAttribute("braid", braid);
            model.addAttribute("remark", remark);
            model.addAttribute("erpcode", erpcode);
            model.addAttribute("termmodel", termmodel);

            model.addAttribute("softlist", softlist);
            model.addAttribute("termlist", termlist);

            //数据列表
            model.addAttribute("datalist", devicelist);

            /*返回信息展示*/
            if (!"".equals(ConvUtil.convToStr(msg))) {
                model.addAttribute("msg", msg);
            }

            /*EMQ启动服务*/
            if (!emqClient.emqIsConnected()) {
                emqClient.emqBoot();
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
            e.printStackTrace();
        }
        return "thymeleaf/device";
    }

    /**
     * 根据软件名称获取版本号
     *
     * @param softwarename
     * @param clounm
     * @return
     */
    @RequestMapping(value = "/getClounm")
    @ResponseBody
    public JSONObject getClounm(HttpServletRequest request, String softwarename, String clounm) {
        JSONObject jsonMsg = new JSONObject();
        String msg = "success";
        try {
            //加入公司信息
            HttpSession session = request.getSession();
            UserProfile userProfile = (UserProfile) session.getAttribute("userProfile");

            //查询框添加
            Map<String, Object> condition = new HashMap<>(16);

            //非管理员用户只能查看该账号下设备
            int adminId = 888, searchId = 0;
            if (userProfile.getCurrentUserId() != adminId) {
                Userinfo user = userinfoService.getUserById(userProfile.getCurrentUserId());

                if (user != null) {
                    if (user.getCompany() != null) {
                        //查询框加入公司信息
                        condition.put("companyid", ConvUtil.convToInt(user.getCompany()));
                    } else {
                        condition.put("companyid", 1);
                    }
                }
            }

            condition.put("softwarename", ConvUtil.convToStr(softwarename));

            jsonMsg.put("obj", deviceService.searchDeviceClounmByCondition(clounm, condition));
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
    public JSONObject devicedetail(Integer id) {
        JSONObject jsonMsg = new JSONObject();
        String msg = "success";
        try {
            Device device = deviceService.searchDeviceById(id);
            //格式化日期
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            dateFormat.setLenient(false);
            String createtime = ConvUtil.convToStr(dateFormat.format(device.getCreatetime()));
            String moditytime = ConvUtil.convToStr(dateFormat.format(device.getModitytime()));
            String updatetime = "";
            if (device.getUpdatetime() != null) {
                updatetime = ConvUtil.convToStr(dateFormat.format(device.getUpdatetime()));
            }

            Map<String, String> timeMap = new HashMap<>(16);
            timeMap.put("createtime", createtime);
            timeMap.put("moditytime", moditytime);
            timeMap.put("updatetime", updatetime);
            jsonMsg.put("obj2", device);
            jsonMsg.put("obj3", timeMap);

            //公司分组信息
            List<Menu> companys = menuService.getMenuListByRank(1);
            List<Menu> groups = menuService.getMenuListByPid(ConvUtil.convToInt(device.getCompanyid()));
            jsonMsg.put("companys", companys);
            jsonMsg.put("groups", groups);

            //设备软件列表
            jsonMsg.put("softwarelist", deviceswService.getByDeviceId(ConvUtil.convToInt(device.getId())));

        } catch (Exception e) {
            e.printStackTrace();
            msg = "系统异常！";
        }
        jsonMsg.put("obj1", msg);
        return jsonMsg;
    }


    /**
     * 设备详情保存
     *
     * @param id
     * @param companyid
     * @param projectid
     * @param contact
     * @param telno
     * @param address
     * @param remark
     * @return
     */
    @RequestMapping(value = "/updatedevice")
    @ResponseBody
    public JSONObject saveremark(Integer id, Integer companyid, Integer projectid, String contact,
                                 String telno, String address, String remark) {
        JSONObject jsonMsg = new JSONObject();
        String msg = "success";
        try {
            Device device = deviceService.searchDeviceById(id);

            if (ConvUtil.convToInt(companyid) == 0) {
                //若解除绑定公司，则同时解除绑定账户
                device.setClientcode("");
            } else if (companyid != device.getCompanyid()) {
                //若更改公司则修改设备上相应的用户手机号
                Userinfo user = userinfoService.getUserByCompany(ConvUtil.convToStr(companyid));
                if (user != null) {
                    device.setClientcode(ConvUtil.convToStr(user.getPhone()));
                } else {
                    msg = "提示：该公司下没有账号，请到“用户管理”添加！";
                    jsonMsg.put("obj1", msg);
                    return jsonMsg;
                }
            }
            device.setCompanyid(companyid);
            device.setProjectid(projectid);
            device.setContact(contact);
            device.setTelno(telno);
            device.setAddress(address);
            device.setRemark(remark);
            device.setModitytime(new Date());
            deviceService.update(device);
        } catch (Exception e) {
            e.printStackTrace();
            msg = "系统异常！";
        }
        jsonMsg.put("obj1", msg);
        return jsonMsg;
    }

    /**
     * 获取软件版本
     *
     * @param idstr
     * @return
     */
    @RequestMapping(value = "/getversion")
    @ResponseBody
    public JSONObject machineupdate(String idstr) {
        JSONObject jsonMsg = new JSONObject();
        String msg = "success";
        try {
            List<Devicesw> softwareList = new ArrayList<>();
            /*如果时勾选的则判断勾选的设备软件erp是否一样*/
            JSONObject idmap = JSONObject.parseObject(idstr);
            if (idmap.size() > 0) {
                softwareList = deviceswService.canTheyUpdateByDeviceId(idmap);
            }

            //添加可直接下发的软件erp
            List<Software> dirSoftwareList = softwareService.getDirctList();
            for (Software dsoft : dirSoftwareList) {
                Devicesw dirErp = new Devicesw();
                dirErp.setErpcode(ConvUtil.convToStr(dsoft.getErpcode()));
                dirErp.setSoftwarename(dsoft.getSoftwarename());
                softwareList.add(dirErp);
            }

            if (softwareList.size() > 0) {
                //获取型号列表
                List<Device> termmodelList = deviceService.getClounmById(idmap, "termmodel");

                //根据erp获取软件信息列表
                versions = new JSONObject(); nameinfo = new JSONObject();
                for (Devicesw devicesw : softwareList) {
                    List<JSONObject> versionlist = softwareService.getVersionByErpAndTermmodel(devicesw.getErpcode(), termmodelList);
                    if (versionlist.size() > 0) {
                        versions.put(devicesw.getSoftwarename(), versionlist);
                        nameinfo.put(devicesw.getSoftwarename(), versionlist.get(0).get("customername"));
                    }
                }
                if (versions.size() == 0) {
                    if (termmodelList.size() > 1) {
                        StringBuilder termmodels = new StringBuilder();
                        for (Device device : termmodelList) {
                            termmodels.append(device.getTermmodel() + "、");
                        }
                        msg = "没有同时支持设备型号为（" + termmodels.substring(0, termmodels.lastIndexOf("、")) + "）的软件版本！";
                    } else {
                        msg = "没有可升级的软件版本！";
                    }
                }
            } else {
                if (idmap.size() > 1) {
                    msg = "没有同时支持这些设备升级的可用软件版本！";
                } else {
                    msg = "没有可升级的软件版本！";
                }
            }
            jsonMsg.put("versions", versions);
            jsonMsg.put("nameinfo", nameinfo);
        } catch (Exception e) {
            e.printStackTrace();
            msg = "升级失败：数据异常，请刷新后重试！";
        }
        jsonMsg.put("obj1", msg);
        return jsonMsg;
    }

    /**
     * 设备升级发布
     *
     * @param request
     * @param idstr
     * @param softwarename
     * @param softwareversion
     * @return
     */
    @RequestMapping(value = "/updatepub")
    @ResponseBody
    public JSONObject machinepubmsg(HttpServletRequest request, String idstr, String softwarename, String softwareversion) {
        JSONObject jsonMsg = new JSONObject();
        JSONObject versioninfo = new JSONObject();
        String msg = "success";
        try {
            //找到version对象
            JSONArray versionlist = versions.getJSONArray(softwarename);
            if (versionlist != null && versionlist.size() > 0) {
                for (Object version : versionlist) {
                    if (((JSONObject) version).get("softwareversion").equals(softwareversion)) {
                        versioninfo = (JSONObject) version;
                    }
                }

                JSONObject idmap = JSONObject.parseObject(idstr);
                StringBuilder termsns = new StringBuilder();
                for (Object value : idmap.values()) {
                    Device device = deviceService.searchDeviceById(ConvUtil.convToInt(value));
                    String topic = "update/" + device.getTermsn();

                    //添加适配设备类型校验
                    Map<String, Object> param = new HashMap<>(16);
                    param.put("softwarename", versioninfo.get("softwarename"));
                    param.put("softwareversion", versioninfo.get("softwareversion"));
                    List<Software> softwareList = softwareService.getMaxList(param);
                    Software sw = new Software();
                    if (softwareList.size() > 0) {
                        sw = softwareList.get(0);
                    }
                    if ("".equals(ConvUtil.convToStr(sw.getFitmodel())) || ConvUtil.convToStr(sw.getFitmodel()).contains(device.getTermmodel())) {
                        termsns.append(device.getTermsn() + ",");
                        //在线设备若没有正在执行的任务则直接发送；离线设备或其他情况转存离线任务消息
                        Devicesw updatedw = deviceswService.getUpdateByDeviceId(ConvUtil.convToInt(device.getId()));
                        int taskCount = deviceswService.getUpdateCount();

                        //v1.2版本开始增加消息确认机制，所有消息统一记录并添加流水号
                        versioninfo = CommonForDevice.emqRetPrase(offlinemsgService, topic, versioninfo, device, softwarename);

                        if (ConvUtil.convToInt(device.getStatus()) == 1 && updatedw == null && taskCount < 100) {
                            //发布消息
                            emqClient.pubTopic(topic, JSONObject.toJSONString(versioninfo));

                            //将设备软件状态修改为升级中
                            Devicesw dw = deviceswService.getByDeviceIdAndName(ConvUtil.convToInt(device.getId()), softwarename);
                            if (dw != null) {
                                dw.setStatus(2);
                                dw.setModitytime(new Date());
                                deviceswService.update(dw);
                            }

                        } else {
                            if (!CommonForDevice.isSuportEmqRet(device.getEmqversion())) {
                                Offlinemsg offlinemsg = new Offlinemsg(topic, JSONObject.toJSONString(versioninfo), device.getClientid(), softwarename);
                                offlinemsgService.insert(offlinemsg);
                            }
                        }
                    }
                }
                if (termsns.length() > 0) {
                    //获取软件中文名称
                    Software software = softwareService.getSoftwareByName(ConvUtil.convToStr(versioninfo.get("softwarename")));
                    //添加系统日志
                    UserProfile userProfile = (UserProfile) request.getSession().getAttribute("userProfile");
                    Map<String, Object> sysMap = new HashMap<>(16);
                    sysMap.put("type", 30);
                    sysMap.put("loginfo", "用户(" + userProfile.getCurrentUserLoginName() + ")向设备(" + termsns.substring(0, termsns.length() - 1) + ")发布软件（" + software.getCustomername() + "(" + versioninfo.get("softwareversion") + ")"+ "）升级消息");
                    sysMap.put("userid", userProfile.getCurrentUserId());
                    sysMap.put("userip", userProfile.getCurrentUserLoginIp());
                    syslogService.addSyslog(sysMap);
                } else {
                    msg = "发布失败：该版本软件与设备不适配！";
                }
            } else {
                msg = "软件获取失败：请重试！";
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
     * 设备分组
     *
     * @param request
     * @param idstr
     * @param menuId
     * @return
     */
    @RequestMapping(value = "/deviceToGroup")
    @ResponseBody
    public JSONObject deviceToGroup(HttpServletRequest request, String idstr, Integer menuId) {
        JSONObject jsonMsg = new JSONObject();
        String msg = "success";
        try {
            //获取用户信息
            HttpSession session = request.getSession();
            UserProfile userProfile = (UserProfile) session.getAttribute("userProfile");
            Userinfo user = userinfoService.getUserById(userProfile.getCurrentUserId());

            //获取设备列表
            JSONObject idmap = JSONObject.parseObject(idstr);

            //获取菜单信息
            Menu menu = menuService.getMenuById(menuId);

            //根据权限修改设备信息
            int adminId = 888;
            if (user != null && userProfile.getCurrentUserId() != adminId) {
                //普通用户，设备分配到分组
                Device device = new Device();
                if (menu != null && ConvUtil.convToInt(menu.getRank()) != 1) {
                    for (Object value : idmap.values()) {
                        device = deviceService.searchDeviceById(ConvUtil.convToInt(value));
                        device.setProjectid(menuId);
                        deviceService.update(device);
                    }
                } else {
                    if (menu == null) {
                        for (Object value : idmap.values()) {
                            device = deviceService.searchDeviceById(ConvUtil.convToInt(value));
                            device.setProjectid(0);
                            deviceService.update(device);
                        }
                    } else {
                        msg = "分组失败：分组不存在！";
                    }
                }
            } else {
                //管理员，设备分配到公司
                Device device = new Device();
                if (menu != null && ConvUtil.convToInt(menu.getRank()) == 1) {
                    for (Object value : idmap.values()) {
                        device = deviceService.searchDeviceById(ConvUtil.convToInt(value));
                        device.setCompanyid(menuId);
                        device.setProjectid(0);
                        deviceService.update(device);
                    }
                } else {
                    if (menu == null) {
                        for (Object value : idmap.values()) {
                            device = deviceService.searchDeviceById(ConvUtil.convToInt(value));
                            device.setCompanyid(0);
                            device.setProjectid(0);
                            deviceService.update(device);
                        }
                    } else {
                        msg = "分组失败：公司不存在！";
                    }
                }
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
     * 软件下载
     *
     * @param request
     * @param response
     * @param index
     */
    @RequestMapping(value = "/softwaredownload")
    public void downSoftware(HttpServletRequest request, HttpServletResponse response, Integer index) {
        try {
            logger.info("接口下载软件开始...");
            Software software = softwareService.getSoftwareById(index);
            String ctxPath = PropertiesUtil.getDataFromPropertiseFile("deviceCommon", "zip_path") + software.getFiletimestamp();
            //FileUtil.downFile(response, ctxPath, software.getUploadsoftwarename(), ConvUtil.convToInt(software.getSize()))

            //断点续传
            FileUtil.downFileByBreak(request, response, ctxPath, software.getUploadsoftwarename());
            logger.info("下载软件完成！");
        } catch (Exception e) {
            logger.error("下载软件异常：" + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 模板下载
     *
     * @param response
     */
    @RequestMapping(value = "/downtemplete")
    public void downtemplete(HttpServletResponse response) {
        try {
            String templetePath = PropertiesUtil.getDataFromPropertiseFile("deviceCommon", "templete_path");
            String templeteName = PropertiesUtil.getDataFromPropertiseFile("deviceCommon", "templete_name");
            String fullPath = templetePath + "/" + templeteName;
            FileUtil fileUtil = new FileUtil();
            //下载文件
            boolean downflag = fileUtil.writeFlieToPath(response, fullPath, templeteName);

            if (!downflag) {
                logger.error("下载模板失败：模板不存在！");
            } else {
                logger.info("下载模板完成！");
            }
        } catch (Exception e) {
            logger.error("下载模板异常：" + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 导入excel
     *
     * @param request
     * @param response
     * @return
     * @throws IOException
     */
    @RequestMapping(value = "/excelimport", method = RequestMethod.POST)
    @ResponseBody
    public JSONObject excelimport(HttpServletRequest request, HttpServletResponse response) throws IOException {
        logger.info("Excel解析开始...");
        JSONObject jsonMsg = new JSONObject();
        String msg = "success";
        MultipartHttpServletRequest multipartHttpServletRequest = (MultipartHttpServletRequest) request;
        MultipartFile multipartFile = multipartHttpServletRequest.getFile("file");
        response.setHeader("Access-Control-Allow-Origin", "*");
        //Excel文件名
        String uploadExcelName = multipartFile.getOriginalFilename();
        uploadExcelName = FileUtil.getFileRealName(uploadExcelName);
        String ctxPath = PropertiesUtil.getDataFromPropertiseFile("deviceCommon", "excel_path");
        String excelPath = ctxPath + "/" + uploadExcelName;
        try {
            //文件路径若不存在，就先创建路径
            File target = new File(ctxPath);
            if (!target.exists()) {
                target.mkdirs();
            }
            /*先上传文件，再解析*/
            FileUtils.copyInputStreamToFile(multipartFile.getInputStream(), new File(excelPath));
            /*获取解析内容*/
            logger.info("开始解析文件内容.................");
            List<List<String>> excelList = ExcelUtil.getSheetList(excelPath, 1);
            if (excelList.size() == 0) {
                msg = "文件内容为空！";
            } else if (excelList.size() == 1) {
                String fail = "fail";
                if (excelList.get(0).get(0).equals(fail)) {
                    msg = excelList.get(0).get(1);
                } else {
                    msg = "文件内容为空！";
                }
            } else {
                /*解析成功读取文件*/
                logger.info("解析成功，开始入库.................");
                for (int i = 1; i < excelList.size(); i++) {
                    List<String> deviceinfo = excelList.get(i);
                    Device device = deviceService.searchDeviceBySn(deviceinfo.get(0));
                    if (!"".equals(ConvUtil.convToStr(device.getId()))) {
                        int projectid = ConvUtil.convToInt(PinyinUtil.strToInt(deviceinfo.get(2)) + "" + PinyinUtil.strToPinyin(deviceinfo.get(2)).length());
                        device.setProjectid(projectid);
                        device.setBarcode(deviceinfo.get(1));
                        device.setProjectname(deviceinfo.get(2));
                        if (deviceinfo.get(3).indexOf(".") > -1) {
                            device.setOrderid(deviceinfo.get(3).substring(0, deviceinfo.get(3).indexOf(".")));
                        } else {
                            device.setOrderid(deviceinfo.get(3));
                        }
                        device.setContact(deviceinfo.get(4));
                        if (deviceinfo.get(5).indexOf(".") > -1) {
                            device.setTelno(deviceinfo.get(5).substring(0, deviceinfo.get(3).indexOf(".")));
                        } else {
                            device.setTelno(deviceinfo.get(5));
                        }
                        device.setAddress(deviceinfo.get(6));
                        device.setRemark(deviceinfo.get(7));
                        device.setModitytime(new Date());
                        deviceService.update(device);
                    }

                }
                logger.info("入库成功！");
            }
        } catch (Exception e) {
            //解析失败，删除文件
            FileUtil.delefile(excelPath);
            logger.error("解析Excel异常:{" + e.getMessage() + "}");
            e.printStackTrace();
            msg = "解析Excel异常！";
        }
        jsonMsg.put("obj1", msg);
        return jsonMsg;
    }
}
