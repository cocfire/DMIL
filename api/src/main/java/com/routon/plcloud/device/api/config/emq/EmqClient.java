package com.routon.plcloud.device.api.config.emq;


import com.routon.plcloud.device.api.utils.FileUtil;
import com.routon.plcloud.device.api.utils.MqttUtil;
import com.routon.plcloud.device.api.utils.SslUtil;
import com.routon.plcloud.device.core.utils.PropertiesUtil;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;

/**
 * @author FireWang
 * @date 2020/5/7 15:45
 * emq客户端
 */
@Service
public class EmqClient {
    private static Logger logger = LoggerFactory.getLogger(EmqClient.class);
    /**
     * 定义emq连接参数，注意使用ssl加密连接
     */
    private static String host = PropertiesUtil.getDataFromPropertiseFile("emqForMqtt", "host");
    /**
     * 订阅客户端上下线$SYS系统主题
     */
    private static String onlineTopic = "$SYS/brokers/+/clients/#";
    /**
     * 客户端ID，保证唯一
     */
    private static String clientId = PropertiesUtil.getDataFromPropertiseFile("emqForMqtt", "clientid");
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
     * 发布主题 mqttTopic
     */
    private volatile static MqttTopic mqttTopic = null;
    /**
     * 发布信息 mqttMessage
     */
    private volatile static MqttMessage mqttMessage = null;
    /**
     * 连接客户端 client
     */
    private volatile static MqttClient client = null;
    /**
     * 回调信息 msg
     */
    private volatile static String msg = "success";

    @Autowired
    private EmqCallback emqCallback;

    /**
     * 初始化并获取emq客户端
     *
     * @throws MqttException
     */
    protected static MqttClient getClient() {
        try {
            //设置mqtt信息
            if (mqttUtil == null) {
                logger.info("设置mqtt信息......");
                mqttUtil = new MqttUtil();
                mqttUtil.setHost(host);
                mqttUtil.setClienId(clientId);
                mqttUtil.setUserName(userName);
                mqttUtil.setPassWord(passWord);
            }
            //设置连接参数
            if (options == null) {
                logger.info("设置连接参数......");
                options = new MqttConnectOptions();
                //是否保存离线消息
                //false保存离线消息，下次上线可以接收离线时接收到的消息；
                //true不保存离线消息，下次上线不接收离线时接收到的消息；
                options.setCleanSession(false);
                //设置用户名密码
                options.setUserName(mqttUtil.getUserName());
                options.setPassword(mqttUtil.getPassWord().toCharArray());
                // 设置连接超时时间60s
                options.setConnectionTimeout(60);
                // 设置会话心跳时间30s
                options.setKeepAliveInterval(30);
                //读取ssl加密连接需要的ca证书、客户端证书、客户端秘钥
                try {
                    options.setSocketFactory(SslUtil.getSocketFactoryInJar(mqttUtil.getCaFilePath(),
                            mqttUtil.getClientCrtFilePath(), mqttUtil.getClientKeyFilePath(), ""));
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    logger.info("EMQ客户端获取证书失败!");
                    msg = "fail";
                }
            }
            //获取连接客户端client，如果客户端不存在则创建新的连接
            if (client == null) {
                logger.info("创建EMQ客户端连接......");
                client = new MqttClient(mqttUtil.getHost(), mqttUtil.getClienId(), new MemoryPersistence());
            }
        } catch (MqttException e) {
            e.printStackTrace();
        }
        return client;
    }

    /**
     * 获取EMQ连接状态
     *
     * @return
     */
    public boolean emqIsConnected() {
        try {
            if (client != null && client.isConnected()) {
                msg = "success";
                logger.info("EMQ服务连接正常！");
                return true;
            } else {
                msg = "fail";
                logger.info("EMQ服务未连接！");
                return false;
            }
        } catch (Exception e) {
            logger.info("EMQ服务连接异常！");
            e.printStackTrace();
            return false;
        }
    }

    /**
     * mqtt订阅上下线信息
     *
     * @throws MqttException
     */
    public String subOnline() {
        return subTopic(onlineTopic);
    }

    /**
     * mqtt订阅
     *
     * @param topic 订阅主题
     * @throws MqttException
     */
    public String subTopic(String topic) {
        //初始化客户端
        getClient();
        mqttUtil.setTopic(topic);
        try {
            //判断客户端连接状态，若未连接则线连接再订阅，若已连接则直接订阅
            if (!client.isConnected()) {
                client.setCallback(emqCallback);
                //建立连接
                client.connect(options);
                if (client.isConnected()) {
                    client.subscribe(topic, 2);
                } else {
                    logger.info("EMQ客户端连接失败，请重试！");
                }
            } else {
                client.subscribe(topic, 2);
            }
            logger.info("主题：“" + topic + "”订阅成功！");
        } catch (MqttException e) {
            e.printStackTrace();
        }
        emqIsConnected();
        return msg;
    }

