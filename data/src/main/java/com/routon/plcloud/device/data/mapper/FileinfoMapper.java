package com.routon.plcloud.device.data.mapper;

import com.routon.plcloud.device.data.entity.Fileinfo;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author FireWang
 * @date 2020/5/07 15:09
 */
@Mapper
@Repository
public interface FileinfoMapper {

    /**
     * 通用查询方法
     *
     * @param sql
     * @return
     */
    @Select("${sql}")
    List<Fileinfo> selectBySql(@Param("sql") String sql);

    /**
     * 通用插入方法
     *
     * @param fileinfo
     * @return
     */
    @Insert("insert into fileinfo(filename, filesize, filetype, filepath, uploadname, uploadtime, downstatus, md5size, fitmodel, remark, userid)" +
        " values(#{filename}, #{filesize}, #{filetype}, #{filepath}, #{uploadname}, #{uploadtime}, #{downstatus}, #{md5size}, #{fitmodel}, #{remark}, #{userid})")
    int insertNew(Fileinfo fileinfo);

    /**
     * 通用更新方法
     *
     * @param fileinfo
     * @return
     */
    @Update("update fileinfo set id=#{id}, filename=#{filename}, filesize=#{filesize}, filetype=#{filetype}, filepath=#{filepath}, uploadname=#{uploadname}, uploadtime=#{uploadtime}, downstatus=#{downstatus}, md5size=#{md5size}, fitmodel=#{fitmodel}, remark=#{remark}, userid=#{userid}" +
        " where id = #{id}")
    int updateBySql(Fileinfo fileinfo);

    /**
     * 通用删除方法
     *
     * @param id
     * @return
     */
    @Delete("delete from fileinfo where id = #{id}")
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