package com.routon.plcloud.device.api.controller.otherdevice;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.routon.plcloud.device.api.utils.HexStrUtil;
import com.routon.plcloud.device.core.service.DeviceService;
import com.routon.plcloud.device.core.utils.ConvUtil;
import com.routon.plcloud.device.data.entity.Device;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author FireWang
 * @date 2020/5/7 15:45
 * 该Controller用于接入第三方设备：巨龙公司体温检测设备
 */
@Controller
@RequestMapping(value = "/dragons")
public class DragonsController {
    private Logger logger = LoggerFactory.getLogger(DragonsController.class);
    private String termName = "iDR720-HJFR";

    @Autowired
    private DeviceService deviceService;

    /**
     * 设备型号编码，对应轻量级系统
     */
    private Integer termmodel = 24;

    /**
     * 设备注册+注册转发
     *
     * @param registerInfo
     * @return
     * @throws IOException
     */
    @RequestMapping(value = "/deviceregister")
    public @ResponseBody
    JSONObject deviceregister(@RequestBody JSONObject registerInfo) throws IOException {
        logger.info("收到巨龙设备注册请求：" + registerInfo);
        //创建转发对象
        JSONObject requsetlist = new JSONObject();
        //创建应答对象
        JSONObject responselist = new JSONObject();
        try {
            JSONObject deviceInfo = registerInfo.getJSONObject("Data").getJSONObject("DeviceInfo");
            Device device = deviceService.searchDeviceBySn(termName + ConvUtil.convToStr(deviceInfo.get("DeviceId")));

            //判断是否已经注册过：已经注册跳过转发和新增
            if ("".equals(ConvUtil.convToStr(device.getId()))) {
                //组织转发报文
                requsetlist.put("face_library_name", CommonForFace.getFaceLibName());
                requsetlist.put("term_name", termName + "-" + ConvUtil.convToStr(deviceInfo.get("DeviceId")));
                requsetlist.put("term_sn", ConvUtil.convToStr(deviceInfo.get("DeviceUUID")));
                requsetlist.put("term_business", 0);
                requsetlist.put("soft_ver", ConvUtil.convToStr(deviceInfo.get("CoreVersion")));
                requsetlist.put("term_ip", ConvUtil.convToStr(deviceInfo.get("DeviceIP")));
                requsetlist.put("term_model", termmodel);
                JSONObject resultMap = CommonForFace.termInfoRegister(requsetlist);
                if (ConvUtil.convToStr(resultMap.get(CommonForFace.result)).equals(CommonForFace.success)) {
                    Map<String, Object> paramMap = new HashMap<>(16);
                    paramMap.put("termsn", termName + ConvUtil.convToStr(deviceInfo.get("DeviceId")));
                    paramMap.put("deviceid", ConvUtil.convToStr(deviceInfo.get("DeviceId")));
                    paramMap.put("uuid", ConvUtil.convToStr(deviceInfo.get("DeviceUUID")));
                    paramMap.put("lanip", ConvUtil.convToStr(deviceInfo.get("DeviceIP")));
                    paramMap.put("termmodel", ConvUtil.convToStr(termmodel));
                    paramMap.put("terminfo", termName);
                    paramMap.put("termid", ConvUtil.convToStr(resultMap.get("term_id")));
                    paramMap.put("facelibrid", ConvUtil.convToStr(resultMap.get("face_library_id")));
                    paramMap.put("status", 1);
                    //注册成功，新建设备数据
                    device = deviceService.addDevice(paramMap);
                    if (!"".equals(ConvUtil.convToStr(device.getId()))) {
                        responselist.put("Code", 1);
                        responselist.put("Message", "注册成功");
                        responselist.put("TimeStamp", device.getModitytime().getTime());
                    } else {
                        responselist.put("Code", 1001);
                        responselist.put("Message", "注册失败：设备中间层异常");
                        responselist.put("TimeStamp", System.currentTimeMillis());
                    }
                } else {
                    responselist.put("Code", 0);
                    responselist.put("Message", "注册失败");
                    responselist.put("TimeStamp", System.currentTimeMillis());
                }
            } else {
                responselist.put("Code", 1);
                responselist.put("Message", "注册成功");
                responselist.put("TimeStamp", device.getModitytime().getTime());
            }

            //组织应答报文
            JSONObject dataMap = new JSONObject();
            responselist.put("Name", "registerResponse");
            dataMap.put("Session", ConvUtil.convToStr(deviceInfo.get("DeviceUUID")) + "_" + responselist.get("TimeStamp"));
            dataMap.put("ServerVersion", "V2.0.1");
            responselist.put("Data", dataMap);
        } catch (Exception e) {
            logger.info(e.getMessage());
            e.printStackTrace();
        }
        logger.info("巨龙设备注册请求响应：" + responselist);
        return responselist;
    }


