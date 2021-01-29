package com.routon.plcloud.device.core.service;

import com.routon.plcloud.device.data.entity.Menu;

import java.util.List;

/**
 * @author FireWang
 * @date 2020/5/07 15:09
 */
public interface MenuService {
    /**
     * 修改信息
     *
     * @param menu
     * @return
     */
    boolean save(Menu menu);

    /**
     * 添加菜单
     *
     * @param menu
     * @return
     */
    boolean addMenu(Menu menu);

    /**
     * 删除菜单
     *
     * @param id
     * @return
     */
    boolean delMenuById(Integer id);

    /**
     * 根据名称获取菜单
     *
     * @param name
     * @return
     */
    Menu getMenuByName(String name);

    /**
     * 根据id获取菜单
     *
     * @param id
     * @return
     */
    Menu getMenuById(Integer id);

    /**
     * 根据pid和名称获取菜单
     *
     * @param name
     * @param pid
     * @return
     */
    Menu getMenuByNameForPid(String name, Integer pid);

    /**
     * 根据等级取菜单列表
     *
     * @param rank
     * @return
     * @throws Exception
     */
    List<Menu> getMenuListByRank(Integer rank) throws Exception;

    /**
     * 根据父级id取菜单列表
     *
     * @param pid
     * @return
     * @throws Exception
     */
    List<Menu> getMenuListByPid(Integer pid) throws Exception;

}