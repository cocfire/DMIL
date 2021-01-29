package com.routon.plcloud.device.core.service;

import com.routon.plcloud.device.data.entity.Fileinfo;

import java.util.List;
import java.util.Map;

/**
 * @author FireWang
 * @date 2020/5/07 15:09
 */
public interface FileinfoService {

    /**
     * 根据格式新增文件
     *
     * @param fileinfo
     * @return
     * @throws Exception
     */
    boolean insertFile(Fileinfo fileinfo) throws Exception;

    /**
     * 根据格式删除文件
     *
     * @param fileinfo
     * @return
     * @throws Exception
     */
    boolean deleteFile(Fileinfo fileinfo) throws Exception;

    /**
     * 修改信息
     *
     * @param fileinfo
     * @return
     */
    boolean update(Fileinfo fileinfo);

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
    List<Fileinfo> getMaxList(Map<String, Object> paramMap) throws Exception;

    /**
     * 根据条件查询设备列表并分页
     *
     * @param paramMap 查询条件Map
     * @param page     页号
     * @param pageSize 条数
     * @return
     * @throws Exception
     */
    List<Fileinfo> searchFileforpage(Map<String, Object> paramMap, Integer page, Integer pageSize) throws Exception;

    /**
     * 根据id获取文件信息
     *
     * @param id
     * @return
     */
    Fileinfo getFileById(Integer id);

    /**
     * 根据uploadname获取文件信息
     *
     * @param filename
     * @return
     */
    Fileinfo getFileByFileName(String filename);

    /**
     * 根据uploadname获取文件信息
     *
     * @param uploadname
     * @return
     */
    Fileinfo getFileByUploadName(String uploadname);

}