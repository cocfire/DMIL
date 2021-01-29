package com.routon.plcloud.device.core.service;

import com.routon.plcloud.device.core.common.TreeBean;
import com.routon.plcloud.device.data.entity.Syslog;

import java.util.List;
import java.util.Map;

/**
 * @author FireWang
 * @date 2020/5/07 15:09
 */
public interface SyslogService {
    /**
     * 修改信息
     *
     * @param syslog
     * @return
     */
    boolean save(Syslog syslog);

    /**
     * 获取日志分类树
     *
     * @return
     * @throws Exception
     */
    List<TreeBean> getSyslogTree(Integer userid) throws Exception;

    /**
     * 根据条件获取最大条数
     *
     * @param paramMap
     * @return
     * @throws Exception
     */
    Integer getMaxCount(Map<String, Object> paramMap);

    /**
     * 根据条件获取列表
     *
     * @param paramMap 查询条件Map
     * @return
     * @throws Exception
     */
    List<Syslog> getMaxList(Map<String, Object> paramMap) throws Exception;

    /**
     * 根据条件查询软件列表并分页
     *
     * @param paramMap 查询条件Map
     * @param page     页号
     * @param pageSize 条数
     * @return
     * @throws Exception
     */
    List<Syslog> searchsyslogforpage(Map<String, Object> paramMap, Integer page, Integer pageSize) throws Exception;


    /**
     * 新增软件产品管理
     *
     * @param paramMap
     * @return
     * @throws Exception
     */
    boolean addSyslog(Map<String, Object> paramMap) throws Exception;
}