    /**
     * mqtt发布方法
     *
     * @param topic   发布主题
     * @param message 发布信息
     * @throws MqttException
     */
    public String pubTopic(String topic, String message) {
        //初始化客户端
        getClient();
        mqttUtil.setTopic(topic);
        mqttUtil.setMessage(message);
        try {
            //设置mqttTopic参数
            mqttTopic = client.getTopic(mqttUtil.getTopic());
            //设置mqttmessage参数
            //创建发送的消息对象
            mqttMessage = new MqttMessage();
            //设置qos质量等级
            mqttMessage.setQos(2);
            //setRetained设置保留消息
            //false不保留消息，发布一个主题后，只有当前有订阅者存在的情况下才接收的到消息
            //true保留消息，发布一个主题后，在发送给当前订阅者后，还会存到服务器，如果有
            //新的订阅者上线也会把该消息发给新的订阅者
            mqttMessage.setRetained(false);
            mqttMessage.setPayload(mqttUtil.getMessage().getBytes());
            //判断客户端连接状态，若未连接则线连接再订阅，若已连接则直接订阅
            if (!client.isConnected()) {
                client.setCallback(emqCallback);
                //建立连接
                client.connect(options);
                if (client.isConnected()) {
                    mqttTopic.publish(mqttMessage);
                } else {
                    logger.info("EMQ客户端连接失败，请重试！");
                }
            } else {
                mqttTopic.publish(mqttMessage);
            }
            logger.info("消息发布成功：(topic:"+ topic +")"+ message);
        } catch (MqttException e) {
            e.printStackTrace();
        }
        emqIsConnected();
        return msg;
    }

    /**
     * EMQ下载证书到本地：根据业务需求在配置文件中设置证书路径
     *
     * @return
     */
    public boolean createCaFileToPath() {
        boolean finalflag = false;
        try {
            //第一次自动判断证书是否存在
            boolean caflag = false;
            boolean certflag = false;
            boolean keyflag = false;

            String certFilePath = PropertiesUtil.getDataFromPropertiseFile("emqForMqtt", "key_path");
            String targetPath = PropertiesUtil.getDataFromPropertiseFile("emqForMqtt", "put_path");
            /**
             * ca证书文件
             */
            String caFileName = "ca-cert.pem";
            /**
             * 客户端证书文件
             */
            String clientCrtFileName = "client-cert.pem";
            /**
             * 客户端秘钥
             */
            String clientKeyFileName = "client-key.pem";

            File certFile = new File(targetPath);
            //如果证书不存在，先生成证书路径，再将证书文件下载到本地
            if (!certFile.exists()) {
                certFile.mkdirs();
            }

            //逐个生成文件
            FileUtil fileUtil = new FileUtil();
            caflag = getKeyCert(certFilePath, targetPath, caFileName);
            certflag = getKeyCert(certFilePath, targetPath, clientCrtFileName);
            keyflag = getKeyCert(certFilePath, targetPath, clientKeyFileName);

            if (caflag && certflag && keyflag) {
                finalflag = true;
            } else {
                logger.info("EMQ证书生成失败！");
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        return finalflag;
    }

    /**
     * 获取秘钥文件并生成到指定路径
     *
     * @param filepath
     * @param targetpath
     * @param filename
     * @return
     */
    private boolean getKeyCert(String filepath, String targetpath, String filename) {
        try {
            FileUtil fileUtil = new FileUtil();
            if (!new File(targetpath + filename).exists()) {
                logger.info("正在生证书文件：" + filename);
                return fileUtil.writeCertToPath(filepath + filename, targetpath + filename);
            } else {
                return true;
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
            return false;
        }
    }

    /**
     * EMQ服务启动程序：根据业务需求设置主题
     */
    public void emqBoot() {
        try {
            String success = "success";
            //订阅设备在线信息
            String onlineFlag = subOnline();
            if (!onlineFlag.equals(success)) {
                logger.info(onlineFlag);
            }
            //订阅终端信息
            String infoFlag = subTopic("info");
            if (!infoFlag.equals(success)) {
                logger.info(infoFlag);
            }
            //订阅设备消息
            String messageFlag = subTopic("message");
            if (!messageFlag.equals(success)) {
                logger.info(messageFlag);
            }
            //订阅设备升级码消息
            String qrcodeFlag = subTopic("update_code");
            if (!qrcodeFlag.equals(success)) {
                logger.info(qrcodeFlag);
            }
            //订阅设备参数消息
            String paramFlag = subTopic("param");
            if (!paramFlag.equals(success)) {
                logger.info(paramFlag);
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }
}