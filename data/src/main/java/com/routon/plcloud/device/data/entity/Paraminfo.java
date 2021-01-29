package com.routon.plcloud.device.data.entity;

import com.alibaba.fastjson.JSON;
import java.util.Date;

/**
 * @author FireWang
 * @date 2020/5/07 15:09
 */
public class Paraminfo {
	private Object id;
	private Object linkid;
	private String termsn;
	private Object paramid;
	private String paraminfo;
	private Date createtime;
	private Date modifytime;
	private Object status;
	private String remark;

	public void setId(Object id) {
		this.id = id;
	}

	public Object getId() {
		return id;
	}

	public void setLinkid(Object linkid) {
		this.linkid = linkid;
	}

	public Object getLinkid() {
		return linkid;
	}

	public void setTermsn(String termsn) {
		this.termsn = termsn;
	}

	public String getTermsn() {
		return termsn;
	}

	public void setParamid(Object paramid) {
		this.paramid = paramid;
	}

	public Object getParamid() {
		return paramid;
	}

	public void setParaminfo(String paraminfo) {
		this.paraminfo = paraminfo;
	}

	public String getParaminfo() {
		return paraminfo;
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
