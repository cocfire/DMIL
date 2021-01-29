package com.routon.plcloud.device.data.mapper;

import com.routon.plcloud.device.data.entity.Helloworld;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * @author FireWang
 * @date 2020/5/07 15:09
 */
@Mapper
@Repository
public interface HelloworldMapper {

    /**
     * 通用查询方法
     *
     * @param sql
     * @return
     */
    @Select("${sql}")
    List<Map> selectBySql(@Param("sql") String sql);

    /**
     * 通用插入方法
     *
     * @param helloworld
     * @return
     */
    @Insert("insert into helloworld(api, core, data) " +
            "values(#{api}, #{core}, #{data})")
    int insertNew(Helloworld helloworld);

    /**
     * 通用更新方法
     *
     * @param helloworld
     * @return
     */
    @Update("{sql}")
    int updateBySql(Helloworld helloworld);

    /**
     * 通用删除方法
     *
     * @param id
     * @return
     */
    @Delete("delete from helloworld where id = #{id}")
    int deleteById(@Param("id") int id);

    /**
     * 清空方法
     *
     * @return
     */
    @Delete("delete from helloworld")
    int deleteall();
}