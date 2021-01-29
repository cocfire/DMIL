package com.routon.plcloud.device.api.utils;


import org.jcodec.api.FrameGrab;
import org.jcodec.api.JCodecException;
import org.jcodec.common.model.Picture;
import org.jcodec.scale.AWTUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

/**
 * @author FireWang
 * @date 2020/9/14 18:49
 */
public class VideoUtil {
    private static Logger logger = LoggerFactory.getLogger(VideoUtil.class);
    private static final int THUMB_FRAME = 5;

    /**
     * 获取视频指定帧
     *
     * @author sunk
     */
    public static BufferedImage getFrame(File file, int frameNumber)
            throws IOException, JCodecException {
        try {
            Picture picture = FrameGrab.getFrameFromFile(file, frameNumber);
            return AWTUtil.toBufferedImage(picture);
        } catch (IOException e) {
            logger.error(e.getMessage());
            return null;
        } catch (Exception e) {
            logger.error(e.getMessage());
            return null;
        }
    }

    /**
     * 根据帧数获取缩略图
     *
     * @author sunk
     */
    public static ByteArrayOutputStream getThumbnail(File file, int limit) throws IOException, JCodecException {
        try {
            BufferedImage frameBi = getFrame(file, THUMB_FRAME);
            return ImageUtil.getThumbnail(frameBi, limit);
        } catch (IOException e) {
            logger.error(e.getMessage());
            return null;
        } catch (Exception e) {
            logger.error(e.getMessage());
            return null;
        }
    }

    /**
     * 获取缩略图
     *
     * @author sunk
     */
    public static ByteArrayOutputStream getThumbnail(File file)
            throws IOException, JCodecException {
        try {
            BufferedImage frameBi = getFrame(file, THUMB_FRAME);
            return ImageUtil.getThumbnail(frameBi);
        } catch (IOException e) {
            logger.error(e.getMessage());
            return null;
        } catch (Exception e) {
            logger.error(e.getMessage());
            return null;
        }
    }
}
