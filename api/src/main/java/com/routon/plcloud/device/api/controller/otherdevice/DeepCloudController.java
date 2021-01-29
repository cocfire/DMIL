package com.routon.plcloud.device.api.controller.otherdevice;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.routon.plcloud.device.api.utils.DateUtils;
import com.routon.plcloud.device.api.utils.FileUtil;
import com.routon.plcloud.device.api.utils.HexStrUtil;
import com.routon.plcloud.device.api.utils.TimeUtil;
import com.routon.plcloud.device.api.config.ServerConfig;
import com.routon.plcloud.device.core.utils.ConvUtil;
import com.routon.plcloud.device.core.utils.PropertiesUtil;
import com.routon.plcloud.device.core.service.DeviceService;
import com.routon.plcloud.device.data.entity.Device;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static com.routon.plcloud.device.api.utils.Base64ToFileUtil.base64ToMultipart;

/**
 * @ClassName DeepCloudController
 * @Description 深云智慧的设备接入
 * @Author shiquan
 * @Date 2020/5/8 9:23
 */
@Controller
@RequestMapping(value = "/deepcloud")
public class DeepCloudController {
    private Logger logger = LoggerFactory.getLogger(DeepCloudController.class);
    private static String termName = "iDR700-HSF";

    @Autowired
    private ServerConfig serverConfig;

    @Autowired
    private DeviceService deviceService;

    /**
     * 设备型号编码，对应轻量级系统
     */
    private Integer termmodel = 25;

    /**
     * 设备注册
     *
     * @param device
     * @param termsn
     * @param deviceid
     * @return
     * @throws Exception
     */
    private boolean deviceRegister(Device device, String termsn, Integer deviceid) throws Exception {
        boolean flag = false;
        //创建转发对象
        JSONObject requsetlist = new JSONObject();
        //组织转发报文
        requsetlist.put("face_library_name", CommonForFace.getFaceLibName());
        requsetlist.put("term_name", termName + "-" + deviceid);
        requsetlist.put("term_sn", termsn);
        requsetlist.put("term_business", 0);
        requsetlist.put("term_model", termmodel);
        JSONObject resultMap = CommonForFace.termInfoRegister(requsetlist);
        if (ConvUtil.convToStr(resultMap.get(CommonForFace.result)).equals(CommonForFace.success)) {
            Map<String, Object> paramMap = new HashMap<>(16);
            paramMap.put("termsn", termsn);
            paramMap.put("deviceid", deviceid);
            paramMap.put("termmodel", ConvUtil.convToStr(termmodel));
            paramMap.put("termid", ConvUtil.convToStr(resultMap.get("term_id")));
            paramMap.put("terminfo", termName);
            paramMap.put("facelibrid", ConvUtil.convToStr(resultMap.get("face_library_id")));
            paramMap.put("status", 1);
            //注册成功，新建设备数据
            device = deviceService.addDevice(paramMap);
            if (!"".equals(ConvUtil.convToStr(device.getId()))) {
                logger.info("深云设备{" + deviceid + "}注册成功");
                flag = true;
            } else {
                logger.info("深云设备{" + deviceid + "}注册失败");
            }
        } else {
            logger.info("深云设备{" + deviceid + "}注册失败");
        }
        return flag;
    }


