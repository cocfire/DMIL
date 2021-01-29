package com.routon.plcloud.device.api.config;

import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * @author FireWang
 * @date 2020/4/29 13:14
 * 过滤器，用于过滤请求的url，判断用户登录状态
 */
@Component
public class RouteFilter implements Filter {
    /**
     * 拦截日志
     */
    private Logger logger = LoggerFactory.getLogger(RouteFilter.class);

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void destroy() {

    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {
        try {
            HttpServletRequest request = (HttpServletRequest) servletRequest;
            HttpServletResponse response = (HttpServletResponse) servletResponse;
            HttpSession session = request.getSession();

            /**
             * 示例页面路径：/DMIL/login/index.html
             * request.getRequestURI()返回：/DMIL/login/index.html
             * request.getContextPath()返回：/DMIL
             * request.getServletPath()返回：/login/index.html
             */

            //过滤器白名单，白名单内的请求可直接访问
            if (dofiltration(request)) {
                filterChain.doFilter(request, response);
                return;
            }

            //虚拟设备管理，直接跳过
            String vrdevice = "/vrdevice", vrdevicei = "/vrdevice/";
            if (request.getRequestURI().contains(vrdevicei)) {
                filterChain.doFilter(request, response);
                return;
            } else if (request.getRequestURI().contains(vrdevice)) {
                response.sendRedirect(request.getContextPath() + "/vrdevice/start");
                return;
            }

            logger.info("当前会话请求url：" + request.getRequestURI());

            //取出用户缓存做登录状态校验,校验不通过则直接跳转登录页
            UserProfile userProfile = (UserProfile) session.getAttribute("userProfile");
            //用户尚未登录，直接返回登录页
            if (userProfile == null || userProfile.getCurrentUserId() < 0) {
                logger.info("当前的用户会话认证无效，需重新登录........contextpath[" + request.getContextPath() + "]");
                //判断是否是ajax请求,是的话设置请求超时，直接返回登录页
                String requested = "x-requested-with";
                if (request.getHeader(requested) != null &&
                        "XMLHttpRequest".equals(request.getHeader(requested))) {
                    logger.info("ajax请求：" + request.getRequestURI());
                    setTimeOut(response);
                } else {
                    //方法2可实现跳转，从新生成一个servlet，url会变化
                    response.sendRedirect(request.getContextPath() + "/login");
                }
                return;
            } else {
                //已经有过登录记录的用户，判断是否失效
                if (userProfile.getCurrentUserstatus() != 1) {
                    logger.info("当前的用户会话认证无效，需重新登录........contextpath[" + request.getContextPath() + "]");
                    response.sendRedirect(request.getContextPath() + "/login");
                    return;
                }

                //对于已经登录的用户，若访问登录页面路径，则返回设备管理
                if (request.getRequestURI().equals(request.getContextPath()) ||
                        request.getRequestURI().equals(request.getContextPath() + "/")) {
                    response.sendRedirect(request.getContextPath() + "/device/list");
                    return;
                }

                //设置用户IP
                userProfile.setCurrentUserLoginIp(request.getRemoteAddr());
            }

            //过滤后，对访问连接进行处理
            filterChain.doFilter(request, response);
        } catch (Exception e) {
            logger.info("过滤器异常：" + e.getMessage());
        }
    }

    /**
     * ajax请求超时处理
     *
     * @param response
     */
    private void setTimeOut(HttpServletResponse response) {
        try {
            JSONObject json = new JSONObject();
            json.put("obj1", "登录超时，请重新登录！");
            response.setCharacterEncoding("utf-8");
            response.setContentType("application/json;charset=utf-8");
            PrintWriter out = null;
            out = response.getWriter();
            out.write(json.toString());
        } catch (Exception e) {
            logger.info("过滤器异常：" + e.getMessage());
            e.printStackTrace();
            response.setHeader("sessionStatus", "timeout");
        }
    }

    /**
     * 接口访问跳过过滤规则
     *
     * @param request
     */
    private boolean dofiltration(HttpServletRequest request) {
        /**
         * 示例页面路径：/DMIL/login/index.html
         * request.getRequestURI()返回：/DMIL/login/index.html
         * request.getContextPath()返回：/DMIL
         * request.getServletPath()返回：/login/index.html
         */
        try {
            //跳过静态资源访问
            String staticpath = request.getContextPath() + "/static";

            //登录页面跳过过滤规则
            String login = "/DMIL/login", dologin = "/DMIL/dologin", dologout = "/DMIL/dologout";

            //接口访问跳过过滤规则--EMQ软件下载接口
            String softwaredownload = "/device/softwaredownload";

            //接口访问跳过过滤规则--节目单文件下载
            String filedownload = "/file/downFile";

            //接口访问跳过过滤规则--深云智慧设备接口
            String deepcloud = "/deepcloud/";

            //接口访问跳过过滤规则--巨龙设备接口
            String dragons = "/dragons/";

            //接口访问跳过过滤规则--仁硕设备接口
            String rsnetdevice = "/rsnetdevice/";

            //接口访问跳过过滤规则--旷视设备接口
            String megvii = "/megvii/";

            //接口访问跳过过滤规则--WebService接口
            String webService = "/WebService/";

            if (request.getRequestURI().startsWith(staticpath) ||
                    request.getRequestURI().equals(login) ||
                    request.getRequestURI().equals(dologin) ||
                    request.getRequestURI().equals(dologout) ||

                    request.getRequestURI().contains(softwaredownload) ||
                    request.getRequestURI().contains(filedownload) ||

                    request.getRequestURI().contains(deepcloud) ||
                    request.getRequestURI().contains(dragons) ||
                    request.getRequestURI().contains(rsnetdevice) ||
                    request.getRequestURI().contains(megvii) ||

                    request.getRequestURI().contains(webService)) {
                return true;
            }
        } catch (Exception e) {
            logger.info("过滤器异常：" + e.getMessage());
            e.printStackTrace();
            return false;
        }
        return false;
    }

}

