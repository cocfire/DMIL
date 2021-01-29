package com.routon.plcloud.device.api.config.emq;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.routon.plcloud.device.api.utils.HexStrUtil;
import com.routon.plcloud.device.core.service.*;
import com.routon.plcloud.device.core.utils.ConvUtil;
import com.routon.plcloud.device.data.entity.*;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * @author FireWang
 * @date 2020/5/7 15:45
 * emq客户端回调
 */
@Service
public class EmqCallback implements MqttCallbackExtended {
    private Logger logger = LoggerFactory.getLogger(EmqCallback.class);

    @Autowired
    private EmqClient emqClient;

    @Autowired
    private DeviceService deviceService;

    @Autowired
    private DeviceswService deviceswService;

    @Autowired
    private OfflinemsgService offlinemsgService;

    @Autowired
    private SyslogService syslogService;

    @Autowired
    private SoftwareService softwareService;

    @Autowired
    private UserinfoService userinfoService;

    @Autowired
    private ProgramsService programsService;

    @Autowired
    private ParaminfoService paraminfoService;

    @Autowired
    private ConfigService configService;

    private volatile static List<String> taskList = new ArrayList<>();

    /**
     * 连接丢失后触发，可以断线重连
     *
     * @param throwable
     */
    @Override
    public void connectionLost(Throwable throwable) {
        try {
            logger.info("EMQ客户端已断开连接！");

            //延时3s重连
            javax.swing.Timer timer = new javax.swing.Timer(3000, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    logger.info("EMQ客户端正在尝试重连..........");
                    try {
                        EmqClient.getClient().connect();
                    } catch (MqttException e1) {
                        e1.printStackTrace();
                    }
                }
            });
            timer.setRepeats(false);
            timer.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
        throwable.printStackTrace();
    }

    /**
     * 连接建立成功后回调，可以在这里进行订阅
     *
     * @param reconnect
     * @param serverUri
     */
    @Override
    public void connectComplete(boolean reconnect, String serverUri) {
        try {
            if (reconnect) {
                logger.info("EMQ客户端重新连接成功！");
                emqClient.emqBoot();
            } else {
                logger.info("EMQ客户端连接成功！");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 收到消息后触发，回显消息或插入数据库
     *
     * @param topic
     * @param mqttMessage
     * @throws Exception
     */
    @Override
    public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
        /*
         * 消费消息的回调接口，需要确保该接口不抛异常，该接口运行返回即代表消息消费成功。
         * 消费消息需要保证在规定时间内完成，如果消费耗时超过服务端约定的超时时间，
         * 对于可靠传输的模式，服务端可能会重试推送。
         */
        //中文转码GBK
        String message = new String(mqttMessage.getPayload(), Charset.forName("UTF-8"));
        logger.info("接收到EMQ消息");
        logger.info("回调消息head：" + topic);
        logger.info("回调消息body：" + message);

        /*
         * 收到回调信息，需要解析出topic，根据topic来做不同回调功能
         * 根据需要解析mqttMessag
         */
        String sysTopic = "$SYS";
        String infoTopic = "info";
        String messageTopic = "message";
        String updateTopic = "update_code";
        String paramTopic = "param";

        //使用线程的方法对数据库读写，避免因回调函数时间过长触发mqtt服务器抛弃连接
        //判断是否是$SYS系统主题，是的话就修改设备上下线信息
        int sysTopicLength = 4;
        if (topic.substring(0, sysTopicLength).equals(sysTopic)) {
            logger.info("EMQ主题信息：online");
            CompletableFuture.supplyAsync(() -> onlineOffline(topic));

        } else if (topic.equals(infoTopic)) {
            logger.info("EMQ主题信息：info");
            CompletableFuture.supplyAsync(() -> deviceInfo(message));

        } else if (topic.equals(messageTopic)) {
            logger.info("EMQ主题信息：message");
            CompletableFuture.supplyAsync(() -> deviceMessage(message));

        } else if (topic.equals(updateTopic)) {
            logger.info("EMQ主题信息：update_code");
            CompletableFuture.supplyAsync(() -> updateCode(message));

        } else if (topic.equals(paramTopic)) {
            logger.info("EMQ主题信息：param");
            CompletableFuture.supplyAsync(() -> deviceparam(message));

        } else {
            logger.info("消息发送成功！");
        }
    }


    /**
     * 消息到达后回调
     *
     * @param token
     */
    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        //消息传递成功token.getMessage()会返回null
        //消息正在传送中token.getMessage()会返回正在传递的消息
        try {
            if (token.getMessage() == null) {
                logger.info("消息发送成功");
            } else {
                logger.info("消息" + token.getMessage() + "正在传递.....");
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("消息发送失败！");
        }
    }


    /**
     * 设备上下线入库
     *
     * @param topic
     * @return
     */
    private boolean onlineOffline(String topic) {
        try {
            //截取订阅接收到的主题字符串后面的两个部分，一个是客户端id，一个是在线状态
            String str1 = topic.substring(0, topic.indexOf("clients/"));
            String str2 = topic.substring(str1.length() + 8);
            String clientId = str2.substring(0, str2.indexOf("/"));
            String status = str2.substring(clientId.length() + 1);
            //不对自己本身的上下线状态进行入库
            if (!clientId.equals(EmqClient.getClient().getClientId())) {
                //判断设备是否已经入网，若已经入网就修改在线状态
                Device device = deviceService.searchDeviceByCid(clientId);
                if (device != null) {
                    String connected = "connected";
                    if (status.equals(connected)) {
                        device.setStatus(1);
                    } else {
                        device.setStatus(0);
                    }
                    device.setModitytime(new Date());
                    deviceService.update(device);
                }
            }

            /** ---------- 离线消息处理 ----------- */
            //从任务队列中取出最早一条任务执行
            if (taskList.size() > 0) {
                //若顺利完成则将该任务从队列中移除
                if (offlineTask(taskList.get(0))) {
                    taskList.remove(0);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            logger.info("平台数据服务异常,信息更新失败！");
        }
        return true;
    }


    /**
     * 设备信息上报
     *
     * @param message
     * @return
     */
    private boolean deviceInfo(String message) {
        try {
            // 设备信息入库
            JSONObject jo = JSONObject.parseObject(message);

            //获取clientId
            String clientId = ConvUtil.convToStr(jo.get("clientId"));
            //获取软件列表
            JSONArray softwarelist = jo.getJSONArray("software_list");
            //获取收到信息的时间戳
            Date time = new Date();
            if (!"".equals(clientId)) {
                /** --------根据clientId查询设备信息是否已存在，存在则更新，不存在则新建--------*/
                Map<String, Object> paramMap = new HashMap<>(16);
                paramMap.put("clientid", clientId);
                List<Device> deviceList = deviceService.getMaxList(paramMap);

                //给true说明信息存在，反之给false
                if (deviceList.size() > 0) {
                    doingsql(deviceList.get(0), jo, time, true);

                    //删除多余的设备信息
                    for (int i = 1; i < deviceList.size(); i++) {
                        deviceService.deleteDevice(deviceList.get(i));
                    }
                } else {
                    doingsql(new Device(), jo, time, false);
                }

                /** -------- 整理设备软件表相关软件信息 --------*/
                if (softwarelist != null) {
                    resetDeviceSoftware(clientId, softwarelist);
                } else {
                    resetDeviceSoftware(clientId, new JSONArray());
                }

                /** ---------- 离线消息处理 ----------- */
                offlineTask(clientId);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return  false;
        }
        return true;
    }


    /**
     * 设备反馈消息
     *
     * @param message
     * @return
     */
    private boolean deviceMessage(String message) {
        try {
            JSONObject messageinfo = JSONObject.parseObject(message);
            String termsn = ConvUtil.convToStr(messageinfo.get("term_sn"));
            String msg = ConvUtil.convToStr(messageinfo.get("message"));

            //获取设备信息
            Device device = deviceService.searchDeviceBySn(termsn);

            //v1.2版本开始增加消息确认机制，在这里处理确认消息
            int type = ConvUtil.convToInt(messageinfo.get("type")), enter = 2;
            if (type == enter) {
                //获取消息messageid，解析后并修改状态；
                String msgid = ConvUtil.convToStr(messageinfo.get("message_id"));
                int id = ConvUtil.convToInt(msgid.substring(10));
                Offlinemsg offlinemsg = offlinemsgService.getMsgById(id);
                offlinemsg.setModitytime(new Date());
                offlinemsg.setStatus(1);
                offlinemsgService.update(offlinemsg);

                /** ----- 添加确认消息日志 -----*/
                //根据类型确认日志内容
                String sysMsg = "";
                String msgtype = offlinemsg.getTopic().substring(0, offlinemsg.getTopic().indexOf('/'));
                String update = "update", filelist = "filelist", param = "param";
                if (update.equals(msgtype)) {
                    //软件升级确认消息，先获取软件信息再记录日志
                    JSONObject versioninfo = JSON.parseObject(offlinemsg.getMessage());
                    Software software = softwareService.getSoftwareByName(ConvUtil.convToStr(versioninfo.get("softwarename")));
                    sysMsg = "软件（"+ software.getCustomername() + "(" + versioninfo.get("softwareversion") + ")" +"）升级消息";
                } else if (filelist.equals(msgtype)) {
                    //节目单确认消息，先获取节目单信息再记录日志，remark字段存储的是节目单id
                    JSONObject programinfo = JSON.parseObject(offlinemsg.getMessage());
                    Programs program = programsService.getProgramById(ConvUtil.convToInt(programinfo.get("remark")));
                    if (program != null) {
                        sysMsg = "节目单发布（"+ program.getName() +"）消息";
                    } else {
                        sysMsg = "节目单发布消息";
                    }
                } else if (param.equals(param)) {
                    sysMsg = "参数设置消息";
                } else {
                    sysMsg = "平台发布的最新消息";
                }

                //根据设备所属公司获取用户信息
                Integer companyid = (Integer) device.getCompanyid();
                Userinfo user = userinfoService.getUserByCompany(ConvUtil.convToStr(companyid));

                Map<String, Object> paramMap = new HashMap<>(16);
                paramMap.put("type", 31);
                paramMap.put("loginfo", "设备(" + termsn + ")已收到：" + sysMsg);
                if (user != null) {
                    paramMap.put("userid", user.getId());
                }
                paramMap.put("userip", device.getLanip());
                syslogService.addSyslog(paramMap);
            } else {
                //普通消息，添加系统日志
                Map<String, Object> paramMap = new HashMap<>(16);
                paramMap.put("type", 31);
                paramMap.put("loginfo", "收到设备(" + termsn + ")消息：" + msg);
                paramMap.put("userip", device.getLanip());
                syslogService.addSyslog(paramMap);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }


    /**
     * 设备升级码
     *
     * @param message
     * @return
     */
    private boolean updateCode(String message) {
        try {
            JSONObject qrcodeinfo = JSONObject.parseObject(message);
            String termsn = ConvUtil.convToStr(qrcodeinfo.get("term_sn"));
            String qrcode = ConvUtil.convToStr(qrcodeinfo.get("update_code"));

            //解析升级码并校验
            int qrlength = 13;
            if (qrcode.length() == qrlength && HexStrUtil.checkChecksum(qrcode)) {
                Software software = softwareService.getSoftwareByQr(qrcode);
                if (!"".equals(ConvUtil.convToStr(software.getQrpath()))) {
                    //获取升级对象，发布升级命令
                    JSONObject version = softwareService.getVersion(software);
                    emqClient.pubTopic("update/" + termsn, JSONObject.toJSONString(version));
                } else {
                    logger.info("升级码不存在，无法发布升级命令！");
                }
            } else {
                logger.info("升级码校验失败，无法发布升级命令！");
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("升级码发布异常！");
        }
        return true;
    }


    /**
     * 设备参数消息
     *
     * @param message
     * @return
     */
    private boolean deviceparam(String message) {
        try {
            JSONObject paraminfo = JSONObject.parseObject(message);
            String termsn = ConvUtil.convToStr(paraminfo.get("term_sn"));
            JSONArray paramList = paraminfo.getJSONArray("param_list");

            //解析获取参数列表
            for (Object ob : paramList) {
                JSONObject paramOb = JSONObject.parseObject(ConvUtil.convToStr(ob));
                int paramCode = ConvUtil.convToInt(paramOb.get("param_code"));
                String paramValue = ConvUtil.convToStr(paramOb.get("param_value"));

                //获取参数信息
                Paraminfo param = paraminfoService.getByParamidAndSn(paramCode, termsn);
                if (param != null) {
                    //存在，则在此处更更新
                    param.setParaminfo(paramValue);
                    param.setModifytime(new Date());
                    paraminfoService.update(param);
                } else {
                    //不存在则新建
                    param = new Paraminfo();
                    //linkid储存设备运行软件对应的erp
                    Config config = configService.getByConfigid(paramCode);
                    param.setLinkid(ConvUtil.convToInt(config.getConfiginfo()));
                    param.setTermsn(termsn);
                    param.setParamid(paramOb.getString("param_code"));
                    param.setParaminfo(paramOb.getString("param_value"));
                    param.setCreatetime(new Date());
                    param.setModifytime(new Date());
                    param.setStatus(1);
                    paraminfoService.insert(param);
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }


    /**
     * 公用入库方法
     *
     * @param device
     * @param jo
     * @param time
     * @param existflag
     * @return
     */
    protected boolean doingsql(Device device, JSONObject jo, Date time, Boolean existflag) {
        try {

            //其他信息
            String clientId = ConvUtil.convToStr(jo.get("clientId"));
            String termsn = ConvUtil.convToStr(jo.get("term_sn"));
            String termid = ConvUtil.convToStr(jo.get("term_id"));
            String termcode = ConvUtil.convToStr(jo.get("term_code"));
            String termtype = ConvUtil.convToStr(jo.get("term_type"));
            String terminfo = ConvUtil.convToStr(jo.get("term_info"));
            String macaddress = ConvUtil.convToStr(jo.get("mac_address"));
            String hardwarename = ConvUtil.convToStr(jo.get("hardware_name"));
            String licencecode = ConvUtil.convToStr(jo.get("licence_code"));
            String clientname = ConvUtil.convToStr(jo.get("client_name"));
            String clientcode = ConvUtil.convToStr(jo.get("client_code"));
            String contact = ConvUtil.convToStr(jo.get("contact"));
            String address = ConvUtil.convToStr(jo.get("address"));
            String telno = ConvUtil.convToStr(jo.get("telno"));
            String remark = ConvUtil.convToStr(jo.get("remark"));
            String termmodel = ConvUtil.convToStr(jo.get("term_model"));
            String lanip = ConvUtil.convToStr(jo.get("ip_addr"));
            String wifiip = ConvUtil.convToStr(jo.get("wifi_ip"));
            String emqversion = ConvUtil.convToStr(jo.get("emq_version"));
            String deviceid = ConvUtil.convToStr(jo.get("sam_id"));
            String udid = ConvUtil.convToStr(jo.get("adv_tid"));
            String uuid = ConvUtil.convToStr(jo.get("product_id"));
            String barcode = ConvUtil.convToStr(jo.get("user_sn"));
            String imei = ConvUtil.convToStr(jo.get("imei"));
            String machinetype = ConvUtil.convToStr(jo.get("meid"));


            //兼容EMQV0.6
            if ("".equals(emqversion)) {
                return false;
            }

            //数据入库
            if (!"".equals(clientId)) {
                if (!existflag) {
                    device.setClientid(clientId);
                    device.setCreatetime(time);
                }
            } else {
                logger.info("数据入库失败：clientId不能为空！");
                return false;
            }
            if (!"".equals(termsn)) {
                device.setTermsn(termsn);
            } else {
                logger.info("数据入库失败：term_sn不能为空！");
                return false;
            }
            if (!"".equals(termtype)) {
                device.setTermtype(Integer.parseInt(termtype));
            } else {
                device.setTermtype(0);
            }

            if (!"".equals(termcode)) {
                device.setTermcode(termcode);
            }
            if (!"".equals(termid)) {
                device.setTermid(termid);
            }
            if (!"".equals(terminfo)) {
                device.setTerminfo(terminfo);
            }
            if (!"".equals(macaddress)) {
                device.setMacaddress(macaddress);
            }
            if (!"".equals(hardwarename)) {
                device.setHardwarename(hardwarename);
            }
            if (!"".equals(licencecode)) {
                device.setLicencecode(licencecode);
            }
            if (!"".equals(clientname)) {
                device.setClientname(clientname);
            }
            if (!"".equals(clientcode)) {
                int result = 0;
                String message = "设备注册失败！";
                JSONObject msg = new JSONObject();
                Userinfo user;

                //查询注册账号（手机号）
                if (ConvUtil.convToLong(clientcode) == 0) {
                    logger.info("查询设备注册状态......");
                    //若已绑定公司说明已注册
                    if (ConvUtil.convToInt(device.getCompanyid()) != 0) {
                        result = 1;
                        message = ConvUtil.convToStr(device.getClientcode());
                    } else {
                        result = 0;
                        message = "设备未注册！";
                    }
                } else {
                    //根据客户代码绑定设备到公司（目前客户代码为手机号），绑定成功/失败后返回信息到设备
                    user = userinfoService.getUserByPhone(clientcode);
                    if (user != null && !"".equals(ConvUtil.convToStr(user.getCompany()))) {
                        result = 1;
                        message = clientcode;
                        //绑定到公司
                        device.setCompanyid(ConvUtil.convToInt(user.getCompany()));
                        device.setClientcode(clientcode);
                        logger.info("设备注册成功！");
                    } else {
                        logger.info("设备注册失败！");
                    }
                }

                //发布注册反馈信息
                msg.put("type", 1);
                msg.put("result", result);
                msg.put("message", message);
                emqClient.pubTopic("message/" + termsn, JSONObject.toJSONString(msg));
            }
            if (!"".equals(contact)) {
                device.setContact(contact);
            }
            if (!"".equals(address)) {
                device.setAddress(address);
            }
            if (!"".equals(telno)) {
                device.setTelno(telno);
            }
            if (!"".equals(remark)) {
                device.setRemark(remark);
            }
            if (!"".equals(termmodel)) {
                device.setTermmodel(termmodel);
            }
            if (!"".equals(lanip)) {
                device.setLanip(lanip);
            }
            if (!"".equals(wifiip)) {
                device.setWifiip(wifiip);
            }
            if (!"".equals(emqversion)) {
                device.setEmqversion(emqversion);
            }
            if (!"".equals(deviceid)) {
                device.setDeviceid(deviceid);
            }
            if (!"".equals(udid)) {
                device.setUdid(udid);
            }
            if (!"".equals(uuid)) {
                device.setUuid(uuid);
            }
            if (!"".equals(barcode)) {
                device.setBarcode(barcode);
            }
            if (!"".equals(imei)) {
                device.setImei(imei);
            }
            if (!"".equals(machinetype)) {
                device.setMachinetype(machinetype);
            }

            device.setStatus(1);
            device.setModitytime(time);

            if (existflag) {
                deviceService.update(device);
            } else {
                deviceService.insertDevice(device);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }


    /**
     * 重新调整设备软件列表
     *
     * @param clientId
     * @param softwarelist
     * @return
     * @throws Exception
     */
    private void resetDeviceSoftware(String clientId, JSONArray softwarelist) throws Exception {
        //根据clientid查出设备软件列表
        Device device = deviceService.searchDeviceByCid(ConvUtil.convToStr(clientId));
        if ("".equals(ConvUtil.convToStr(device.getId()))) {
            return;
        }
        List<Devicesw> deviceswList = deviceswService.searchByDeviceId(ConvUtil.convToInt(device.getId()));

        //判断软件是否已存在，存在则更新信息，不存在则新增，并标注为运行状态
        for (Object erpobj : softwarelist) {
            boolean exist = false;
            String erpcode = ConvUtil.convToStr(((JSONObject) erpobj).get("software_erp"));
            String softwarename = ConvUtil.convToStr(((JSONObject) erpobj).get("software_name"));
            String softwareversion = ConvUtil.convToStr(((JSONObject) erpobj).get("software_version"));
            for (Devicesw devicesw : deviceswList) {
                String historyerp = ConvUtil.convToStr(devicesw.getErpcode());
                if (erpcode.equals(historyerp)) {
                    exist = true;
                    devicesw.setSoftwarename(softwarename);
                    devicesw.setSoftwareversion(softwareversion);
                    devicesw.setModitytime(new Date());
                    devicesw.setStatus(1);
                    deviceswService.update(devicesw);
                }
            }
            if (!exist) {
                Devicesw ds = new Devicesw();
                ds.setDeviceid(device.getId());
                ds.setSoftwarename(softwarename);
                ds.setErpcode(erpcode);
                ds.setSoftwareversion(softwareversion);
                ds.setStatus(1);
                ds.setCreatetime(new Date());
                ds.setModitytime(new Date());
                deviceswService.insertDevicesw(ds);
            }
        }

        //禁用目前不包含在info内的软件数据
        for (Devicesw devicesw : deviceswList) {
            boolean exist = false;
            String historyerp = ConvUtil.convToStr(devicesw.getErpcode());
            for (Object erpobj : softwarelist) {
                String erpcode = ConvUtil.convToStr(((JSONObject) erpobj).get("software_erp"));
                if (erpcode.equals(historyerp)) {
                    exist = true;
                }
            }
            if (!exist) {
                devicesw.setStatus(0);
                devicesw.setModitytime(new Date());
                deviceswService.update(devicesw);
            }
        }
    }

    /**
     * 离线任务调度
     *
     * @param clientId
     * @return
     * @throws Exception
     */
    @Transactional(rollbackFor = Exception.class)
    boolean offlineTask(String clientId) throws Exception {
        try {
            Device device = deviceService.searchDeviceByCid(clientId);
            Devicesw devicesw = deviceswService.getUpdateByDeviceId(ConvUtil.convToInt(device.getId()));

            //验证在线情况，若不在线则直接退出；若还有未完成的升级任务则直接退出
            if (ConvUtil.convToInt(device.getStatus()) == 0 || devicesw != null) {
                return false;
            }

            //执行离线任务，目前机制当前只能执行一个任务
            List<Offlinemsg> msglist = offlinemsgService.searchMsgByCid(clientId);
            if (msglist.size() > 0) {
                int taskCount = deviceswService.getUpdateCount();
                int limit = 100;
                if (taskCount < limit) {
                    //若为升级任务则只处理第一个，若不是升级任务则依次处理
                    for (Offlinemsg offlinemsg : msglist) {

                        //延时5s确保设备收到离线消息，v1.4.3设备重连3s后才订阅升级
                        javax.swing.Timer timer = new javax.swing.Timer(3200, new ActionListener() {
                            @Override
                            public void actionPerformed(ActionEvent e) {
                                logger.info("正在向设备(" + clientId + ")发送离线消息........");
                                emqClient.pubTopic(offlinemsg.getTopic(), offlinemsg.getMessage());
                            }
                        });
                        timer.setRepeats(false);
                        timer.start();

                        //将离线任务修改为已完成
                        offlinemsg.setModitytime(new Date());
                        offlinemsg.setCounts(ConvUtil.convToInt(offlinemsg.getCounts()) + 1);

                        //v1.2版本开始不在这里更改状态，即消息中含message_id
                        JSONObject message = JSON.parseObject(offlinemsg.getMessage());
                        String messageid = ConvUtil.convToStr(message.get("message_id"));
                        if ("".equals(messageid)) {
                            offlinemsg.setStatus(1);
                        }
                        offlinemsgService.update(offlinemsg);

                        //升级任务需要在这里修改软件运行状态，并退出
                        if (offlinemsg.getTopic().contains("update/")) {
                            //离线任务若为软件升级则修改软件状态为升级中，remake中存的是softwarename
                            Devicesw dw = deviceswService.getByDeviceIdAndName(ConvUtil.convToInt(device.getId()), offlinemsg.getRemark());
                            if (dw != null) {
                                dw.setStatus(2);
                                dw.setModitytime(new Date());
                                deviceswService.update(dw);
                            }
                            break;
                        }
                    }
                } else {
                    //在这里将超过数量100的任务加入队列，做延后
                    //由于一台设备每次只能执行一个任务，所以只添加一次，待其完成后会再次
                    if (!taskList.contains(clientId)) {
                        taskList.add(clientId);
                    }
                    return false;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
}
