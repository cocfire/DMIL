package com.routon.plcloud.device.data.mapper;

import com.routon.plcloud.device.data.entity.Programs;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author FireWang
 * @date 2020/5/07 15:09
 */
@Mapper
@Repository
public interface ProgramsMapper {

    /**
     * 通用查询方法
     *
     * @param sql
     * @return
     */
    @Select("${sql}")
    List<Programs> selectBySql(@Param("sql") String sql);

    /**
     * 通用插入方法
     *
     * @param programs
     * @return
     */
    @Insert("insert into programs(name, type, fileids, timelast, starttime, endtime, createtime, modifytime, fitmodel, remark, createuser, companyid, projectid)" +
        " values(#{name}, #{type}, #{fileids}, #{timelast}, #{starttime}, #{endtime}, #{createtime}, #{modifytime}, #{fitmodel}, #{remark}, #{createuser}, #{companyid}, #{projectid})")
    int insertNew(Programs programs);

    /**
     * 通用更新方法
     *
     * @param programs
     * @return
     */
    @Update("update programs set id=#{id}, name=#{name}, type=#{type}, fileids=#{fileids}, timelast=#{timelast}, starttime=#{starttime}, endtime=#{endtime}, createtime=#{createtime}, modifytime=#{modifytime}, fitmodel=#{fitmodel}, remark=#{remark}, createuser=#{createuser}, companyid=#{companyid}, projectid=#{projectid}" +
        " where id = #{id}")
    int updateBySql(Programs programs);

    /**
     * 通用删除方法
     *
     * @param id
     * @return
     */
    @Delete("delete from programs where id = #{id}")
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