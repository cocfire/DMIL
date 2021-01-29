package com.routon.plcloud.device.api.utils;

import org.apache.commons.lang.StringUtils;

/**
 * @author FireWang
 * @date 2020/5/18 14:40
 */
public class HexStrUtil {

    public static String bytesToHex(byte[] array) {

        StringBuffer hexString = new StringBuffer();
        for (byte b : array) {
            int intVal = b & 0xff;
            if (intVal < 0x10) {
                hexString.append("0");
            }
            hexString.append(Integer.toHexString(intVal));
        }
        return hexString.toString();
    }

    public static byte[] hexToBytes(String hexString)
            throws NumberFormatException {

        int length = hexString.length();
        byte[] bytes = new byte[(length + 1) / 2];
        boolean evenByte = true;
        byte nextByte = 0;
        int bytesOffset = 0;

        int lentwo = 2, lenone = 1;
        if ((length % lentwo) == lenone) {
            evenByte = false;
        }
        for (int i = 0; i < length; i++) {
            char c = hexString.charAt(i);
            int nibble;
            if ((c >= '0') && (c <= '9')) {
                nibble = c - '0';
            } else if ((c >= 'A') && (c <= 'F')) {
                nibble = c - 'A' + 0x0A;
            } else if ((c >= 'a') && (c <= 'f')) {
                nibble = c - 'a' + 0x0A;
            } else {
                throw new NumberFormatException("Invalid hex digit '" + c
                        + "'.");
            }

            if (evenByte) {
                nextByte = (byte) (nibble << 4);
            } else {
                nextByte += (byte) nibble;
                bytes[bytesOffset++] = nextByte;
            }
            evenByte = !evenByte;

        }
        return bytes;
    }

    /**
     * 将身份证号码转换为16进制字符串
     *
     * @param id 身份证号码
     * @return 转换后内容，如失败则返回null，需判断
     */
    public static String id2HexStr(String id) {
        String strId = "";
        int len18 = 18, len15 = 15;
        boolean isId = StringUtils.isEmpty(id) || id.trim().length() != len18 && id.trim().length() != len15;
        if (isId) {
            return null;
        }

        String endx = "X";
        if (id.endsWith(endx)) {
            strId = id.trim().substring(0, id.length() - 1);
        } else {
            strId = id;
        }

        Long l = 0L;
        try {
            l = Long.parseLong(strId);
        } catch (NumberFormatException e) {
            // TODO Auto-generated catch block
            return null;
        }

        return String.format("%016X", l);
    }

    /**
     * 奇偶校验生成器
     *
     * @param data   目标码
     * @param offset 起始位
     * @param count  结束位
     * @return 一位返回校验码
     */
    private static byte calcCheckSum(byte[] data, int offset, int count) {
        byte checkSum = 0;
        int i;

        for (i = 0; i < count; i++) {
            checkSum ^= data[offset + i];
        }

        return checkSum;
    }

    /**
     * byte[]转int
     *
     * @param bytes
     * @return
     */
    public static int byteArrayToInt(byte[] bytes) {
        int value = 0;
        // 由高位到低位
        int len4 = 4;
        for (int i = 0; i < len4; i++) {
            int shift = (4 - 1 - i) * 8;
            // 往高位游
            value += (bytes[i] & 0x000000FF) << shift;
        }
        return value;
    }

    /**
     * 奇偶校验生成器--平台定制
     *
     * @param str 目标码
     * @return 返回完整的目标码
     */
    public static String appendChecksum(String str) {
        char[] base36dic = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '1', '3', '5', '7', '9', '0'};
        byte checksum = 0;
        for (byte b : str.getBytes()) {
            checksum ^= b;
        }
        int index = (checksum & (byte) 0x0f);
        return str + base36dic[index];
    }

    /**
     * 奇偶校验--平台定制
     *
     * @param str 目标码
     * @return 检验成功与否
     */
    public static boolean checkChecksum(String str) {
        return str.equals(appendChecksum(str.substring(0, str.length() - 1)));
    }
}
