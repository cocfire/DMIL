package com.routon.plcloud.device.data.entity;

import com.alibaba.fastjson.JSON;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author FireWang
 * @date 2020/4/30 11:11
 */
public class Syslog {
    private Object id;
    private String option;
    private Object type;
    private String loginfo;
    private Date timestamp;
    private Object userid;
    private String userip;
    private String remark;
    public static final Map<Integer, String> SYS_TYPE = new LinkedHashMap<Integer, String>();

    static {
        /*用户操作*/
        SYS_TYPE.put(10, "用户登录");
        SYS_TYPE.put(11, "修改密码");
        SYS_TYPE.put(12, "添加用户");
        SYS_TYPE.put(13, "删除用户");
        SYS_TYPE.put(14, "重置密码");

        /*软件管理操作*/
        SYS_TYPE.put(20, "软件上传");
        SYS_TYPE.put(21, "软件下载");
        SYS_TYPE.put(22, "软件删除");

        /*设备管理操作*/
        SYS_TYPE.put(30, "设备升级");
        SYS_TYPE.put(31, "设备消息");

        /*分组操作*/
        SYS_TYPE.put(40, "添加公司");
        SYS_TYPE.put(41, "删除公司");
        SYS_TYPE.put(42, "添加分组");
        SYS_TYPE.put(43, "删除分组");

        /*文件管理操作*/
        SYS_TYPE.put(50, "文件上传");
        SYS_TYPE.put(51, "文件下载");
        SYS_TYPE.put(52, "文件删除");
        SYS_TYPE.put(53, "发布节目单");
    }

    public void setId(Object id) {
        this.id = id;
    }

    public Object getId() {
        return id;
    }

    public void setOption(String option) {
        this.option = option;
    }

    public String getOption() {
        return option;
    }

    public void setType(Object type) {
        this.type = type;
    }

    public Object getType() {
        return type;
    }

    public void setLoginfo(String loginfo) {
        this.loginfo = loginfo;
    }

    public String getLoginfo() {
        return loginfo;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setUserid(Object userid) {
        this.userid = userid;
    }

    public Object getUserid() {
        return userid;
    }

    public void setUserip(String userip) {
        this.userip = userip;
    }

    public String getUserip() {
        return userip;
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