    /**
     * 获取人脸库更新 + 终端注册接口
     *
     * @param deviceid
     * @param lastQueryTime
     * @return
     */
    @RequestMapping(value = "/syrecord/{deviceid}", method = RequestMethod.GET)
    public @ResponseBody
    JSONObject syrecord(
            @PathVariable(value = "deviceid") Integer deviceid,
            @RequestParam(name = "lastQueryTime", required = true) long lastQueryTime) {
        logger.info("收到深云设备获取人脸库更新请求：deviceid{" + deviceid + "}");
        //创建转发对象
        JSONObject requsetlist = new JSONObject();
        //创建应答对象
        JSONObject responselist = new JSONObject();
        JSONObject serviceResponse = new JSONObject();
        JSONArray operationlist = new JSONArray();
        try {
            String termsn = termName + deviceid;
            Device device = deviceService.searchDeviceBySn(termsn);

            //判断是否已经注册过：已经注册跳过转发和新增
            if ("".equals(ConvUtil.convToStr(device.getId()))) {
                deviceRegister(device, termsn, deviceid);
            } else {
                if (ConvUtil.convToStr(device.getStatus()).equals(CommonForFace.fail)) {
                    requsetlist.put("face_library_id", device.getFacelibrid());
                    requsetlist.put("term_id", device.getTermid());
                    //1、查询数据状态
                    JSONObject resultMap = CommonForFace.dataStatusQuery(requsetlist);
                    if (ConvUtil.convToStr(resultMap.get(CommonForFace.result)).equals(CommonForFace.success)) {
                        JSONArray revnos = resultMap.getJSONArray("face_versions");
                        JSONObject revno;
                        for (int i = 0; i < revnos.size(); i++) {
                            revno = (JSONObject) revnos.get(i);
                            if ("term_face_version".equals(ConvUtil.convToStr(revno.get("version")))) {
                                if (!device.getLink().equals(ConvUtil.convToStr(revno.get("revno")))) {
                                    //设备处理任务中暂不接受新任务
                                    device.setStatus(0);
                                    deviceService.update(device);
                                } else {
                                    responselist.put("message", "Nothing to update!");
                                }
                            }
                        }
                    } else {
                        responselist.put("message", "连接人脸服务失败！");
                    }
                    //若设备状态为0，说明有信息变化
                    if (ConvUtil.convToStr(device.getStatus()).equals(CommonForFace.success)) {
                        String revno = device.getLink();
                        //组织获取终端权限报文
                        requsetlist.put("face_library_id", device.getFacelibrid());
                        requsetlist.put("term_id", device.getTermid());
                        requsetlist.put("mode", 1);
                        requsetlist.put("revno", revno);
                        requsetlist.put("start", 0);
                        requsetlist.put("rec_num", -1);
                        //2、获取终端权限
                        JSONObject resultMap2 = CommonForFace.termFaceQuery(requsetlist);
                        if (ConvUtil.convToStr(resultMap2.get(CommonForFace.result)).equals(CommonForFace.success)) {
                            revno = ConvUtil.convToStr(resultMap2.get("revno"));
                            JSONArray facelist = new JSONArray();
                            String faceNum = "face_num";
                            if (ConvUtil.convToInt(resultMap2.get(faceNum)) > 0) {
                                facelist = resultMap2.getJSONArray("face_list");
                            }
                            JSONObject faceinfo;

                            //返给深云设备的人员信息
                            String userId = "", userName = "";
                            for (int i = 0; i < facelist.size(); i++) {
                                faceinfo = (JSONObject) facelist.get(i);

                                //组织获取人员信息接口参数
                                JSONObject faceInforq = new JSONObject();
                                faceInforq.put("face_library_id", device.getFacelibrid());
                                faceInforq.put("face_id", faceinfo.get("face_id"));
                                faceInforq.put("attribute", faceinfo.get("attribute"));
                                userId = ConvUtil.convToStr(faceinfo.get("face_id")) + faceinfo.get("attribute");

                                //3、获取人员信息
                                JSONObject faceinfoMap = CommonForFace.faceInfoQuery(faceInforq);
                                if (ConvUtil.convToStr(faceinfoMap.get(CommonForFace.result)).equals(CommonForFace.success)) {
                                    JSONArray faces = faceinfoMap.getJSONArray("face_list");
                                    if (faces.size() > 0 && faceinfo.get("status").equals(0)) {
                                        //若人员有效，则添加人脸
                                        JSONObject faceInfo = (JSONObject) faces.get(0);
                                        userName = ConvUtil.convToStr(faceInfo.get("name"));

                                        //组织获取人脸照片接口参数
                                        JSONObject faceimgrq = new JSONObject();
                                        faceimgrq.put("face_library_id", device.getFacelibrid());
                                        faceimgrq.put("attribute", faceinfo.get("attribute"));
                                        faceimgrq.put("image_num", 0);
                                        faceimgrq.put("image_type", 0);
                                        faceimgrq.put("face_num", 1);
                                        JSONArray getfacelist = new JSONArray();
                                        JSONObject setface = new JSONObject();
                                        setface.put("face_id", faceinfo.get("face_id"));
                                        getfacelist.add(setface);
                                        faceimgrq.put("face_list", getfacelist);

                                        //4、获取人脸照片
                                        JSONObject faceimgs = CommonForFace.facePhotoQuery(faceimgrq);
                                        if (ConvUtil.convToStr(faceimgs.get(CommonForFace.result)).equals(CommonForFace.success) && ConvUtil.convToInt(faceimgs.get("face_num")) > 0) {
                                            JSONArray imagelist = ((JSONObject) faceimgs.getJSONArray("face_list").get(0)).getJSONArray("image_list");
                                            String photourl = "";
                                            if (imagelist != null && imagelist.size() > 0) {
                                                //将base64字符转换为照片存在服务器并生成url
                                                String imageBase = ConvUtil.convToString(((JSONObject) imagelist.get(0)).get("image_data"));
                                                photourl = getImageUrlByBase64Image(imageBase, userId);
                                            }

                                            //组织添加人脸操作任务
                                            JSONObject operater = new JSONObject();
                                            operater.put("operation", "save");
                                            operater.put("userId", userId);
                                            operater.put("userName", userName);
                                            operater.put("userRemark", "添加人脸");
                                            operater.put("sex", faceInfo.get("gender"));
                                            operater.put("userImageUrl", photourl);
                                            operater.put("operateTime", ConvUtil.convToLong(DateUtils.getTime()));
                                            operationlist.add(operater);
                                        }
                                    } else {//若人员无效，则删除人脸

                                        //组织删除人脸任务
                                        JSONObject operater = new JSONObject();
                                        operater.put("operation", "delete");
                                        operater.put("userId", userId);
                                        operater.put("userName", userName);
                                        operater.put("sex", 1);
                                        operater.put("userRemark", "删除人脸");
                                        operater.put("userImageUrl", "");
                                        operater.put("operateTime", ConvUtil.convToLong(DateUtils.getTime()));
                                        operationlist.add(operater);
                                    }
                                }
                            }
                            //更新修订号和状态
                            device.setLink(revno);
                            device.setStatus(1);
                            device.setTerminfo(termName);
                            deviceService.update(device);
                        } else {

                        }
                    }
                } else {
                    //设备正忙
                    responselist.put("message", "设备正忙，请稍后重试！");
                }
            }
        } catch (Exception e) {
            responselist.put("message", e.getMessage());
            e.printStackTrace();
            logger.error(e.getMessage());
        }
        //组织返回报文
        serviceResponse.put("list", operationlist);
        serviceResponse.put("lastQueryTime", lastQueryTime);
        responselist.put("serviceResponse", serviceResponse);
        logger.info("深云设备{" + deviceid + "}获取人脸库更新响应：" + responselist);
        return responselist;
    }


