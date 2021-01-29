package com.routon.plcloud.device.core.service;

import com.routon.plcloud.device.data.entity.Offlinemsg;

import java.util.List;

/**
 * @author FireWang
 * @date 2020/5/07 15:09
 */
public interface OfflinemsgService {

    /**
     * 根据clientid获取离线消息
     *
     * @param clientid
     * @return
     * @throws Exception
     */
    List<Offlinemsg> searchMsgByCid(String clientid) throws Exception;

    /**
     * 根据设备格式新增离线消息
     *
     * @param offlinemsg
     * @return
     * @throws Exception
     */
    boolean insert(Offlinemsg offlinemsg) throws Exception;

    /**
     * 修改离线消息
     *
     * @param offlinemsg
     * @return
     * @throws Exception
     */
    boolean update(Offlinemsg offlinemsg) throws Exception;

    /**
     * 根据设备格式新增离线消息并返回当前id
     *
     * @param offlinemsg
     * @return
     * @throws Exception
     */
    int insertAndRid(Offlinemsg offlinemsg) throws Exception;

    /**
     * 根据id获取信息
     *
     * @param id
     * @return
     */
    Offlinemsg getMsgById(Integer id);
}