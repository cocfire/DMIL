package com.routon.plcloud.device.data.entity;

import com.alibaba.fastjson.JSON;
import java.util.Date;

/**
 * @author FireWang
 * @date 2020/5/07 15:09
 */
public class Programs {
	private Object id;
	private String name;
	private Object type;
	private String fileids;
	private String timelast;
	private Date starttime;
	private Date endtime;
	private Date createtime;
	private Date modifytime;
	private String fitmodel;
	private String remark;
	private Object createuser;
	private Object companyid;
	private Object projectid;

	public void setId(Object id) {
		this.id = id;
	}

	public Object getId() {
		return id;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setType(Object type) {
		this.type = type;
	}

	public Object getType() {
		return type;
	}

	public void setFileids(String fileids) {
		this.fileids = fileids;
	}

	public String getFileids() {
		return fileids;
	}

	public void setTimelast(String timelast) {
		this.timelast = timelast;
	}

	public String getTimelast() {
		return timelast;
	}

	public void setStarttime(Date starttime) {
		this.starttime = starttime;
	}

	public Date getStarttime() {
		return starttime;
	}

	public void setEndtime(Date endtime) {
		this.endtime = endtime;
	}

	public Date getEndtime() {
		return endtime;
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

	public void setFitmodel(String fitmodel) {
		this.fitmodel = fitmodel;
	}

	public String getFitmodel() {
		return fitmodel;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public String getRemark() {
		return remark;
	}

	public void setCreateuser(Object createuser) {
		this.createuser = createuser;
	}

	public Object getCreateuser() {
		return createuser;
	}

	public void setCompanyid(Object companyid) {
		this.companyid = companyid;
	}

	public Object getCompanyid() {
		return companyid;
	}

	public void setProjectid(Object projectid) {
		this.projectid = projectid;
	}

	public Object getProjectid() {
		return projectid;
	}


	@Override
	public String toString() {
	    return JSON.toJSONString(this);
    }

}
