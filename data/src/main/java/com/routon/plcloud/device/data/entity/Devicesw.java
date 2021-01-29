package com.routon.plcloud.device.data.entity;

import com.alibaba.fastjson.JSON;
import java.util.Date;

/**
 * @author FireWang
 * @date 2020/5/07 15:09
 */
public class Devicesw {
	private Object id;
	private String softwarename;
	private String erpcode;
	private String softwareversion;
	private String remark;
	private Object status;
	private Date createtime;
	private Date moditytime;
	private Object deviceid;

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

	public void setDeviceid(Object deviceid) {
		this.deviceid = deviceid;
	}

	public Object getDeviceid() {
		return deviceid;
	}


	@Override
	public String toString() {
	    return JSON.toJSONString(this);
    }

}
