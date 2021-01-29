package com.routon.plcloud.device.data.mapper;

import com.routon.plcloud.device.data.entity.Vrdevice;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author FireWang
 * @date 2020/5/07 15:09
 */
@Mapper
@Repository
public interface VrdeviceMapper {

    /**
     * 通用查询方法
     *
     * @param sql
     * @return
     */
    @Select("${sql}")
    List<Vrdevice> selectBySql(@Param("sql") String sql);

    /**
     * 通用插入方法
     *
     * @param vrdevice
     * @return
     */
    @Insert("insert into vrdevice(vrdeviceid, name, type, info, remark, status, createtime, updatetime, link, version)" +
        " values(#{vrdeviceid}, #{name}, #{type}, #{info}, #{remark}, #{status}, #{createtime}, #{updatetime}, #{link}, #{version})")
    int insertNew(Vrdevice vrdevice);

    /**
     * 通用更新方法
     *
     * @param vrdevice
     * @return
     */
    @Update("update vrdevice set id=#{id}, vrdeviceid=#{vrdeviceid}, name=#{name}, type=#{type}, info=#{info}, remark=#{remark}, status=#{status}, createtime=#{createtime}, updatetime=#{updatetime}, link=#{link}, version=#{version}" +
        " where id = #{id}")
    int updateBySql(Vrdevice vrdevice);

    /**
     * 通用删除方法
     *
     * @param id
     * @return
     */
    @Delete("delete from vrdevice where id = #{id}")
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