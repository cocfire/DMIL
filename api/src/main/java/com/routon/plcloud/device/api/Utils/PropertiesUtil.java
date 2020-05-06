package com.routon.plcloud.device.api.Utils;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.ResourceBundle;

/**
 * @ClassName:PropertiesUtil
 * @Description:读取配置文件工具类
 * @Author:shiquan
 * @Date:2019/3/18 17:36
 **/
public class PropertiesUtil {
    private static Logger logger = LoggerFactory.getLogger(PropertiesUtil.class);

    /**
     * 通过属性key获取配置文件的属性值
     *
     * @param fileName
     * @param key
     * @return
     */
    public static String getDataFromPropertiseFile(String fileName, String key) {
        if (isEmpty(fileName)) {
            logger.info("getDataFromPropertiseFile fileName is null" );
            return null;
        }
        if (isEmpty(key)) {
            logger.info("getDataFromPropertiseFile key is null" );
            return null;
        }
        try {
            ResourceBundle resource = ResourceBundle.getBundle(fileName);
            if (resource == null) {
                logger.error(fileName + "配置文件不存在" );
                return null;
            }
            return resource.getString(key);
        } catch (Exception e) {
            logger.error(fileName + "配置文件获取参数异常,key=" + key);
            return null;
        }
    }

    /**
     * 通过属性key获取配置文件的属性值数组
     *
     * @return
     */
    public static String[] getPropertyStringArray(String fileName, String key) {
        if (isEmpty(fileName)) {
            logger.info("getDataFromPropertiseFile fileName is null" );
            return null;
        }
        if (isEmpty(key)) {
            logger.info("getDataFromPropertiseFile key is null" );
            return null;
        }
        try {
            ResourceBundle resource = ResourceBundle.getBundle(fileName);
            if (resource == null) {
                logger.error(fileName + "配置文件不存在" );
                return null;
            }
            return getStringArray(resource, key);
        } catch (Exception e) {
            logger.error(fileName + "配置文件获取参数异常,key=" + key);
            return null;
        }
    }

    /**
     * 获取配置文件中数组
     *
     * @param bundle
     * @param keyPrefix
     * @return
     */
    private static String[] getStringArray(ResourceBundle bundle, String keyPrefix) {
        String[] result;
        Enumeration<String> keys = bundle.getKeys();
        ArrayList<String> temp = new ArrayList<String>();

        // get the keys and add them in a temporary ArrayList
        for (Enumeration<String> e = keys; keys.hasMoreElements(); ) {
            String key = e.nextElement();
            if (key.startsWith(keyPrefix)) {
                temp.add(key);
            }
        }
        // create a string array based on the size of temporary ArrayList
        result = new String[temp.size()];

        // store the bundle Strings in the StringArray
        for (int i = 0; i < temp.size(); i++) {
            result[i] = bundle.getString(temp.get(i));
        }
        return result;
    }

    /**
     * 读取字符串配置项
     *
     * @param fileName
     * @param key
     * @return
     */
    public static String getString(String fileName, String key) {
        String val = getDataFromPropertiseFile(fileName, key);
        return val;
    }

    /**
     * 读取整形配置项
     *
     * @param fileName
     * @param key
     * @return
     */
    public static Integer getInteger(String fileName, String key) {
        String val = getDataFromPropertiseFile(fileName, key);
        try {
            Integer n = Integer.valueOf(val);
            return n;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 读取整形配置项
     *
     * @param fileName
     * @param key
     * @return
     */
    public static Long getLong(String fileName, String key) {
        String val = getDataFromPropertiseFile(fileName, key);
        try {
            Long n = Long.valueOf(val);
            return n;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 读取布尔型配置项
     *
     * @param fileName
     * @param key
     * @return
     */
    public static Boolean getBoolean(String fileName, String key) {
        String val = getDataFromPropertiseFile(fileName, key);
        if (val == null) {
            return null;
        }
        if (val.equalsIgnoreCase("true" )) {
            return Boolean.valueOf(true);
        }
        if (val.equalsIgnoreCase("false" )) {
            return Boolean.valueOf(false);
        }
        return null;
    }

    public static boolean isEmpty(String str) {
        return str == null || str.length() == 0;
    }

    public static void main(String[] args) {
        System.out.println(getDataFromPropertiseFile("application", "color" ));
        String[] str = getPropertyStringArray("scheduleTask", "action" );
        for (String string : str) {
            System.out.println(string);
        }
    }
}
