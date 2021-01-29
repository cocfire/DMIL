package com.routon.plcloud.device.core.service;

import com.routon.plcloud.device.data.entity.Vrdevice;

import java.util.List;
import java.util.Map;

/**
 * @author FireWang
 * @date 2020/5/07 15:09
 */
public interface VrdeviceService {

    /**
     * 根据设备格式新增设备软件
     *
     * @param vrdevice
     * @return
     * @throws Exception
     */
    boolean insertVrDevice(Vrdevice vrdevice) throws Exception;

    /**
     * 根据设备格式删除设备软件
     *
     * @param vrdevice
     * @return
     * @throws Exception
     */
    boolean deleteVrDevice(Vrdevice vrdevice) throws Exception;

    /**
     * 修改设备软件信息
     *
     * @param vrdevice
     * @return
     * @throws Exception
     */
    boolean update(Vrdevice vrdevice) throws Exception;

    /**
     * 根据vrdeviceid查询设备所有（包括历史）软件列表
     *
     * @param vrdeviceid
     * @return
     * @throws Exception
     */
    Vrdevice searchByDeviceId(String vrdeviceid) throws Exception;

    /**
     * 根据id获取设备软件
     *
     * @param id
     * @return
     * @throws Exception
     */
    Vrdevice getById(Integer id) throws Exception;

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
    List<Vrdevice> getMaxList(Map<String, Object> paramMap) throws Exception;

    /**
     * 根据条件查询设备列表并分页
     *
     * @param paramMap 查询条件Map
     * @param page     页号
     * @param pageSize 条数
     * @return
     * @throws Exception
     */
    List<Vrdevice> searchvrdeviceforpage(Map<String, Object> paramMap, Integer page, Integer pageSize) throws Exception;
}