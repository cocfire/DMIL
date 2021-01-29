package com.routon.plcloud.device.api.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author FireWang
 * @date 2020/5/29 10:29
 */
public class TimeUtil {

    /**
     * 时间戳转换成时间
     *
     * @param timestamp 传入的时间戳
     * @return 返回格式化时间
     */
    public static String timeStampToTime(String timestamp) {
        return timeStamp2Date(timestamp, "yyyy-MM-dd HH:mm:ss");
    }

    /**
     * 时间转换成时间戳
     *
     * @param time 传入的时间
     * @return 返回时间戳
     * @throws ParseException
     */
    public static String timeTotimeStamp(String time) throws ParseException {
        return date2TimeStamp(time, "yyyy-MM-dd HH:mm:ss");
    }

    /**
     * 时间戳转换成日期格式字符串
     *
     * @param seconds 精确到秒的字符串
     * @param format
     * @return
     */
    public static String timeStamp2Date(String seconds, String format) {

        String nullobj = "null", defult = "yyyy-MM-dd HH:mm:ss";
        if (seconds == null || seconds.isEmpty() || seconds.equals(nullobj)) {
            return "";
        }
        if (format == null || format.isEmpty()) {
            format = defult;
        }
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(new Date(Long.valueOf(seconds + "000")));
    }

    /**
     * 日期格式字符串转换成时间戳
     *
     * @param dateStr 字符串日期
     * @param format  如：yyyy-MM-dd HH:mm:ss
     * @return
     */
    public static String date2TimeStamp(String dateStr, String format) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(format);
            return String.valueOf(sdf.parse(dateStr).getTime() / 1000);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 取得当前时间戳（精确到秒）
     *
     * @return
     */
    public static String timeStamp() {
        long time = System.currentTimeMillis();
        String t = String.valueOf(time / 1000);
        return t;
    }
}
