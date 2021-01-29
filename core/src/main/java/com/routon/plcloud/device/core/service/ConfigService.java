package com.routon.plcloud.device.core.service;

import com.routon.plcloud.device.core.common.TreeBean;
import com.routon.plcloud.device.data.entity.Config;

import java.util.List;
import java.util.Map;

/**
 * @author FireWang
 * @date 2020/5/07 15:09
 */
public interface ConfigService {

    /**
     * 根据格式新增
     *
     * @param config
     * @return
     */
    boolean insert(Config config);

    /**
     * 删除记录
     *
     * @param config
     * @return
     */
    boolean delete(Config config);

    /**
     * 更新记录
     *
     * @param config
     * @return
     */
    boolean update(Config config);

    /**
     * 根据id获取记录
     *
     * @param id
     * @return
     */
     Config getById(Integer id);

    /**
     * 根据configid获取记录
     *
     * @param configid
     * @return
     */
    Config getByConfigid(Integer configid);

    /**
     * 获取配置分类树
     *
     * @return
     * @throws Exception
     */
    List<TreeBean> getConfigTree() throws Exception;

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
     * @param paramMap
     * @return
     * @throws Exception
     */
    List<Config> getMaxList(Map<String, Object> paramMap);

    /**
     * 根据条件查询设备列表并分页
     *
     * @param paramMap 查询条件Map
     * @param page     页号
     * @param pageSize 条数
     * @return
     * @throws Exception
     */
    List<Config> searchforpage(Map<String, Object> paramMap, Integer page, Integer pageSize);

    /**
     * 查看设备是否具有默认值，v1.4.4
     *
     * @param paramMap
     * @return
     */
    boolean checkDefultParam(Map<String, Object> paramMap);

    /**
     * 根据条件获取有效配置参数列表
     *
     * @param termsn
     * @return
     * @throws Exception
     */
    List<Config> getVaildList(String termsn);
}