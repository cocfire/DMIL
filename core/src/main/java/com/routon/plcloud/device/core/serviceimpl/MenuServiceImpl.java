package com.routon.plcloud.device.core.serviceimpl;

import com.routon.plcloud.device.core.service.MenuService;
import com.routon.plcloud.device.data.entity.Menu;
import com.routon.plcloud.device.data.mapper.MenuMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author FireWang
 * @date 2020/5/07 15:09
 */
@Service
public class MenuServiceImpl implements MenuService {
    @Autowired
    private MenuMapper menuMapper;

    @Override
    public boolean save(Menu menu) {

        return false;
    }

    @Override
    public boolean addMenu(Menu menu) {
        menu.setCreatetime(new Date());
        menu.setModifytime(new Date());
        menu.setStatus(1);

        if (menuMapper.insertNew(menu) > 0) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean delMenuById(Integer id) {
        Menu menu = getMenuById(id);
        if (menu != null && menuMapper.deleteById(id) > 0) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public Menu getMenuByName(String name) {
        List<Menu> menuList = menuMapper.selectBySql("SELECT * FROM menu where name = '" + name + "'");
        if (menuList.size() > 0) {
            return menuList.get(0);
        } else {
            return null;
        }
    }

    @Override
    public Menu getMenuById(Integer id) {
        Menu menu;
        List<Menu> menuList = menuMapper.selectBySql("SELECT * FROM menu where id = " + id);
        if (menuList.size() > 0) {
            menu = menuList.get(0);
        } else {
            menu = null;
        }
        return menu;
    }

    @Override
    public Menu getMenuByNameForPid(String name, Integer pid) {
        Menu menu;
        List<Menu> menuList = menuMapper.selectBySql("SELECT * FROM menu where pid = "+ pid +" and name = '" + name + "'");
        if (menuList.size() > 0) {
            menu = menuList.get(0);
        } else {
            menu = null;
        }
        return menu;
    }

    @Override
    public List<Menu> getMenuListByRank(Integer rank) throws Exception {
        List<Menu> target;
        target = menuMapper.selectBySql("SELECT * FROM menu where status = 1 and rank = " + rank);
        if (target.size() > 0) {
            return target;
        } else {
            return new ArrayList<>();
        }
    }

    @Override
    public List<Menu> getMenuListByPid(Integer pid) throws Exception {
        List<Menu> target;
        target = menuMapper.selectBySql("SELECT * FROM menu where status = 1 and pid = " + pid);
        if (target.size() > 0) {
            return target;
        } else {
            return new ArrayList<>();
        }
    }

}