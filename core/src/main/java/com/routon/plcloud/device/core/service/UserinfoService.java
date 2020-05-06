package com.routon.plcloud.device.core.service;

import com.routon.plcloud.device.data.entity.Userinfo;

/**
 * @author FireWang
 * @date 2020/4/26 18:59
 */
public interface UserinfoService {

    //查询第一条即管理员记录
    Userinfo getadmin();

    //修改信息，如修改密码
    boolean saveadmin(Userinfo user);
}
