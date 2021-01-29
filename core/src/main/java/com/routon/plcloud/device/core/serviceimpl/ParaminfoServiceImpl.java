package com.routon.plcloud.device.core.serviceimpl;

import com.routon.plcloud.device.core.service.DeviceService;
import com.routon.plcloud.device.data.entity.Paraminfo;
import com.routon.plcloud.device.data.mapper.ParaminfoMapper;
import com.routon.plcloud.device.core.service.ParaminfoService;
import com.routon.plcloud.device.core.utils.ConvUtil;
import com.routon.plcloud.device.core.utils.SqlUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * @author FireWang
 * @date 2020/12/10 10:00
 */
@Service
public class ParaminfoServiceImpl implements ParaminfoService {
    @Autowired
    private ParaminfoMapper paraminfoMapper;

    @Autowired
    private DeviceService deviceService;

    @Override
    public boolean insert(Paraminfo paraminfo) {
        if (paraminfoMapper.insertNew(paraminfo) > 0) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean delete(Paraminfo paraminfo) {
        if (paraminfoMapper.deleteById(ConvUtil.convToInt(paraminfo.getId())) > 0) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean update(Paraminfo paraminfo) {
        boolean succ = false;
        int ns = paraminfoMapper.updateBySql(paraminfo);
        if (ns > 0) {
            succ = true;
        }
        return succ;
    }

    @Override
    public Paraminfo getById(Integer id) {
        List<Paraminfo> list = paraminfoMapper.selectBySql("SELECT * FROM paraminfo where id = " + id);
        if (list.size() > 0) {
            return list.get(0);
        } else {
            return null;
        }
    }

    @Override
    public Paraminfo getByLinkId(Integer id) {
        List<Paraminfo> list = paraminfoMapper.selectBySql("SELECT * FROM paraminfo where linkid = " + id);
        if (list.size() > 0) {
            return list.get(0);
        } else {
            return null;
        }
    }

    @Override
    public Paraminfo getBySn(String termsn) {
        List<Paraminfo> list = paraminfoMapper.selectBySql("SELECT * FROM paraminfo where termsn = '" + termsn + "'");
        if (list.size() > 0) {
            return list.get(0);
        } else {
            return null;
        }
    }

    @Override
    public Paraminfo getByParamidAndSn(int paramid, String termsn) {
        List<Paraminfo> list = paraminfoMapper.selectBySql("SELECT * FROM paraminfo " +
                "where paramid = "+ paramid +" AND termsn = '" + termsn + "'");
        if (list.size() > 0) {
            return list.get(0);
        } else {
            return null;
        }
    }

    @Override
    public List<Paraminfo> getMaxList(Map<String, Object> paramMap) {
        String getallsql = "SELECT * FROM paraminfo ";
        String limitsql = " order by id desc";
        String wheresql = " where 1=1";
        wheresql += SqlUtil.getLikeSql(paramMap, "termsn");
        wheresql += SqlUtil.getAsSql(paramMap, "paramid");
        wheresql += SqlUtil.getAsSql(paramMap, "status");
        List<Paraminfo> target = paraminfoMapper.selectBySql(getallsql + wheresql + limitsql);
        return target;
    }

    @Override
    public List<Paraminfo> searchforpage(Map<String, Object> paramMap, Integer page, Integer pageSize) {
        int startrow = pageSize * (page - 1);
        String getallsql = "SELECT * FROM paraminfo ";
        String wheresql = " where 1=1";
        wheresql += SqlUtil.getLikeSql(paramMap, "termsn");
        wheresql += SqlUtil.getAsSql(paramMap, "paramid");
        String limitsql = " order by id desc limit " + pageSize + " offset " + startrow;

        List<Paraminfo> target = paraminfoMapper.selectBySql(getallsql + wheresql + limitsql);
        return target;
    }

    @Override
    public Integer getMaxCount(Map<String, Object> paramMap){
        String getallsql = "SELECT COUNT(id) id FROM ";
        String wheresql = getParamSql(paramMap, "");
        getallsql = getallsql + wheresql;
        List<Paraminfo> target = paraminfoMapper.selectBySql(getallsql);
        if (target.size() > 0) {
            Paraminfo p = target.get(0);
            return ConvUtil.convToInt(p.getId());
        } else {
            return 0;
        }
    }

    @Override
    public List<Paraminfo> searchparamforpage(Map<String, Object> paramMap, Integer page, Integer pageSize) {
        int startrow = pageSize * (page - 1);
        String getallsql = "SELECT * FROM ";
        String limitsql = " LIMIT " + pageSize + " OFFSET " + startrow;
        String wheresql = getParamSql(paramMap, limitsql);
        getallsql = getallsql + wheresql;

        List<Paraminfo> target = paraminfoMapper.selectBySql(getallsql);
        return target;
    }

    @Override
    public List<Paraminfo> getVaildList(String termsn) {
        String sql = "SELECT DISTINCT a.* FROM paraminfo a " +
                "LEFT JOIN config b ON b.configid = a.paramid " +
                "LEFT JOIN devicesw c ON c.erpcode = b.configinfo " +
                "LEFT JOIN device d ON d.id = c.deviceid " +
                "WHERE b.type = 2 AND b.status = 1 AND c.status = 1 " +
                "AND a.termsn = '" + termsn + "' " +
                "AND d.termsn = '" + termsn + "' ";
        return paraminfoMapper.selectBySql(sql);
    }

    /** 获取设备参数相关sql
     *
     */
    private String getParamSql(Map<String, Object> paramMap, String limit) {
        String target = "(SELECT DISTINCT " +
                "a.id, a.termsn, a.status, a.termmodel paraminfo, COALESCE(a.remark,'') remark, " +
                "COUNT(DISTINCT d.paramid) linkid FROM device a " +
                "LEFT JOIN devicesw b ON b.deviceid = a.id " +
                "LEFT JOIN config c ON c.configinfo = b.erpcode " +
                "LEFT JOIN paraminfo d ON d.termsn = a.termsn " +
                "AND d.paramid = c.configid AND b.status=1 AND c.status=1 AND c.type=2 ";
        String wheresql = getsql(paramMap);
        target = target + wheresql + " GROUP BY a.id ORDER BY a.id DESC " + ") e";
        boolean paramSet = ConvUtil.convToBool(paramMap.get("isSet"));
        if (paramSet) {
            target += " WHERE e.linkid != '0'";
        }
        return target + limit;
    }

    /**
     * 公共方法：根据条件获取查询条件sql语句
     **/
    private String getsql(Map<String, Object> paramMap) {
        String wheresql = "where clientid is not null and clientid != '' and emqversion !='' ";
        wheresql += SqlUtil.getAsSql(paramMap, "id");
        wheresql += SqlUtil.getAsSql(paramMap, "termmodel");
        wheresql += SqlUtil.getAsSql(paramMap, "status");
        wheresql += SqlUtil.getAsSql(paramMap, "a.status");
        wheresql += SqlUtil.getAsSql(paramMap, "b.erpcode");

        wheresql += SqlUtil.getLikeSql(paramMap, "termsn");
        wheresql += SqlUtil.getLikeSql(paramMap, "a.termsn");
        wheresql += SqlUtil.getLikeSql(paramMap, "remark");
        wheresql += SqlUtil.getLikeSql(paramMap, "a.remark");

        String start = ConvUtil.convToStr(paramMap.get("start")), end = ConvUtil.convToStr(paramMap.get("end"));
        if (!"".equals(start)) {
            wheresql += " and createtime >= '" + start + "' ";
        }
        if (!"".equals(end)) {
            wheresql += " and createtime <= '" + end + "' ";
        }

        wheresql += SqlUtil.getSqlByGroup(paramMap);

        return wheresql;
    }
}