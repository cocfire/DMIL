package com.routon.plcloud.device.api.controller.vrclient;

import com.routon.plcloud.device.api.utils.MqttUtil;
import com.routon.plcloud.device.api.utils.SslUtil;
import com.routon.plcloud.device.core.service.VrdeviceService;
import com.routon.plcloud.device.core.utils.ConvUtil;
import com.routon.plcloud.device.core.utils.PropertiesUtil;
import com.routon.plcloud.device.data.entity.Vrdevice;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.charset.Charset;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author FireWang
 * @date 2020/7/6 16:57
 * 用于创建虚拟EMQ设备
 */
@Service
public class VrEmqUtils {
    private static Logger logger = LoggerFactory.getLogger(VrEmqUtils.class);
    /**
     * 定义emq连接参数，注意使用ssl加密连接
     */
    private static String host = PropertiesUtil.getDataFromPropertiseFile("emqForMqtt", "host");
    /**
     * emq连接账号密码
     */
    private static String userName = PropertiesUtil.getDataFromPropertiseFile("emqForMqtt", "userName");
    private static String passWord = PropertiesUtil.getDataFromPropertiseFile("emqForMqtt", "passWord");
    /**
     * 连接信息 mqttUtil
     */
    private volatile static MqttUtil mqttUtil = null;
    /**
     * 连接参数 option
     */
    private volatile static MqttConnectOptions options = null;

    /**
     * 虚拟客户端群组 clientGroup
     */
    private volatile static Map<String, MqttClient> clientGroup = new HashMap<>(16);

    @Autowired
    private VrdeviceService vrdeviceService;

