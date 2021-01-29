package com.routon.plcloud.device.core.service;

import com.routon.plcloud.device.core.common.TreeBean;
import com.routon.plcloud.device.data.entity.Device;

import java.util.List;
import java.util.Map;

/**
 * @author FireWang
 * @date 2020/5/07 15:09
 */
public interface DeviceService {

    /**
     * 根据条件新增设备
     *
     * @param paramMap
     * @return
     * @throws Exception
     */
    Device addDevice(Map<String, Object> paramMap) throws Exception;

    /**
     * 根据设备格式新增设备
     *
     * @param device
     * @return
     * @throws Exception
     */
    boolean insertDevice(Device device) throws Exception;

    /**
     * 根据设备格式删除设备
     *
     * @param device
     * @return
     * @throws Exception
     */
    boolean deleteDevice(Device device) throws Exception;

    /**
     * 修改设备信息
     *
     * @param device
     * @return
     * @throws Exception
     */
    boolean update(Device device) throws Exception;

    /**
     * 根据id获取设备
     *
     * @param id
     * @return
     * @throws Exception
     */
    Device getDeviceById(Integer id) throws Exception;

    /**
     * 根据deviceid获取设备
     *
     * @param deviceid
     * @return
     * @throws Exception
     */
    Device getDeviceByDeviceId(String deviceid) throws Exception;

    /**
     * 根据id获取设备
     *
     * @param id
     * @return
     * @throws Exception
     */
    Device searchDeviceById(Integer id) throws Exception;

    /**
     * 根据termsn获取设备
     *
     * @param termsn
     * @return
     * @throws Exception
     */
    Device searchDeviceBySn(String termsn) throws Exception;

    /**
     * 根据clientid获取设备
     *
     * @param clientid
     * @return
     * @throws Exception
     */
    Device searchDeviceByCid(String clientid) throws Exception;

    /**
     * 根据clientid获取设备列表
     *
     * @param clientid
     * @return
     * @throws Exception
     */
    List<Device> searchDeviceListByCid(String clientid) throws Exception;

    /**
     * 根据uuid获取设备
     *
     * @param uuid
     * @return
     * @throws Exception
     */
    Device searchDeviceByUuid(String uuid) throws Exception;

    /**
     * 获公司分组树：根据companyId展开相应的分支
     *
     * @param companyId
     * @param searchId
     * @return
     * @throws Exception
     */
    List<TreeBean> getDviceTree(Integer companyId, Integer searchId) throws Exception;

    /**
     * 根据项目名称获取id
     *
     * @param projectname
     * @return
     * @throws Exception
     */
    int getProjectidByname(String projectname) throws Exception;

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
    List<Device> getMaxList(Map<String, Object> paramMap) throws Exception;

    /**
     * 根据条件获取列表--第三方设备
     *
     * @param paramMap
     * @return
     * @throws Exception
     */
    List<Device> getOtherMaxList(Map<String, Object> paramMap) throws Exception;

    /**
     * 根据条件查询设备列表并分页
     *
     * @param paramMap 查询条件Map
     * @param page     页号
     * @param pageSize 条数
     * @return
     * @throws Exception
     */
    List<Device> searchdeviceforpage(Map<String, Object> paramMap, Integer page, Integer pageSize) throws Exception;

    /**
     * 根据条件查询第三方设备列表并分页
     *
     * @param paramMap 查询条件Map
     * @param page     页号
     * @param pageSize 条数
     * @return
     * @throws Exception
     */
    List<Device> searchotherdeviceforpage(Map<String, Object> paramMap, Integer page, Integer pageSize) throws Exception;

    /**
     * 根据字段名称和条件获取字段种类列表
     *
     * @param clounm
     * @param condition
     * @return
     * @throws Exception
     */
    List<Device> searchDeviceClounmByCondition(String clounm, Map<String, Object> condition) throws Exception;

    /**
     * 根据字段名称和条件获取字段种类列表--第三方设备
     *
     * @param clounm
     * @param condition
     * @return
     * @throws Exception
     */
    List<Device> searchOtherDeviceClounmByCondition(String clounm, Map<String, Object> condition) throws Exception;

    /**
     * 根据包含id的查询软件列表
     *
     * @param idmap
     * @return
     * @throws Exception
     */
    List<Device> canTheyUpdateById(Map idmap) throws Exception;

    /**
     * 根据id获取指定字段列表
     *
     * @param idmap
     * @param clounm
     * @return
     * @throws Exception
     */
    List<Device> getClounmById(Map idmap, String clounm) throws Exception;

    /**
     * 根据条件查询软件列表
     *
     * @param paramMap
     * @return
     * @throws Exception
     */
    List<Device> canTheyUpdateByParam(Map<String, Object> paramMap) throws Exception;

    /**
     * 根据菜单id清除相关设备信息
     *
     * @param menuId
     */
    void delLinkedMenuId(Integer menuId);
}