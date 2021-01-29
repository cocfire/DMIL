package com.routon.plcloud.device.api.utils;

import org.apache.commons.codec.binary.Hex;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.Arrays;

/**
 * @author FireWang
 * @date 2020/8/21 17:26
 * 图片处理工具类：主要用于格式判断
 */
public class FileTypeUtil {
    /**
     * 枚举图片格式集合
     */
    public enum ImageFileTypeEnum {
        //图片格式
        JPG("ffd8ffe000104a464946" ),
        PNG("89504e470d0a1a0a0000" ),
        GIF("47494638396126026f01" ),
        //16色位图(bmp)
        BMP_1("424d228c010000000000" ),
        //24位位图(bmp)
        BMP_2("424d8240090000000000" ),
        //256色位图(bmp)
        BMP("424d8e1b030000000000" ),
        TIF("49492a00d8910300803f"),
        TIF_1("49492a00227105008037" );

        private String value = "";

        private ImageFileTypeEnum(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }

    /**
     * 枚举影音文件格式集合
     */
    public enum VideoFileTypeEnum {
        //视频格式
        RMVB("2e524d46000000120001" ),
        RM("2e524d46000000120001" ),
        FLV("464c5601050000000900" ),
        F4V("464c5601050000000900" ),
        MP4("00000020667479706973" ),
        MP4_1("0000001c667479706973" ),
        MP4_2("00000020667479706d70" ),
        MPG("000001ba210001000180" ),
        WMV("3026b2758e66cf11a6d9" ),
        ASF("3026b2758e66cf11a6d9" ),
        WAV("52494646e27807005741" ),
        AVI("52494646d07d60074156" ),
        MOV("6D6F6F76" ),

        //音频格式
        MID("4d546864000000060001" ),
        MP3("49443303000000002176" );

        private String value = "";

        private VideoFileTypeEnum(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }

    /**
     * 枚举办公文档格式集合
     */
    public enum OfficeFileTypeEnum {
        //文档格式
        RTF("7b5c727466315c616e73" ),
        EML("46726f6d3a203d3f6762" ),
        DOC("d0cf11e0a1b11ae10000" ),
        DOCX("504b0304140006000800" ),
        VSD("d0cf11e0a1b11ae10000" ),
        MDB("5374616E64617264204A" ),
        WPS("d0cf11e0a1b11ae10000" ),
        WPD("FF575043" ),
        CHM("49545346030000006000" ),
        DBX("CFAD12FEC5FD746F" ),
        PST("2142444E" ),
        PWL("E3828596" );

        private String value = "";

        private OfficeFileTypeEnum(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }

    /**
     * 枚举压缩文件格式集合
     */
    public enum CompressFileTypeEnum {
        //压缩文件格式
        GZ("1f8b0800000000000000" ),
        ZIP("504b0304140000000800" ),
        RAR("526172211a0700cf9073" ),
        INI("235468697320636f6e66" ),
        JAR("504b03040a0000000000" );

        private String value = "";

        private CompressFileTypeEnum(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }

    /**
     * 枚举运行文件格式集合
     */
    public enum RunFileTypeEnum {
        //运行文件格式
        MXP("04000000010000001300" ),
        TORRENT("6431303a637265617465" ),
        EXE("4d5a9000030000000400" );

        private String value = "";

        private RunFileTypeEnum(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }

    /**
     * 枚举图形文件格式集合
     */
    public enum GraphFileTypeEnum {
        //图形文件格式
        DWG("41433130313500000000" ),
        PSD("38425053000100000000" ),
        PS("252150532D41646F6265" ),
        PDF("255044462d312e350d0a" ),
        QDF("AC9EBD8F" );

        private String value = "";

        private GraphFileTypeEnum(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }

    /**
     * 枚举程序文件格式集合
     */
    public enum ProgramFileTypeEnum {
        //程序文件格式
        HTML("3c21444f435459504520" ),
        HTM("3c21646f637479706520" ),
        CSS("48544d4c207b0d0a0942" ),
        JS("696b2e71623d696b2e71" ),
        JSP("3c25402070616765206c" ),
        MF("4d616e69666573742d56" ),
        XML("3c3f786d6c2076657273" ),
        SQL("494e5345525420494e54" ),
        JAVA("7061636b616765207765" ),
        PROPERTIES("6c6f67346a2e726f6f74" ),
        CLASS("cafebabe0000002e0041" ),
        BAT("406563686f206f66660d" ),
        RAM("2E7261FD" );

        private String value = "";

        private ProgramFileTypeEnum(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }

    /**
     * 判断前台上传文件是否为图片
     *
     * @param multipartFile
     * @return
     * @throws IOException
     */
    public static String imageFlieTypeCheck(MultipartFile multipartFile) throws IOException {
        InputStream inputStream = multipartFile.getInputStream();
        byte[] buffer = new byte[10];
        inputStream.read(buffer);

        //返回当前文件的真实类型
        return getTrueFileType(1, bytesToHexFileTypeString(buffer));
    }

