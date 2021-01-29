package com.routon.plcloud.device.api.config.download;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor.AbortPolicy;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 学习资源:
 * https://blog.csdn.net/qq_34401512/article/details/77867576
 *
 * com.routon.plcloud.device.api.config.download.ConcurrentDownLoad
 *         .builder()
 *         // 设置URL
 *         .setUrl("http://117.148.175.41/cdn/pcfile/20190904/16/58/GeePlayerSetup_app.exe?dis_k=26cbb7b142c2446397843d6543da209ae&dis_t=1573620667&dis_dz=CMNET-GuangDong&dis_st=36")
 *         // 设置线程每次请求的大小
 *         .setBlockSize(1024)
 *         // 设置线程数量
 *         .setThreadCount(10)
 *         // 设置保存路径
 *         .setPath("C:\\Users\\houyu\\Desktop\\GeePlayerSetup_app_my33333333.exe")
 *         // 设置存在是否删除
 *         .setDeleteIfExist(true)
 *         // 创建
 *         .build()
 *         // 开始
 *         .start((msg, total, current, speed) -> {});
 *
 * @description 并发下载文件工具
 * @date 2019-11-12 14:35:26
 * @author houyu for.houyu@foxmail.com
 */
public class ConcurrentDownLoad {

    private static final Logger log = LoggerFactory.getLogger(ConcurrentDownLoad.class);

    private Builder builder;
    /** HttpClient */
    private HttpClient httpClient;
    /** 线程池 */
    private ThreadPoolExecutor poolExecutor;
    /** 信号量 */
    private Semaphore semaphore;
    /** CountDownLatch */
    private CountDownLatch countDownLatch;
    /** 总长度 */
    private long total;
    /** 当前的进度 */
    private AtomicLong current;
    /** 回调方法 */
    private Callback callback;

    public static Builder builder() {
        return new Builder();
    }

    protected ConcurrentDownLoad(Builder builder) {
        this.builder = builder;
        httpClient = HttpClient.buildHttpClient();
        poolExecutor = new ThreadPoolExecutor(builder.threadCount, builder.threadCount,0L,TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(), new AbortPolicy());
    }

    private Long getContentLength() {
        HttpClient.Response<Long> response = httpClient.buildRequest(this.builder.url).GET().execute((request, http) -> {
            if(http.getResponseCode() == HttpURLConnection.HTTP_OK) {
                return http.getContentLengthLong();
            }
            return null;
        });
        return response.getBody();
    }

    private List<long[]> getPaces(long totalLength) {
        List<long[]> paces = new ArrayList<>(16);
        long currentLength = totalLength;
        long startIndex = 0;
        long endIndex;
        while(currentLength > 0) {
            long size = currentLength >= this.builder.blockSize ? this.builder.blockSize : currentLength;
            endIndex = startIndex + size;
            endIndex = endIndex >= totalLength ? totalLength : endIndex;
            paces.add(new long[]{startIndex, endIndex});
            currentLength = currentLength - size;
            startIndex = endIndex + 1;
        }
        return paces;
    }

    public void start(Callback call) {
        this.run((msg, total, current, speed) -> {
            log.debug("msg:{} total:{} current:{} speed:{}", msg, total, current, speed);
            call.accept(msg, total, current, speed);
        });
    }

    public void start() {
        this.start((msg, total, current, speed) -> {});
    }

