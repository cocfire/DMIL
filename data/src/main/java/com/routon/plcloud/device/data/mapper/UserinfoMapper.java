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

    @Insert("insert into userinfo( id, openid, username, realname, password, phone, createtime, modifytime, stutas, counts, lastloginip, lastlogintime, company, project, remark)" +
        " values( #{id}, #{openid}, #{username}, #{realname}, #{password}, #{phone}, #{createtime}, #{modifytime}, #{stutas}, #{counts}, #{lastloginip}, #{lastlogintime}, #{company}, #{project}, #{remark})")
    int insertNew(Userinfo userinfo);

    @Update("{sql}")
    int uopdateBySql(Userinfo userinfo);

    @Delete("delete from userinfo where id = #{id}")
    int deleteById(@Param("id") int id);
}