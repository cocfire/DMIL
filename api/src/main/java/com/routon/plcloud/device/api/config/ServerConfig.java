package com.routon.plcloud.device.api.config;

import com.routon.plcloud.device.core.utils.PropertiesUtil;
import org.springframework.boot.web.context.WebServerInitializedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author FireWang
 * @date 2020/5/26 13:21
 * 服务配置信息获取类
 */
@Component
public class ServerConfig implements ApplicationListener<WebServerInitializedEvent> {
    private int serverPort;

    /** 获取服务的ip和端口号
     *
     * @return
     */
    public String getHostUrl() {
        InetAddress address = null;
        try {
            address = InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return "http://" + address.getHostAddress() + ":" + this.serverPort;
    }

    @Override
    public void onApplicationEvent(WebServerInitializedEvent event) {
        this.serverPort = event.getWebServer().getPort();
    }

    /**
     * 获取请求ip
     */
    public String getIpAddr(HttpServletRequest request) {
        String ip = request.getHeader(" x-forwarded-for " );
        String unknown = "unknown";
        if (ip == null || ip.length() == 0 || unknown.equalsIgnoreCase(ip)) {
            ip = request.getHeader(" Proxy-Client-IP " );
        }
        if (ip == null || ip.length() == 0 || unknown.equalsIgnoreCase(ip)) {
            ip = request.getHeader(" WL-Proxy-Client-IP " );
        }
        if (ip == null || ip.length() == 0 || unknown.equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

    /**
     * 获取系统版本信息
     *
     * @return
     */
    public Map<String, String> getVersionInfo() {
        Map<String, String> versionInfo = new HashMap<>(16);
        String version = PropertiesUtil.getDataFromPropertiseFile("versionInfo", "version");
        String update = PropertiesUtil.getDataFromPropertiseFile("versionInfo", "update");
        versionInfo.put("version", version);
        versionInfo.put("update", update);
        return versionInfo;
    }
}