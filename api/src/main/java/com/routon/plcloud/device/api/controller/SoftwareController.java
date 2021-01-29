package com.routon.plcloud.device.api.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.routon.plcloud.device.api.controller.commom.CommonForUser;
import com.routon.plcloud.device.api.utils.*;
import com.routon.plcloud.device.api.config.emq.EmqClient;
import com.routon.plcloud.device.api.config.UserProfile;
import com.routon.plcloud.device.core.utils.ConvUtil;
import com.routon.plcloud.device.core.utils.PropertiesUtil;
import com.routon.plcloud.device.core.common.TreeBean;
import com.routon.plcloud.device.core.service.SoftwareService;
import com.routon.plcloud.device.core.service.SyslogService;
import com.routon.plcloud.device.data.entity.Software;
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
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

/**
 * @author FireWang
 * @date 2020/5/6 22:12
 * 软件管理
 */
@Controller
@RequestMapping(value = "/software")
public class SoftwareController {
    private Logger logger = LoggerFactory.getLogger(SoftwareController.class);

    @Autowired
    private SoftwareService softwareService;

    @Autowired
    private SyslogService syslogService;

    @Autowired
    private EmqClient emqClient;

    private String success = "success";

    /**
     * 软件管理页面初始化
     */
    @RequestMapping(value = "/list")
    public String listinit(HttpServletRequest request, Model model, String typeid, String erpcode, String softwarename,
                           Integer page, Integer pageSize, Integer maxPage, Integer columns, String msg) throws Exception {
        try {
            //当前用户信息
            UserProfile userProfile = CommonForUser.getUserProfile(request);
            model = CommonForUser.getUserinfo(model, request);

            /*获取软件树形分级目录*/
            List<TreeBean> softwaretree = softwareService.getSoftwareNameTree(userProfile.getCurrentUserId());

            /*添加查询条件*/
            Map<String, Object> paramMap = new HashMap<>(16);
            //非管理员只显示自己上传的软件
            if (!userProfile.isTheAdmin()) {
                paramMap.put("downstatus", userProfile.getCurrentUserId());
            }
            paramMap.put("softwaretype", typeid);
            String noValue = "0";
            if (!ConvUtil.convToStr(erpcode).equals(noValue)) {
                paramMap.put("erpcode", erpcode);
            }
            paramMap.put("softwarename", softwarename);

            /* 树形目录查询定位 */
            if (!"".equals(ConvUtil.convToStr(softwarename))) {
                String erp = softwareService.getErpBySoftware(softwarename);
                if (!"".equals(erp)) {
                    erpcode = erp;
                }
            }

            /*设置最大页码，翻页用*/
            columns = softwareService.getMaxCount(paramMap);
            PageUtil currentPage = new PageUtil(page, pageSize, columns);
            model = CommonForUser.initPage(model, currentPage);

            /* 获取软件信息列表 */
            List<Software> softwarelist = softwareService.searchsoftwareforpage(paramMap, currentPage.getPage(), currentPage.getPageSize());

            /*设置返回页面的数据----分级目录数据*/
            //分级目录
            model.addAttribute("menuTreeBeans", JSONArray.toJSON(softwaretree));
            //选中数据的软件类型
            model.addAttribute("typeid", typeid);
            //选中的数据的软件erp
            model.addAttribute("erpcode", erpcode);
            //查询内容：软件名称
            model.addAttribute("softwarename", softwarename);

            //数据列表
            model.addAttribute("datalist", softwarelist);

            /*返回信息展示*/
            if (!"".equals(ConvUtil.convToStr(msg))) {
                model.addAttribute("msg", msg);
            }

        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        return "thymeleaf/software";
    }

    /**
     * 上传软件
     */
    @RequestMapping(value = "/softwareAdd", method = RequestMethod.POST)
    @ResponseBody
    public JSONObject softwareAdd(HttpServletRequest request, HttpServletResponse response) throws IOException {
        //获取用户信息
        UserProfile userProfile = (UserProfile) request.getSession().getAttribute("userProfile");
        JSONObject jsonMsg = new JSONObject();
        String msg = "success";
        logger.info("软件上传开始...");
        MultipartHttpServletRequest multipartHttpServletRequest = (MultipartHttpServletRequest) request;
        MultipartFile multipartFile = multipartHttpServletRequest.getFile("file");
        response.setHeader("Access-Control-Allow-Origin", "*");
        //软件包名
        String uploadsoftwarenameAll = multipartFile.getOriginalFilename();
        uploadsoftwarenameAll = FileUtil.getFileRealName(uploadsoftwarenameAll);
        String fileTimestamp = "";
        String ctxPath = "";
        //是否上传到服务器下
        boolean isUpToTomcat = false;
        //是否上传软件信息到数据库
        boolean isUpInfoToDb = false;
        Map<String, Object> paramMap = new HashMap<>(16);
        paramMap.put("uploadsoftwarename", uploadsoftwarenameAll);
        Software software = new Software();
        try {
            String zip = ".zip", dot = ".";
            if (!zip.equals(uploadsoftwarenameAll.substring(uploadsoftwarenameAll.lastIndexOf(dot)))) {
                msg = "文件格式错误：请上传(.zip)格式的文件！";
            } else {
                //1.上传软件到服务器路径下
                fileTimestamp = ConvUtil.convToStr(DateUtils.getTimestamp());
                ctxPath = PropertiesUtil.getDataFromPropertiseFile("deviceCommon", "zip_path") + fileTimestamp + "/";

                logger.info("开始上传文件...");
                isUpToTomcat = FileUtil.uploadToPath(multipartFile, ctxPath, uploadsoftwarenameAll);
                if (isUpToTomcat) {
                    logger.info("已上传到服务器...");
                } else {
                    msg = "上传失败：服务器正忙，请稍后重试！";
                    jsonMsg.put("obj1", msg);
                    return jsonMsg;
                }

                //文件路径
                String serverFilePath = ctxPath + uploadsoftwarenameAll;

                //2.读取version.txt中的信息到paramMap
                paramMap = txtToMap(paramMap, serverFilePath);

                String versionTxt = "version.txt";
                if (!paramMap.containsKey(versionTxt)) {
                    msg = "上传失败：软件包(.zip)内缺少必要的version.txt说明文件";
                    jsonMsg.put("obj1", msg);
                    return jsonMsg;
                }

                String erpcode = "erpcode", softversion = "softwareversion";
                if (!paramMap.containsKey(erpcode) || !paramMap.containsKey(softversion) ||
                        !paramMap.containsKey("softwarename") || !paramMap.containsKey("customername") ||
                        !paramMap.containsKey("signflag")) {
                    msg = "上传失败：软件包(.zip)内的version.txt说明文件缺少必要信息";
                    jsonMsg.put("obj1", msg);
                    return jsonMsg;
                }
                paramMap.put("size", multipartFile.getSize());
                paramMap.put("filetimestamp", fileTimestamp);

                //3.将软件信息存入software表
                if (softwareService.isExist(paramMap)) {
                    msg = "软件上传失败：版本重复，请删除对应版本后再次上传！";
                    jsonMsg.put("obj1", msg);
                    return jsonMsg;
                } else {
                    //添加userid（v1.4.0版本添加）
                    paramMap.put("downstatus", userProfile.getCurrentUserId());
                    software = softwareService.addSoftwarePro(paramMap);
                }

                //添加系统日志
                Map<String, Object> sysMap = new HashMap<>(16);
                sysMap.put("type", 20);
                if (!"".equals(ConvUtil.convToStr(software.getId()))) {
                    isUpInfoToDb = true;

                    //上传成功更新同款软件的中文名（customername）
                    softwareService.updateSameSoftwareCustName(ConvUtil.convToStr(software.getErpcode()), software.getCustomername());

                    sysMap.put("loginfo", "用户(" + userProfile.getCurrentUserLoginName() + ")上传软件：" + software.getCustomername() + "(" + software.getSoftwareversion() + ")");
                } else {
                    sysMap.put("loginfo", "用户(" + userProfile.getCurrentUserLoginName() + ")上传软件失败：" + uploadsoftwarenameAll);
                }
                sysMap.put("userid", userProfile.getCurrentUserId());
                sysMap.put("userip", userProfile.getCurrentUserLoginIp());
                syslogService.addSyslog(sysMap);
            }
        } catch (Exception e) {
            if (isUpToTomcat) {
                //上传失败，删除文件
                FileUtil.delefile(ctxPath);
            }
            if (isUpInfoToDb) {
                //删除表数据
                if (!softwareService.deleteSoftware(software)) {
                    logger.info("软件上传失败，数据删除异常:{" + e.getMessage() + "}");
                }
            }
            logger.error("软件上传异常：{" + e.getMessage() + "}");
            e.printStackTrace();
            msg = e.getMessage();
        }
        jsonMsg.put("obj1", msg);
        return jsonMsg;
    }

    /**
     * 解析Zipz中的TXT字段到Map
     *
     * @param paramMap
     * @param serverFilePath
     * @return
     */
    private Map<String, Object> txtToMap(Map<String, Object> paramMap, String serverFilePath) {
        try {
            ZipInputStream zin = new ZipInputStream(new BufferedInputStream(new FileInputStream(serverFilePath)));
            ZipEntry zipEntry;
            ZipFile zipFile = new ZipFile(serverFilePath);
            String softwareversion = "";
            while ((zipEntry = zin.getNextEntry()) != null) {
                if (zipEntry.getName().contains("version.txt")) {
                    paramMap.put("version.txt", "version.txt");
                    long size = zipEntry.getSize();
                    if (size > 0) {
                        BufferedReader br = new BufferedReader(new InputStreamReader(zipFile.getInputStream(zipEntry), "GBK"));
                        String line;
                        while ((line = br.readLine()) != null) {
                            if (line.contains("software_erp")) {
                                paramMap.put("erpcode", line.substring(line.indexOf("=") + 1));
                            }
                            if (line.contains("software_version")) {
                                if (!"".equals(softwareversion)) {
                                    softwareversion = line.substring(line.indexOf("=") + 1) + softwareversion;
                                } else {
                                    softwareversion = line.substring(line.indexOf("=") + 1);
                                }
                            }
                            if (line.contains("software_name")) {
                                paramMap.put("softwarename", line.substring(line.indexOf("=") + 1));
                            }
                            if (line.contains("erpcode_desc")) {
                                paramMap.put("customername", line.substring(line.indexOf("=") + 1));
                            }
                            if (line.contains("is_support_sign")) {
                                paramMap.put("signflag", line.substring(line.indexOf("=") + 1));
                            }
                            if (line.contains("fit_devmodel")) {
                                paramMap.put("fitmodel", line.substring(line.indexOf("=") + 1));
                            }
                            if (line.contains("version_postfix")) {
                                if (!"".equals(softwareversion)) {
                                    softwareversion = softwareversion + line.substring(line.indexOf("=") + 1);
                                } else {
                                    softwareversion = line.substring(line.indexOf("=") + 1);
                                }
                            }
                            if (line.contains("publish_without_install")) {
                                if("true".equals(line.indexOf("=") + 1)) {
                                    paramMap.put("pubflag", 1);
                                }
                            }
                        }
                        br.close();
                    }
                }
            }
            paramMap.put("softwareversion", softwareversion);
            zin.closeEntry();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return paramMap;
    }

    /**
     * 删除已上传的软件
     */
    @RequestMapping(value = "/deleteSoftware", method = RequestMethod.POST)
    @ResponseBody
    public JSONObject deleteSoftware(HttpServletRequest request, Integer id) {
        JSONObject jsonMsg = new JSONObject();
        String msg = "success";
        try {
            logger.info("删除软件开始...");
            Software software = softwareService.getSoftwareById(id);
            String ctxPath = PropertiesUtil.getDataFromPropertiseFile("deviceCommon", "zip_path") + software.getFiletimestamp() + "/" + software.getUploadsoftwarename();

            /* 1.删除软件包 */
            FileUtil.delefile(ctxPath);

            /* 2.删除二维码 */
            if (!"".equals(ConvUtil.convToStr(software.getQrpath()))) {
                FileUtil.delefile(software.getQrpath());
            }

            /* 3.删除表信息 */
            if (!softwareService.deleteSoftware(software)) {
                msg = "删除软件失败：软件不存在！";
            }

            //添加系统日志
            UserProfile userProfile = (UserProfile) request.getSession().getAttribute("userProfile");
            Map<String, Object> sysMap = new HashMap<>(16);
            sysMap.put("type", 22);
            sysMap.put("loginfo", "用户(" + userProfile.getCurrentUserLoginName() + ")删除软件：" + software.getCustomername() + "(" + software.getSoftwareversion() + ")");
            sysMap.put("userid", userProfile.getCurrentUserId());
            sysMap.put("userip", userProfile.getCurrentUserLoginIp());
            syslogService.addSyslog(sysMap);

        } catch (Exception e) {
            logger.error("删除软件异常：" + e.getMessage());
            e.printStackTrace();
            msg = "删除失败：系统异常，请重试！";
        }
        jsonMsg.put("obj1", msg);
        return jsonMsg;
    }

    /**
     * 软件下载
     */
    @RequestMapping(value = "/downSoftware")
    public void downSoftware(HttpServletRequest request, HttpServletResponse response, Integer id) {
        try {
            logger.info("下载软件开始...");
            Software software = softwareService.getSoftwareById(id);
            String ctxPath = PropertiesUtil.getDataFromPropertiseFile("deviceCommon", "zip_path") + software.getFiletimestamp();
            boolean isdown = FileUtil.downFile(response, ctxPath, software.getUploadsoftwarename(), ConvUtil.convToInt(software.getSize()));

            //添加系统日志
            UserProfile userProfile = (UserProfile) request.getSession().getAttribute("userProfile");
            Map<String, Object> sysMap = new HashMap<>(16);
            sysMap.put("type", 21);
            sysMap.put("loginfo", "用户(" + ConvUtil.convToStr(userProfile.getCurrentUserLoginName()) + ")下载软件：" + software.getCustomername() + "(" + software.getSoftwareversion() + ")");
            sysMap.put("userid", ConvUtil.convToInt(userProfile.getCurrentUserId()));
            sysMap.put("userip", ConvUtil.convToStr(userProfile.getCurrentUserLoginIp()));
            if (!isdown) {
                sysMap.put("remark", "下载失败：没找到文件！");
            }
            syslogService.addSyslog(sysMap);

            logger.info("下载软件完成！");
        } catch (Exception e) {
            logger.error("下载软件异常:" + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 获取二维码
     */
    @RequestMapping(value = "/getqrcode")
    @ResponseBody
    public JSONObject getQrbyid(Integer id) {
        JSONObject resJson = new JSONObject();
        String msg = "查看失败！";
        Software software = softwareService.getSoftwareById(id);
        try {
            /* 判断是否已经生成二维码 */
            File qrFile = new File(ConvUtil.convToStr(software.getQrpath()));

            /* 不存在就先生成 */
            if (!qrFile.exists()) {
                new File(PropertiesUtil.getDataFromPropertiseFile("deviceCommon", "qrcode_path")).mkdirs();
                List<String> content = new ArrayList<>();
                content.add(software.getCustomername());
                content.add(software.getSoftwarename());
                content.add(software.getSoftwareversion());

                /* 二维码生成规则：9位erp+3位顺序位+1位校验位，校验位等于3减去顺序位的位数*/
                StringBuilder numstr = new StringBuilder(ConvUtil.convToStr(softwareService.getNumByErp(software)));
                /*顺序位前面补0*/
                int orderlen = 3;
                if (numstr.length() < orderlen) {
                    int addzero = 3 - numstr.length();
                    for (int i = 0; i < addzero; i++) {
                        StringBuilder zero = new StringBuilder("0");
                        numstr = zero.append(numstr);
                    }
                }
                /*末尾添加校验码*/
                String qrcode = HexStrUtil.appendChecksum(software.getErpcode() + numstr.toString());
                content.add("程序码：" + qrcode);

                /* 生成二维码 */
                String qrpath = PropertiesUtil.getDataFromPropertiseFile("deviceCommon", "qrcode_path") + "/" + qrcode + ".jpg";
                getQrCode("程序二维码", content, qrcode, qrpath);

                /* 更新数据库 */
                software.setQrcode(qrcode);
                software.setQrpath(qrpath);
                softwareService.update(software);
            }

            /* 返回展示二维码：返回下载路径 */
            resJson.put("obj2", "/DMIL/software/downqr?id=" + software.getId());
            msg = "success";

        } catch (Exception e) {
            logger.error(e.getMessage());
            e.printStackTrace();
            resJson.put("obj1", e.getMessage());
            return resJson;
        }
        resJson.put("obj1", msg);
        return resJson;
    }

    /**
     * 二维码下载
     */
    @RequestMapping(value = "/downqr")
    public void downqr(HttpServletResponse response, Integer id) {
        try {
            logger.info("下载二维码开始...");

            Software software = softwareService.getSoftwareById(id);
            String ctxPath = PropertiesUtil.getDataFromPropertiseFile("deviceCommon", "qrcode_path");
            FileUtil.downFile(response, ctxPath, software.getQrcode() + ".jpg", 0);

            logger.info("下载二维码完成！");
        } catch (Exception e) {
            logger.error("下载二维码异常：" + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 软件发布
     */
    @RequestMapping(value = "/pubforsoftware")
    @ResponseBody
    public JSONObject pubforsoftware(Integer id) {
        JSONObject resJson = new JSONObject();
        String msg = "success";
        try {
            //获取版本对象
            Software software = softwareService.getSoftwareById(id);
            JSONObject version = softwareService.getVersion(software);

            //发布软件
            String pubflag = emqClient.pubTopic("update/" + software.getSoftwarename(), JSONObject.toJSONString(version));
            if (pubflag.equals(success)) {
                logger.info("软件发布完成！");
            } else {
                msg = "发布失败：消息服务器异常！";
                logger.info("软件发布失败...");
            }

        } catch (Exception e) {
            msg = "发布失败！";
            logger.error("软件发布异常：" + e.getMessage());
            e.printStackTrace();
        }
        resJson.put("obj1", msg);
        return resJson;
    }

    /**
     * 软件备注
     */
    @RequestMapping(value = "/remarksoftware")
    @ResponseBody
    public JSONObject remarksoftware(Integer id, String remark) {
        JSONObject resJson = new JSONObject();
        String msg = "success";
        try {
            //获取软件对象
            Software software = softwareService.getSoftwareById(id);

            //保存软件
            software.setRemark(remark);
            if (!softwareService.update(software)) {
                msg = "备注失败!";
            }
        } catch (Exception e) {
            msg = "备注异常！";
            logger.error("软件备注异常：" + e.getMessage());
            e.printStackTrace();
        }
        resJson.put("obj1", msg);
        return resJson;
    }

    /**
     * 生成下方附文字的二维码图片
     *
     * @param content 下付文字内容
     * @param qrval   二维码内容
     * @param qrpath  二维码路径
     **/
    private boolean getQrCode(String title, List<String> content, String qrval, String qrpath) {
        boolean issucc = false;
        try {
            MultiFormatWriter multiFormatWriter = new MultiFormatWriter();

            @SuppressWarnings("rawtypes")
            Map hints = new HashMap(16);

            //设置UTF-8， 防止中文乱码
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            //设置二维码四周白色区域的大小
            hints.put(EncodeHintType.MARGIN, 2);
            //设置二维码的容错性
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);

            //width:图片完整的宽;height:图片完整的高
            //因为要在二维码下方附上文字，所以把图片设置为长方形（高大于宽）
            int width = 450;
            int height = 680;

            //画二维码，记得调用multiFormatWriter.encode()时最后要带上hints参数，不然上面设置无效
            BitMatrix bitMatrix = multiFormatWriter.encode(qrval, BarcodeFormat.QR_CODE, width, height, hints);

            //开始画二维码
            BufferedImage barCodeImage = QrCodeUtils.writeToFile(bitMatrix, "jpg");

            //字体大小
            int font = 20;
            //字体风格
            String fontStyle = "宋体";

            //在二维码下方添加文字（文字居中）
            issucc = QrCodeUtils.pressText(title, content, qrpath, barCodeImage, fontStyle, Color.black, font, width, height);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return issucc;
    }

}
