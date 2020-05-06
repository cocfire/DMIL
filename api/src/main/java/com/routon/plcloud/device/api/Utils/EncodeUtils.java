package com.routon.plcloud.device.api.Utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.apache.commons.codec.binary.Hex;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author FireWang
 * @date 2020/5/6 16:37
 * 定义编码相关的工具类
 */
@Component
public class EncodeUtils {
    private Logger logger = LoggerFactory.getLogger(EncodeUtils.class);

    /**
     * 获取密码的MD5戳
     *
     * @param password  密码
     * @return 密码的MD5戳
     */
    public String getPasswordMD5(String password) {
        MessageDigest messageDigest = null;
        byte[] digest = null;

        try {
            //获取报文摘要算法
            messageDigest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            logger.error(e.getMessage(), e);
        }

        if (messageDigest == null) {
            return password;
        }
        //
        digest = messageDigest.digest((password+"tvb").getBytes());


        return Hex.encodeHexString(digest);
    }
}
