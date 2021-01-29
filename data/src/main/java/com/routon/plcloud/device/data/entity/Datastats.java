package com.routon.plcloud.device.data.entity;

import com.alibaba.fastjson.JSON;
import java.util.Date;

/**
 * @author FireWang
 * @date 2020/5/07 15:09
 */
public class Datastats {
	private Object id;
	private String linktable;
	private Object linkid;
	private String linkinfo;
	private Object infoa;
	private String infob;
	private String stats;
	private Date modifytime;
	private Date createtime;
	private String remark;
	private Object companyid;

	public void setId(Object id) {
		this.id = id;
	}

	public Object getId() {
		return id;
	}

	public void setLinktable(String linktable) {
		this.linktable = linktable;
	}

	public String getLinktable() {
		return linktable;
	}

	public void setLinkid(Object linkid) {
		this.linkid = linkid;
	}

	public Object getLinkid() {
		return linkid;
	}

	public void setLinkinfo(String linkinfo) {
		this.linkinfo = linkinfo;
	}

	public String getLinkinfo() {
		return linkinfo;
	}

	public void setInfoa(Object infoa) {
		this.infoa = infoa;
	}

	public Object getInfoa() {
		return infoa;
	}

	public void setInfob(String infob) {
		this.infob = infob;
	}

	public String getInfob() {
		return infob;
	}

	public void setStats(String stats) {
		this.stats = stats;
	}

	public String getStats() {
		return stats;
	}

	public void setModifytime(Date modifytime) {
		this.modifytime = modifytime;
	}

	public Date getModifytime() {
		return modifytime;
	}

	public void setCreatetime(Date createtime) {
		this.createtime = createtime;
	}

	public Date getCreatetime() {
		return createtime;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public String getRemark() {
		return remark;
	}

	public void setCompanyid(Object companyid) {
		this.companyid = companyid;
	}

	public Object getCompanyid() {
		return companyid;
	}


	@Override
	public String toString() {
	    return JSON.toJSONString(this);
    }

}
