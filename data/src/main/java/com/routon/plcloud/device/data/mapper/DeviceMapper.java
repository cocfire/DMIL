package com.routon.plcloud.device.data.mapper;

import com.routon.plcloud.device.data.entity.Device;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author FireWang
 * @date 2020/5/07 15:09
 */
@Mapper
@Repository
public interface DeviceMapper {

    /**
     * 通用查询方法
     *
     * @param sql
     * @return
     */
    @Select("${sql}")
    List<Device> selectBySql(@Param("sql") String sql);

    /**
     * 通用插入方法
     *
     * @param device
     * @return
     */
    @Insert("insert into device(clientid, termsn, termid, termcode, termtype, terminfo, macaddress, hardwarename, machinetype, licencecode, softwarename, erpcode, softwareversion, orderid, projectid, projectname, companyid, companyname, clientname, clientcode, contact, address, telno, remark, status, createtime, moditytime, updatetime, link, termmodel, lanip, wifiip, emqversion, deviceid, imei, udid, uuid, barcode, facelibrid)" +
            " values(#{clientid}, #{termsn}, #{termid}, #{termcode}, #{termtype}, #{terminfo}, #{macaddress}, #{hardwarename}, #{machinetype}, #{licencecode}, #{softwarename}, #{erpcode}, #{softwareversion}, #{orderid}, #{projectid}, #{projectname}, #{companyid}, #{companyname}, #{clientname}, #{clientcode}, #{contact}, #{address}, #{telno}, #{remark}, #{status}, #{createtime}, #{moditytime}, #{updatetime}, #{link}, #{termmodel}, #{lanip}, #{wifiip}, #{emqversion}, #{deviceid}, #{imei}, #{udid}, #{uuid}, #{barcode}, #{facelibrid})")
    int insertNew(Device device);

    /**
     * 通用更新方法
     *
     * @param device
     * @return
     */
    @Update("update device set id=#{id}, clientid=#{clientid}, termsn=#{termsn}, termid=#{termid}, termcode=#{termcode}, termtype=#{termtype}, terminfo=#{terminfo}, macaddress=#{macaddress}, hardwarename=#{hardwarename}, machinetype=#{machinetype}, licencecode=#{licencecode}, softwarename=#{softwarename}, erpcode=#{erpcode}, softwareversion=#{softwareversion}, orderid=#{orderid}, projectid=#{projectid}, projectname=#{projectname}, companyid=#{companyid}, companyname=#{companyname}, clientname=#{clientname}, clientcode=#{clientcode}, contact=#{contact}, address=#{address}, telno=#{telno}, remark=#{remark}, status=#{status}, createtime=#{createtime}, moditytime=#{moditytime}, updatetime=#{updatetime}, link=#{link}, termmodel=#{termmodel}, lanip=#{lanip}, wifiip=#{wifiip}, emqversion=#{emqversion}, deviceid=#{deviceid}, imei=#{imei}, udid=#{udid}, uuid=#{uuid}, barcode=#{barcode}, facelibrid=#{facelibrid}" +
            " where id = #{id}")
    int updateBySql(Device device);

    /**
     * 通用删除方法
     *
     * @param id
     * @return
     */
    @Delete("delete from device where id = #{id}")
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