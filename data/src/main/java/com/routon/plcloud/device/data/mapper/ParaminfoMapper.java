package com.routon.plcloud.device.data.mapper;

import com.routon.plcloud.device.data.entity.Paraminfo;
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
public interface ParaminfoMapper {

    /**
     * 通用查询方法
     *
     * @param sql
     * @return
     */
    @Select("${sql}")
    List<Paraminfo> selectBySql(@Param("sql") String sql);

    /**
     * 通用插入方法
     *
     * @param paraminfo
     * @return
     */
    @Insert("insert into paraminfo(linkid, termsn, paramid, paraminfo, createtime, modifytime, status, remark)" +
        " values(#{linkid}, #{termsn}, #{paramid}, #{paraminfo}, #{createtime}, #{modifytime}, #{status}, #{remark})")
    int insertNew(Paraminfo paraminfo);

    /**
     * 通用更新方法
     *
     * @param paraminfo
     * @return
     */
    @Update("update paraminfo set id=#{id}, linkid=#{linkid}, termsn=#{termsn}, paramid=#{paramid}, paraminfo=#{paraminfo}, createtime=#{createtime}, modifytime=#{modifytime}, status=#{status}, remark=#{remark}" +
        " where id = #{id}")
    int updateBySql(Paraminfo paraminfo);

    /**
     * 通用删除方法
     *
     * @param id
     * @return
     */
    @Delete("delete from paraminfo where id = #{id}")
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
     * 通用查询方法2
     *
     * @param sql
     * @return
     */
    @Select("${sql}")
    List<Map> getBySql(@Param("sql") String sql);
}