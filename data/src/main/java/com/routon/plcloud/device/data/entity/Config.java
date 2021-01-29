package com.routon.plcloud.device.data.entity;

import com.alibaba.fastjson.JSON;
import java.util.Date;

/**
 * @author FireWang
 * @date 2020/5/07 15:09
 */
public class Config {
	private Object id;
	private String name;
	private Object type;
	private Object configid;
	private String configinfo;
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

	public void setConfigid(Object configid) {
		this.configid = configid;
	}

	public Object getConfigid() {
		return configid;
	}

	public void setConfiginfo(String configinfo) {
		this.configinfo = configinfo;
	}

	public String getConfiginfo() {
		return configinfo;
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