    /**
     * 上传反馈记录
     *
     * @param deviceid
     * @return
     */
    @RequestMapping(value = "/syresult/{deviceid}", method = RequestMethod.POST)
    public @ResponseBody
    JSONObject syresult(@PathVariable(value = "deviceid") Integer deviceid,
                        @RequestBody JSONObject resultInfo) {
        logger.info("收到深云设备{" + deviceid + "}上传反馈记录请求：" + resultInfo);
        //创建应答对象
        JSONObject responselist = new JSONObject();
        try {
            String termsn = termName + deviceid;
            Device device = deviceService.searchDeviceBySn(termsn);
            //判断是否已经注册过：已经注册则说明上一批任务处理完成，那么设置设备状态
            if (!"".equals(ConvUtil.convToStr(device.getId()))) {
                device.setStatus(1);
                deviceService.update(device);
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e.getMessage());
        }
        //组织应答报文
        responselist.put("ret", "0");
        responselist.put("desc", "SUCCESS");
        logger.info("深云设备{" + deviceid + "}上传反馈记录响应：" + responselist);
        return responselist;
    }


    /**
     * 上传比对结果
     *
     * @param deviceid
     * @return
     */
    @RequestMapping(value = "/syface/{deviceid}", method = RequestMethod.POST)
    public @ResponseBody
    JSONObject syface(@PathVariable(value = "deviceid") Integer deviceid,
                      @RequestBody JSONObject userInfo) {
        logger.info("收到深云设备{" + deviceid + "}上传比对结果请求：" + userInfo);
        //创建发送对象
        JSONObject requsetlist = new JSONObject();
        //创建应答对象
        JSONObject responselist = new JSONObject();
        String ret = "0";
        String desc = "Success";
        int openDoor = 1;
        try {
            String termsn = termName + deviceid;
            String userId = ConvUtil.convToStr(userInfo.get("userId"));
            JSONObject idCardInfo = CommonForFace.getParamByName(userInfo, "idCardInfo");
            Device device = deviceService.searchDeviceBySn(termsn);

            //判断是否已经注册过：已经注册则继续上传
            if (!"".equals(ConvUtil.convToStr(device.getId()))) {
                //组织转发报文
                requsetlist.put("face_library_id", device.getFacelibrid());
                if (ConvUtil.convToInt(userId) == 0) {
                    requsetlist.put("attribute", 1);
                    requsetlist.put("face_id", 0);
                } else {
                    requsetlist.put("attribute", userId.substring(userId.length() - 1));
                    requsetlist.put("face_id", userId.substring(0, userId.length() - 1));
                }
                String id = ConvUtil.convToStr(idCardInfo.get("id"));
                if (!"".equals(id)) {
                    requsetlist.put("idcard", HexStrUtil.id2HexStr(id));
                    requsetlist.put("name", ConvUtil.convToStr(idCardInfo.get("name")));
                    String mal = "男";
                    String gender = ConvUtil.convToStr(idCardInfo.get("gender"));
                    if (mal.equals(gender)) {
                        requsetlist.put("gender", 1);
                    } else {
                        requsetlist.put("gender", 2);
                    }
                    requsetlist.put("nation", ConvUtil.convToStr(idCardInfo.get("national")));
                    requsetlist.put("addr", ConvUtil.convToStr(idCardInfo.get("address")));
                    requsetlist.put("birth", ConvUtil.convToStr(idCardInfo.get("birthday")));
                    requsetlist.put("agency", ConvUtil.convToStr(idCardInfo.get("maker")));
                    requsetlist.put("valid_date", idCardInfo.get("startDate") + "-" + idCardInfo.get("endDate"));
                } else {
                    requsetlist.put("name", ConvUtil.convToStr(userInfo.get("userName")));
                }
                requsetlist.put("term_id", device.getTermid());
                requsetlist.put("compare_result", 0);
                requsetlist.put("compare_score", userInfo.get("similarity"));
                requsetlist.put("temperature", ConvUtil.convToStr(userInfo.get("temperature")));
                boolean maskflag = ConvUtil.convToBool(userInfo.get("mask"));
                if (maskflag) {
                    requsetlist.put("mask", 1);
                } else {
                    requsetlist.put("mask", 0);
                }
                requsetlist.put("image", ConvUtil.convToStr(userInfo.get("img")).replaceAll("[\\s*\t\n\r]", ""));
                requsetlist.put("create_time", TimeUtil.timeStampToTime(ConvUtil.convToStr(userInfo.get("screenTime"))));
                requsetlist.put("idcard_img", ConvUtil.convToStr(userInfo.get("IDCardImg")).replaceAll("[\\s*\t\n\r]", ""));
                requsetlist.put("business", 0);
                requsetlist.put("rec_id", 0);
                requsetlist.put("rec_type", 0);

                //转发到轻量级
                JSONObject resultMap = CommonForFace.faceRecordUpload(requsetlist);
                if (!ConvUtil.convToStr(resultMap.get(CommonForFace.result)).equals(CommonForFace.success)) {
                    ret = "1";
                    desc = "上传失败";
                    openDoor = 0;
                }
            } else {
                //解析错误直接应答
                ret = "1";
                desc = "设备未注册！";
                openDoor = 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e.getMessage());
            ret = "1";
            desc = "fail";
            responselist.put("openDoor", 0);
        }
        responselist.put("ret", ret);
        responselist.put("desc", desc);
        responselist.put("openDoor", openDoor);
        responselist.put("unlockDelay", 20);
        logger.info("深云设备{" + deviceid + "}上传比对结果响应：" + responselist);
        return responselist;
    }


