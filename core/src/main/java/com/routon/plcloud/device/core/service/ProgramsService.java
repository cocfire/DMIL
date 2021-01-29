package com.routon.plcloud.device.core.service;

import com.routon.plcloud.device.core.common.TreeBean;
import com.routon.plcloud.device.data.entity.Fileinfo;
import com.routon.plcloud.device.data.entity.Programs;

import java.util.List;

/**
 * @author FireWang
 * @date 2020/5/07 15:09
 */
public interface ProgramsService {

    /**
     * 根据格式新增节目单
     *
     * @param programs
     * @return
     * @throws Exception
     */
    boolean insertProgram(Programs programs) throws Exception;

    /**
     * 根据格式删除节目单
     *
     * @param programs
     * @return
     * @throws Exception
     */
    boolean deleteProgram(Programs programs) throws Exception;

    /**
     * 修改信息
     *
     * @param programs
     * @return
     */
    boolean update(Programs programs);

    /**
     * 获公司分组树：根据companyId展开相应的分支
     *
     * @param companyId
     * @param searchId
     * @return
     * @throws Exception
     */
    List<TreeBean> getProgramTree(Integer companyId, Integer searchId) throws Exception;

    /**
     * 根据id获取节目单信息
     *
     * @param id
     * @return
     */
    Programs getProgramById(Integer id);

    /**
     * 根据name获取节目单信息
     *
     * @param name
     * @return
     */
    Programs getProgramByOnlyName(String name);

    /**
     * 根据name获取节目单信息
     *
     * @param name
     * @@param companyid
     * @return
     */
    Programs getProgramByName(String name, Integer companyid);

    /**
     * 根据name模糊获取节目单
     *
     * @param name
     * @@param companyid
     * @return
     */
    List<Programs> getProgramByNamecase(String name, Integer companyid);

    /**
     * 根据设备id获取节目单信息
     *
     * @param deviceid
     * @param companyid
     * @return
     */
    List<Programs> getProgramByDeviceId(Integer deviceid, Integer companyid);

}