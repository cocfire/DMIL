package com.routon.plcloud.device.core.service;

import com.routon.plcloud.device.data.entity.Devicesw;

import java.util.List;
import java.util.Map;

/**
 * @author FireWang
 * @date 2020/5/07 15:09
 */
public interface DeviceswService {

    /**
     * 根据设备格式新增设备软件
     *
     * @param devicesw
     * @return
     * @throws Exception
     */
    boolean insertDevicesw(Devicesw devicesw) throws Exception;

    /**
     * 根据设备格式删除设备软件
     *
     * @param devicesw
     * @return
     * @throws Exception
     */
    boolean deleteDevicesw(Devicesw devicesw) throws Exception;

    /**
     * 修改设备软件信息
     *
     * @param devicesw
     * @return
     * @throws Exception
     */
    boolean update(Devicesw devicesw) throws Exception;

    /**
     * 根据deviceid查询设备所有（包括历史）软件列表
     *
     * @param deviceid
     * @return
     * @throws Exception
     */
    List<Devicesw> searchByDeviceId(Integer deviceid) throws Exception;

    /**
     * 根据id获取设备软件
     *
     * @param id
     * @return
     * @throws Exception
     */
    Devicesw getById(Integer id) throws Exception;

    /**
     * 根据deviceid获取设备当前（正在运行）软件列表
     *
     * @param deviceid
     * @return
     * @throws Exception
     */
    List<Devicesw> getByDeviceId(Integer deviceid) throws Exception;

    /**
     * 根据deviceid的查询公共软件列表
     *
     * @param idmap
     * @return
     * @throws Exception
     */
    List<Devicesw> canTheyUpdateByDeviceId(Map idmap) throws Exception;

    /**
     * 根据deviceid获取正在升级的设备软件
     *
     * @param deviceid
     * @return
     * @throws Exception
     */
    Devicesw getUpdateByDeviceId(Integer deviceid) throws Exception;

    /**
     * 根据deviceid和softwarename获取唯一的设备软件
     *
     * @param deviceid
     * @param softwarename
     * @return
     * @throws Exception
     */
    Devicesw getByDeviceIdAndName(Integer deviceid, String softwarename) throws Exception;

    /**
     * 根据deviceid和erpcode获取唯一的设备软件
     *
     * @param deviceid
     * @param erpcode
     * @return
     * @throws Exception
     */
    Devicesw getByDeviceIdAndErpcode(Integer deviceid, String erpcode) throws Exception;

    /**
     * 根据deviceid、softwarename和status获取唯一的设备软件
     *
     * @param deviceid
     * @param softwarename
     * @param status
     * @return
     * @throws Exception
     */
    Devicesw getByStatus(Integer deviceid, String softwarename, int status) throws Exception;

    /**
     * 获取当前升级任务数量
     *
     * @return
     * @throws Exception
     */
    int getUpdateCount() throws Exception;
}