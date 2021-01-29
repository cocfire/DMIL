package com.routon.plcloud.device.data.entity;

import com.alibaba.fastjson.JSON;

import java.util.Date;

/**
 * @author FireWang
 * @date 2020/4/30 11:11
 */
public class Software {

    private Object id;
    private String softwarename;
    private String softwareversion;
    private Date uploadtime;
    private String size;
    private String customername;
    private Object downstatus;
    private Date createtime;
    private Date moditytime;
    private String uploadsoftwarename;
    private Object erpcode;
    private String filetimestamp;
    private String softwaretype;
    private String signflag;
    private String qrcode;
    private String qrpath;
    private String fitmodel;
    private String remark;
    private int pubflag;


    public void setId(Object id) {
        this.id = id;
    }

    public Object getId() {
        return id;
    }

    public void setSoftwarename(String softwarename) {
        this.softwarename = softwarename;
    }

    public String getSoftwarename() {
        return softwarename;
    }

    public void setSoftwareversion(String softwareversion) {
        this.softwareversion = softwareversion;
    }

    public String getSoftwareversion() {
        return softwareversion;
    }

    public void setUploadtime(Date uploadtime) {
        this.uploadtime = uploadtime;
    }

    public Date getUploadtime() {
        return uploadtime;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getSize() {
        return size;
    }

    public void setCustomername(String customername) {
        this.customername = customername;
    }

    public String getCustomername() {
        return customername;
    }

    public void setDownstatus(Object downstatus) {
        this.downstatus = downstatus;
    }

    public Object getDownstatus() {
        return downstatus;
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

    public void setUploadsoftwarename(String uploadsoftwarename) {
        this.uploadsoftwarename = uploadsoftwarename;
    }

    public String getUploadsoftwarename() {
        return uploadsoftwarename;
    }

    public void setErpcode(Object erpcode) {
        this.erpcode = erpcode;
    }

    public Object getErpcode() {
        return erpcode;
    }

    public void setFiletimestamp(String filetimestamp) {
        this.filetimestamp = filetimestamp;
    }

    public String getFiletimestamp() {
        return filetimestamp;
    }

    public void setSoftwaretype(String softwaretype) {
        this.softwaretype = softwaretype;
    }

    public String getSoftwaretype() {
        return softwaretype;
    }

    public void setSignflag(String signflag) {
        this.signflag = signflag;
    }

    public String getSignflag() {
        return signflag;
    }

    public void setQrcode(String qrcode) {
        this.qrcode = qrcode;
    }

    public String getQrcode() {
        return qrcode;
    }

    public void setQrpath(String qrpath) {
        this.qrpath = qrpath;
    }

    public String getQrpath() {
        return qrpath;
    }

    public String getFitmodel() {
        return fitmodel;
    }

    public void setFitmodel(String fitmodel) {
        this.fitmodel = fitmodel;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public int getPubflag() {
        return pubflag;
    }

    public void setPubflag(int pubflag) {
        this.pubflag = pubflag;
    }

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}
