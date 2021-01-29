package com.routon.plcloud.device.api.utils;

import org.apache.commons.io.FileUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * @author FireWang
 * @date 2020/5/18 14:40
 */
public class FileUtil {
    private int maxIoLen = 8192;

    /**
     * 根据目录删除文件
     */
    public static void delefile(String ctxPath) {
        File files = new File(ctxPath);
        FileUtil fileUtil = new FileUtil();
        fileUtil.deleteFile(files);
    }

    /**
     * 删除文件
     *
     * @param deleteFile
     */
    public void deleteFile(File deleteFile) {
        if (deleteFile.isDirectory()) {
            String[] child = deleteFile.list();
            for (int i = 0; i < child.length; i++) {
                deleteFile(new File(deleteFile, child[i]));
            }
        }
        //window删除时可能被占用删不掉，所以要先进行资源回收
        System.gc();
        deleteFile.delete();
    }

    /**
     * Zip文件上传
     *
     * @param source
     * @param destinct
     */
    public void zipFilesDiGui(String source, String destinct) {
        List fileList = loadFilename(new File(source));
        try {
            ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(new File(destinct)));
            byte[] buffere = new byte[8192];
            int length;
            BufferedInputStream bis;
            for (int i = 0; i < fileList.size(); i++) {
                File file = (File) fileList.get(i);
                zos.putNextEntry(new ZipEntry(getEntryName(source, file)));
                bis = new BufferedInputStream(new FileInputStream(file));

                while (true) {
                    length = bis.read(buffere);
                    if (length == -1) {
                        break;
                    }
                    zos.write(buffere, 0, length);
                }
                bis.close();
                zos.closeEntry();
            }
            zos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取绝对路径
     *
     * @param base
     * @param file
     */
    private String getEntryName(String base, File file) {
        File baseFile = new File(base);
        String filename = file.getPath();
        if (baseFile.getParentFile().getParentFile() == null) {
            return filename.substring(baseFile.getParent().length());
        }
        return filename.substring(baseFile.getParent().length() + 1);
    }

    /**
     * 返回路径下的文件列表
     *
     * @param file
     */
    private List loadFilename(File file) {
        List filenameList = new ArrayList();
        if (file.isFile()) {
            filenameList.add(file);
        }
        if (file.isDirectory()) {
            for (File f : file.listFiles()) {
                filenameList.addAll(loadFilename(f));
            }
        }
        return filenameList;
    }

    /**
     * 文件下载-适用于小型文件，不含断点续传
     *
     * @param response
     * @param serverPath
     * @param fileName
     * @param size
     * @return
     */
    public static boolean downFile(HttpServletResponse response, String serverPath, String fileName, int size) {
        try {
            String path = serverPath + "/" + fileName;
            File file = new File(path);
            if (file.exists()) {
                InputStream ins = new FileInputStream(path);
                // 放到缓冲流里面
                BufferedInputStream bins = new BufferedInputStream(ins);
                // 获取文件输出IO流
                OutputStream outs = response.getOutputStream();
                BufferedOutputStream bouts = new BufferedOutputStream(outs);
                // 设置response内容的类型
                response.setContentType("application/x-download");
                // 设置文件大小：字节数
                if (size > 0) {
                    response.setContentLength(size);
                }
                // 设置头部信息
                response.setHeader("Content-disposition", "attachment;filename="
                        + URLEncoder.encode(fileName, "UTF-8"));
                int bytesRead = 0;
                byte[] buffer = new byte[8192];
                //开始向网络传输文件流
                int maxLen = 8192;
                while ((bytesRead = bins.read(buffer, 0, maxLen)) != -1) {
                    bouts.write(buffer, 0, bytesRead);
                }
                // 这里一定要调用flush()方法
                bouts.flush();
                ins.close();
                bins.close();
                outs.close();
                bouts.close();
            } else {
                System.out.println("File Download----------------------没有找到文件！");
                response.setContentType("text/plain");
                response.sendRedirect("list?msg=Download Faild!");
                return false;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * 文件下载-适用于大型文件，含断点续传
     *
     * @param request
     * @param response
     * @param serverPath
     * @param filename
     * @return
     */
    public static boolean downFileByBreak(HttpServletRequest request, HttpServletResponse response,
                                          String serverPath, String filename) {
        BufferedInputStream bis = null;
        String path = serverPath + "/" + filename;
        try {
            File file = new File(path);
            if (file.exists()) {
                long p = 0L;
                long toLength = 0L;
                long contentLength = 0L;
                int rangeSwitch = 0;
                // 0,从头开始的全文下载；1,从某字节开始的下载（bytes=27000-）；2,从某字节开始到某字节结束的下载（bytes=27000-39000）
                long fileLength;
                String rangBytes = "";
                fileLength = file.length();

                // get file content
                InputStream ins = new FileInputStream(file);
                bis = new BufferedInputStream(ins);

                // tell the client to allow accept-ranges
                response.reset();
                response.setHeader("Accept-Ranges", "bytes");

                // client requests a file block download start byte
                String range = request.getHeader("Range");
                String blankstr = "null";
                if (range != null && range.trim().length() > 0 && !blankstr.equals(range)) {
                    response.setStatus(javax.servlet.http.HttpServletResponse.SC_PARTIAL_CONTENT);
                    rangBytes = range.replaceAll("bytes=", "");
                    // bytes=270000-
                    String dash = "-";
                    if (rangBytes.endsWith(dash)) {
                        rangeSwitch = 1;
                        p = Long.parseLong(rangBytes.substring(0, rangBytes.indexOf("-")));
                        // 客户端请求的是270000之后的字节（包括bytes下标索引为270000的字节）
                        contentLength = fileLength - p;
                        // bytes=270000-320000
                    } else {
                        rangeSwitch = 2;
                        String temp1 = rangBytes.substring(0, rangBytes.indexOf("-"));
                        String temp2 = rangBytes.substring(rangBytes.indexOf("-") + 1, rangBytes.length());
                        p = Long.parseLong(temp1);
                        toLength = Long.parseLong(temp2);
                        // 客户端请求的是 270000-320000 之间的字节
                        contentLength = toLength - p + 1;
                    }
                } else {
                    contentLength = fileLength;
                }

                // 如果设设置了Content-Length，则客户端会自动进行多线程下载。如果不希望支持多线程，则不要设置这个参数。
                // Content-Length: [文件的总大小] - [客户端请求的下载的文件块的开始字节]
                response.setHeader("Content-Length", new Long(contentLength).toString());

                // 断点开始
                // 响应的格式是:
                // Content-Range: bytes [文件块的开始字节]-[文件的总大小 - 1]/[文件的总大小]
                if (rangeSwitch == 1) {
                    String contentRange = new StringBuffer("bytes ").append(new Long(p).toString()).append("-")
                            .append(new Long(fileLength - 1).toString()).append("/")
                            .append(new Long(fileLength).toString()).toString();
                    response.setHeader("Content-Range", contentRange);
                    bis.skip(p);
                } else if (rangeSwitch == 2) {
                    String contentRange = range.replace("=", " ") + "/" + new Long(fileLength).toString();
                    response.setHeader("Content-Range", contentRange);
                    bis.skip(p);
                } else {
                    String contentRange = new StringBuffer("bytes ").append("0-").append(fileLength - 1).append("/")
                            .append(fileLength).toString();
                    response.setHeader("Content-Range", contentRange);
                }

                String fileName = file.getName();
                response.setContentType("application/octet-stream");
                response.addHeader("Content-Disposition", "attachment;filename=" + fileName);

                OutputStream out = response.getOutputStream();
                int n = 0;
                long readLength = 0;
                int bsize = 1024;
                byte[] bytes = new byte[bsize];
                if (rangeSwitch == 2) {
                    // 针对 bytes=27000-39000 的请求，从27000开始写数据
                    while (readLength <= contentLength - bsize) {
                        n = bis.read(bytes);
                        readLength += n;
                        out.write(bytes, 0, n);
                    }
                    if (readLength <= contentLength) {
                        n = bis.read(bytes, 0, (int) (contentLength - readLength));
                        out.write(bytes, 0, n);
                    }
                } else {
                    while ((n = bis.read(bytes)) != -1) {
                        out.write(bytes, 0, n);
                    }
                }
                out.flush();
                out.close();
                bis.close();
            }
        } catch (IOException ie) {
            // 忽略 ClientAbortException 之类的异常
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * 解决浏览器兼容获取文件名问题，判断是否为IE浏览器的文件名，IE浏览器下文件名会带有盘符信息
     *
     * @param fileName 文件名
     */
    public static String getFileRealName(String fileName) {
        // Check for Unix-style path
        int unixSep = fileName.lastIndexOf('/');
        // Check for Windows-style path
        int winSep = fileName.lastIndexOf('\\');
        // Cut off at latest possible point
        int pos = (winSep > unixSep ? winSep : unixSep);
        if (pos != -1) {
            // Any sort of path separator found...
            fileName = fileName.substring(pos + 1);
        }
        return fileName;
    }

    /**
     * 加载内部文件到指定的路径：由于工程打成jar包后内部文件无法直接读取，所以该方法用于生成File格式的项目文件
     * <p>
     * 1、Class.getResourceAsStream(String path) ： path 不以’/'开头时默认是从此类所在的包下取资源，以’/'开头则是从ClassPath（/classes/）根下获取。
     * 其只是通过path构造一个绝对路径，最终还是由ClassLoader获取资源。
     * <p>
     * 2. Class.getClassLoader.getResourceAsStream(String path) ：默认则是从ClassPath根下获取，path不能以’/'开头，最终是由ClassLoader获取资源。
     * <p>
     * 3. ServletContext. getResourceAsStream(String path)：默认从WebAPP根目录下取资源，Tomcat下path是否以’/'开头无所谓，
     *
     * @param filePath 文件路径
     * @param fileName 文件名称
     */
    public boolean writeFlieToPath(HttpServletResponse response, String filePath, String fileName) throws IOException {
        try {
            File file = new File(filePath);
            InputStream ins = null;
            if (!file.exists()) {
                //读取指定资源文件的输入流
                ins = this.getClass().getResourceAsStream(filePath);

            } else {
                ins = new FileInputStream(filePath);
            }
            // 放到缓冲流里面
            BufferedInputStream bins = new BufferedInputStream(ins);
            // 获取文件输出IO流
            OutputStream outs = response.getOutputStream();
            BufferedOutputStream bouts = new BufferedOutputStream(outs);
            // 设置response内容的类型
            response.setContentType("application/x-download");
            // 设置头部信息
            response.setHeader("Content-disposition", "attachment;filename="
                    + URLEncoder.encode(fileName, "UTF-8"));
            int bytesRead = 0;
            byte[] buffer = new byte[8192];
            //开始向网络传输文件流
            while ((bytesRead = bins.read(buffer, 0, maxIoLen)) != -1) {
                bouts.write(buffer, 0, bytesRead);
            }
            // 这里一定要调用flush()方法
            bouts.flush();
            ins.close();
            bins.close();
            outs.close();
            bouts.close();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * 将jar包内文件到指定的路径
     *
     * @param pathInJar  证书路径
     * @param targetPath 目标路径
     */
    public boolean writeCertToPath(String pathInJar, String targetPath) throws IOException {
        try {
            //读取指定资源文件的输入流
            InputStream ins = this.getClass().getResourceAsStream(pathInJar);
            OutputStream os = new FileOutputStream(targetPath);

            int bytesRead = 0;
            byte[] buffer = new byte[8192];
            while ((bytesRead = ins.read(buffer, 0, maxIoLen)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
            os.close();
            ins.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    /**
     * 上传软件到目录 multipartFile
     *
     * @param multipartFile
     * @param filePath  尾部带“/”的路径
     * @param fileName
     * @return
     */
    public static boolean uploadToPath(MultipartFile multipartFile, String filePath, String fileName) {
        try {
            InputStream is = multipartFile.getInputStream();
            return uploadToPath(is, filePath, fileName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 上传软件到目录 File
     *
     * @param file
     * @param filePath  尾部带“/”的路径
     * @param fileName
     * @return
     */
    public static boolean uploadToPath(File file, String filePath, String fileName) throws FileNotFoundException {
        try {
            InputStream is = new FileInputStream(file);
            return uploadToPath(is, filePath, fileName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 上传软件到目录 File
     *
     * @param is
     * @param filePath  尾部带“/”的路径
     * @param fileName
     * @return
     */
    public static boolean uploadToPath(InputStream is, String filePath, String fileName) {
        boolean target = false;
        try {
            File targetFile = new File(filePath);
            //生成文件路径中包含的文件夹
            if (!targetFile.exists()) {
                targetFile.mkdirs();
            }
            FileUtils.copyInputStreamToFile(is, new File(filePath + fileName));
            target = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return target;
    }
}
