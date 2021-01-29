package com.routon.plcloud.device.core.serviceimpl;

import com.routon.plcloud.device.data.entity.Datastats;
import com.routon.plcloud.device.data.mapper.DatastatsMapper;
import com.routon.plcloud.device.core.service.DatastatsService;
import com.routon.plcloud.device.core.utils.ConvUtil;
import com.routon.plcloud.device.core.utils.SqlUtil;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * @author FireWang
 * @date 2020/12/10 10:00
 */
@Service
public class DatastatsServiceImpl implements DatastatsService {
    @Autowired
    private DatastatsMapper datastatsMapper;

    @Override
    public boolean insert(Datastats datastats) {
        if (datastatsMapper.insertNew(datastats) > 0) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean delete(Datastats datastats) {
        if (datastatsMapper.deleteById(ConvUtil.convToInt(datastats.getId())) > 0) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean update(Datastats datastats) {
        boolean succ = false;
        int ns = datastatsMapper.updateBySql(datastats);
        if (ns > 0) {
            succ = true;
        }
        return succ;
    }

    @Override
    public Datastats getById(Integer id) {
        List<Datastats> list = datastatsMapper.selectBySql("SELECT * FROM datastats where id = " + id);
        if (list.size() > 0) {
            return list.get(0);
        } else {
            return null;
        }
    }

    @Override
    public Datastats getByLink(Integer linkid, String termsn, String playdate) {
        String sql = "SELECT * FROM datastats ";
        String whesql =  " WHERE  infob = '"+ playdate +"' and linkinfo = '"+ termsn +"' and linkid = " + linkid;
        List<Datastats> list = datastatsMapper.selectBySql(sql + whesql);
        if (list.size() > 0) {
            return list.get(0);
        } else {
            return null;
        }
    }

    @Override
    public Integer getMaxCount(Map<String, Object> paramMap){
        String getallsql = "SELECT COUNT(id) FROM datastats ";
        String wheresql = getsql(paramMap);
        Integer target = datastatsMapper.countBySql(getallsql + wheresql);
        return target;
    }

    @Override
    public List<Datastats> getMaxList(Map<String, Object> paramMap) {
        String getallsql = "SELECT * FROM datastats ";
        String limitsql = " order by id desc";
        String wheresql = getsql(paramMap);
        List<Datastats> target = datastatsMapper.selectBySql(getallsql + wheresql + limitsql);
        return target;
    }

    @Override
    public List<Datastats> searchforpage(Map<String, Object> paramMap, Integer page, Integer pageSize) {
        int startrow = pageSize * (page - 1);
        String getallsql = "SELECT * FROM datastats ";
        String wheresql = getsql(paramMap);
        String limitsql = " order by id desc limit " + pageSize + " offset " + startrow;

        List<Datastats> target = datastatsMapper.selectBySql(getallsql + wheresql + limitsql);
        return target;
    }

    private int switchLen(Integer type) {
        int cutLen = 10;
        switch (type) {
            case 0:
                cutLen = 1;
                break;
            case 1:
                cutLen = 4;
                break;
            case 2:
                cutLen = 7;
                break;
            case 3:
                cutLen = 10;
                break;
            default:
                break;
        }
        return cutLen;
    }

    @Override
    public List<Map> getDataByType(Map<String, Object> paramMap, Integer type) {
        //根据统计类型设置sql值
        int cutLen = switchLen(type);
        String qureysql = "SELECT LEFT(infob,"+ cutLen +") as time, " +
                "SUM(infoa) count, COUNT(DISTINCT linkinfo) dcount, COUNT(DISTINCT infob) days " +
                "FROM datastats ";
        String wheresql = getsql(paramMap);
        String limitsql = " GROUP BY time ORDER BY time DESC";

        List<Map> target = datastatsMapper.getDataBySql(qureysql + wheresql + limitsql);

        return target;
    }

    @Override
    public List<Map> getDataByTypeForPage(Map<String, Object> paramMap, Integer type, Integer page, Integer pageSize) {
        //根据统计类型设置sql值
        int cutLen = switchLen(type);
        String qureysql = "SELECT LEFT(infob,"+ cutLen +") as time, " +
                "SUM(infoa) count, COUNT(DISTINCT linkinfo) dcount, COUNT(DISTINCT infob) days " +
                "FROM datastats ";
        String wheresql = getsql(paramMap);
        int startrow = pageSize * (page - 1);
        String limitsql = " GROUP BY time ORDER BY time DESC LIMIT " + pageSize + " OFFSET " + startrow;

        List<Map> target = datastatsMapper.getDataBySql(qureysql + wheresql + limitsql);

        return target;
    }

    @Override
    public List<Map> getDataByType1(Map<String, Object> paramMap, Integer type) {
        //根据统计类型设置sql值
        int cutLen = switchLen(type);
        String qureysql = "SELECT COALESCE(LEFT(infob,"+ cutLen +")||','||b.remark||','||c.remark, '') info, SUM(infoa) counts " +
                "FROM datastats a " +
                "LEFT JOIN device b ON b.termsn = a.linkinfo " +
                "LEFT JOIN fileinfo c ON c.id = a.linkid ";
        String wheresql = getsql(paramMap);
        String limitsql = " GROUP BY info ORDER BY info DESC";

        List<Map> target = datastatsMapper.getDataBySql(qureysql + wheresql + limitsql);

        return target;
    }

    @Override
    public List<Map> getDataByTypeForPage1(Map<String, Object> paramMap, Integer type, Integer page, Integer pageSize) {
        //根据统计类型设置sql值
        int cutLen = switchLen(type);
        String qureysql = "SELECT COALESCE(LEFT(infob,"+ cutLen +")||','||b.remark||','||c.remark, '') info, SUM(infoa) counts " +
                "FROM datastats a " +
                "LEFT JOIN device b ON b.termsn = a.linkinfo " +
                "LEFT JOIN fileinfo c ON c.id = a.linkid ";
        String wheresql = getsql(paramMap);
        int startrow = pageSize * (page - 1);
        String limitsql = " GROUP BY info ORDER BY info DESC LIMIT " + pageSize + " OFFSET " + startrow;

        List<Map> target = datastatsMapper.getDataBySql(qureysql + wheresql + limitsql);

        return target;
    }

    @Override
    public List<Datastats> getDataClounmByCondition(String clounm, Map<String, Object> condition) throws Exception {
        String getNum = "SELECT DISTINCT " + clounm + " FROM datastats ";
        String wheresql = "";
        if (condition != null) {
            wheresql += getsql(condition);
        }
        String sql = getNum + wheresql + " and " + clounm + " is not null and " + clounm + " != ''";
        List<Datastats> dataList = datastatsMapper.selectBySql(sql);
        return dataList;
    }

    /**
     * 公共方法：根据条件获取查询条件sql语句
     **/
    private String getsql(Map<String, Object> paramMap) {
        String wheresql = " where 1=1 ";
        wheresql += SqlUtil.getLikeSql(paramMap, "remark");
        wheresql += SqlUtil.getAsSql(paramMap, "id");
        wheresql += SqlUtil.getAsSql(paramMap, "linktable");
        wheresql += SqlUtil.getAsSql(paramMap, "linkid");
        wheresql += SqlUtil.getAsSql(paramMap, "b.companyid");

        String start = ConvUtil.convToStr(paramMap.get("start")), end = ConvUtil.convToStr(paramMap.get("end"));
        if (!"".equals(start)) {
            wheresql = wheresql + " and infob >= '" + start + "' ";
        }
        if (!"".equals(end)) {
            wheresql = wheresql + " and infob <= '" + end + "' ";
        }

        return wheresql;
    }
}