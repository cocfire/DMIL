package com.routon.plcloud.device.data.mapper;

import com.routon.plcloud.device.data.entity.Syslog;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author FireWang
 * @date 2020/5/07 15:09
 */
@Mapper
@Repository
public interface SyslogMapper {

    /**
     * 通用查询方法
     *
     * @param sql
     * @return
     */
    @Select("${sql}")
    List<Syslog> selectBySql(@Param("sql") String sql);

    /**
     * 通用插入方法
     *
     * @param syslog
     * @return
     */
    @Insert("insert into syslog( option, type, loginfo, timestamp, userid, userip, remark)" +
            " values(#{option}, #{type}, #{loginfo}, #{timestamp}, #{userid}, #{userip}, #{remark})")
    int insertNew(Syslog syslog);

    /**
     * 通用更新方法
     *
     * @param syslog
     * @return
     */
    @Update("update syslog set id=#{id}, option=#{option}, type=#{type}, loginfo=#{loginfo}, timestamp=#{timestamp}, userid=#{userid}, userip=#{userip}, remark=#{remark}" +
            " where id = #{id}")
    int updateBySql(Syslog syslog);

    /**
     * 通用删除方法
     *
     * @param id
     * @return
     */
    @Delete("delete from syslog where id = #{id}")
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