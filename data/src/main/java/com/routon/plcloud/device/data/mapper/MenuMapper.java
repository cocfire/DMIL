package com.routon.plcloud.device.data.mapper;

import com.routon.plcloud.device.data.entity.Menu;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author FireWang
 * @date 2020/5/07 15:09
 */
@Mapper
@Repository
public interface MenuMapper {

    /**
     * 通用查询方法
     *
     * @param sql
     * @return
     */
    @Select("${sql}")
    List<Menu> selectBySql(@Param("sql") String sql);

    /**
     * 通用插入方法
     *
     * @param menu
     * @return
     */
    @Insert("insert into menu(name, rank, pid, menuorder, path, createtime, modifytime, status, remark)" +
        " values(#{name}, #{rank}, #{pid}, #{menuorder}, #{path}, #{createtime}, #{modifytime}, #{status}, #{remark})")
    int insertNew(Menu menu);

    /**
     * 通用更新方法
     *
     * @param menu
     * @return
     */
    @Update("update menu set id=#{id}, name=#{name}, rank=#{rank}, pid=#{pid}, menuorder=#{menuorder}, path=#{path}, createtime=#{createtime}, modifytime=#{modifytime}, status=#{status}, remark=#{remark}" +
        " where id = #{id}")
    int updateBySql(Menu menu);

    /**
     * 通用删除方法
     *
     * @param id
     * @return
     */
    @Delete("delete from menu where id = #{id}")
    int deleteById(@Param("id") int id);

    /**
     * 通用查询总数方法
     *
     * @param sql
     * @return
     */
    @Select("${sql}")
    int countBySql(@Param("sql") String sql);
}