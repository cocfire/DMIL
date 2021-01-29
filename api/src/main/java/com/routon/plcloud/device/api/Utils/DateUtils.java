package com.routon.plcloud.device.api.utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @ClassName:DateUtils
 * @Description:时间处理工具类
 * @Author:shiquan
 * @Date:2019/3/18 10:02
 **/
public class DateUtils {

    /**
     * 常用日期格式化模版
     */
    private static final String NORMAL_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private static final String NORMAL_DATE_FORMAT_YY_MM_DD = "yyyy-MM-dd";

    private static final String NORMAL_DATE_FORMAT_YY_MM_DD_N = "yyyy年MM月dd日";
    private static final String NORMAL_DATE_FORMAT_MM_DD = "MM月dd日";

    private static final String NORMAL_DATE_FORMAT_YY_MM_DD_ = "yyyyMMdd";
    private static final String NORMAL_DATE_FORMAT_YYMMDDHHMISS = "yyyyMMddHHmmss";

    private static final String NORMAL_DATE_FORMAT_HHMISS = "HHmmss";
    private static final String NORMAL_DATE_FORMAT_HH_MI_SS = "HH:mm:ss";

    private static final Map<String, SimpleDateFormat> DATE_FORMAT_STORE = new HashMap<String, SimpleDateFormat>();


    /**
     * 给定日期(当前时间)转日期格式字符串
     *
     * @param date
     * @return yyyy-MM-dd HH:mm:ss
     */
    public static String dateFormat(Date date) {
        return new SimpleDateFormat(NORMAL_DATE_FORMAT).format(date);
    }

    /**
     * 给定日期(当前时间)转日期格式字符串
     *
     * @param date
     * @return yyyyMMdd
     */
    public static String dateFormatToDay(Date date) {
        return new SimpleDateFormat(NORMAL_DATE_FORMAT_YY_MM_DD_).format(date);
    }

