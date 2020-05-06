package com.routon.plcloud.device.api.config;

import java.io.Serializable;

/**
 * @author FireWang
 * @date 2020/4/29 15:54
 * Serializable类用于将用户缓存信息(UserProfile)信息序列化，便于交给用户服务做校验
 */
public class UserProfile implements Serializable {
    //Alt+Enter自动添加，用于验证版本一致性的
    private static final long serialVersionUID = -2512376725821143773L;

    /**
     * 当前登录用户ID
     */
    protected Integer currentUserId;

    /**
     * 当前登录用户状态：0-失效；1-有效；
     */
    protected int currentUserstatus;

    /**
     * 当前登录用户账号
     */
    protected String currentUserLoginName;

    /**
     * 当前登录姓名
     */
    protected String currentUserRealName;

    /**
     * 当前登录用户登录Ip
     */
    protected String currentUserLoginIp;

    /**
     * 当前用户所属公司
     */
    protected String currentUserCompany;

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public Integer getCurrentUserId() {
        return currentUserId;
    }

    public void setCurrentUserId(Integer currentUserId) {
        this.currentUserId = currentUserId;
    }

    public int getCurrentUserstatus() {
        return currentUserstatus;
    }

    public void setCurrentUserstatus(int currentUserstatus) {
        this.currentUserstatus = currentUserstatus;
    }

    public String getCurrentUserLoginName() {
        return currentUserLoginName;
    }

    public void setCurrentUserLoginName(String currentUserLoginName) {
        this.currentUserLoginName = currentUserLoginName;
    }

    public String getCurrentUserRealName() {
        return currentUserRealName;
    }

    public void setCurrentUserRealName(String currentUserRealName) {
        this.currentUserRealName = currentUserRealName;
    }

    public String getCurrentUserLoginIp() {
        return currentUserLoginIp;
    }

    public void setCurrentUserLoginIp(String currentUserLoginIp) {
        this.currentUserLoginIp = currentUserLoginIp;
    }

    public String getCurrentUserCompany() {
        return currentUserCompany;
    }

    public void setCurrentUserCompany(String currentUserCompany) {
        this.currentUserCompany = currentUserCompany;
    }
}
