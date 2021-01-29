package com.routon.plcloud.device.data.entity;

import com.alibaba.fastjson.JSON;

import java.util.Date;

/**
 * @author FireWang
 * @date 2020/5/07 15:09
 */
public class Userinfo {
    private Integer id;
    private Object openid;
    private String username;
    private String realname;
    private String password;
    private String phone;
    private Date createtime;
    private Date modifytime;
    private Object status;
    private Object counts;
    private String lastloginip;
    private Date lastlogintime;
    private String company;
    private String project;
    private String remark;

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public void setOpenid(Object openid) {
        this.openid = openid;
    }

    public Object getOpenid() {
        return openid;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public void setRealname(String realname) {
        this.realname = realname;
    }

    public String getRealname() {
        return realname;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPassword() {
        return password;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPhone() {
        return phone;
    }

    public void setCreatetime(Date createtime) {
        this.createtime = createtime;
    }

    public Date getCreatetime() {
        return createtime;
    }

    public void setModifytime(Date modifytime) {
        this.modifytime = modifytime;
    }

    public Date getModifytime() {
        return modifytime;
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

    public void setLastloginip(String lastloginip) {
        this.lastloginip = lastloginip;
    }

    public String getLastloginip() {
        return lastloginip;
    }

    public void setLastlogintime(Date lastlogintime) {
        this.lastlogintime = lastlogintime;
    }

    public Date getLastlogintime() {
        return lastlogintime;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getCompany() {
        return company;
    }

    public void setProject(String project) {
        this.project = project;
    }

    public String getProject() {
        return project;
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
