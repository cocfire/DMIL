package com.routon.plcloud.device.api.controller.otherdevice;

import com.alibaba.fastjson.JSONObject;
import com.jnrsmcu.sdk.netdevice.*;
import com.routon.plcloud.device.api.config.UserProfile;
import com.routon.plcloud.device.api.controller.commom.CommonForUser;
import com.routon.plcloud.device.core.service.DeviceService;
import com.routon.plcloud.device.core.utils.ConvUtil;
import com.routon.plcloud.device.data.entity.Device;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.awt.*;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.List;

/**
 * @ClassName RsNetController
 * @Description 仁硕电子的设备接入
 * @Author wangzhao
 * @Date 2020/10/27 11:23
 */
@Controller
@RequestMapping(value = "/rsnetdevice")
@Service
public class RsNetController {
    private Logger logger = LoggerFactory.getLogger(RsNetController.class);
    private String termName = "iDR700-RS";

    @Autowired
    private DeviceService deviceService;

    /**
     * 设备监听器
     */
    private static RSServer rsServer = null;

    /**
     * 设备监听端口号
     */
    private static Integer listenProt = 2404;

    /**
     * 扫描设备集合
     */
    private static Map<Integer, Device> deviceMap = new HashMap<>(16);

    /**
     * 设备型号编码，对应轻量级系统
     */
    private Integer termmodel = 26;


