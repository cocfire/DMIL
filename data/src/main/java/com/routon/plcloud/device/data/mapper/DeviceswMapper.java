package com.routon.plcloud.device.data.mapper;

import com.routon.plcloud.device.data.entity.Devicesw;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author FireWang
 * @date 2020/5/07 15:09
 */
@Mapper
@Repository
public interface DeviceswMapper {

    /**
     * 通用查询方法
     *
     * @param sql
     * @return
     */
    @Select("${sql}")
    List<Devicesw> selectBySql(@Param("sql") String sql);

    /**
     * 通用插入方法
     *
     * @param devicesw
     * @return
     */
    @Insert("insert into devicesw(softwarename, erpcode, softwareversion, remark, status, createtime, moditytime, deviceid)" +
        " values(#{softwarename}, #{erpcode}, #{softwareversion}, #{remark}, #{status}, #{createtime}, #{moditytime}, #{deviceid})")
    int insertNew(Devicesw devicesw);

    /**
     * 通用更新方法
     *
     * @param devicesw
     * @return
     */
    @Update("update devicesw set id=#{id}, softwarename=#{softwarename}, erpcode=#{erpcode}, softwareversion=#{softwareversion}, remark=#{remark}, status=#{status}, createtime=#{createtime}, moditytime=#{moditytime}, deviceid=#{deviceid}" +
        " where id = #{id}")
    int updateBySql(Devicesw devicesw);

    /**
     * 通用删除方法
     *
     * @param id
     * @return
     */
    @Delete("delete from devicesw where id = #{id}")
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