    /**
     * 设备心跳
     *
     * @param heartbeatinfo
     * @return
     * @throws IOException
     */
    @RequestMapping(value = "/heartbeat")
    public @ResponseBody
    JSONObject heartbeat(@RequestBody JSONObject heartbeatinfo) throws IOException {
        logger.info("收到巨龙设备心跳信息：" + heartbeatinfo);
        //创建转发对象
        JSONObject requsetlist = new JSONObject();
        //创建应答对象
        JSONObject responselist = new JSONObject();
        //组织应答报文
        JSONObject dataMap;
        responselist.put("Name", "heartbeatResponse");
        responselist.put("Code", 1);
        responselist.put("Message", "SUCCESS");
        int eventCount = 0;
        try {
            JSONObject deviceInfo = heartbeatinfo.getJSONObject("Data").getJSONObject("DeviceInfo");
            Device device = deviceService.searchDeviceBySn(termName + ConvUtil.convToStr(deviceInfo.get("DeviceId")));

            //判断是否已经注册过：已经注册就去获取任务，没注册就直接返回
            String freeflag = "1";
            if (!"".equals(ConvUtil.convToStr(device.getId())) && ConvUtil.convToStr(device.getStatus()).equals(freeflag)) {
                responselist.put("Session", device.getUuid() + "_" + device.getModitytime().getTime());
                //组织转发报文
                requsetlist.put("face_library_id", device.getFacelibrid());
                requsetlist.put("term_id", device.getTermid());
                //查询数据状态
                JSONObject resultMap = CommonForFace.dataStatusQuery(requsetlist);
                if (ConvUtil.convToStr(resultMap.get(CommonForFace.result)).equals(CommonForFace.success)) {
                    JSONArray revnos = resultMap.getJSONArray("face_versions");
                    JSONObject revno;
                    for (int i = 0; i < revnos.size(); i++) {
                        revno = (JSONObject) revnos.get(i);
                        String termFaceVersion = "term_face_version";
                        if (ConvUtil.convToStr(revno.get("version")).equals(termFaceVersion)) {
                            if (!device.getLink().equals(ConvUtil.convToStr(revno.get("revno")))) {
                                eventCount = 1;

                                //设备处理任务中暂不接受新任务
                                device.setStatus(0);
                                deviceService.update(device);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.info(e.getMessage());
            e.printStackTrace();
        }
        responselist.put("TimeStamp", System.currentTimeMillis());
        responselist.put("EventCount", eventCount);
        logger.info("巨龙设备心跳响应：" + responselist);
        return responselist;
    }


    /**
     * 主动获取任务
     *
     * @param eventinfo
     * @return
     * @throws IOException
     */
    @RequestMapping(value = "/eventrequest")
    public @ResponseBody
    JSONObject eventrequest(@RequestBody JSONObject eventinfo) throws IOException {
        logger.info("收到巨龙设备获取任务请求：" + eventinfo);
        //创建转发对象
        JSONObject requsetlist = new JSONObject();
        //创建应答对象
        JSONObject responselist = new JSONObject();
        //组织应答报文：第一步
        responselist.put("Session", ConvUtil.convToStr(eventinfo.get("Session")));
        JSONObject dataMap = new JSONObject();
        JSONArray eventList = new JSONArray();
        try {
            Device device = deviceService.searchDeviceByUuid(ConvUtil.convToStr(eventinfo.get("UUID")));

            //判断是否已经注册过：已经注册就去获取终端权限，没注册就直接返回
            if (!"".equals(ConvUtil.convToStr(device.getId()))) {
                String revno = device.getLink();
                //组织获取终端权限报文
                requsetlist.put("face_library_id", device.getFacelibrid());
                requsetlist.put("term_id", device.getTermid());
                requsetlist.put("mode", 1);
                requsetlist.put("revno", revno);
                requsetlist.put("start", 0);
                requsetlist.put("rec_num", -1);
                //1、获取终端权限
                JSONObject resultMap = CommonForFace.termFaceQuery(requsetlist);
                if (ConvUtil.convToStr(resultMap.get(CommonForFace.result)).equals(CommonForFace.success)) {
                    revno = ConvUtil.convToStr(resultMap.get("revno"));
                    JSONArray facelist = new JSONArray();
                    String faceNum = "face_num";
                    if (ConvUtil.convToInt(resultMap.get(faceNum)) > 0) {
                        facelist = resultMap.getJSONArray("face_list");
                    }
                    JSONObject faceinfo;

                    //返给巨龙设备的人员信息
                    String personId = "", personName = "";
                    for (int i = 0; i < facelist.size(); i++) {
                        faceinfo = (JSONObject) facelist.get(i);

                        //组织获取人员信息接口参数
                        JSONObject faceInforq = new JSONObject();
                        faceInforq.put("face_library_id", device.getFacelibrid());
                        faceInforq.put("face_id", faceinfo.get("face_id"));
                        faceInforq.put("attribute", faceinfo.get("attribute"));
                        personId = ConvUtil.convToStr(faceinfo.get("face_id")) + faceinfo.get("attribute");

                        //2、获取人员信息
                        JSONObject faceinfoMap = CommonForFace.faceInfoQuery(faceInforq);
                        if (ConvUtil.convToStr(faceinfoMap.get(CommonForFace.result)).equals(CommonForFace.success)) {
                            JSONArray faces = faceinfoMap.getJSONArray("face_list");
                            if (faces.size() > 0 && faceinfo.get("status").equals(0)) {
                                //若人员有效，则添加人脸
                                JSONObject faceInfo = (JSONObject) faces.get(0);
                                personName = ConvUtil.convToStr(faceInfo.get("name"));

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

                                //3、获取人脸照片
                                JSONObject faceimgs = CommonForFace.facePhotoQuery(faceimgrq);
                                if (ConvUtil.convToStr(faceimgs.get(CommonForFace.result)).equals(CommonForFace.success) && ConvUtil.convToInt(faceimgs.get("face_num")) > 0) {
                                    JSONArray imagelist = ((JSONObject) faceimgs.getJSONArray("face_list").get(0)).getJSONArray("image_list");
                                    String imageBase = ConvUtil.convToString(((JSONObject) imagelist.get(0)).get("image_data"));

                                    //组织添加人脸任务
                                    JSONObject event = new JSONObject();
                                    event.put("Action", "addPerson");
                                    event.put("SN", eventList.size() + 1);
                                    event.put("PersonType", 2);
                                    event.put("PersonId", personId);
                                    event.put("PersonName", personName);
                                    event.put("PhotoType", 1);
                                    event.put("PersonPhoto", imageBase);
                                    eventList.add(event);
                                }
                            } else {//若人员无效，则删除人脸

                                //组织删除人脸任务
                                JSONObject event = new JSONObject();
                                event.put("Action", "deletePerson");
                                event.put("SN", eventList.size() + 1);
                                event.put("PersonType", 2);
                                event.put("PersonId", personId);
                                eventList.add(event);
                            }
                        }
                    }
                    //更新修订号
                    device.setLink(revno);
                    device.setTerminfo(termName);
                    deviceService.update(device);
                }
            }
            //组织应答报文：第二步
            responselist.put("Name", "eventResponse");
            responselist.put("Code", 1);
            responselist.put("Message", "SUCCESS");
            dataMap.put("NextEvent", 0);
            dataMap.put("ListCount", eventList.size());
            dataMap.put("List", eventList);
            responselist.put("Data", dataMap);
        } catch (Exception e) {
            logger.info(e.getMessage());
            e.printStackTrace();
        }
        responselist.put("TimeStamp", System.currentTimeMillis());
        logger.info("巨龙设备获取任务请求响应：" + responselist);
        return responselist;
    }


    /**
     * 任务结果上报
     *
     * @param resulinfo
     * @return
     * @throws IOException
     */
    @RequestMapping(value = "/resultrequest")
    public @ResponseBody
    JSONObject resultrequest(@RequestBody JSONObject resulinfo) throws IOException {
        logger.info("收到巨龙设备任务结果上报请求：" + resulinfo);
        //创建应答对象
        JSONObject responselist = new JSONObject();
        try {
            Device device = deviceService.searchDeviceByUuid(ConvUtil.convToStr(resulinfo.get("UUID")));
            //判断是否已经注册过：已经注册则说明上一批任务处理完成，那么设置设备状态
            if (!"".equals(ConvUtil.convToStr(device.getId()))) {
                device.setStatus(1);
                deviceService.update(device);
            }
        } catch (Exception e) {
            logger.info(e.getMessage());
            e.printStackTrace();
        }
        //组织应答报文
        responselist.put("Session", ConvUtil.convToStr(resulinfo.get("Session")));
        responselist.put("Name", "resultResponse");
        responselist.put("Code", 1);
        responselist.put("Message", "SUCCESS");
        responselist.put("TimeStamp", System.currentTimeMillis());
        logger.info("巨龙设备任务结果上报响应：" + responselist);
        return responselist;
    }


    /**
     * 抓拍信息解析转发
     *
     * @param captureInfo
     * @return
     * @throws IOException
     */
    @RequestMapping(value = "/captureinforequest")
    public @ResponseBody
    JSONObject postcaptureInfo(@RequestBody JSONObject captureInfo) throws IOException {
        logger.info("收到巨龙设备抓拍信息：" + captureInfo);
        //创建发送对象
        JSONObject requsetlist = new JSONObject();
        //创建应答对象
        JSONObject responselist = new JSONObject();
        //组织设备应答报文
        responselist.put("Name", "captureInfoResponse");
        responselist.put("Session", ConvUtil.convToStr(captureInfo.get("Session")));
        try {
            JSONObject deviceInfo = CommonForFace.getParamByName(captureInfo.getJSONObject("Data"),"DeviceInfo");
            JSONObject compareInfo = CommonForFace.getParamByName(captureInfo.getJSONObject("Data"),"CompareInfo");
            JSONObject attribute = CommonForFace.getParamByName(compareInfo, "Attribute");
            JSONObject personInfo = CommonForFace.getParamByName(compareInfo,"PersonInfo");
            JSONObject idCardInfo = CommonForFace.getParamByName(compareInfo,"IDCardInfo");
            JSONObject temperaInfo = CommonForFace.getParamByName(captureInfo.getJSONObject("Data"),"TemperaInfo");
            JSONObject captureinfo = CommonForFace.getParamByName(captureInfo.getJSONObject("Data"),"CaptureInfo");
            Device device = deviceService.searchDeviceByUuid(ConvUtil.convToStr(deviceInfo.get("DeviceUUID")));
            String personId = ConvUtil.convToStr(personInfo.get("PersonId"));

            //判断是否已经注册
            if (!"".equals(ConvUtil.convToStr(device.getId()))) {
                //组织转发报文
                requsetlist.put("face_library_id", device.getFacelibrid());
                if (ConvUtil.convToInt(personId) == 0) {
                    requsetlist.put("attribute", 1);
                    requsetlist.put("face_id", 0);
                } else {
                    requsetlist.put("attribute", personId.substring(personId.length() - 1));
                    requsetlist.put("face_id", personId.substring(0, personId.length() - 1));
                }
                String cardNo = ConvUtil.convToStr(idCardInfo.get("CardNo"));
                if (!"".equals(cardNo)) {
                    requsetlist.put("idcard", HexStrUtil.id2HexStr(cardNo));
                    requsetlist.put("name", ConvUtil.convToStr(idCardInfo.get("Name")));
                    String mal = "男";
                    String gender = ConvUtil.convToStr(idCardInfo.get("Gender"));
                    if (mal.equals(gender)) {
                        requsetlist.put("gender", 1);
                    } else {
                        requsetlist.put("gender", 2);
                    }
                    requsetlist.put("idcard_img", ConvUtil.convToStr(idCardInfo.get("CardPhoto")).replaceAll("[\\s*\t\n\r]", ""));
                    requsetlist.put("nation", ConvUtil.convToStr(idCardInfo.get("Nation")));
                    requsetlist.put("addr", ConvUtil.convToStr(idCardInfo.get("Address")));
                    requsetlist.put("birth", ConvUtil.convToStr(idCardInfo.get("Birthday")));
                    requsetlist.put("agency", ConvUtil.convToStr(idCardInfo.get("Issue")));
                    requsetlist.put("valid_date", idCardInfo.get("StartDate") + "-" + idCardInfo.get("EndDate"));
                    requsetlist.put("compare_score", ConvUtil.convToDouble(idCardInfo.get("Similarity")));
                } else {
                    requsetlist.put("name", ConvUtil.convToStr(personInfo.get("PersonName")));
                    requsetlist.put("compare_score", ConvUtil.convToDouble(compareInfo.get("Similarity")));
                }
                requsetlist.put("term_id", device.getTermid());
                requsetlist.put("compare_result", 0);
                requsetlist.put("temperature", ConvUtil.convToString(temperaInfo.get("Temperature")));
                requsetlist.put("mask", ConvUtil.convToInt(attribute.get("Mask")));
                requsetlist.put("image", ConvUtil.convToStr(captureinfo.get("FacePicture")).replaceAll("[\\s*\t\n\r]", ""));
                requsetlist.put("create_time", captureinfo.get("CaptureTime"));
                requsetlist.put("business", 0);
                requsetlist.put("rec_id", 0);
                requsetlist.put("rec_type", 0);

                //转发到轻量级
                JSONObject resultMap = CommonForFace.faceRecordUpload(requsetlist);
                if (ConvUtil.convToStr(resultMap.get(CommonForFace.result)).equals(CommonForFace.success)) {
                    responselist.put("Code", 1);
                    responselist.put("Message", "SUCCESS");
                }
            } else {//解析错误直接应答
                responselist.put("Code", 0);
                responselist.put("Message", "上传失败：设备未注册！");
            }
        } catch (Exception e) {
            logger.info(e.getMessage());
            e.printStackTrace();
            responselist.put("Code", 0);
            responselist.put("Message", "上传失败！");
        }
        responselist.put("TimeStamp", System.currentTimeMillis());
        logger.info("巨龙设备抓拍信息响应：" + responselist);
        return responselist;
    }
}
