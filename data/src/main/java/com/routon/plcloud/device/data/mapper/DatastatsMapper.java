package com.routon.plcloud.device.data.mapper;

import com.routon.plcloud.device.data.entity.Datastats;
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
public interface DatastatsMapper {

    /**
     * 通用查询方法
     *
     * @param sql
     * @return
     */
    @Select("${sql}")
    List<Datastats> selectBySql(@Param("sql") String sql);

    /**
     * 通用插入方法
     *
     * @param datastats
     * @return
     */
    @Insert("insert into datastats(linktable, linkid, linkinfo, infoa, infob, stats, modifytime, createtime, remark, companyid)" +
        " values(#{linktable}, #{linkid}, #{linkinfo}, #{infoa}, #{infob}, #{stats}, #{modifytime}, #{createtime}, #{remark}, #{companyid})")
    int insertNew(Datastats datastats);

    /**
     * 通用更新方法
     *
     * @param datastats
     * @return
     */
    @Update("update datastats set id=#{id}, linktable=#{linktable}, linkid=#{linkid}, linkinfo=#{linkinfo}, infoa=#{infoa}, infob=#{infob}, stats=#{stats}, modifytime=#{modifytime}, createtime=#{createtime}, remark=#{remark}, companyid=#{companyid}" +
        " where id = #{id}")
    int updateBySql(Datastats datastats);

    /**
     * 通用删除方法
     *
     * @param id
     * @return
     */
    @Delete("delete from datastats where id = #{id}")
    int deleteById(@Param("id") int id);

    /**
     * 通用查询总数方法
     *
     * @param sql
     * @return
     */
    @Select("${sql}")
    int countBySql(@Param("sql") String sql);

    /**
     * 通用查询总数方法
     *
     * @param sql
     * @return
     */
    @Select("${sql}")
    List<Map> getDataBySql(@Param("sql") String sql);
}