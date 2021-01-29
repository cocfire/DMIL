package com.routon.plcloud.device.api.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.routon.plcloud.device.api.config.UserProfile;
import com.routon.plcloud.device.api.config.emq.EmqClient;
import com.routon.plcloud.device.api.controller.commom.CommonForDevice;
import com.routon.plcloud.device.api.controller.commom.CommonForUser;
import com.routon.plcloud.device.api.utils.*;
import com.routon.plcloud.device.core.common.TreeBean;
import com.routon.plcloud.device.core.service.*;
import com.routon.plcloud.device.core.utils.ConvUtil;
import com.routon.plcloud.device.core.utils.PropertiesUtil;
import com.routon.plcloud.device.data.entity.*;
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

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.util.*;

/**
 * @author FireWang
 * @date 2020/8/17 10:30
 * 文件管理
 */
@Controller
@RequestMapping(value = "/file")
public class FileController {
    private static Logger logger = LoggerFactory.getLogger(FileController.class);

    @Autowired
    private FileinfoService fileinfoService;

    @Autowired
    private ProgramsService programsService;

    @Autowired
    private DeviceService deviceService;

    @Autowired
    private OfflinemsgService offlinemsgService;


    @Autowired
    private SyslogService syslogService;

    @Autowired
    private UserinfoService userinfoService;

    @Autowired
    DatastatsService datastatsService;

    @Autowired
    MenuService menuService;

    @Autowired
    private EmqClient emqClient;

    /**
     * 初始化信息发布
     *
     * @param model
     * @param programid
     * @param companyid
     * @param msg
     * @param remark
     * @param page
     * @param pageSize
     * @param maxPage
     * @param columns
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/list")
    public String listinit(HttpServletRequest request, Model model, String msg, String remark, Integer filetype,
                           Integer programid, Integer companyid, String start, String end,
                           Integer page, Integer pageSize, Integer maxPage, Integer columns) throws Exception {
        try {
            //当前用户信息
            UserProfile userProfile = CommonForUser.getUserProfile(request);
            model = CommonForUser.getUserinfo(model, request);

            //选中数据的公司
            model.addAttribute("companyid", companyid);
            model.addAttribute("programid", programid);

            //查询框添加
            Map<String, Object> condition = new HashMap<>(16);

            //非管理员用户只能查看该账号下设备
            int adminId = 888, searchId = 0;
            if (userProfile.getCurrentUserId() != adminId) {
                Userinfo user = userinfoService.getUserById(userProfile.getCurrentUserId());
                if (user != null) {
                    if (user.getCompany() != null) {
                        companyid = ConvUtil.convToInt(user.getCompany());

                        //查询框加入公司信息
                        condition.put("companyid", ConvUtil.convToInt(user.getCompany()));
                    } else {
                        companyid = 2;
                    }
                    searchId = companyid;
                } else {
                    //登录过程中用户被删除，返回登录页
                    return new LoginController().logout(request);
                }
            }

            /* 获取软件树形分级目录 */
            List<TreeBean> programtree = programsService.getProgramTree(companyid, searchId);

            /* 添加查询条件 */
            Map<String, Object> paramMap = new HashMap<>(16);
            paramMap.put("companyid", companyid);
            paramMap.put("programid", programid);
            paramMap.put("remark", remark);
            paramMap.put("filetype", filetype);
            paramMap.put("start", start);
            paramMap.put("end", end);
            if (!userProfile.isTheAdmin()) {
                paramMap.put("userid", userProfile.getCurrentUserId());
            }

            /* 设置最大页码，翻页用 */
            columns = fileinfoService.getMaxCount(paramMap);
            PageUtil currentPage = new PageUtil(page, pageSize, columns);
            model = CommonForUser.initPage(model, currentPage);

            /* 获取设备信息列表 */
            List<Fileinfo> filelist = fileinfoService.searchFileforpage(paramMap, currentPage.getPage(), currentPage.getPageSize());

            /** 设置返回页面的数据----分级目录数据 */
            //分级目录
            model.addAttribute("menuTreeBeans", JSONArray.toJSON(programtree));

            //还原查询条件
            model.addAttribute("start", start);
            model.addAttribute("end", end);
            model.addAttribute("remark", remark);
            model.addAttribute("choosetype", filetype);

            /*返回信息展示*/
            if (!"".equals(ConvUtil.convToStr(msg))) {
                model.addAttribute("msg", msg);
            }

