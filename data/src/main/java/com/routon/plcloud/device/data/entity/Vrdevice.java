package com.routon.plcloud.device.data.entity;

import com.alibaba.fastjson.JSON;
import java.util.Date;

/**
 * @author FireWang
 * @date 2020/5/07 15:09
 */
public class Vrdevice {
	private Object id;
	private String vrdeviceid;
	private String name;
	private String type;
	private String info;
	private String remark;
	private Object status;
	private Date createtime;
	private Date updatetime;
	private String link;
	private String version;

	public void setId(Object id) {
		this.id = id;
	}

	public Object getId() {
		return id;
	}

	public void setVrdeviceid(String vrdeviceid) {
		this.vrdeviceid = vrdeviceid;
	}

	public Object getVrdeviceid() {
		return vrdeviceid;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getType() {
		return type;
	}

	public void setInfo(String info) {
		this.info = info;
	}

	public String getInfo() {
		return info;
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

	public void setVersion(String version) {
		this.version = version;
	}

	public String getVersion() {
		return version;
	}


	@Override
	public String toString() {
	    return JSON.toJSONString(this);
    }

}