    /**
     * 判断前台上传文件是否为影音文件
     *
     * @param multipartFile
     * @return
     * @throws IOException
     */
    public static String videoFlieTypeCheck(MultipartFile multipartFile) throws IOException {
        InputStream inputStream = multipartFile.getInputStream();
        byte[] buffer = new byte[10];
        inputStream.read(buffer);

        //返回当前文件的真实类型
        return getTrueFileType(2, bytesToHexFileTypeString(buffer));
    }

    /**
     * 判断前台上传文件是否为办公文档
     *
     * @param multipartFile
     * @return
     * @throws IOException
     */
    public static String officeFlieTypeCheck(MultipartFile multipartFile) throws IOException {
        InputStream inputStream = multipartFile.getInputStream();
        byte[] buffer = new byte[10];
        inputStream.read(buffer);

        //返回当前文件的真实类型
        return getTrueFileType(3, bytesToHexFileTypeString(buffer));
    }

    /**
     * 判断前台上传文件是否为压缩文件
     *
     * @param multipartFile
     * @return
     * @throws IOException
     */
    public static String compressFlieTypeCheck(MultipartFile multipartFile) throws IOException {
        InputStream inputStream = multipartFile.getInputStream();
        byte[] buffer = new byte[10];
        inputStream.read(buffer);

        //返回当前文件的真实类型
        return getTrueFileType(4, bytesToHexFileTypeString(buffer));
    }

    /**
     * 判断前台上传文件是否为运行文件
     *
     * @param multipartFile
     * @return
     * @throws IOException
     */
    public static String runFlieTypeCheck(MultipartFile multipartFile) throws IOException {
        InputStream inputStream = multipartFile.getInputStream();
        byte[] buffer = new byte[10];
        inputStream.read(buffer);

        //返回当前文件的真实类型
        return getTrueFileType(2, bytesToHexFileTypeString(buffer));
    }

    /**
     * 判断前台上传文件是否为图形文件
     *
     * @param multipartFile
     * @return
     * @throws IOException
     */
    public static String programFlieTypeCheck(MultipartFile multipartFile) throws IOException {
        InputStream inputStream = multipartFile.getInputStream();
        byte[] buffer = new byte[10];
        inputStream.read(buffer);

        //返回当前文件的真实类型
        return getTrueFileType(2, bytesToHexFileTypeString(buffer));
    }

    /**
     * 判断前台上传文件是否为程序文件
     *
     * @param multipartFile
     * @return
     * @throws IOException
     */
    public static String graphFlieTypeCheck(MultipartFile multipartFile) throws IOException {
        InputStream inputStream = multipartFile.getInputStream();
        byte[] buffer = new byte[10];
        inputStream.read(buffer);

        //返回当前文件的真实类型
        return getTrueFileType(2, bytesToHexFileTypeString(buffer));
    }

    /**
     * 判断文件类型是否为当前传参的类型
     *
     * @param multipartFile
     * @param specifiedType
     * @return
     * @throws IOException
     */
    public static boolean fileTypeCheck(MultipartFile multipartFile, String specifiedType) throws IOException {
        boolean fileTypeIsVaild = false;

        InputStream inputStream = multipartFile.getInputStream();
        byte[] buffer = new byte[10];
        inputStream.read(buffer);

        //文件串
        String filestr = bytesToHexFileTypeString(buffer);

        //文件种类
        int type = getFileType(filestr);

        //获取当前文件的真实类型
        String curfileType = getTrueFileType(type, filestr);

        //指定文件类型中是否匹配当前文件类型
        if (specifiedType.toUpperCase().equals(curfileType)) {
            fileTypeIsVaild = true;
        }
        return fileTypeIsVaild;
    }

