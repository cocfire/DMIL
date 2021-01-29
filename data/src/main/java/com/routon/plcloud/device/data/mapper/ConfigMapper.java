package com.routon.plcloud.device.data.mapper;

import com.routon.plcloud.device.data.entity.Config;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author FireWang
 * @date 2020/5/07 15:09
 */
@Mapper
@Repository
public interface ConfigMapper {

    /**
     * 通用查询方法
     *
     * @param sql
     * @return
     */
    @Select("${sql}")
    List<Config> selectBySql(@Param("sql") String sql);

    /**
     * 通用插入方法
     *
     * @param config
     * @return
     */
    @Insert("insert into config(name, type, configid, configinfo, createtime, modifytime, status, remark)" +
        " values(#{name}, #{type}, #{configid}, #{configinfo}, #{createtime}, #{modifytime}, #{status}, #{remark})")
    int insertNew(Config config);

    /**
     * 通用更新方法
     *
     * @param config
     * @return
     */
    @Update("update config set id=#{id}, name=#{name}, type=#{type}, configid=#{configid}, configinfo=#{configinfo}, createtime=#{createtime}, modifytime=#{modifytime}, status=#{status}, remark=#{remark}" +
        " where id = #{id}")
    int updateBySql(Config config);

    /**
     * 通用删除方法
     *
     * @param id
     * @return
     */
    @Delete("delete from config where id = #{id}")
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