            //数据列表
            model.addAttribute("datalist", filelist);


        } catch (Exception e) {
            logger.error(e.getMessage());
            e.printStackTrace();
        }
        return "thymeleaf/files";
    }

    /**
     * 上传文件
     */
    @RequestMapping(value = "/fileUpload", method = RequestMethod.POST)
    @ResponseBody
    public JSONObject fileUpload(HttpServletRequest request, HttpServletResponse response) throws Exception {
        JSONObject jsonMsg = new JSONObject();
        String msg = "success";
        logger.info("文件上传开始...");
        MultipartHttpServletRequest multipartHttpServletRequest = (MultipartHttpServletRequest) request;
        MultipartFile multipartFile = multipartHttpServletRequest.getFile("file");
        response.setHeader("Access-Control-Allow-Origin", "*");
        //文件上传名称
        String uploadname = multipartFile.getOriginalFilename(), filename = "", filePath = "";
        //获取文件名，该方法兼容IE浏览器（带盘符信息）
        uploadname = FileUtil.getFileRealName(uploadname);
        //是否上传到服务器下
        boolean isUpToTomcat = false;
        //是否上传软件信息到数据库
        boolean isUpInfoToDb = false;
        Fileinfo fileinfo = new Fileinfo();
        try {
            //文件原后缀
            String dot = ".", fmt = uploadname.substring(uploadname.lastIndexOf(dot));

            //文件类型：1-图片；2-影音文件；3-办公文档；4-压缩文件；5-执行文件；6-图形文件；7-程序文件
            int filetype = FileTypeUtil.getFileTypeNumByName(uploadname);

            if (filetype == 0) {
                msg = "文件类型暂不支持，请重新上传！";
            } else {
                //1.上传软件到服务器路径下
                String fileTimestamp = ConvUtil.convToStr(DateUtils.getTimestamp());
                filename = fileTimestamp + fmt;
                String ctxPath = PropertiesUtil.getDataFromPropertiseFile("deviceCommon", "image_path") + "/";
                //文件路径
                filePath = ctxPath + filename;

                logger.info("开始上传文件...");
                isUpToTomcat = FileUtil.uploadToPath(multipartFile, ctxPath, filename);
                if (!isUpToTomcat) {
                    msg = "上传失败：服务器正忙，请稍后重试！";
                    jsonMsg.put("obj1", msg);
                    return jsonMsg;
                }

                //2.将文件信息存入fileinfo表
                fileinfo.setFilename(filename);
                fileinfo.setFilepath(filePath);
                fileinfo.setFilesize(multipartFile.getSize() + "");
                fileinfo.setFiletype(filetype);
                fileinfo.setUploadname(uploadname);
                fileinfo.setRemark(uploadname);
                fileinfo.setUploadtime(new Date());

                //若为视频文件则获取缩略图
                int tow = 2;
                if (filetype == tow) {
                    //截图名称
                    String shotname = fileTimestamp + ".jpg";

                    //截图路径
                    String shotPath = PropertiesUtil.getDataFromPropertiseFile("deviceCommon", "screenshot_path") + "/";
                    String screenshotPath = shotPath + shotname;

                    //获取截图
                    BufferedImage bufferedImage = VideoUtil.getFrame(new File(filePath), 30);

                    //获取不到截图的用系统默认图
                    if (bufferedImage == null) {
                        //使用示例图片
                        InputStream shotis = FileUtil.class.getResourceAsStream("/static/icon/playimageicon1.jpg");
                        FileUtil.uploadToPath(shotis, shotPath, shotname);
                    } else {
                        File targetFile = new File(shotPath);
                        //生成文件路径中包含的文件夹
                        if (!targetFile.exists()) {
                            targetFile.mkdirs();
                        }
                        File outputfile = new File(screenshotPath);
                        ImageIO.write(bufferedImage, "jpg", outputfile);
                    }

                }

                //获取文件MD5大小
                fileinfo.setMd5size(FileTypeUtil.getMd5(multipartFile));

                //获取userid
                UserProfile userProfile = (UserProfile) request.getSession().getAttribute("userProfile");
                fileinfo.setUserid(userProfile.getCurrentUserId());

                //保存文件到数据库
                isUpInfoToDb = fileinfoService.insertFile(fileinfo);

                //3.添加系统日志
                Map<String, Object> sysMap = new HashMap<>(16);
                sysMap.put("type", 50);
                if (isUpInfoToDb) {
                    sysMap.put("loginfo", "用户(" + userProfile.getCurrentUserLoginName() + ")上传文件：" + uploadname);
                } else {
                    sysMap.put("loginfo", "用户(" + userProfile.getCurrentUserLoginName() + ")上传文件失败：" + uploadname);
                }
                sysMap.put("userid", userProfile.getCurrentUserId());
                sysMap.put("userip", userProfile.getCurrentUserLoginIp());
                syslogService.addSyslog(sysMap);
            }
        } catch (Exception e) {
            if (isUpToTomcat) {
                //上传失败，删除文件
                FileUtil.delefile(filePath);
            }
            if (isUpInfoToDb) {
                //删除表数据
                if (!fileinfoService.deleteFile(fileinfo)) {
                    logger.info("文件上传失败，数据删除异常:{" + e.getMessage() + "}");
                }
            }
            logger.error("文件上传异常：{" + e.getMessage() + "}");
            e.printStackTrace();
            msg = "上传失败：文件异常！";
        }
        jsonMsg.put("obj1", msg);
        return jsonMsg;
    }

    /**
     * 文件下载
     *
     * @param request
     * @param response
     * @param id       文件内部id
     */
    @RequestMapping(value = "/downFile")
    public void downFile(HttpServletRequest request, HttpServletResponse response, Integer id) {
        try {
            Fileinfo fileinfo = fileinfoService.getFileById(id);
            if (fileinfo != null) {
                String ctxPath = PropertiesUtil.getDataFromPropertiseFile("deviceCommon", "image_path");
                //媒体文件均使用断点续传：
                FileUtil.downFileByBreak(request, response, ctxPath, fileinfo.getFilename());
            } else {
                logger.error("下载文件失败：文件不存在！");
            }
        } catch (Exception e) {
            logger.error("下载文件异常：" + e.getMessage());
            e.printStackTrace();
        }
    }

    @RequestMapping(value = "/downFileqq")
    public void downFileqq(HttpServletRequest request, HttpServletResponse response, Integer id) {
        try {
            Fileinfo fileinfo = fileinfoService.getFileById(id);
            if (fileinfo != null) {
                System.out.println("test");
            }
        } catch (Exception e) {
            logger.error("下载文件异常：" + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 缩略图下载
     *
     * @param response
     * @param id       文件内部id
     */
    @RequestMapping(value = "/downShotFile")
    public void downShotFile(HttpServletResponse response, Integer id) {
        try {
            Fileinfo fileinfo = fileinfoService.getFileById(id);
            if (fileinfo != null) {
                String ctxPath = PropertiesUtil.getDataFromPropertiseFile("deviceCommon", "screenshot_path");
                String shotname = fileinfo.getFilename().substring(0, fileinfo.getFilename().indexOf(".")) + ".jpg";
                FileUtil.downFile(response, ctxPath, shotname, 0);
            } else {
                logger.error("获取缩略图失败：视频不存在！");
            }

        } catch (Exception e) {
            logger.error("获取缩略图异常：" + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 文件删除
     */
    @RequestMapping(value = "deleteFile", method = RequestMethod.POST)
    @ResponseBody
    public JSONObject deleteFile(HttpServletRequest request, Integer id) {
        JSONObject jsonMsg = new JSONObject();
        String msg = "success";
        try {
            logger.info("删除文件开始...");
            Fileinfo fileinfo = fileinfoService.getFileById(id);

            /* 1.删除文件 */
            FileUtil.delefile(fileinfo.getFilepath());
            //如果是视频文件则需同时删除缩略图
            int tow = 2;
            if(ConvUtil.convToInt(fileinfo.getFiletype()) == tow) {
                //获取缩略图路径
                String shotPath = PropertiesUtil.getDataFromPropertiseFile("deviceCommon", "screenshot_path") + "/";
                String screenshotPath = shotPath + fileinfo.getFilename().substring(0, fileinfo.getFilename().lastIndexOf(".")) + ".jpg";
                FileUtil.delefile(screenshotPath);
            }

            /* 2.删除数据 */
            if (!fileinfoService.deleteFile(fileinfo)) {
                msg = "删除文件失败：文件不存在！";
            }

            //添加系统日志
            UserProfile userProfile = (UserProfile) request.getSession().getAttribute("userProfile");
            Map<String, Object> sysMap = new HashMap<>(16);
            sysMap.put("type", 52);
            sysMap.put("loginfo", "用户(" + userProfile.getCurrentUserLoginName() + ")删除文件：" + fileinfo.getRemark());
            sysMap.put("userid", userProfile.getCurrentUserId());
            sysMap.put("userip", userProfile.getCurrentUserLoginIp());
            syslogService.addSyslog(sysMap);

        } catch (Exception e) {
            logger.error("删除文件异常：" + e.getMessage());
            e.printStackTrace();
            msg = "删除失败：系统异常，请重试！";
        }
        jsonMsg.put("obj1", msg);
        return jsonMsg;
    }

    /**
     * 获取文件大图
     */
    @RequestMapping(value = "/getFile")
    @ResponseBody
    public JSONObject getFile(Integer id) {
        JSONObject resJson = new JSONObject();
        String msg = "预览失败：文件不存在！";
        try {
            Fileinfo fileinfo = fileinfoService.getFileById(id);
            if (fileinfo != null) {
                // 返回下载信息
                resJson.put("filetype", fileinfo.getFiletype());
                resJson.put("obj2", "/DMIL/file/downFile?id=" + fileinfo.getId());
                msg = "success";
            }
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
     * 文件备注
     */
    @RequestMapping(value = "remarkfile")
    @ResponseBody
    public JSONObject remarkfile(Integer id, String remark, String name) {
        JSONObject resJson = new JSONObject();
        String msg = "success";
        try {
            Fileinfo fileinfo;
            //若为创建时备注获取文件名
            String fullfilename = ConvUtil.convToStr(name);
            String filename = fullfilename.substring(fullfilename.lastIndexOf(File.separator) + 1);
            if (!"".equals(filename)) {
                fileinfo = fileinfoService.getFileByUploadName(filename);
            } else {
                fileinfo = fileinfoService.getFileById(id);
            }

            //保存软件
            if (fileinfo != null) {
                fileinfo.setRemark(remark);
                if (!fileinfoService.update(fileinfo)) {
                    msg = "备注失败!";
                }
            }
        } catch (Exception e) {
            msg = "备注异常！";
            logger.error("文件备注异常：" + e.getMessage());
            e.printStackTrace();
        }
        resJson.put("obj1", msg);
        return resJson;
    }

    /**
     * 获取节目单详情
     *
     * @param programid
     * @return
     */
    @RequestMapping(value = "/getProgram")
    @ResponseBody
    public JSONObject getProgram(HttpServletRequest request, Integer programid) {
        JSONObject jsonMsg = new JSONObject();
        String msg = "success";
        try {
            Programs program = programsService.getProgramById(programid);

            if (program != null) {
                jsonMsg.put("obj2", program);

                //获取设备列表
                List<Device> devicelist = new ArrayList<>();
                String[] devices = ConvUtil.convToStr(program.getRemark()).split(",");
                for (String deviceid : devices) {
                    Device device = deviceService.getDeviceById(ConvUtil.convToInt(deviceid));
                    if (device != null) {
                        //校验设备所属公司
                        Integer company = ConvUtil.convToInt(program.getCompanyid());
                        Integer devicecompany = ConvUtil.convToInt(device.getCompanyid());
                        if (devicecompany.equals(company)) {
                            devicelist.add(device);
                        }
                    }
                }
                jsonMsg.put("devicelist", devicelist);

                //获取文件列表
                List<Fileinfo> filelist = new ArrayList<>();
                String[] fileids = program.getFileids().split(",");
                for (String fileid : fileids) {
                    Fileinfo fileinfo = fileinfoService.getFileById(ConvUtil.convToInt(fileid));
                    if (fileinfo != null) {
                        filelist.add(fileinfo);
                    }
                }
                jsonMsg.put("filelist", filelist);

                //时长信息
                jsonMsg.put("timelast", program.getTimelast().split(","));
            } else {
                msg = "节目单不存在！";
            }
        } catch (Exception e) {
            e.printStackTrace();
            msg = "系统异常！";
        }
        jsonMsg.put("obj1", msg);
        return jsonMsg;
    }

    /**
     * 删除节目单
     *
     * @param programid
     * @return
     */
    @RequestMapping(value = "/deleteProgram")
    @ResponseBody
    public JSONObject deleteProgram(Integer programid) {
        JSONObject jsonMsg = new JSONObject();
        String msg = "success";
        try {
            Programs program = programsService.getProgramById(programid);

            if (program != null) {
                programsService.deleteProgram(program);
            } else {
                msg = "节目单不存在！";
            }
        } catch (Exception e) {
            e.printStackTrace();
            msg = "系统异常！";
        }
        jsonMsg.put("obj1", msg);
        return jsonMsg;
    }

    /**
     * 获取设备列表
     *
     * @param termsn
     * @param remark
     * @param termmodel
     * @param projectid
     * @param status
     * @param devices
     * @return
     */
    @RequestMapping(value = "/getDeviceList")
    @ResponseBody
    public JSONObject getDevice(HttpServletRequest request, String termsn, String remark, String termmodel,
                                String projectid, Integer status, String devices, Integer toPage, Integer pageSize) {
        UserProfile userProfile = (UserProfile) request.getSession().getAttribute("userProfile");
        JSONObject jsonMsg = new JSONObject();
        String msg = "success";
        try {
            //非管理员并且尚未分配公司的用户没有权限
            String companyid = userProfile.getCurrentUserCompany();
            if (!userProfile.isTheAdmin() && "".equals(ConvUtil.convToStr(companyid))) {
                jsonMsg.put("obj1", "您没有权限：请联系管理员！");
                return jsonMsg;
            }

            //获取公司设备
            Map<String, Object> param = new HashMap<>(16);
            param.put("termsn", termsn);
            if (userProfile.isTheAdmin() && projectid != null) {
                companyid = projectid;
            } else {
                param.put("projectid", projectid);
            }
            param.put("companyid", companyid);
            param.put("a.remark", remark);
            param.put("termmodel", termmodel);
            param.put("a.status", status);

            //获取页码信息
            Integer columns = deviceService.getMaxCount(param);
            PageUtil currentPage = new PageUtil(ConvUtil.convToInt(toPage), pageSize, columns);

            List<Device> alllist = deviceService.searchdeviceforpage(param, currentPage.getPage(), currentPage.getPageSize());
            List<Device> devicelist = new ArrayList<>();
            //不显示已经选择的设备
            for (Device device : alllist) {
                if (!ConvUtil.convToStr(devices).contains(ConvUtil.convToStr(device.getId()))) {
                    devicelist.add(device);
                }
            }

            String[] devicearr = devices.split(",");
            columns = columns - devicearr.length;
            PageUtil truePage = new PageUtil(toPage, pageSize, columns);
            jsonMsg.put("maxPage", truePage.getMaxPage());
            jsonMsg.put("count", columns);

            jsonMsg.put("devicelist", devicelist);

            //获取设备类型列表
            Map<String, Object> condition = new HashMap<>(16);
            condition.put("companyid", companyid);
            List<Device> termlist = deviceService.searchDeviceClounmByCondition("termmodel", condition);
            jsonMsg.put("termlist", termlist);

            //公司分组信息
            List<Menu> groups;
            if (userProfile.isTheAdmin()) {
                groups = menuService.getMenuListByRank(1);
            } else {
                groups = menuService.getMenuListByPid(ConvUtil.convToInt(companyid));
            }
            jsonMsg.put("groups", groups);

        } catch (Exception e) {
            e.printStackTrace();
            msg = "系统异常！";
        }
        jsonMsg.put("obj1", msg);
        return jsonMsg;
    }

    /**
     * 获取文件列表
     *
     * @param remark
     * @param startTime
     * @param endTime
     * @param toPage
     * @return
     */
    @RequestMapping(value = "/getFileList")
    @ResponseBody
    public JSONObject getFileList(HttpServletRequest request, String remark, String startTime, String endTime,
                                  Integer toPage, Integer pageSize) {
        UserProfile userProfile = (UserProfile) request.getSession().getAttribute("userProfile");
        JSONObject jsonMsg = new JSONObject();
        String msg = "success";
        try {
            String companyid = userProfile.getCurrentUserCompany();
            if (!userProfile.isTheAdmin() && "".equals(ConvUtil.convToStr(companyid))) {
                msg = "您没有权限：请联系管理员！";
                jsonMsg.put("obj1", msg);
                return jsonMsg;
            }

            //获取公司文件
            Map<String, Object> paramMap = new HashMap<>(16);
            paramMap.put("companyid", companyid);
            paramMap.put("remark", remark);
            if (!userProfile.isTheAdmin()) {
                paramMap.put("userid", userProfile.getCurrentUserId());
            }
            paramMap.put("start", startTime);
            paramMap.put("end", endTime);

            //获取页码信息
            Integer columns = fileinfoService.getMaxCount(paramMap);
            PageUtil currentPage = new PageUtil(ConvUtil.convToInt(toPage), pageSize, columns);
            jsonMsg.put("maxPage", currentPage.getMaxPage());
            jsonMsg.put("count", columns);

            /* 获取设备信息列表 */
            List<Fileinfo> filelist = fileinfoService.searchFileforpage(paramMap, currentPage.getPage(), currentPage.getPageSize());
            jsonMsg.put("filelist", filelist);


        } catch (Exception e) {
            e.printStackTrace();
            msg = "系统异常！";
        }
        jsonMsg.put("obj1", msg);
        return jsonMsg;
    }

    /**
     * 保存节目单
     *
     * @param programid
     * @param programname
     * @param programtype
     * @param filmodel
     * @param deviceids
     * @param fileids
     * @param timelast
     * @return
     */
    @RequestMapping(value = "/saveProgram")
    @ResponseBody
    public JSONObject saveProgram(HttpServletRequest request, Integer programid, String programname, Integer programtype,
                                  String filmodel, String deviceids, String fileids, String timelast) {
        UserProfile userProfile = (UserProfile) request.getSession().getAttribute("userProfile");
        JSONObject jsonMsg = new JSONObject();
        String msg = "success";
        Programs program = new Programs();
        try {
            int companyid = ConvUtil.convToInt(userProfile.getCurrentUserCompany());

            //判断是否新增
            boolean isnew = true;
            if (programid != 0) {
                program = programsService.getProgramById(programid);
                isnew = false;
            }

            if (program != null) {
                //写入节目单信息
                program.setName(programname);
                program.setType(programtype);
                program.setFileids(fileids);
                program.setFitmodel(filmodel);
                program.setCreateuser(ConvUtil.convToInt(userProfile.getCurrentUserId()));
                //管理员不需要保存公司
                if (!userProfile.isTheAdmin()) {
                    program.setCompanyid(companyid);
                }

                //将设备更新到节目单
                program = checkDevice(program, deviceids, companyid);

                //播放时长设置，如果是0s改为默认10s
                String[] timestr = timelast.split(",");
                String timelasts = "";
                for (int i = 0; i < timestr.length; i++) {
                    String time = timestr[i];
                    if (ConvUtil.convToInt(time) == 0) {
                        time = "10";
                    }
                    if (i == 0) {
                        timelasts = time;
                    } else {
                        timelasts += "," + time;
                    }
                }
                program.setTimelast(timelasts);

                //保存节目单
                if (isnew) {
                    //判断节目单名称是否重复
                    if (programsService.getProgramByName(programname, companyid) == null) {
                        //新增
                        program.setCreatetime(new Date());
                        programsService.insertProgram(program);
                    } else {
                        msg = "保存失败：节目单名称重复！";
                    }
                } else {
                    //更新
                    programsService.update(program);
                }

            } else {
                msg = "保存失败：节目单不存在！";
            }
        } catch (Exception e) {
            e.printStackTrace();
            msg = "系统异常！";
        }
        jsonMsg.put("obj1", msg);
        return jsonMsg;
    }

    /**
     * 设备去重，保持设备只在一个节目单
     *
     * @param program
     * @param deviceids
     * @param companyid
     * @return
     */
    private Programs checkDevice(Programs program, String deviceids, Integer companyid) {
        if (!deviceids.equals(ConvUtil.convToStr(program.getRemark()))) {
            //这里如果有变化，先去掉其他节目单里的相同设备，保证设备唯一存在
            String[] devices = deviceids.split(",");
            for (String deviceid : devices) {
                //获取包含该设备的节目单
                List<Programs> pList = programsService.getProgramByDeviceId(ConvUtil.convToInt(deviceid), companyid) ;
                //从其他节目单里删除该设备信息
                for (Programs pgram : pList) {
                    String[] dids = pgram.getRemark().split(",");

                    //重新保存原有节目单
                    String newids = "";
                    for (int i = 0; i < dids.length; i++) {
                        if (!dids[i].equals(deviceid)) {
                            if (i == 0) {
                                newids = dids[1];
                            } else {
                                newids += "," + dids[i];
                            }
                        }
                    }
                    pgram.setRemark(newids);
                    programsService.update(pgram);
                }
            }
            program.setRemark(deviceids);
        }
        return program;
    }

    /**
     * 发布节目单
     *
     * @param programid
     * @param programname
     * @param programtype
     * @param filmodel
     * @param deviceids
     * @param fileids
     * @param timelast
     * @return
     */
    @RequestMapping(value = "/pubProgram")
    @ResponseBody
    public JSONObject pubProgram(HttpServletRequest request, Integer programid, String programname, Integer programtype,
                                 String filmodel, String deviceids, String fileids, String timelast) {
        JSONObject jsonMsg = new JSONObject();
        String msg = "success";
        try {
            //当前用户信息
            UserProfile userProfile = CommonForUser.getUserProfile(request);

            //先保存节目单再发布
            saveProgram(request, programid, programname, programtype, filmodel, deviceids, fileids, timelast);

            //获取公司设备
            List<Device> devicelist = new ArrayList<>();
            String companyid = userProfile.getCurrentUserCompany();
            if (!"".equals(ConvUtil.convToStr(companyid)) || userProfile.isTheAdmin()) {
                String[] devices = deviceids.split(",");
                for (String deviceid : devices) {
                    Device device = deviceService.searchDeviceById(ConvUtil.convToInt(deviceid));
                    //保险起见，只能向本公司设备发布，除非是管理员
                    if (userProfile.isTheAdmin()) {
                        devicelist.add(device);
                    } else {
                        if (companyid.equals(ConvUtil.convToStr(device.getCompanyid()))) {
                            devicelist.add(device);
                        }
                    }

                }
            } else {
                if (!userProfile.isTheAdmin()) {
                    msg = "发布失败：您没有权限！";
                    jsonMsg.put("obj1", msg);
                    return jsonMsg;
                }
            }

            //组织发布报文
            JSONObject message = new JSONObject();
            message.put("list_type", "1");
            message.put("play_rate", ConvUtil.convToStr(programtype));
            message.put("start_time", "");
            message.put("end_time", "");
            Programs program;
            if (userProfile.isTheAdmin()) {
                program = programsService.getProgramByOnlyName(programname);
            } else {
                program = programsService.getProgramByName(programname, ConvUtil.convToInt(companyid));
            }
            String url = PropertiesUtil.getDataFromPropertiseFile("deviceCommon", "hostrul") + "/DMIL/file/downFile";
            if (program != null) {
                //由于确认机制remark字段用于保存id，适用于v1.4.4及以上
                message.put("remark", ConvUtil.convToStr(program.getId()));

                //文件列表
                List<JSONObject> filelist = new ArrayList<>();
                String[] fileidstr = fileids.split(","), timestr = timelast.split(",");
                for (int i = 0; i < fileidstr.length; i++) {
                    Fileinfo fileinfo = fileinfoService.getFileById(ConvUtil.convToInt(fileidstr[i]));
                    if (fileinfo != null) {
                        JSONObject file = new JSONObject();
                        file.put("sn", i + 1);
                        file.put("file_type", fileinfo.getFiletype());
                        //若为视频文件则获取缩略图
                        int tow = 2;
                        if (ConvUtil.convToInt(fileinfo.getFiletype()) == tow) {
                            file.put("time_last", 0);
                        } else {
                            file.put("time_last", timestr[i]);
                        }
                        file.put("filename", fileinfo.getFilename());
                        file.put("totalsize", fileinfo.getFilesize());
                        file.put("md5", fileinfo.getMd5size());
                        file.put("url", url + "?id=" + fileinfo.getId());
                        filelist.add(file);
                    } else {
                        msg = "发布失败：文件已被删除！";
                        jsonMsg.put("obj1", msg);
                        return jsonMsg;
                    }
                }
                message.put("file_list", filelist);
            } else {
                msg = "节目单不存在！";
                jsonMsg.put("obj1", msg);
                return jsonMsg;
            }

            //发布节目单
            if (devicelist.size() > 0) {
                for (Device device : devicelist) {
                    //进行发布流程
                    String topic = "filelist/" + device.getTermsn();
                    CommonForDevice.emqRetPub(emqClient, offlinemsgService, topic, message, device, "");
                }

                //添加系统日志
                Map<String, Object> sysMap = new HashMap<>(16);
                sysMap.put("type", 53);
                sysMap.put("loginfo", "用户(" + userProfile.getCurrentUserLoginName() + ")发布节目单：" + programname);
                sysMap.put("userid", userProfile.getCurrentUserId());
                sysMap.put("userip", userProfile.getCurrentUserLoginIp());
                syslogService.addSyslog(sysMap);
            } else {
                msg = "没有可以发布的设备！";
            }

        } catch (Exception e) {
            e.printStackTrace();
            msg = "系统异常！";
        }
        jsonMsg.put("obj1", msg);
        return jsonMsg;
    }

    /**
     * 查询节目单
     *
     * @param querytype
     * @param queryinfo
     * @return
     */
    @RequestMapping(value = "/queryProgram")
    @ResponseBody
    public JSONObject queryProgram(HttpServletRequest request, Integer querytype, String queryinfo) {
        UserProfile userProfile = (UserProfile) request.getSession().getAttribute("userProfile");
        JSONObject jsonMsg = new JSONObject();
        String msg = "success";
        List<Programs> programlist = new ArrayList<>();
        try {
            //获取公司id
            int companyid = ConvUtil.convToInt(userProfile.getCurrentUserCompany());
            switch (querytype) {
                case 1:
                    //按照节目单名称查询
                    programlist = programsService.getProgramByNamecase(queryinfo, companyid);
                    break;
                case 2:
                    //按照终端序列号查询：先获取设备id，在根据设备id查询节目单
                    Device device = deviceService.searchDeviceBySn(queryinfo);
                    if (ConvUtil.convToInt(device.getId()) != 0) {
                        programlist = programsService.getProgramByDeviceId(ConvUtil.convToInt(device.getId()), companyid);
                    }
                    break;
                default:
            }

            jsonMsg.put("programlist", programlist);

        } catch (Exception e) {
            e.printStackTrace();
            msg = "系统异常！";
        }
        jsonMsg.put("obj1", msg);
        return jsonMsg;
    }

    /**
     * 获取文件统计数据
     *
     * @param id
     * @param dataOption
     * @param startTime
     * @param endTime
     * @return
     */
    @RequestMapping(value = "/getfiledata")
    @ResponseBody
    public JSONObject getfiledata(Integer id, Integer dataOption, String startTime, String endTime,
                                  Integer toPage, Integer pageSize) {
        JSONObject jsonMsg = new JSONObject();
        String msg = "success";
        try {
            //获取数据详情
            Map<String, Object> paramMap = new HashMap<>(16);
            paramMap.put("linktable", "fileinfo");
            paramMap.put("start", startTime);
            paramMap.put("end", endTime);
            paramMap.put("linkid", id);

            //获取页码信息
            List<Map> alldata = datastatsService.getDataByType(paramMap, dataOption);
            PageUtil currentPage = new PageUtil(ConvUtil.convToInt(toPage), pageSize, alldata.size());

            /* 获取设备信息列表 */
            List<Map> dataList = datastatsService.getDataByTypeForPage(paramMap, dataOption, currentPage.getPage(), currentPage.getPageSize());

            jsonMsg.put("maxPage", currentPage.getMaxPage());
            jsonMsg.put("count", alldata.size());
            jsonMsg.put("dataList", dataList);

        } catch (Exception e) {
            e.printStackTrace();
            msg = "系统异常！";
        }
        jsonMsg.put("obj1", msg);
        return jsonMsg;
    }

    /**
     * 获取设备统计数据
     *
     * @param dataOption
     * @param startTime
     * @param endTime
     * @return
     */
    @RequestMapping(value = "/getdevicedata")
    @ResponseBody
    public JSONObject getdevicedata(HttpServletRequest request, Integer dataOption, String startTime, String endTime,
                                    Integer toPage, Integer pageSize) {
        UserProfile userProfile = (UserProfile) request.getSession().getAttribute("userProfile");
        JSONObject jsonMsg = new JSONObject();
        String msg = "success";
        try {
            //获取查询条
            Map<String, Object> paramMap = getparamMap(userProfile, startTime, endTime);

            //获取页码信息
            List<Map> alldata = datastatsService.getDataByType1(paramMap, dataOption);
            PageUtil currentPage = new PageUtil(ConvUtil.convToInt(toPage), pageSize, alldata.size());

            /* 获取设备信息列表 */
            List<Map> dataList = datastatsService.getDataByTypeForPage1(paramMap, dataOption, currentPage.getPage(), currentPage.getPageSize());

            jsonMsg.put("maxPage", currentPage.getMaxPage());
            jsonMsg.put("count", alldata.size());
            jsonMsg.put("dataList", dataList);

        } catch (Exception e) {
            e.printStackTrace();
            msg = "系统异常！";
        }
        jsonMsg.put("obj1", msg);
        return jsonMsg;
    }

    /**
     * 统计数据导出
     *
     * @param response
     */
    @RequestMapping(value = "/dataOutPut")
    public void downtemplete(HttpServletRequest request, HttpServletResponse response,
                             Integer dataOption, String startTime, String endTime) {
        UserProfile userProfile = (UserProfile) request.getSession().getAttribute("userProfile");
        try {
            //获取数据详情
            Map<String, Object> paramMap = getparamMap(userProfile, startTime, endTime);
            // 获取设备信息列表
            List<Map> dataList = datastatsService.getDataByTypeForPage1(paramMap, dataOption, 1, Integer.MAX_VALUE);

            /** ---------------组织生成Excel的列表--------------- */
            List<List<String>> excelList = new ArrayList<>();
            //插入表头
            List<String> title = new ArrayList<>();
            title.add("统计日期");
            title.add("设备名称");
            title.add("素材名称");
            title.add("播放次数");
            excelList.add(title);
            //生成Excel数据列表
            for (Map dataOb : dataList) {
                String info = ConvUtil.convToStr(dataOb.get("info"));
                String counts = ConvUtil.convToStr(dataOb.get("counts"));
                if (!"".equals(info) && !"".equals(counts)) {
                    List<String> rowdata = new ArrayList<>();
                    String[] str = info.split(",");
                    for (String data : str) {
                        if (!"2".equals(data)) {
                            rowdata.add(data);
                        } else {
                            rowdata.add("");
                        }
                    }
                    rowdata.add(counts);
                    excelList.add(rowdata);
                }
            }

            //将数据列表转化为Excel文件并输出
            ExcelUtil.listToExcel(response, excelList, "data");

        } catch (Exception e) {
            logger.error("统计数据导出异常：" + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 获取数据查询条件
     *
     * @param userProfile
     * @param startTime
     * @param endTime
     * @return
     */
    private Map<String, Object> getparamMap(UserProfile userProfile, String startTime, String endTime) {
        //获取公司id
        int companyid = ConvUtil.convToInt(userProfile.getCurrentUserCompany());

        //获取数据详情
        Map<String, Object> paramMap = new HashMap<>(16);
        if (!userProfile.isTheAdmin()) {
            paramMap.put("b.companyid", companyid);
        }
        paramMap.put("linktable", "fileinfo");
        paramMap.put("start", startTime);
        paramMap.put("end", endTime);

        return paramMap;
    }
}
