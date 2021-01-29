package com.routon.plcloud.device.api.utils;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * @author WZ
 */
public class Signfile {


    /**
     * 对文件filename签名。filetype为文件类型id。成功返回0，其它失败
     *
     * @param filename
     * @param filetype
     * @return
     */
    public int signfile(String filename, int filetype) {

        int fileDescLength = 128;
        int fileSafeDescLength = 2 + 2 + 4 + fileDescLength + 8;
        int fileExtBlockSize = fileSafeDescLength + 2 + 16 + 2;
        //标志位，返回结果
        int ret = -1;
        // 文件验证，取样数据长度
        int md5FileHitLen = (64 * 4);
        // 文件验证，取样间隔
        int md5FileHitSpan = (1024 * 20);
        byte[] buffer = new byte[fileExtBlockSize];

        int size;
        long filelen;
        byte[] tempBuf = new byte[md5FileHitLen];
        File file = new File(filename);
        FileSafeDesc desc = new FileSafeDesc(buffer);
        if (!file.exists()) {
            return ret;
        }
        // 文件长度，字节数
        filelen = file.length();
        RandomAccessFile rf;

        try {
            // 打开一个随机访问文件流，按读写方式
            rf = new RandomAccessFile(filename, "rw");
            desc.setDesc(filename);
            desc.setFlag(0x0100);
            desc.setExtLen(16);
            desc.setType(filetype);
            //filetype转byte数组
            byte[] typebytes = reverseArray(short2ByteNew((short) filetype));
            desc.setOrgSize((int) filelen);
            //164长度转byte
            short md5datalength = 164;
            byte[] bytemd5datalength = reverseArray(short2ByteNew(md5datalength));
            //原文件长度转byte数组
            byte[] oldlength = reverseArray(intToByteArray((int) filelen));
            //获取当前时间
            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
            desc.setTimeStamp(dateFormat.format(calendar.getTime()));
            //计算签名秘钥
            byte[] key = genKey((int) filelen + fileExtBlockSize, desc);
            //开始计算md5
            MessageDigest md;
            byte[] desResult1 = null;
            byte[] desResult2 = null;
            try {

                byte[] descbyte = new byte[fileDescLength];
                //路径地址
                String stradd = toHexString(desc.getDesc());
                //时间戳
                String strtime = toHexString(desc.getTimeStamp());
                //路径地址byte数组，固定128字节
                byte[] descbyte1 = HexStrUtil.hexToBytes(stradd);
                //时间戳byte数组
                byte[] descbyte2 = HexStrUtil.hexToBytes(strtime);
                System.arraycopy(descbyte1, 0, descbyte, 0, descbyte1.length);
                //拼接byte，固定0,1
                byte[] flag = {0, 1};
                //拼接buffer前144个字节
                System.arraycopy(typebytes, 0, buffer, 0, 2);
                System.arraycopy(flag, 0, buffer, 2, 2);
                System.arraycopy(oldlength, 0, buffer, 4, 4);
                System.arraycopy(descbyte, 0, buffer, 8, 128);
                System.arraycopy(descbyte2, 0, buffer, 136, 8);


                md = MessageDigest.getInstance("MD5");
                size = (int) desc.getOrgSize();
                //文件指针移动到文件头
                rf.seek(0);
                int offset = 0;
                while (size > md5FileHitSpan) {
                    rf.read(tempBuf, 0, md5FileHitLen);
                    md.update(tempBuf);
                    offset += md5FileHitSpan;
                    rf.seek(offset);
                    size -= md5FileHitSpan;
                }
                if (size > md5FileHitLen) {
                    size = md5FileHitLen;
                }
                byte[] tmpBuf = new byte[size];
                rf.read(tmpBuf, 0, size);
                md.update(tmpBuf);
                md.update(subBytes(buffer, 0, fileSafeDescLength));
                // MD5结果
                byte[] resultByteArray = md.digest();
                //md5数据分别存放
                byte[] sub1 = subBytes(resultByteArray, 0, 8);
                byte[] sub2 = subBytes(resultByteArray, 8, 8);
                //计算签名数据
                desResult1 = encryptKey(sub1, key);
                desResult2 = encryptKey(sub2, key);


                //文件指针移动到文件尾
                rf.seek(filelen);
                byte[] extlen = reverseArray(short2ByteNew((short) desc.getExtLen()));
                System.arraycopy(extlen, 0, buffer, 144, 2);
                System.arraycopy(desResult1, 0, buffer, 146, 8);
                System.arraycopy(desResult2, 0, buffer, 154, 8);
                System.arraycopy(bytemd5datalength, 0, buffer, 162, 2);


            } catch (NoSuchAlgorithmException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
                rf.close();
                return ret;
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                rf.close();
                return ret;
            }

            rf.write(buffer);
            System.out.println(rf.length());
            ret = 1;

            rf.close();

        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return ret;
    }


    /**
     * 反转数组
     *
     * @param array
     * @return
     */
    public static byte[] reverseArray(byte[] array) {
        byte[] newArray = new byte[array.length];
        for (int i = 0; i < newArray.length; i++) {
            newArray[i] = array[array.length - i - 1];
        }
        return newArray;
    }


    public static byte[] short2ByteNew(short x) {
        //定义byte
        byte high = (byte) (0x00FF & (x >> 8));
        byte low = (byte) (0x00FF & x);
        byte[] bytes = new byte[2];
        bytes[0] = high;
        bytes[1] = low;
        return bytes;
    }


    /**
     * 计算时间戳
     *
     * @return
     */
    public static byte[] getLocalTime() {
        long l = System.currentTimeMillis();
        Date date = new Date(l);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
        String time = dateFormat.format(date);

        byte[] timeByte = new byte[8];
        System.arraycopy(time.getBytes(), 0, timeByte, 0, time.getBytes().length);
        return timeByte;
    }


    /**
     * 将int转为byte数组
     *
     * @param i
     * @return
     */
    public static byte[] intToByteArray(int i) {
        byte[] result = new byte[4];
        // 由高位到低位
        result[0] = (byte) ((i >> 24) & 0xFF);
        result[1] = (byte) ((i >> 16) & 0xFF);
        result[2] = (byte) ((i >> 8) & 0xFF);
        result[3] = (byte) (i & 0xFF);
        return result;
    }

    /**
     * 转化字符串为十六进制编码
     */
    public static String toHexString(String s) {
        String str = "";
        for (int i = 0; i < s.length(); i++) {
            int ch = (int) s.charAt(i);
            String s4 = Integer.toHexString(ch);
            str = str + s4;
        }
        return str;
    }


    /**
     * @author WZ
     * type 文件类型id
     * flag 文件加密标志，此版本固定为0x0100，升级算法时修改此值
     * sizeOrg 原文件字节数
     * extLen  签名长度，此版本固定为16字节
     */
    public class FileSafeDesc {
        private int type;
        private int flag;
        private int sizeOrg;
        private String desc;
        private int extLen;
        private String timeStamp;

        public FileSafeDesc() {
            ;
        }

        public byte[] subBytes(byte[] src, int begin, int count) {
            byte[] bs = new byte[count];
            for (int i = begin; i < begin + count; i++) {
                bs[i - begin] = src[i];
            }
            return bs;
        }

        public FileSafeDesc(byte[] data) {
            byte[] subData = subBytes(data, 0, 2);
            type = byte2int(subData, 2);

            subData = subBytes(data, 2, 2);
            flag = byte2int(subData, 2);

            subData = subBytes(data, 4, 4);
            sizeOrg = byte2int(subData, 4);

            subData = subBytes(data, 144, 2);
            extLen = byte2int(subData, 2);
        }

        public void setType(int type) {
            this.type = type;
        }

        public int getType() {
            return type;
        }

        public void setFlag(int flag) {
            this.flag = flag;
        }

        public int getFlag() {
            return flag;
        }

        public void setOrgSize(int sizeOrg) {
            this.sizeOrg = sizeOrg;
        }

        public int getOrgSize() {
            return sizeOrg;
        }

        public String getDesc() {
            return desc;
        }

        public void setDesc(String filename) {
            this.desc = filename;
        }

        public int getExtLen() {
            return extLen;
        }

        public void setExtLen(int extLen) {
            this.extLen = extLen;
        }

        public String getTimeStamp() {
            return timeStamp;
        }

        public void setTimeStamp(String timeStamp) {
            this.timeStamp = timeStamp;
        }

    }


    /**
     * @param bt
     * @param key
     * @return
     * @throws Exception
     */
    private byte[] encryptKey(byte[] bt, byte[] key) throws Exception {
        byte[] encryptedTransferKey = null;

        DESKeySpec dks = new DESKeySpec(key);

        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
        SecretKey key2 = keyFactory.generateSecret(dks);
        Cipher cipher = Cipher.getInstance("DES/ECB/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, key2);
        encryptedTransferKey = cipher.doFinal(bt);

        return encryptedTransferKey;
    }

    private byte[] subBytes(byte[] src, int begin, int count) {
        byte[] bs = new byte[count];
        for (int i = begin; i < begin + count; i++) {
            bs[i - begin] = src[i];
        }
        return bs;
    }

    /**
     * @param size
     * @param desc
     * @return
     */
    private byte[] genKey(int size, FileSafeDesc desc) {
        byte[] key = new byte[8];
        int tmp = ~size;
        int len4 = 4;
        for (int i = 0; i < len4; i++) {
            key[i] = (byte) ((tmp >> (i * 8)) & 0xff);
        }
        short tmpShort = (short) desc.getType();
        tmpShort = (short) (65535 - tmpShort);
        tmp = (tmpShort << 16) + desc.getFlag();
        for (int i = 0; i < len4; i++) {
            key[i + 4] = (byte) ((tmp >> (i * 8)) & 0xff);
        }
        return key;
    }

    /**
     * @param tmp
     * @param len
     * @return
     */
    private int byte2int(byte[] tmp, int len) {
        int result = 0;
        for (int i = 0; i < len; i++) {
            result = (result << 8) + (tmp[len - i - 1] & 0xff);
        }

        return result;
    }
}