    /**
     * 初始化EMQ参数
     *
     * @param clientId
     * @return
     */
    private static void initSetting(String clientId) {
        try {
            //设置mqtt信息
            if (mqttUtil == null) {
                mqttUtil = new MqttUtil();
                mqttUtil.setHost(host);
                mqttUtil.setUserName(userName);
                mqttUtil.setPassWord(passWord);
            }
            mqttUtil.setClienId(clientId);

            //设置连接参数
            if (options == null) {
                options = new MqttConnectOptions();
                //false保存离线消息，下次上线可以接收离线时接收到的消息；
                //true不保存离线消息，下次上线不接收离线时接收到的消息；
                options.setCleanSession(false);
                //设置用户名密码
                options.setUserName(mqttUtil.getUserName());
                options.setPassword(mqttUtil.getPassWord().toCharArray());
                // 设置连接超时时间30s
                options.setConnectionTimeout(90);
                // 设置会话心跳时间20s
                options.setKeepAliveInterval(60);
                //读取ssl加密连接需要的ca证书、客户端证书、客户端秘钥
                try {
                    options.setSocketFactory(SslUtil.getSocketFactoryInJar(mqttUtil.getCaFilePath(),
                            mqttUtil.getClientCrtFilePath(), mqttUtil.getClientKeyFilePath(), ""));
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    logger.info("虚拟EMQ客户端获取证书失败!");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取虚拟设备的emq客户端
     *
     * @param clientId
     * @return
     */
    protected static MqttClient getClientById(String clientId) {
        //获取连接客户端client
        MqttClient client = clientGroup.get(clientId);
        try {
            if (client == null) {
                //如果客户端不存在则创建新的EMQ客户端，并加到客户端群组
                initSetting(clientId);
                client = new MqttClient(mqttUtil.getHost(), mqttUtil.getClienId(), new MemoryPersistence());
                clientGroup.put(clientId, client);
            }
        } catch (MqttException e) {
            e.printStackTrace();
        }
        return client;
    }

    /**
     * 设备上线操作
     *
     * @param clientId
     * @return
     */
    public String clientOnline(String clientId) {
        //获取连接客户端client
        MqttClient client = getClientById(clientId);
        String msg = "success";
        try {
            if (!client.isConnected()) {
                client.setCallback(new VrCallback(clientId));
                client.connect(options);
            }
        } catch (MqttException e) {
            e.printStackTrace();
            msg = "系统异常：设备" + clientId + "连接失败....";
        }
        return msg;
    }

    /**
     * 设备离线操作
     *
     * @param clientId
     * @return
     */
    public String clientOffline(String clientId) {
        //获取连接客户端client
        MqttClient client = getClientById(clientId);
        String msg = "success";
        try {
            if (client.isConnected()) {
                client.disconnect();
            }
            //修改为离线状态
            setOffline(clientId);
        } catch (MqttException e) {
            e.printStackTrace();
            msg = "系统异常：设备" + clientId + "离线失败....";
        } catch (Exception e) {
            e.printStackTrace();
        }
        return msg;
    }

    /**
     * 设备订阅操作
     *
     * @param clientId
     * @param topic
     * @return
     */
    public String clientSub(String clientId, String topic) {
        //获取连接客户端client
        MqttClient client = getClientById(clientId);
        String msg = "success";
        try {
            if (!client.isConnected()) {
                client.setCallback(new VrCallback(clientId));
                client.connect(options);
            }
            client.subscribe(topic, 2);

        } catch (MqttException e) {
            e.printStackTrace();
            msg = "系统异常：设备" + clientId + "订阅主题(" + topic + ")失败；";
        }
        return msg;
    }

    /**
     * 设备发布操作
     *
     * @param clientId
     * @param topic
     * @param message
     * @return
     */
    public String clientPub(String clientId, String topic, String message) {
        //获取连接客户端client
        MqttClient client = getClientById(clientId);
        String msg = "success";
        try {
            if (!client.isConnected()) {
                client.setCallback(new VrCallback(clientId));
                client.connect(options);
            }
            //设置mqttTopic参数
            MqttTopic mqttTopic = client.getTopic(topic);
            //设置mqttmessage参数
            //创建发送的消息对象
            MqttMessage mqttMessage = new MqttMessage();
            //设置qos质量等级
            mqttMessage.setQos(2);
            //setRetained设置保留消息
            //false不保留消息，发布一个主题后，只有当前有订阅者存在的情况下才接收的到消息
            //true保留消息，发布一个主题后，在发送给当前订阅者后，还会存到服务器，如果有
            //新的订阅者上线也会把该消息发给新的订阅者
            mqttMessage.setRetained(false);
            mqttMessage.setPayload(message.getBytes());

            //发布消息
            mqttTopic.publish(mqttMessage);

        } catch (MqttException e) {
            e.printStackTrace();
            msg = "系统异常：设备" + clientId + "订阅主题(" + topic + ")失败；";
        }
        return msg;
    }

    /**
     * EMQ服务器回调监控
     *
     */
    private class VrCallback implements MqttCallbackExtended {
        private String clientId;

        public VrCallback(String clientId) {
            this.clientId = ConvUtil.convToStr(clientId);
        }

        //连接建立成功后回调
        @Override
        public void connectComplete(boolean reconnect, String serverURI) {
            logger.info("客户端(" + clientId + ")连接成功！");
            try {
                //修改为在线状态
                Vrdevice vrdevice = vrdeviceService.searchByDeviceId(clientId);
                if (vrdevice != null) {
                    vrdevice.setStatus(1);
                    vrdevice.setUpdatetime(new Date());
                    vrdeviceService.update(vrdevice);

                    //发布信息
                    clientPub(clientId, "info", vrdevice.getInfo());

                    //默认订阅升级主题
                    clientSub(clientId, "update/" + clientId);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        //连接丢失后回调
        @Override
        public void connectionLost(Throwable cause) {
            logger.info("客户端(" + clientId + ")连接断开！");
            try {
                //修改为离线状态
                setOffline(clientId);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        //收到消息后的执行方法
        @Override
        public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
            try {
                //中文转码GBK
                String message = new String(mqttMessage.getPayload(), Charset.forName("UTF-8"));

                logger.info("设备("+ clientId +")接收到MQTT消息");
                logger.info("主题：" + topic);
                logger.info("消息：" + message);

                //存入数据库
                Vrdevice vrdevice = vrdeviceService.searchByDeviceId(clientId);
                if (vrdevice != null) {
                    vrdevice.setRemark(message);
                    vrdevice.setUpdatetime(new Date());
                    vrdeviceService.update(vrdevice);
                } else {
                    logger.info("消入库失败，设备不存在！");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        //消息到达后回调
        @Override
        public void deliveryComplete(IMqttDeliveryToken token) {
            try {
                if (token.getMessage() == null) {
                    logger.info("消息发送成功!");
                } else {
                    logger.info("消息" + token.getMessage() + "正在传递");
                }
            } catch (Exception e) {
                e.printStackTrace();
                logger.error("消息发送失败！");
            }
        }
    }

    private void setOffline(String clientId) {
        try {
            Vrdevice vrdevice = vrdeviceService.searchByDeviceId(clientId);
            if (vrdevice != null) {
                vrdevice.setStatus(0);
                vrdevice.setUpdatetime(new Date());
                vrdeviceService.update(vrdevice);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
