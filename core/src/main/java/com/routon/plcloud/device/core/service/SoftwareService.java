package com.routon.plcloud.device.core.service;

import com.alibaba.fastjson.JSONObject;
import com.routon.plcloud.device.core.common.TreeBean;
import com.routon.plcloud.device.data.entity.Device;
import com.routon.plcloud.device.data.entity.Software;

import java.util.List;
import java.util.Map;


/**
 * @author FireWang
 * @date 2020/5/07 15:09
 */
public interface SoftwareService {

    /**
     * 根据id获取软件信息
     *
     * @param id
     * @return
     */
    Software getSoftwareById(Integer id);

    /**
     * 根据softwarename获取软件信息
     *
     * @param name
     * @return
     */
    Software getSoftwareByName(String name);

    /**
     * 修改信息
     *
     * @param software
     * @return
     */
    boolean update(Software software);

    /**
     * 获取软件树
     *
     * @return
     * @throws Exception
     */
    List<TreeBean> getSoftwareNameTree(Integer userid) throws Exception;

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
    List<Software> getMaxList(Map<String, Object> paramMap) throws Exception;

    /**
     * 根据条件查询软件列表并分页
     *
     * @param paramMap 查询条件Map
     * @param page     页号
     * @param pageSize 条数
     * @return
     * @throws Exception
     */
    List<Software> searchsoftwareforpage(Map<String, Object> paramMap, Integer page, Integer pageSize) throws Exception;

    /**
     * 校验软件产品管理中软件名和版本号是否唯一
     *
     * @param paramMap
     * @return
     */
    boolean isExist(Map<String, Object> paramMap);

    /**
     * 新增软件
     *
     * @param paramMap
     * @return
     * @throws Exception
     */
    Software addSoftwarePro(Map<String, Object> paramMap) throws Exception;

    /**
     * 新增软件更新历史同款软件的中文描述
     *
     * @param erpcode
     * @param customername
     * @return
     * @throws Exception
     */
    boolean updateSameSoftwareCustName(String erpcode, String customername) throws Exception;

    /**
     * 根据id删除数据
     *
     * @param software
     * @return
     */
    boolean deleteSoftware(Software software);

    /**
     * 根据erp获取该软件个数
     *
     * @param software
     * @return
     */
    int getNumByErp(Software software);

    /**
     * 根据软件名称获取erp
     *
     * @param softwarename
     * @return
     * @throws Exception
     */
    String getErpBySoftware(String softwarename) throws Exception;

    /**
     * 根据erp获取版本列表
     *
     * @param erpcode
     * @return
     * @throws Exception
     */
    List<JSONObject> getVersionByErp(String erpcode) throws Exception;

    /**
     * 根据erp和termmodel获取版本列表
     *
     * @param erpcode
     * @return
     * @throws Exception
     */
    List<JSONObject> getVersionByErpAndTermmodel(String erpcode, List<Device> termmodels) throws Exception;

    /**
     * 获取版本对象
     *
     * @param software
     * @return
     * @throws Exception
     */
    JSONObject getVersion(Software software) throws Exception;

    /**
     * 根据升级码获取软件信息
     *
     * @param qrcode
     * @return
     * @throws Exception
     */
    Software getSoftwareByQr(String qrcode) throws Exception;

    /**
     * 获取可直接下发的软件
     *
     * @return
     * @throws Exception
     */
    List<Software> getDirctList() throws Exception;

    /**
     * 根据条件获取软件列表
     *
     * @param condition
     * @return
     * @throws Exception
     */
    List<Software> getSoftwareByCondition(Map<String, Object> condition) throws Exception;
}