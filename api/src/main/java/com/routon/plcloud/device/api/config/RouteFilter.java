package com.routon.plcloud.device.api.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * @author FireWang
 * @date 2020/4/29 13:14
 * 过滤器，用于过滤请求的url，判断用户登录状态
 */
@Component
public class RouteFilter implements Filter {
    //拦截日志
    private Logger logger = LoggerFactory.getLogger(RouteFilter.class);

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

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

            //跳过静态资源访问
            if(request.getRequestURI().startsWith(request.getContextPath() + "/static")){
                filterChain.doFilter(request, response);
                return;
            }

            //指定页面跳过过滤规则
            if(request.getRequestURI().equals("/DMIL/login")){//登录页面直接跳过
                filterChain.doFilter(request, response);
                return;
            }
            if(request.getRequestURI().equals("/DMIL/dologin")){//登录页面直接跳过
                filterChain.doFilter(request, response);
                return;
            }

            //接口访问跳过过滤规则
            if(request.getRequestURI().contains("/hello")){
                filterChain.doFilter(request, response);
                return;
            }

            logger.info("当前会话请求url："+ request.getRequestURI());

            //取出用户缓存做登录状态校验,校验不通过则直接跳转登录页
            UserProfile userProfile = (UserProfile) session.getAttribute("userProfile");
            if (userProfile == null || userProfile.getCurrentUserId() < 0) {//用户尚未登录，直接返回登录页
                logger.info("当前的用户会话认证无效，需重新登录........contextpath["+ request.getContextPath() +"]");
                //判断是否是ajax请求,是的话设置请求超时，直接返回登录页
                if( request.getHeader("x-requested-with") != null &&
                        request.getHeader("x-requested-with").equals("XMLHttpRequest") ) {
                    logger.info("ajax请求："+ request.getRequestURI());
                    response.setHeader("sessionStatus", "timeout");
                } else {
                    //该方法可实现跳转，但共用一个servlet，url不会变化
                    //request.getRequestDispatcher("/index").forward(request, response);

                    //该方法可实现跳转，从新生成一个servlet，url会变化
                    response.sendRedirect(request.getContextPath() + "/login");
                }
                return;
            } else {
                //已经有过登录记录的用户，判断是否失效
                if(userProfile.getCurrentUserstatus() != 1){
                    logger.info("当前的用户会话认证无效，需重新登录........contextpath["+ request.getContextPath() +"]");
                    response.sendRedirect(request.getContextPath() + "/login");
                    return;
                }

                //对于已经登录的用户，若访问登录页面路径，则返回导航页面
                if (request.getRequestURI().equals(request.getContextPath()) || request.getRequestURI().equals(request.getContextPath()+"/")) {
                    response.sendRedirect(request.getContextPath() + "/begin");
                    return;
                }

                //设置用户IP
                userProfile.setCurrentUserLoginIp(request.getRemoteAddr());
            }

            //过滤后，对访问连接进行处理
            filterChain.doFilter(request, response);
        } catch (Exception e){
            logger.info("过滤器异常：" + e.getMessage());
        }
    }

    @Override
    public void destroy() {

    }
}
