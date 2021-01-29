package com.routon.plcloud.device.data.mapper;

import com.routon.plcloud.device.data.entity.Software;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author FireWang
 * @date 2020/5/07 15:09
 */
@Mapper
@Repository
public interface SoftwareMapper {

    /**
     * 通用查询方法
     *
     * @param sql
     * @return
     */
    @Select("${sql}")
    List<Software> selectBySql(@Param("sql") String sql);

    /**
     * 通用插入方法
     *
     * @param software
     * @return
     */
    @Insert("insert into software(softwarename, softwareversion, uploadtime, size, customername, downstatus, createtime, moditytime, uploadsoftwarename, erpcode, filetimestamp, softwaretype, signflag, qrcode, qrpath, fitmodel, remark, pubflag)" +
            " values(#{softwarename}, #{softwareversion}, #{uploadtime}, #{size}, #{customername}, #{downstatus}, #{createtime}, #{moditytime}, #{uploadsoftwarename}, #{erpcode}, #{filetimestamp}, #{softwaretype}, #{signflag}, #{qrcode}, #{qrpath}, #{fitmodel}, #{remark}, #{pubflag})")
    int insertNew(Software software);

    /**
     * 通用更新方法
     *
     * @param software
     * @return
     */
    @Update("update software set id=#{id}, softwarename=#{softwarename}, softwareversion=#{softwareversion}, uploadtime=#{uploadtime}, size=#{size}, customername=#{customername}, downstatus=#{downstatus}, createtime=#{createtime}, moditytime=#{moditytime}, uploadsoftwarename=#{uploadsoftwarename}, erpcode=#{erpcode}, filetimestamp=#{filetimestamp}, softwaretype=#{softwaretype}, signflag=#{signflag}, qrcode=#{qrcode}, qrpath=#{qrpath}, fitmodel=#{fitmodel}, remark=#{remark}, pubflag=#{pubflag}" +
            " where id = #{id}")
    int updateBySql(Software software);

    /**
     * 通用删除方法
     *
     * @param id
     * @return
     */
    @Delete("delete from software where id = #{id}")
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