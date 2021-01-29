package com.routon.plcloud.device.api.utils;

import com.routon.plcloud.device.core.utils.ConvUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author FireWang
 * @date 2020/11/11 10:32
 * 翻页工具
 */
public class PageUtil {
    private static Logger logger = LoggerFactory.getLogger(PageUtil.class);

    /**
     * 当前页面序号
     */
    private Integer page = 1;

    /**
     * 页面展示条数
     */
    private Integer pageSize = 10;

    /**
     * 最大页数
     */
    private Integer maxPage = 1;

    /**
     * 最大条数
     */
    private Integer columns = 0;

    /**
     * 格式化页面构方法
     *
     * @param page
     * @param pageSize
     * @param columns
     * @return
     */
    public PageUtil(Integer page, Integer pageSize, Integer columns) {
        if (ConvUtil.convToInt(page) != 0) {
            this.setPage(page);
        }
        if (ConvUtil.convToInt(pageSize) != 0) {
            this.setPageSize(pageSize);
        }
        if (ConvUtil.convToInt(columns) != 0) {
            this.setColumns(columns);
            this.setMaxPage((int) Math.ceil(columns / (double) this.pageSize));
        }
    }

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public Integer getMaxPage() {
        return maxPage;
    }

    public void setMaxPage(Integer maxPage) {
        this.maxPage = maxPage;
    }

    public Integer getColumns() {
        return columns;
    }

    public void setColumns(Integer columns) {
        this.columns = columns;
    }
}
