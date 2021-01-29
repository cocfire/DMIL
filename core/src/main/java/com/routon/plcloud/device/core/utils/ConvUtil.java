package com.routon.plcloud.device.core.utils;

import org.apache.commons.lang.StringUtils;

import java.math.BigDecimal;

/**
 * @ClassName:ConvUtil
 * @Description:类型转换工具类
 * @Author:shiquan
 * @Date:2019/4/3 08:56
 **/
public class ConvUtil {
    /**
     * 对象转换成string
     *
     * @param obj
     * @return
     */
    public static String convToString(Object obj) {
        if (obj == null) {
            return "";
        }
        if (StringUtils.isEmpty(obj.toString())) {
            return "";
        }
        return obj.toString();
    }

    /**
     * 对象转换成string并去空格
     *
     * @param obj
     * @return
     */
    public static String convToStr(Object obj) {
        if (obj == null) {
            return "";
        } else {
            return obj.toString().trim();
        }
    }

    /**
     * 对象转换成utf-8的字符串
     *
     * @param obj
     * @return
     */
    public static String convToStringUtf8(Object obj) {
        if (obj == null) {
            return "";
        }
        if (StringUtils.isEmpty(obj.toString())) {
            return "";
        }
        try {
            return new String(obj.toString().getBytes(), "UTF-8");
        } catch (Exception e) {
            return obj.toString();
        }
    }

    /**
     * 转换换行符
     *
     * @param obj
     * @return
     */
    public static String convToEscapeString(Object obj) {
        if (obj == null) {
            return "";
        }
        if (StringUtils.isEmpty(obj.toString())) {
            return "";
        }
        return obj.toString().replace("\r\n", "<br>");
    }

    /**
     * 转换值为boolean型。
     *
     * @param obj 值
     * @return boolean型
     */
    public static boolean convToBool(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj instanceof byte[]) {
            return convBitToBool((byte[]) obj);
        }
        String one = "1", zero = "0", t = "true", f = "false";
        if (one.equals(obj.toString())) {
            return true;
        } else if (zero.equals(obj.toString())) {
            return false;
        } else if (t.equalsIgnoreCase(obj.toString())) {
            return true;
        } else if (f.equalsIgnoreCase(obj.toString())) {
            return false;
        }
        return false;
    }

    /**
     * 转换bit为boolean型。
     *
     * @param bytes byte
     * @return boolean型
     */
    public static boolean convBitToBool(byte[] bytes) {
        if (bytes == null) {
            return false;
        }
        return bytes[0] == 0x01 ? true : false;
    }

    /**
     * 转换成int
     *
     * @param value
     * @return
     */
    public static int convToInt(Object value) {
        int intValue = 0;
        if (value == null) {
            return intValue;
        } else {
            try {
                intValue = Integer.parseInt(value.toString().trim().replace(",", ""));
            } catch (NumberFormatException ex) {
                return intValue;
            }
        }
        return intValue;
    }

    /**
     * 转换成short
     *
     * @param value
     * @return
     */
    public static short convToShort(Object value) {
        short intValue = 0;
        if (value == null) {
            return intValue;
        } else {
            try {
                intValue = Short.parseShort(value.toString().trim().replace(",", ""));
            } catch (NumberFormatException ex) {
                return intValue;
            }
        }
        return intValue;
    }

    /**
     * 转换值为long型。
     *
     * @param value 值
     * @return int型
     */
    public static long convToLong(Object value) {
        long longValue = 0L;
        if (value == null) {
            return longValue;
        } else {
            try {
                longValue = Long.parseLong(value.toString().trim().replace(",", ""));
            } catch (NumberFormatException ex) {
                return longValue;
            }
        }
        return longValue;
    }

    /**
     * 转换值为double型。
     *
     * @param pValue 值
     * @return double型
     */
    public static double convToDouble(Object pValue) {
        double dblValue = 0D;
        if (pValue == null) {
            return dblValue;
        }
        try {
            dblValue = Double.parseDouble(String.valueOf(pValue).trim().replace(",", ""));
        } catch (Exception ex) {
            return dblValue;
        }
        return dblValue;
    }

    /**
     * 转换值为BigDecimal型。
     *
     * @param value 值
     * @return BigDecimal型
     */
    public static BigDecimal convToDecimal(Object value) {
        BigDecimal dec = new BigDecimal("0");
        if (value == null) {
            return dec;
        } else if (value instanceof BigDecimal) {
            dec = (BigDecimal) value;
        } else {
            try {
                dec = new BigDecimal(String.valueOf(value).trim().replace(",", ""));
            } catch (Exception ex) {
                return dec;
            }
        }
        return dec;
    }

    /**
     * @param pValue
     * @return
     */
    public static String convToMoney(Object pValue) {
        return String.format("%1$,.2f", convToDecimal(pValue));
    }


    public static void main(String[] args) {
        System.out.println(convToString(123));
        System.out.println(convToStringUtf8(234));
        System.out.println(convToEscapeString("\r\n"));
        System.out.println(convToInt("1234"));
        System.out.println(convToShort("123"));
        System.out.println(convToLong(123456789));
        System.out.println(convToDouble(123.4567));
        System.out.println(convToDecimal(100));
        System.out.println(convToMoney(1234567));
    }
}