    /**
     * 初始化监听器并打开
     *
     * @return
     */
    private boolean initRsServer(Integer port) throws IOException, InterruptedException {
        try {
            //监听端口非默认需要重新初始化
            if (!port.equals(listenProt) || rsServer == null) {
                // 初始化：先设备容器，再设备监听器
                if(deviceMap.size() > 0) {
                    deviceMap = new HashMap<>(16);
                }
                rsServer = RSServer.Initiate(port);
                // 添加监听
                rsServer.addDataListener(new IDataListener() {
                    @Override
                    public void receiveTimmingAck(TimmingAck data) {
                        // 校时指令应答处理
                    }

                    @Override
                    public void receiveTelecontrolAck(TelecontrolAck data) {
                        // 遥控指令应答处理
                    }

                    @Override
                    public void receiveStoreData(StoreData data) {
                        // 已存储数据接收处理
                        // 遍历节点数据。数据包括网络设备的数据以及各个节点数据。温湿度数据存放在节点数据中
                    }

                    @Override
                    public void receiveRealtimeData(RealTimeData data) {
                        // 实时数据接收处理
                        // 遍历节点数据。数据包括网络设备的数据以及各个节点数据。温湿度数据存放在节点数据中
                        //logger.info("仁硕测温设备实时数据->设备地址编号:" + data.getDeviceId())
                        getDeviceInfo(data);
                    }

                    @Override
                    public void receiveLoginData(LoginData data) {
                        // 登录数据接收处理
                        logger.info("仁硕测温设备登录->设备地址编号:" + data.getDeviceId());
                        deviceLogin(data.getDeviceId());
                    }

                    @Override
                    public void receiveParamIds(ParamIdsData data) {
                        // 遍历设备中参数id编号
                    }

                    @Override
                    public void receiveParam(ParamData data) {
                        String str = "设备参数->设备编号：" + data.getDeviceId() + "\r\n";
                        logger.info(str);

                    }

                    @Override
                    public void receiveWriteParamAck(WriteParamAck data) {
                        String str = "下载设备参数->设备编号：" + data.getDeviceId() + "\t参数数量："
                                + data.getCount() + "\t"
                                + (data.isSuccess() ? "下载成功" : "下载失败");
                        logger.info(str);

                    }

                    @Override
                    public void receiveTransDataAck(TransDataAck data) {
                        String str = "数据透传->设备编号：" + data.getDeviceId() + "\t响应结果："
                                + data.getData() + "\r\n字节数：" + data.getTransDataLen();
                        logger.info(str);

                    }
                });
            }

            //启动设备监听器
            rsServer.start();

        } catch (Exception e) {
            logger.error(e.getMessage());
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * 设备登录处理
     *
     * @param devNum
     */
    private void deviceLogin(Integer devNum) {
        try {
            //判断该设备是否有已登录信息，不存在表示为新设备
            Device device = deviceMap.get(devNum);
            if (device == null) {
                device = new Device();
                device.setTermsn(termName + "-" + devNum);
                device.setDeviceid(ConvUtil.convToStr(devNum));
                device.setTermmodel(ConvUtil.convToStr(termmodel));
                device.setTerminfo(termName);
                device.setStatus(1);
                device.setModitytime(new Date());
                device.setRemark("环境监测");
                deviceMap.put(devNum, device);
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 设备实时数据处理
     *
     * @param data
     */
    private void getDeviceInfo(RealTimeData data) {
        try {
            //判断该设备是否有已登录信息，没有的话先登录设备，再处理数据
            Integer devNum = data.getDeviceId();
            deviceLogin(devNum);
            Device device = deviceMap.get(devNum);
            if (device != null) {
                //循环写入每个节点的信息
                String info = "";
                for (NodeData nd : data.getNodeList()) {
                    String seedinfo = "节点:" + nd.getNodeId() + ",温度:" + nd.getTem()
                            + ",湿度:" + nd.getHum() + ",经度:" + data.getLng()
                            + ",纬度:" + data.getLat() + ",坐标类型:"
                            + data.getCoordinateType() + ",继电器状态:"
                            + data.getRelayStatus() + ";\n";

                    //写入内容不能超过1024字节
                    int strlen = (info + seedinfo).getBytes("utf-8").length;
                    if (strlen > 1024) {
                        break;
                    } else {
                        info += seedinfo;
                    }
                }
                device.setLink(info);
                device.setModitytime(new Date());
                deviceMap.put(devNum, device);

                //已入网的设备更新数据，分线程
                Device theDevice = deviceService.getDeviceByDeviceId(ConvUtil.convToStr(devNum));
                if (theDevice != null) {
                    theDevice.setLink(device.getLink());
                    theDevice.setModitytime(device.getModitytime());
                    deviceService.update(theDevice);
                }
            }

        } catch (Exception e) {
            logger.error(e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 发现设备：返回已登录设备信息
     *
     * @param port
     * @return
     */
    @RequestMapping(value = "/discoverDevice")
    @ResponseBody
    public JSONObject discoverDevice(Integer port) {
        JSONObject jsonMsg = new JSONObject();
        String msg = "success";
        List<Device> deviceList = new ArrayList<>();
        try {
            //初始化监听器成功后开始扫描
            if (initRsServer(port)) {
                //先移除已掉线的设备
                Date nowTime = new Date();
                for (Device device : deviceMap.values()) {

                    //获取最后刷新时间
                    Date lastTime = device.getModitytime();

                    //60s没有刷新信息认为掉线，那么删除设备信息
                    if (nowTime.getTime() - lastTime.getTime() > 60000) {
                        deviceMap.remove(ConvUtil.convToInt(device.getDeviceid()));
                    } else {
                        deviceList.add(device);
                    }
                }
            }
            jsonMsg.put("deviceList", deviceList);
        } catch (Exception e) {
            logger.error(e.getMessage());
            e.printStackTrace();
            msg = "系统错误！";
        }
        jsonMsg.put("obj1", msg);
        return jsonMsg;
    }

    /**
     * 关闭设备发现监听器
     *
     * @param port
     * @return
     */
    @RequestMapping(value = "/closeDiscover")
    @ResponseBody
    public JSONObject closeDiscover(Integer port) {
        JSONObject jsonMsg = new JSONObject();
        String msg = "success";
        try {
            rsServer.stop();
            if (!port.equals(listenProt)) {
                rsServer = null;
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
            e.printStackTrace();
            //msg = "系统错误！";
        }
        jsonMsg.put("obj1", msg);
        return jsonMsg;
    }

    /**
     * 添加扫描设备到本地
     *
     * @param deviceids
     * @return
     */
    @RequestMapping(value = "/addDevice")
    @ResponseBody
    public JSONObject addDevice(HttpServletRequest request, String deviceids) {
        JSONObject jsonMsg = new JSONObject();
        String msg = "success";
        try {
            String[] ids = deviceids.split(",");
            for (String deviceid : ids) {
                Device oldDevice = deviceService.getDeviceByDeviceId(deviceid);
                Device newDevice = deviceMap.get(ConvUtil.convToInt(deviceid));
                if (oldDevice == null && newDevice != null) {
                    //添加当前用户公司信息
                    UserProfile userProfile = CommonForUser.getUserProfile(request);
                    newDevice.setCompanyid(ConvUtil.convToInt(userProfile.getCurrentUserCompany()));
                    newDevice.setCreatetime(new Date());
                    deviceService.insertDevice(newDevice);
                } else {
                    if (newDevice == null) {
                        msg = "设备已掉线，请重新扫描！";
                    }
                }
            }

        } catch (Exception e) {
            logger.error(e.getMessage());
            e.printStackTrace();
            //msg = "系统错误！";
        }
        jsonMsg.put("obj1", msg);
        return jsonMsg;
    }

    /**
     * 刷新设备实时数据
     *
     * @param deviceid
     * @return
     */
    @RequestMapping(value = "/dataUpdate")
    @ResponseBody
    public JSONObject dataUpdate(Integer deviceid) {
        JSONObject jsonMsg = new JSONObject();
        String msg = "success";
        try {
            Device device = deviceMap.get(deviceid);
            if(device != null) {
                jsonMsg.put("link", device.getLink());
            } else {
                msg = "没有新的数据！";
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
            e.printStackTrace();
            msg = "系统错误！";
        }
        jsonMsg.put("obj1", msg);
        return jsonMsg;
    }


    public static void main(String[] args) throws AWTException, UnsupportedEncodingException {
        //离线时差测试
        Date date1 = new Date();

        //延时5s
        Robot r = new Robot();
        r.delay(5000);

        Date date2 = new Date();
        System.out.println(date1 + "\n" + date2 + "\n" + (date2.getTime() - date1.getTime()));


        //测试字节长度
        String a = "123abc";
        System.out.println(a.length());
        a = "中文";
        System.out.println(a.length());

        String b = "123abc";
        int num = b.getBytes("utf-8").length;
        System.out.println(num);
        b = "中文";
        num = b.getBytes("utf-8").length;
        System.out.println(num);
    }

}

























