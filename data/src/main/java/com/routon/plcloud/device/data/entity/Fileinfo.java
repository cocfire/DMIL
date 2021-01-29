package com.routon.plcloud.device.data.entity;

import com.alibaba.fastjson.JSON;
import java.util.Date;

/**
 * @author FireWang
 * @date 2020/5/07 15:09
 */
public class Fileinfo {
	private Object id;
	private String filename;
	private String filesize;
	private Object filetype;
	private String filepath;
	private String uploadname;
	private Date uploadtime;
	private Object downstatus;
	private String md5size;
	private String fitmodel;
	private String remark;
	private Object userid;

	public void setId(Object id) {
		this.id = id;
	}

	public Object getId() {
		return id;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilesize(String filesize) {
		this.filesize = filesize;
	}

	public String getFilesize() {
		return filesize;
	}

	public void setFiletype(Object filetype) {
		this.filetype = filetype;
	}

	public Object getFiletype() {
		return filetype;
	}

	public void setFilepath(String filepath) {
		this.filepath = filepath;
	}

	public String getFilepath() {
		return filepath;
	}

	public void setUploadname(String uploadname) {
		this.uploadname = uploadname;
	}

	public String getUploadname() {
		return uploadname;
	}

	public void setUploadtime(Date uploadtime) {
		this.uploadtime = uploadtime;
	}

	public Date getUploadtime() {
		return uploadtime;
	}

	public void setDownstatus(Object downstatus) {
		this.downstatus = downstatus;
	}

	public Object getDownstatus() {
		return downstatus;
	}

	public void setMd5size(String md5size) {
		this.md5size = md5size;
	}

	public String getMd5size() {
		return md5size;
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

	public void setUserid(Object userid) {
		this.userid = userid;
	}

	public Object getUserid() {
		return userid;
	}


	@Override
	public String toString() {
	    return JSON.toJSONString(this);
    }

}