    /**
     * 获取一个文件的md5值(可处理大文件)
     *
     * @return md5 value
     */
    public static String getMd5(MultipartFile file) {
        FileInputStream fileInputStream = null;
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5" );
            InputStream inputStream = file.getInputStream();
            byte[] buffer = new byte[8192];
            int length;
            while ((length = inputStream.read(buffer)) != -1) {
                md5.update(buffer, 0, length);
            }
            return new String(Hex.encodeHex(md5.digest()));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 从字符串中获取文件真实格式类型代表数字
     *
     * @param multipartFile
     * @return 1-图片；2-影音文件；3-办公文档；4-压缩文件；5-执行文件；6-图形文件；7-程序文件
     */
    public static int getFileTypeNum(MultipartFile multipartFile) throws IOException {
        InputStream inputStream = multipartFile.getInputStream();
        byte[] buffer = new byte[10];
        inputStream.read(buffer);

        //文件串
        String filestr = bytesToHexFileTypeString(buffer);

        return getFileType(filestr);
    }

    /**
     * 从文件名中获取文件真实格式类型代表数字
     *
     * @param fileName 文件全名
     * @return
     * @throws IOException
     */
    public static int getFileTypeNumByName(String fileName) throws IOException {
        boolean fileType = false;
        //文件原后缀
        String dot = ".", filetype = fileName.substring(fileName.lastIndexOf(dot)+1);

        return getFileTypeByName(filetype);
    }


    /** ---------------- 以下为私有公共方法 ------------------- */

    /**
     * 从字符串中获取文件真实格式类型
     *
     * @param s
     * @return 1-图片；2-影音文件；3-办公文档；4-压缩文件；5-执行文件；6-图形文件；7-程序文件
     */
    private static int getFileType(String s) {
        for (ImageFileTypeEnum fileTypeEnum : ImageFileTypeEnum.values()) {
            if (s.startsWith(fileTypeEnum.getValue())) {
                return 1;
            }
        }
        for (VideoFileTypeEnum fileTypeEnum : VideoFileTypeEnum.values()) {
            if (s.startsWith(fileTypeEnum.getValue())) {
                return 2;
            }
        }
        for (OfficeFileTypeEnum fileTypeEnum : OfficeFileTypeEnum.values()) {
            if (s.startsWith(fileTypeEnum.getValue())) {
                return 3;
            }
        }
        for (CompressFileTypeEnum fileTypeEnum : CompressFileTypeEnum.values()) {
            if (s.startsWith(fileTypeEnum.getValue())) {
                return 4;
            }
        }
        for (RunFileTypeEnum fileTypeEnum : RunFileTypeEnum.values()) {
            if (s.startsWith(fileTypeEnum.getValue())) {
                return 5;
            }
        }
        for (GraphFileTypeEnum fileTypeEnum : GraphFileTypeEnum.values()) {
            if (s.startsWith(fileTypeEnum.getValue())) {
                return 6;
            }
        }
        for (ProgramFileTypeEnum fileTypeEnum : ProgramFileTypeEnum.values()) {
            if (s.startsWith(fileTypeEnum.getValue())) {
                return 7;
            }
        }
        return 0;
    }

    /**
     * 从文件名中获取文件格式类型
     *
     * @param s 后缀（不含.）
     * @return 1-图片；2-影音文件；3-办公文档；4-压缩文件；5-执行文件；6-图形文件；7-程序文件
     */
    private static int getFileTypeByName(String s) {
        //转化为大写
        String filetype = s.toUpperCase();

        /** 创建要求格式数组 */
        //图片
        String[] image = {"JPG", "JPEG", "PNG", "BMP", "TIFF"};
        //视频
        String[] vedio = {"MP4", "AVI", "WMV", "MOV"};
        if (Arrays.asList(image).contains(filetype)) {
            return 1;
        }
        if (Arrays.asList(vedio).contains(filetype)) {
            return 2;
        }
        return 0;
    }


    /**
     * 从字符串中获取文件真实格式
     *
     * @param s
     * @return
     */
    private static String getTrueFileType(int type, String s) {
        switch (type) {
            case 1:
                for (ImageFileTypeEnum fileTypeEnum : ImageFileTypeEnum.values()) {
                    if (s.startsWith(fileTypeEnum.getValue())) {
                        return fileTypeEnum.toString();
                    }
                }
                break;

            case 2:
                for (VideoFileTypeEnum fileTypeEnum : VideoFileTypeEnum.values()) {
                    if (s.startsWith(fileTypeEnum.getValue())) {
                        return fileTypeEnum.toString();
                    }
                }
                break;

            case 3:
                for (OfficeFileTypeEnum fileTypeEnum : OfficeFileTypeEnum.values()) {
                    if (s.startsWith(fileTypeEnum.getValue())) {
                        return fileTypeEnum.toString();
                    }
                }
                break;

            case 4:
                for (CompressFileTypeEnum fileTypeEnum : CompressFileTypeEnum.values()) {
                    if (s.startsWith(fileTypeEnum.getValue())) {
                        return fileTypeEnum.toString();
                    }
                }
                break;

            case 5:
                for (RunFileTypeEnum fileTypeEnum : RunFileTypeEnum.values()) {
                    if (s.startsWith(fileTypeEnum.getValue())) {
                        return fileTypeEnum.toString();
                    }
                }
                break;

            case 6:
                for (GraphFileTypeEnum fileTypeEnum : GraphFileTypeEnum.values()) {
                    if (s.startsWith(fileTypeEnum.getValue())) {
                        return fileTypeEnum.toString();
                    }
                }
                break;
            case 7:
                for (ProgramFileTypeEnum fileTypeEnum : ProgramFileTypeEnum.values()) {
                    if (s.startsWith(fileTypeEnum.getValue())) {
                        return fileTypeEnum.toString();
                    }
                }
                break;
            default:
        }
        return null;
    }

    /**
     * 将字节转换为字符串
     *
     * @param buffer
     * @return
     */
    private static String bytesToHexFileTypeString(byte[] buffer) {
        StringBuilder hexFileTypeStr = new StringBuilder();
        for (byte b : buffer) {
            String hexString = Integer.toHexString(b & 0xFF);
            if (hexString.length() < 2) {
                hexFileTypeStr.append("0" );
            }
            hexFileTypeStr.append(hexString);
        }
        return hexFileTypeStr.toString();
    }
}
