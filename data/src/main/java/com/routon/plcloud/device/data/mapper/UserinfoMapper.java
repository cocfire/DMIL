package com.routon.plcloud.device.data.mapper;

import com.routon.plcloud.device.data.entity.Userinfo;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository
public interface UserinfoMapper {
    @Select("${sql}")
    List<Userinfo> selectBySql(@Param("sql") String sql);

    @Insert("insert into userinfo( id, openid, username, realname, password, phone, createtime, modifytime, status, counts, lastloginip, lastlogintime, company, project, remark)" +
        " values( #{id}, #{openid}, #{username}, #{realname}, #{password}, #{phone}, #{createtime}, #{modifytime}, #{status}, #{counts}, #{lastloginip}, #{lastlogintime}, #{company}, #{project}, #{remark})")
    int insertNew(Userinfo userinfo);

    @Update("update userinfo set id=#{id}, openid=#{openid}, username=#{username}, realname=#{realname}, password=#{password}, phone=#{phone}, createtime=#{createtime}, modifytime=#{modifytime}, status=#{status}, counts=#{counts}, lastloginip=#{lastloginip}, lastlogintime=#{lastlogintime}, company=#{company}, project=#{project}, remark=#{remark}" +
            " where id = #{id}")
    int uopdateBySql(Userinfo userinfo);

    @Delete("delete from userinfo where id = #{id}")
    int deleteById(@Param("id") int id);
}