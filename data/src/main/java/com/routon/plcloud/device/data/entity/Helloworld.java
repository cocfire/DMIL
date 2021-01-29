package com.routon.plcloud.device.data.entity;

import com.alibaba.fastjson.JSON;

/**
 * @author FireWang
 * @date 2020/4/30 11:11
 */
public class Helloworld {

    private Object id;

    private String api;

    private String core;

    private String data;

    public void setId(Object id) {
        this.id = id;
    }

    public Object getId() {
        return id;
    }

    public void setApi(String api) {
        this.api = api;
    }

    public String getApi() {
        return api;
    }

    public void setCore(String core) {
        this.core = core;
    }

    public String getCore() {
        return core;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getData() {
        return data;
    }

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }

}