    private void run(Callback call) {
        try {
            this.callback = call;
            callback.accept("start...", 0, 0, 0);
            Long totalLength = getContentLength();
            if(totalLength == null) {
                callback.accept("获取文件的长度失败", 0, 0, 0);
                throw new RuntimeException("获取文件的长度失败");
            }
            total = totalLength;
            callback.accept(String.format("文件总长度:%s字节(B)", total), total, 0, 0);
            //
            this.builder.setBlockSize(this.builder.blockSize >= totalLength ? totalLength : this.builder.blockSize);
            //
            File file = new File(this.builder.path);
            if(file.exists()) {
                callback.accept("文件存在", total, 0, 0);
                if(builder.keepOnIfDisconnect && new File(this.builder.path + ".conf").exists()) {
                    // 支持断点
                    // String conf = Files.readString(Paths.get(this.builder.path + ".conf"), Charset.forName("UTF-8"));
                    // String[] split = conf.split(";");
                    // for(String s : split) {
                    //     s.split("-")
                    // }
                    // continueList.add()
                } else {
                    if(builder.deleteIfExist) {
                        file.delete();
                        callback.accept("删除文件", total, 0, 0);
                        initFile(totalLength);
                    }
                }
            } else {
                callback.accept("文件不存在, 创建目录", total, 0, 0);
                file.getParentFile().mkdirs();
                initFile(totalLength);
            }
            //
            semaphore = new Semaphore(this.builder.threadCount);
            current = new AtomicLong(0);
            List<long[]> paces = getPaces(totalLength);
            countDownLatch = new CountDownLatch(paces.size());
            for(long[] pace : paces) {
                callback.accept(String.format("pace:%s - %s", pace[0], pace[1]), total, 0, 0);
                poolExecutor.submit(new DownLoadThread(pace[0], pace[1]));
            }
            try {
                countDownLatch.await();
            } catch(InterruptedException e) {
                e.printStackTrace();
            }
            poolExecutor.shutdown();
            callback.accept(String.format("下载完成:%s", this.builder.url), total, current.get(), 0);
        } catch(Exception e) {
            callback.accept(e.getMessage(), total, current.get(), 0);
        }

    }

    private void initFile(Long totalLength) throws IOException {
        RandomAccessFile raf = new RandomAccessFile(this.builder.path, "rwd");
        // 指定创建的文件的长度
        raf.setLength(totalLength);
        raf.close();
    }

    /**
     * 内部类用于实现下载并组装
     */
    private class DownLoadThread implements Runnable {
        /** 下载起始位置 */
        private long startIndex;
        /** 下载结束位置 */
        private long endIndex;

        public DownLoadThread(long startIndex, long endIndex) {
            this.startIndex = startIndex;
            this.endIndex = endIndex;
        }

        @Override
        public void run() {
            try (RandomAccessFile file = new RandomAccessFile(builder.path, "rwd")) {
                semaphore.acquire();
                file.seek(startIndex);
                httpClient.buildRequest(builder.url)
                        // 添加请求头
                        .addHeader("Range", "bytes=" + startIndex + "-" + endIndex)
                        // 执行请求
                        // .execute(BodyHandlers.ofCallbackByteArray(file::write))
                        .execute(HttpClient.BodyHandlers.ofCallbackByteArray((data, index, length) -> {
                            file.write(data, index, length);
                            callback.accept("download...", total, current.addAndGet(length), 0);
                        }))
                ;
            } catch(Exception e) {
                e.printStackTrace();
            } finally {
                countDownLatch.countDown();
                semaphore.release();
            }
        }
    }

    public static class Builder {
        /** 同时下载的线程数*/
        private int threadCount = 5;
        /** 每个线程每次执行的文件大小(b) 0.5M */
        private long blockSize = 1024 * 512;
        /** 服务器请求路径 */
        private String url;
        /** 本地路径 */
        private String path;
        /** 存在是否删除 */
        private boolean deleteIfExist = false;
        /** 是否断点续传 */
        private boolean keepOnIfDisconnect = true;

        public Builder setThreadCount(int threadCount) {
            this.threadCount = threadCount;
            return this;
        }
        public Builder setBlockSize(long blockSizeOfKb) {
            this.blockSize = blockSizeOfKb * 1024;
            return this;
        }
        public Builder setUrl(String url) {
            this.url = url;
            return this;
        }
        public Builder setPath(String path) {
            this.path = path;
            return this;
        }
        public Builder setDeleteIfExist(boolean deleteIfExist) {
            this.deleteIfExist = deleteIfExist;
            return this;
        }
        public Builder setKeepOnIfDisconnect(boolean keepOnIfDisconnect) {
            this.keepOnIfDisconnect = keepOnIfDisconnect;
            return this;
        }

        public ConcurrentDownLoad build() {
            return new ConcurrentDownLoad(this);
        }
    }

    public interface Callback {
        /**
         * 回调方法
         * @param msg 消息
         * @param total 总量
         * @param current 当前量
         * @param speed 速度(k/s) 暂时不实现
         */
        void accept(String msg, long total, long current, long speed);
    }

}
