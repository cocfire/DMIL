package com.routon.plcloud.device.data.mapper;

import com.routon.plcloud.device.data.entity.Offlinemsg;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author FireWang
 * @date 2020/5/07 15:09
 */
@Mapper
@Repository
public interface OfflinemsgMapper {

    /**
     * 通用查询方法
     *
     * @param sql
     * @return
     */
    @Select("${sql}")
    List<Offlinemsg> selectBySql(@Param("sql") String sql);

    /**
     * 通用插入方法
     *
     * @param offlinemsg
     * @return
     */
    @Insert("insert into offlinemsg(topic, message, clientid, createtime, moditytime, status, counts, remark)" +
            " values(#{topic}, #{message}, #{clientid}, #{createtime}, #{moditytime}, #{status}, #{counts}, #{remark})")
    int insertNew(Offlinemsg offlinemsg);

    /**
     * 通用更新方法
     *
     * @param offlinemsg
     * @return
     */
    @Update("update offlinemsg set id=#{id}, topic=#{topic}, message=#{message}, clientid=#{clientid}, createtime=#{createtime}, moditytime=#{moditytime}, status=#{status}, counts=#{counts}, remark=#{remark}" +
            " where id = #{id}")
    int updateBySql(Offlinemsg offlinemsg);

    /**
     * 通用删除方法
     *
     * @param id
     * @return
     */
    @Delete("delete from offlinemsg where id = #{id}")
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