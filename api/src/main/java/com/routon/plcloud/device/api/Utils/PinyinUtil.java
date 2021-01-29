package com.routon.plcloud.device.api.utils;

import net.sourceforge.pinyin4j.PinyinHelper;

/**
 * @author FireWang
 * @date 2020/5/18 18:24
 * 主要用于将汉字转化为拼音，也可将任意字符串转换为唯一数字码
 */
public class PinyinUtil {
    /**
     * 获取汉字的拼音字符串
     *
     * @param str 汉字
     */
    public static String strToPinyin(String str) {
        String target = "";
        for (int i = 0; i < str.length(); i++) {
            String[] pinyin = PinyinHelper.toHanyuPinyinStringArray(str.charAt(i));
            for (String py : pinyin) {
                target = target + py;
            }
        }
        return target;
    }

    /**
     * 获取汉字的数字码
     *
     * @param str 目标字符串
     */
    public static int strToInt(String str) {
        int target = 0;
        for (int i = 0; i < str.length(); i++) {
            target = target + Integer.valueOf(str.charAt(i));
        }
        return target;
    }
}
