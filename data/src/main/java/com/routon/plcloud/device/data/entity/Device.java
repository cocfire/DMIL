package com.routon.plcloud.device.data.entity;

import com.alibaba.fastjson.JSON;

import java.util.Date;

/**
 * @author FireWang
 * @date 2020/4/30 11:11
 */
public class Device {
    private Object id;
    private String clientid;
    private String termsn;
    private String termid;
    private String termcode;
    private Object termtype;
    private String terminfo;
    private String macaddress;
    private String hardwarename;
    private String machinetype;
    private String licencecode;
    private String softwarename;
    private String erpcode;
    private String softwareversion;
    private String orderid;
    private Object projectid;
    private String projectname;
    private Object companyid;
    private String companyname;
    private String clientname;
    private String clientcode;
    private String contact;
    private String address;
    private String telno;
    private String remark;
    private Object status;
    private Date createtime;
    private Date moditytime;
    private Date updatetime;
    private String link;
    private String termmodel;
    private String lanip;
    private String wifiip;
    private String emqversion;
    private String deviceid;
    private String imei;
    private String udid;
    private String uuid;
    private String barcode;
    private Object facelibrid;

    public void setId(Object id) {
        this.id = id;
    }

    public Object getId() {
        return id;
    }

    public void setClientid(String clientid) {
        this.clientid = clientid;
    }

    public String getClientid() {
        return clientid;
    }

    public void setTermsn(String termsn) {
        this.termsn = termsn;
    }

    public String getTermsn() {
        return termsn;
    }

    public void setTermid(String termid) {
        this.termid = termid;
    }

    public String getTermid() {
        return termid;
    }

    public void setTermcode(String termcode) {
        this.termcode = termcode;
    }

    public String getTermcode() {
        return termcode;
    }

    public void setTermtype(Object termtype) {
        this.termtype = termtype;
    }

    public Object getTermtype() {
        return termtype;
    }

    public void setTerminfo(String terminfo) {
        this.terminfo = terminfo;
    }

    public String getTerminfo() {
        return terminfo;
    }

    public void setMacaddress(String macaddress) {
        this.macaddress = macaddress;
    }

    public String getMacaddress() {
        return macaddress;
    }

    public void setHardwarename(String hardwarename) {
        this.hardwarename = hardwarename;
    }

    public String getHardwarename() {
        return hardwarename;
    }

    public void setMachinetype(String machinetype) {
        this.machinetype = machinetype;
    }

    public String getMachinetype() {
        return machinetype;
    }

    public void setLicencecode(String licencecode) {
        this.licencecode = licencecode;
    }

    public String getLicencecode() {
        return licencecode;
    }

    public void setSoftwarename(String softwarename) {
        this.softwarename = softwarename;
    }

    public String getSoftwarename() {
        return softwarename;
    }

    public void setErpcode(String erpcode) {
        this.erpcode = erpcode;
    }

    public String getErpcode() {
        return erpcode;
    }

    public void setSoftwareversion(String softwareversion) {
        this.softwareversion = softwareversion;
    }

    public String getSoftwareversion() {
        return softwareversion;
    }

    public String getOrderid() {
        return orderid;
    }

    public void setOrderid(String orderid) {
        this.orderid = orderid;
    }

    public void setProjectid(Object projectid) {
        this.projectid = projectid;
    }

    public Object getProjectid() {
        return projectid;
    }

    public void setProjectname(String projectname) {
        this.projectname = projectname;
    }

    public String getProjectname() {
        return projectname;
    }

    public Object getCompanyid() {
        return companyid;
    }

    public void setCompanyid(Object companyid) {
        this.companyid = companyid;
    }

    public void setCompanyname(String companyname) {
        this.companyname = companyname;
    }

    public String getCompanyname() {
        return companyname;
    }

    public void setClientname(String clientname) {
        this.clientname = clientname;
    }

    public String getClientname() {
        return clientname;
    }

    public void setClientcode(String clientcode) {
        this.clientcode = clientcode;
    }

    public String getClientcode() {
        return clientcode;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public String getContact() {
        return contact;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getAddress() {
        return address;
    }

    public void setTelno(String telno) {
        this.telno = telno;
    }

    public String getTelno() {
        return telno;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getRemark() {
        return remark;
    }

    public void setStatus(Object status) {
        this.status = status;
    }

    public Object getStatus() {
        return status;
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

    public void setUpdatetime(Date updatetime) {
        this.updatetime = updatetime;
    }

    public Date getUpdatetime() {
        return updatetime;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getLink() {
        return link;
    }

    public void setTermmodel(String termmodel) {
        this.termmodel = termmodel;
    }

    public String getTermmodel() {
        return termmodel;
    }

    public void setLanip(String lanip) {
        this.lanip = lanip;
    }

    public String getLanip() {
        return lanip;
    }

    public void setWifiip(String wifiip) {
        this.wifiip = wifiip;
    }

    public String getWifiip() {
        return wifiip;
    }

    public void setEmqversion(String emqversion) {
        this.emqversion = emqversion;
    }

    public String getEmqversion() {
        return emqversion;
    }

    public void setDeviceid(String deviceid) {
        this.deviceid = deviceid;
    }

    public String getDeviceid() {
        return deviceid;
    }

    public void setImei(String imei) {
        this.imei = imei;
    }

    public String getImei() {
        return imei;
    }

    public void setUdid(String udid) {
        this.udid = udid;
    }

    public String getUdid() {
        return udid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getUuid() {
        return uuid;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public String getBarcode() {
        return barcode;
    }

    public Object getFacelibrid() {
        return facelibrid;
    }

    public void setFacelibrid(Object facelibrid) {
        this.facelibrid = facelibrid;
    }

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }

}
