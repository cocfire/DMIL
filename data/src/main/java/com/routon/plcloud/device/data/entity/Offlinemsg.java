package com.routon.plcloud.device.data.entity;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import java.util.Date;

/**
 * @author FireWang
 * @date 2020/4/30 11:11
 */
public class Offlinemsg {
    private Object id;
    private String topic;
    private String message;
    private String clientid;
    private Date createtime;
    private Date moditytime;
    private Object status;
    private Object counts;
    private String remark;

    public Offlinemsg(){

    }
    /**通用构造方法
     *
     * @param topic
     * @param message
     * @param clientid
     * @param remark
     */
    public Offlinemsg(String topic, String message, String clientid, String remark) {
        this.setTopic(topic);
        this.setMessage(message);
        this.setClientid(clientid);
        this.setStatus(0);
        this.setCreatetime(new Date());
        this.setModitytime(new Date());
        this.setCounts(0);
        this.setRemark(remark);
    }

    public void setId(Object id) {
        this.id = id;
    }

    public Object getId() {
        return id;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getTopic() {
        return topic;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public String getClientid() {
        return clientid;
    }

    public void setClientid(String clientid) {
        this.clientid = clientid;
    }

    public void setCreatetime(Date createtime) {
        this.createtime = createtime;
    }

    public Date getCreatetime() {
        return createtime;
    }

    public void setModitytime(Date moditytime) {
        this.moditytime = moditytime;
    }

    public Date getModitytime() {
        return moditytime;
    }

    public void setStatus(Object status) {
        this.status = status;
    }

    public Object getStatus() {
        return status;
    }

    public void setCounts(Object counts) {
        this.counts = counts;
    }

    public Object getCounts() {
        return counts;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getRemark() {
        return remark;
    }

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }

}
