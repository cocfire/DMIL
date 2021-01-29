package com.routon.plcloud.device.core.service;

import com.routon.plcloud.device.core.common.TreeBean;
import com.routon.plcloud.device.data.entity.Device;
import com.routon.plcloud.device.data.entity.Userinfo;

import java.util.List;
import java.util.Map;

/**
 * @author FireWang
 * @date 2020/4/26 18:59
 */
public interface UserinfoService {
    /**
     * 查询管理员记录
     *
     * @return
     */
    Userinfo getAdmin();

    /**
     * 修改信息，如修改密码
     *
     * @param user
     * @return
     */
    boolean saveAdmin(Userinfo user);

    /**
     * 添加普通用户
     *
     * @param user
     * @return
     */
    boolean addUser(Userinfo user);

    /**
     * 根据id获取用户
     *
     * @param id
     * @return
     */
    Userinfo getUserById(Integer id);

    /**
     * 根据id删除用户
     *
     * @param id
     * @return
     */
    boolean deleteUserById(Integer id);

    /**
     * 根据用户名获取用户
     *
     * @param username
     * @return
     */
    Userinfo getUserByUerName(String username);

    /**
     * 根据手机号获取用户
     *
     * @param phone
     * @return
     */
    Userinfo getUserByPhone(String phone);

    /**
     * 根据公司获取用户
     *
     * @param company
     * @return
     */
    Userinfo getUserByCompany(String company);

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
    List<Userinfo> getMaxList(Map<String, Object> paramMap) throws Exception;

    /**
     * 根据条件查询设备列表并分页
     *
     * @param paramMap 查询条件Map
     * @param page     页号
     * @param pageSize 条数
     * @return
     * @throws Exception
     */
    List<Userinfo> searchUserforpage(Map<String, Object> paramMap, Integer page, Integer pageSize) throws Exception;

    /**
     * 获取公司树
     *
     * @return
     * @throws Exception
     */
    List<TreeBean> getCompanyTree() throws Exception;

    /**
     * 根据菜单id清除相关用户信息
     *
     * @param menuId
     */
    void delLinkedMenuId(Integer menuId);
}
