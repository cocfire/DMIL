package com.routon.plcloud.device.api.Utils;

import com.alibaba.fastjson.JSONObject;
import org.apache.http.*;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.LayeredConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicHeaderElementIterator;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;

/**
 * @ClassName:HttpClientUtil
 * @Description:发送请求工具类
 * @Author:wanghzao
 * @Date:2020/5/6 21:09
 **/
public class HttpClientUtil {
    private final static Logger logger = Logger.getLogger(HttpClientUtil.class);
    private static HttpClientUtil httpClientUtil = new HttpClientUtil();
    private PoolingHttpClientConnectionManager connManager;
    private CloseableHttpClient httpclient;
    private RequestConfig requestConfig;

    public static HttpClientUtil getInstance() {
        return httpClientUtil;
    }


    private HttpClientUtil() {
        try {
            SSLContext sslCtx = SSLContext.getInstance("TLS" );
            X509TrustManager trustManager = new X509TrustManager() {
                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }

                public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
                }

                public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
                }
            };
            sslCtx.init(null, new TrustManager[]{trustManager}, null);
            LayeredConnectionSocketFactory sslSocketFactory = new SSLConnectionSocketFactory(sslCtx, SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
            RegistryBuilder<ConnectionSocketFactory> registryBuilder = RegistryBuilder.<ConnectionSocketFactory>create();
            ConnectionSocketFactory plainSocketFactory = new PlainConnectionSocketFactory();
            registryBuilder.register("http", plainSocketFactory);
            registryBuilder.register("https", sslSocketFactory);

            Registry<ConnectionSocketFactory> registry = registryBuilder.build();
            // 设置连接管理器
            connManager = new PoolingHttpClientConnectionManager(registry);
            // 指定cookie存储对象
            BasicCookieStore cookieStore = new BasicCookieStore();
            // 连接保持活跃策略
            ConnectionKeepAliveStrategy myStrategy = new ConnectionKeepAliveStrategy() {
                public long getKeepAliveDuration(HttpResponse response, HttpContext context) {
                    // 获取'keep-alive'HTTP报文头
                    HeaderElementIterator it = new BasicHeaderElementIterator(response.headerIterator(HTTP.CONN_KEEP_ALIVE));
                    while (it.hasNext()) {
                        HeaderElement he = it.nextElement();
                        String param = he.getName();
                        String value = he.getValue();
                        if (value != null && param.equalsIgnoreCase("timeout" )) {
                            try {
                                return Long.parseLong(value) * 1000;
                            } catch (NumberFormatException ignore) {
                            }
                        }
                    }
                    // 保持65秒活跃
                    return 65 * 1000;
                }
            };
            httpclient = HttpClientBuilder.create().setDefaultCookieStore(cookieStore).setConnectionManager(connManager).setKeepAliveStrategy(myStrategy).build();
            requestConfig = RequestConfig.custom().setConnectTimeout(60000).setSocketTimeout(60000).build();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    /**
     * 基本的Get请求
     *
     * @param url            请求URL
     * @param nameValuePairs 请求List<NameValuePair>查询参数
     * @return
     */
    public String doGet(String url, List<NameValuePair> nameValuePairs) throws Exception {
        byte[] resp = this.doGetByte(url, nameValuePairs);
        return new String(resp, "utf-8" );
    }

    public byte[] doGetByte(String url, List<NameValuePair> nameValuePairs) {
        logger.info("get send url:" + url);
        CloseableHttpResponse response = null;
        HttpGet httpget = new HttpGet();
        try {
            URIBuilder builder = new URIBuilder(url);
            // 填入查询参数
            if (nameValuePairs != null && !nameValuePairs.isEmpty()) {
                builder.setParameters(nameValuePairs);
            }
            httpget.setURI(builder.build());
            httpget.setConfig(requestConfig);

            response = httpclient.execute(httpget);
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                byte[] respMsg = EntityUtils.toByteArray(entity);
                logger.info("==> 响应信息: " + new String(respMsg, "utf-8" ));
                return respMsg;
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            if (response != null) {
                try {
                    response.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    /**
     * 基本的Post请求
     *
     * @param url
     * @param formParams
     * @param formParams
     * @return
     */
    public String doPost(String url, List<NameValuePair> formParams) {
        logger.info("post send url:" + url);
        CloseableHttpResponse response = null;
        HttpPost httppost = new HttpPost();
        try {
            URIBuilder builder = new URIBuilder(url);
            httppost.setURI(builder.build());
            httppost.setConfig(requestConfig);
            if (formParams != null && !formParams.isEmpty()) {
                httppost.setEntity(new UrlEncodedFormEntity(formParams, "UTF-8" ));
            }
            response = httpclient.execute(httppost);
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                String respMsg = EntityUtils.toString(entity);
                logger.info("==> 响应信息: " + respMsg);
                return respMsg;
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            httppost.releaseConnection();
            if (response != null) {
                try {
                    response.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    /**
     * 基本的Post请求
     *
     * @param url
     * @param formParams
     * @param formParams
     * @return
     */
    public String doPostForJson(String url, JSONObject formParams) {
        logger.info("post send url:" + url);
        CloseableHttpResponse response = null;
        HttpPost httppost = new HttpPost();
        try {
            URIBuilder builder = new URIBuilder(url);
            httppost.setURI(builder.build());
            httppost.setHeader("Content-Type", "application/json" );
            httppost.setConfig(requestConfig);
            StringEntity se = new StringEntity(formParams.toString(), "utf-8" );
            if (formParams != null && !formParams.isEmpty()) {
                httppost.setEntity(se);
            }
            response = httpclient.execute(httppost);
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                String respMsg = EntityUtils.toString(entity);
                logger.info("==> 响应信息: " + respMsg);
                return respMsg;
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            httppost.releaseConnection();
            if (response != null) {
                try {
                    response.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }


}
