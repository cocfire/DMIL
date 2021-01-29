package com.routon.plcloud.device.data.entity;

import com.alibaba.fastjson.JSON;
import java.util.Date;

/**
 * @author FireWang
 * @date 2020/5/07 15:09
 */
public class Menu {
	private Object id;
	private String name;
	private Object rank;
	private Object pid;
	private String menuorder;
	private String path;
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

	public void setRank(Object rank) {
		this.rank = rank;
	}

	public Object getRank() {
		return rank;
	}

	public void setPid(Object pid) {
		this.pid = pid;
	}

	public Object getPid() {
		return pid;
	}

	public void setOrder(String menuorder) {
		this.menuorder = menuorder;
	}

	public String getOrder() {
		return menuorder;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getPath() {
		return path;
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
