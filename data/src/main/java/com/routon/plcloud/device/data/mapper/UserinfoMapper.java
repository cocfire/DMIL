package com.routon.plcloud.device.data.mapper;

import com.routon.plcloud.device.data.entity.Userinfo;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author FireWang
 * @date 2020/5/07 15:09
 */
@Mapper
@Repository
public interface UserinfoMapper {

    /**
     * 通用查询方法
     *
     * @param sql
     * @return
     */
    @Select("${sql}")
    List<Userinfo> selectBySql(@Param("sql") String sql);

    /**
     * 通用插入方法
     *
     * @param userinfo
     * @return
     */
    @Insert("insert into userinfo(openid, username, realname, password, phone, createtime, modifytime, status, counts, lastloginip, lastlogintime, company, project, remark)" +
            " values(#{openid}, #{username}, #{realname}, #{password}, #{phone}, #{createtime}, #{modifytime}, #{status}, #{counts}, #{lastloginip}, #{lastlogintime}, #{company}, #{project}, #{remark})")
    int insertNew(Userinfo userinfo);

    /**
     * 通用更新方法
     *
     * @param userinfo
     * @return
     */
    @Update("update userinfo set id=#{id}, openid=#{openid}, username=#{username}, realname=#{realname}, password=#{password}, phone=#{phone}, createtime=#{createtime}, modifytime=#{modifytime}, status=#{status}, counts=#{counts}, lastloginip=#{lastloginip}, lastlogintime=#{lastlogintime}, company=#{company}, project=#{project}, remark=#{remark}" +
            " where id = #{id}")
    int updateBySql(Userinfo userinfo);

    /**
     * 通用删除方法
     *
     * @param id
     * @return
     */
    @Delete("delete from userinfo where id = #{id}")
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