    /**
     * Date转指定格式String
     */
    public static String parseDate2StringByFormat(Date date, String formatType) {
        if (date == null) {
            return null;
        }
        if (formatType == null || "".equals(formatType)) {
            return null;
        }
        String dateStr = "";
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(formatType);
            dateStr = sdf.format(date);
            return dateStr;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 给定秒转日期格式字符串
     *
     * @param date
     * @return yyyy-MM-dd HH:mm:ss
     */
    public static String dateFormatToSecond(long date) {
        return new SimpleDateFormat(NORMAL_DATE_FORMAT).format(new Date(date * 1000));
    }

    /**
     * 给定秒转日期格式字符串
     *
     * @param date
     * @return yyyy-MM-dd
     */
    public static String dateFormatToLinkDate(long date) {
        return new SimpleDateFormat(NORMAL_DATE_FORMAT_YY_MM_DD).format(new Date(date * 1000));
    }

    /**
     * 给定秒转日期格式字符串
     *
     * @param date
     * @return yyyy年MM月dd日
     */
    public static String dateFormatToChinaDate(long date) {
        return new SimpleDateFormat(NORMAL_DATE_FORMAT_YY_MM_DD_N).format(new Date(date * 1000));
    }

    /**
     * String转Date
     *
     * @param dateStr
     * @return
     */
    public static Date parseString2Date(String dateStr) {
        String timeFormat = "yyyy-MM-dd";
        String divide = ":", link = "-", blank = " ";
        if (dateStr.indexOf(divide) == -1 && dateStr.indexOf(blank) > 0) {
            timeFormat = "yyyy-MM-dd HH";
        } else if (dateStr.indexOf(divide) > 0 && dateStr.indexOf(divide) == dateStr.lastIndexOf(divide)) {
            timeFormat = "yyyy-MM-dd HH:mm";
        } else if (dateStr.indexOf(divide) < dateStr.lastIndexOf(divide)) {
            timeFormat = "yyyy-MM-dd HH:mm:ss";
        } else if (dateStr.indexOf(link) == dateStr.lastIndexOf(link)) {
            timeFormat = "yyyy-MM";
        } else {
            timeFormat = "yyyy-MM-dd";
        }

        return parseString2DateByFormat(dateStr, timeFormat);
    }

    /**
     * 根据指定格式解析时间格式字符串
     *
     * @param dateString
     * @param fmtString
     * @return Date
     * @author mengxm
     */
    public static final Date parseString2DateByFormat(String dateString, String fmtString) {
        Date date = null;
        try {
            DateFormat format = new SimpleDateFormat(fmtString);
            date = format.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    /**
     * String转Long
     *
     * @param dateStr
     * @return
     */
    public static Long parseString2Long(String dateStr) {
        return parseString2Date(dateStr).getTime() / 1000;
    }

    /**
     * 获取当前距1970年1月1日秒数.
     *
     * @return 当前距1970年1月1日秒数.
     */
    public static long getTime() {
        return System.currentTimeMillis() / 1000;
    }

    /**
     * 获取当前距1970年1月1日毫秒数.
     *
     * @return 当前距1970年1月1日毫秒数.
     */
    public static long getTimestamp() {
        return System.currentTimeMillis();
    }

    /**
     * 取得AP系统时间。
     *
     * @return AP系统时间
     */
    public static Date getSystemDate() {
        return new Date();
    }

    /**
     * 获取当前距1970年1月1日秒数.
     *
     * @return 获取最近一周的秒数.
     */
    public static long getLastWeekTime() {
        return System.currentTimeMillis() / 1000 - 7 * 24 * 60 * 60;
    }

    /**
     * 获取当前距1970年1月1日秒数.
     *
     * @return 获取最近一个月的秒数.
     */
    public static long getLastMonthTime() {
        return System.currentTimeMillis() / 1000 - 30 * 24 * 60 * 60;
    }

    /**
     * 获取某年某月的第一天
     *
     * @param year
     * @param month
     * @return
     */
    public static Date getFirstDayOfMonth(String year, String month) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, Integer.valueOf(year));
        cal.set(Calendar.MONTH, Integer.valueOf(month) - 1);
        cal.set(Calendar.DAY_OF_MONTH, cal.getMinimum(Calendar.DATE));
        return cal.getTime();
    }

    /**
     * 获取某年某月的最后一天
     *
     * @param year
     * @param month
     * @return
     */
    public static Date getLastDayOfMonth(String year, String month) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, Integer.valueOf(year));
        cal.set(Calendar.MONTH, Integer.valueOf(month) - 1);
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
        return cal.getTime();
    }

    /**
     * 获得前一天的开始时间
     *
     * @param daytime
     * @return
     */
    public static long getLastDayStartTime(long daytime) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(daytime * 1000);
        cal.add(Calendar.DAY_OF_YEAR, -1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis() / 1000;
    }

    /**
     * 获取前一天的结束时间
     *
     * @param daytime
     * @return
     */
    public static long getLastDayEndTime(long daytime) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(daytime * 1000);
        cal.add(Calendar.DAY_OF_MONTH, -1);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 999);
        return cal.getTimeInMillis() / 1000;
    }

    /**
     * 比较两天是否是同一天
     *
     * @return
     */
    public static boolean compareSameDay(Date date1, Date date2) {
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        cal1.setTime(date1);
        cal2.setTime(date2);
        boolean isSameYear = cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR);
        boolean isSameMonth = cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH);
        boolean isSameDay = cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
        if (isSameYear && isSameMonth && isSameDay) {
            return true;
        }
        return false;
    }

    /**
     * 当前时间增加几天
     *
     * @param days
     * @return
     */
    public static long currentDateAddDays(int days) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(getTimestamp());
        cal.add(Calendar.DAY_OF_YEAR, days);
        return cal.getTimeInMillis() / 1000;
    }

    /**
     * 指定时间增加几天
     *
     * @param dateLong
     * @param day
     * @return
     */
    public static long dateAddDay(long dateLong, int day) {
        Calendar cal = Calendar.getInstance();
        Date d = new Date(dateLong * 1000);
        cal.setTime(d);
        cal.add(Calendar.DATE, day);
        return cal.getTimeInMillis() / 1000;
    }

    /**
     * 获取前后时间的相差天数
     *
     * @param stateDate 开始时间
     * @param endDate   结束时间
     * @return 前后时间的日差
     */
    public static int dateDiff(Date stateDate, Date endDate) {
        return (int) ((endDate.getTime() - stateDate.getTime()) / (1000 * 60 * 60 * 24));
    }

    /**
     * 获取给定日期年份
     *
     * @param date
     * @return
     */
    public static int getYear(Date date) {
        if (date == null) {
            return 0;
        }
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal.get(Calendar.YEAR);
    }

    /**
     * 获取给定日期月份
     *
     * @param date
     * @return
     */
    public static int getMonth(Date date) {
        if (date == null) {
            return 0;
        }
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal.get(Calendar.MONTH) + 1;
    }

    /**
     * 获取给定日期天
     *
     * @param date
     * @return
     */
    public static int getDay(Date date) {
        if (date == null) {
            return 0;
        }
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal.get(Calendar.DAY_OF_YEAR);
    }

    /**
     * 获取给定日期小时
     *
     * @param date
     * @return
     */
    public static int getHour(Date date) {
        if (date == null) {
            return 0;
        }
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal.get(Calendar.HOUR_OF_DAY);
    }

    /**
     * 获取给定日期分钟
     *
     * @param date
     * @return
     */
    public static int getMinute(Date date) {
        if (date == null) {
            return 0;
        }
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal.get(Calendar.MINUTE);
    }

    /**
     * 获取给定日期秒
     *
     * @param date
     * @return
     */
    public static int getSecond(Date date) {
        if (date == null) {
            return 0;
        }
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal.get(Calendar.SECOND);
    }

    /**
     * 指定日期加减去几年
     *
     * @param date
     * @param year
     * @return
     */
    public static Date addYear(Date date, int year) {
        if (date == null) {
            return null;
        }
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.YEAR, year);
        return cal.getTime();
    }

    /**
     * 指定日期加减几月
     *
     * @param date
     * @param month
     * @return
     */
    public static Date addMonth(Date date, int month) {
        if (date == null) {
            return null;
        }
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.MONTH, month);
        return cal.getTime();
    }

    /**
     * 指定日期加减几天
     *
     * @param date
     * @param day
     * @return
     */
    public static Date addDay(Date date, int day) {
        if (date == null) {
            return null;
        }
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.DAY_OF_YEAR, day);
        return cal.getTime();
    }

    /**
     * 指定日期加减几小时
     *
     * @param date
     * @param hour
     * @return
     */
    public static Date addHour(Date date, int hour) {
        if (date == null) {
            return null;
        }
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.HOUR_OF_DAY, hour);
        return cal.getTime();
    }

    /**
     * 指定日期加减几分钟
     *
     * @param date
     * @param minute
     * @return
     */
    public static Date addMinute(Date date, int minute) {
        if (date == null) {
            return null;
        }
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.MINUTE, minute);
        return cal.getTime();
    }

    /**
     * 指定日期加减几秒
     *
     * @param date
     * @param second
     * @return
     */
    public static Date addSecond(Date date, int second) {
        if (date == null) {
            return null;
        }
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.SECOND, second);
        return cal.getTime();
    }

    /**
     * 获取当前月第一天
     *
     * @return
     */
    public static String getMonthFristDay() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMinimum(Calendar.DAY_OF_MONTH));
        return parseDate2StringByFormat(cal.getTime(), "yyyy-MM-dd");
    }

    /**
     * 获取当前月最后一天
     *
     * @return
     */
    public static String getMonthLastDay() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
        return parseDate2StringByFormat(cal.getTime(), "yyyy-MM-dd");
    }

    /**
     * 日期比较大小
     *
     * @param date1
     * @param date2
     * @return 1 ： DATE1 大  -1： DATE2 大 0： 相等
     */
    public static int compareDate(String date1, String date2) {
        try {
            Date dt1 = new SimpleDateFormat(NORMAL_DATE_FORMAT_YY_MM_DD).parse(date1);
            Date dt2 = new SimpleDateFormat(NORMAL_DATE_FORMAT_YY_MM_DD).parse(date2);
            if (dt1.getTime() > dt2.getTime()) {
                return 1;
            } else if (dt1.getTime() < dt2.getTime()) {
                return -1;
            } else {
                return 0;
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return 0;
    }

    /**
     * @Description：获取输入日期的前一天
     * @Date：
     * @strData：参数格式：yyyy-MM-dd
     * @return：返回格式：yyyy-MM-dd
     */
    public static String getPreDateByDate(String strData) {
        String preDate = "";
        Calendar c = Calendar.getInstance();
        SimpleDateFormat sdf =new SimpleDateFormat("yyyy-MM-dd");
        Date date = null;
        try {
            date = sdf.parse(strData);
        } catch (java.text.ParseException e) {
            e.printStackTrace();
        }

        c.setTime(date);
        int day1 = c.get(Calendar.DATE);
        c.set(Calendar.DATE, day1 - 1);
        preDate = sdf.format(c.getTime());
        return preDate;
    }


    public static void main(String[] args) throws ParseException {
        System.out.println(dateFormat(new Date()));
        System.out.println(dateFormatToDay(new Date()));
        System.out.println(parseDate2StringByFormat(new Date(), "yyyy-MM-dd HH:mm:ss"));

        System.out.println(dateFormatToSecond(1545963396));
        System.out.println(dateFormatToLinkDate(1545963396));
        System.out.println(dateFormatToChinaDate(1545963396));

        System.out.println(parseString2Date("2018-12-28 12:12:12"));
        System.out.println(parseString2DateByFormat("2018-12-28 12:12:12", "yyyy-MM-dd HH:mm:ss"));
        System.out.println(parseString2Long("2018-12-28 12:12:12"));

        System.out.println(getTime());
        System.out.println(getTimestamp());
        System.out.println(dateFormat(getSystemDate()));
        System.out.println(getLastWeekTime());
        System.out.println(getLastMonthTime());

        System.out.println(getFirstDayOfMonth("2018", "11"));
        System.out.println(getLastDayOfMonth("2018", "2"));

        System.out.println(getLastDayStartTime(1545963396));
        System.out.println(getLastDayEndTime(1545963396));

        System.out.println(compareSameDay(new Date(), new Date()));

        System.out.println(currentDateAddDays(2));
        System.out.println(dateAddDay(getTime(), 2));

        System.out.println(dateDiff(new Date(getTimestamp()), new Date(currentDateAddDays(5) * 1000)));

        Date date = new Date();
        System.out.println(getYear(date) + "-" + getMonth(date) + "-" + getDay(date) + " " + getHour(date) + ":" + getMinute(date) + ":" + getSecond(date));

        System.out.println(getMonthFristDay());
        System.out.println(getMonthLastDay());

        System.out.println(compareDate("2018-11-11", "2018-10-10"));
    }
}