    /**
     * 上传温度
     *
     * @param deviceid
     * @return
     */
    @RequestMapping(value = "/syattribute/{deviceid}", method = RequestMethod.POST)
    public @ResponseBody
    JSONObject syattribute(@PathVariable(value = "deviceid") Integer deviceid,
                           @RequestBody JSONObject temperatureInfo) {
        logger.info("收到深云设备{" + deviceid + "}上传温度请求：" + temperatureInfo);
        //创建发送对象
        JSONObject requsetlist = new JSONObject();
        //创建应答对象
        JSONObject responselist = new JSONObject();
        try {
            String termsn = termName + deviceid;
            Device device = deviceService.searchDeviceBySn(termsn);

            //判断是否已经注册过：已经注册则继续上传
            if (!"".equals(ConvUtil.convToStr(device.getId()))) {
                responselist.put("ret", "0");
                responselist.put("desc", "Success");
            } else {//解析错误直接应答
                responselist.put("ret", "0");
                responselist.put("desc", "设备未注册！");
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e.getMessage());
            responselist.put("ret", "1");
            responselist.put("desc", "fail");
        }
        logger.info("深云设备{" + deviceid + "}上传温度响应：" + responselist);
        return responselist;
    }


    /**
     * 设置心跳
     *
     * @param deviceid
     * @return
     */
    @RequestMapping(value = "/syheartbeat/{deviceid}", method = RequestMethod.POST)
    public @ResponseBody
    JSONObject syheartbeat(@PathVariable(value = "deviceid") Integer deviceid) {
        logger.info("收到深云设备设置心跳请求：deviceid{" + deviceid + "}");
        //创建发送对象
        JSONObject requsetlist = new JSONObject();
        //创建应答对象
        JSONObject responselist = new JSONObject();
        try {
            String termsn = termName + deviceid;
            Device device = deviceService.searchDeviceBySn(termsn);

            //判断是否已经注册过：已经注册查询数据状态
            if (!"".equals(ConvUtil.convToStr(device.getId()))) {
                requsetlist.put("face_library_id", device.getFacelibrid());
                requsetlist.put("term_id", device.getTermid());
                //查询数据状态
                JSONObject resultMap = CommonForFace.dataStatusQuery(requsetlist);
                if (ConvUtil.convToStr(resultMap.get(CommonForFace.result)).equals(CommonForFace.success)) {
                    responselist.put("ret", "0");
                    responselist.put("desc", "Success");
                } else {
                    responselist.put("ret", "1");
                    responselist.put("desc", "fail");
                }
            } else {//解析错误直接应答
                responselist.put("ret", "0");
                responselist.put("desc", "设备未注册！");
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("心跳异常：" + e.getMessage());
            responselist.put("ret", "1");
            responselist.put("desc", "fail");
        }
        logger.info("深云设备{" + deviceid + "}设置心跳响应：" + responselist);
        return responselist;
    }


    /**
     * 照片下载
     *
     * @param response
     * @param photo
     * @param size
     */
    @RequestMapping(value = "/photodownload")
    public void downSoftware(HttpServletResponse response, String photo, String size) {
        try {
            logger.info("接口下载照片开始...");
            String ctxPath = PropertiesUtil.getDataFromPropertiseFile("deviceCommon", "photo_path");
            FileUtil.downFile(response, ctxPath, photo, ConvUtil.convToInt(size));

            logger.info("下载照片完成！");
        } catch (Exception e) {
            logger.error("下载照片异常:" + e.getMessage());
            e.printStackTrace();
        }
    }


    /**
     * 通过imageBase64存储图片返回下载地址
     *
     * @param imgStr
     * @param name
     * @return 下载地址url
     */
    public String getImageUrlByBase64Image(String imgStr, String name) {
        try {

            MultipartFile multipartFile = base64ToMultipart("data:image/jpg;base64," + imgStr);

            String pictureName = name + ".jpg";
            String ctxPath = PropertiesUtil.getDataFromPropertiseFile("deviceCommon", "photo_path");
            File targetFile = new File(ctxPath);
            if (!targetFile.exists()) {
                targetFile.mkdirs();
            }
            FileUtils.copyInputStreamToFile(multipartFile.getInputStream(), new File(ctxPath + "/" + pictureName));

            //String downurl = PropertiesUtil.getDataFromPropertiseFile("deviceCommon", "hostrul") + "/DMIL/deepcloud/photodownload";

            //内网部署url方法。注：端口约定为8800，打包时需修改配置文件
            String downurl = serverConfig.getHostUrl() + "/DMIL/deepcloud/photodownload";

            return downurl + "?photo=" + pictureName + "&size=" + multipartFile.getSize();

        } catch (IOException e) {
            e.printStackTrace();
            logger.error(e.getMessage());
            return "";
        }
    }
}

























