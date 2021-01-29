package com.routon.plcloud.device.api.utils;

import com.routon.plcloud.device.core.utils.PropertiesUtil;

/**
 * @author FireWang
 * @date 2020/5/7 15:45
 * MQTT 对象 存储mqtt相关参数
 */
public class MqttUtil {
    /**
     * 地址端口号
     */
    private String host;
    /**
     * 发布主题
     */
    private String topic;
    /**
     * ca证书文件
     */
    private String caFilePath = PropertiesUtil.getDataFromPropertiseFile("emqForMqtt", "key_path") + "ca-cert.pem";
    /**
     * 客户端证书文件
     */
    private String clientCrtFilePath = PropertiesUtil.getDataFromPropertiseFile("emqForMqtt", "key_path") + "client-cert.pem";
    /**
     * 客户端秘钥
     */
    private String clientKeyFilePath = PropertiesUtil.getDataFromPropertiseFile("emqForMqtt", "key_path") + "client-key.pem";
    /**
     * 客户端ID
     */
    private String clienId;
    /**
     * mqtt服务器用户名
     */
    private String userName;
    /**
     * mqtt服务器密码
     */
    private String passWord;
    /**
     * MQTT消息
     */
    private String message;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getClienId() {
        return clienId;
    }

    public void setClienId(String clienId) {
        this.clienId = clienId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassWord() {
        return passWord;
    }

    public void setPassWord(String passWord) {
        this.passWord = passWord;
    }

    public String getCaFilePath() {
        return caFilePath;
    }

    public void setCaFilePath(String caFilePath) {
        this.caFilePath = caFilePath;
    }

    public String getClientCrtFilePath() {
        return clientCrtFilePath;
    }

    public void setClientCrtFilePath(String clientCrtFilePath) {
        this.clientCrtFilePath = clientCrtFilePath;
    }

    public String getClientKeyFilePath() {
        return clientKeyFilePath;
    }

    public void setClientKeyFilePath(String clientKeyFilePath) {
        this.clientKeyFilePath = clientKeyFilePath;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "MqttUtil{" +
                "host='" + host + '\'' +
                ", topic='" + topic + '\'' +
                ", caFilePath='" + caFilePath + '\'' +
                ", clientCrtFilePath='" + clientCrtFilePath + '\'' +
                ", clientKeyFilePath='" + clientKeyFilePath + '\'' +
                ", clienId='" + clienId + '\'' +
                ", userName='" + userName + '\'' +
                ", passWord='" + passWord + '\'' +
                ", message='" + message + '\'' +
                '}';
    }
}
