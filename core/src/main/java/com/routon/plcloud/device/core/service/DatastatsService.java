package com.routon.plcloud.device.core.service;

import com.routon.plcloud.device.data.entity.Datastats;

import java.util.List;
import java.util.Map;

/**
 * @author FireWang
 * @date 2020/5/07 15:09
 */
public interface DatastatsService {

    /**
     * 根据格式新增
     *
     * @param datastats
     * @return
     */
    boolean insert(Datastats datastats);

    /**
     * 删除记录
     *
     * @param datastats
     * @return
     */
    boolean delete(Datastats datastats);

    /**
     * 更新记录
     *
     * @param datastats
     * @return
     */
    boolean update(Datastats datastats);

    /**
     * 根据id获取记录
     *
     * @param id
     * @return
     */
    Datastats getById(Integer id);

    /**
     * 根据id获取指定日期的记录
     *
     * @param linkid
     * @param playdate
     * @return
     */
    Datastats getByLink(Integer linkid, String termsn, String playdate);

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
    List<Datastats> getMaxList(Map<String, Object> paramMap);

    /**
     * 根据条件查询设备列表并分页
     *
     * @param paramMap 查询条件Map
     * @param page     页号
     * @param pageSize 条数
     * @return
     * @throws Exception
     */
    List<Datastats> searchforpage(Map<String, Object> paramMap, Integer page, Integer pageSize);

    /**
     * 根据统计类型查询统计结果 -- 文件
     *
     * @param paramMap 查询条件Map
     * @param type     统计类型：0-统计所有；1-按年统计；2-按月统计；3-按日统计
     * @return
     * @throws Exception
     */
    List<Map> getDataByType(Map<String, Object> paramMap, Integer type);

    /**
     * 根据统计类型查询统计结果并分页 -- 文件
     *
     * @param paramMap 查询条件Map
     * @param type     统计类型：0-统计所有；1-按年统计；2-按月统计；3-按日统计
     * @param page     页号
     * @param pageSize 条数
     * @return
     * @throws Exception
     */
    List<Map> getDataByTypeForPage(Map<String, Object> paramMap, Integer type, Integer page, Integer pageSize);

    /**
     * 根据统计类型查询统计结果 -- 设备
     *
     * @param paramMap 查询条件Map
     * @param type     统计类型：0-统计所有；1-按年统计；2-按月统计；3-按日统计
     * @return
     * @throws Exception
     */
    List<Map> getDataByType1(Map<String, Object> paramMap, Integer type);

    /**
     * 根据统计类型查询统计结果并分页 -- 设备
     *
     * @param paramMap 查询条件Map
     * @param type     统计类型：0-统计所有；1-按年统计；2-按月统计；3-按日统计
     * @param page     页号
     * @param pageSize 条数
     * @return
     * @throws Exception
     */
    List<Map> getDataByTypeForPage1(Map<String, Object> paramMap, Integer type, Integer page, Integer pageSize);

    /**
     * 根据字段名称和条件获取字段种类列表
     *
     * @param clounm
     * @param condition
     * @return
     * @throws Exception
     */
    List<Datastats> getDataClounmByCondition(String clounm, Map<String, Object> condition) throws Exception;
}