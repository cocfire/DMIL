package com.routon.plcloud.device.core.service;

import com.routon.plcloud.device.data.entity.Paraminfo;

import java.util.List;
import java.util.Map;

/**
 * @author FireWang
 * @date 2020/5/07 15:09
 */
public interface ParaminfoService {

    /**
     * 根据格式新增
     *
     * @param paraminfo
     * @return
     */
    boolean insert(Paraminfo paraminfo);

    /**
     * 删除记录
     *
     * @param paraminfo
     * @return
     */
    boolean delete(Paraminfo paraminfo);

    /**
     * 更新记录
     *
     * @param paraminfo
     * @return
     */
    boolean update(Paraminfo paraminfo);

    /**
     * 根据id获取记录
     *
     * @param id
     * @return
     */
     Paraminfo getById(Integer id);

    /**
     * 根据linkid获取记录
     *
     * @param linkid
     * @return
     */
    Paraminfo getByLinkId(Integer linkid);

    /**
     * 根据termsn获取记录
     *
     * @param termsn
     * @return
     */
    Paraminfo getBySn(String termsn);

    /**
     * 根据paramid、termsn获取记录
     *
     * @param paramid
     * @param termsn
     * @return
     */
    Paraminfo getByParamidAndSn(int paramid, String termsn);

    /**
     * 根据条件获取列表
     *
     * @param paramMap
     * @return
     * @throws Exception
     */
    List<Paraminfo> getMaxList(Map<String, Object> paramMap);

    /**
     * 根据条件查询参数列表并分页
     *
     * @param paramMap 查询条件Map
     * @param page     页号
     * @param pageSize 条数
     * @return
     * @throws Exception
     */
    List<Paraminfo> searchforpage(Map<String, Object> paramMap, Integer page, Integer pageSize);

    /**
     * 根据条件获取最大条数
     *
     * @param paramMap
     * @return
     * @throws Exception
     */
    Integer getMaxCount(Map<String, Object> paramMap);

    /**
     * 根据条件查询设备参数列表并分页
     *
     * @param paramMap 查询条件Map
     * @param page     页号
     * @param pageSize 条数
     * @return
     * @throws Exception
     */
    List<Paraminfo> searchparamforpage(Map<String, Object> paramMap, Integer page, Integer pageSize);

    /**
     * 根据条件获取有效参数列表
     *
     * @param termsn
     * @return
     * @throws Exception
     */
    List<Paraminfo> getVaildList(String termsn);
}