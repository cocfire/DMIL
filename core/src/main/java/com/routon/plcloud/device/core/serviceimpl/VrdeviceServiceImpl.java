package com.routon.plcloud.device.core.serviceimpl;

import com.routon.plcloud.device.core.service.VrdeviceService;
import com.routon.plcloud.device.core.utils.ConvUtil;
import com.routon.plcloud.device.core.utils.SqlUtil;
import com.routon.plcloud.device.data.entity.Vrdevice;
import com.routon.plcloud.device.data.mapper.VrdeviceMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * @author FireWang
 * @date 2020/5/07 15:09
 */
@Service
public class VrdeviceServiceImpl implements VrdeviceService {
    @Autowired
    private VrdeviceMapper vrdeviceMapper;

    @Override
    public boolean insertVrDevice(Vrdevice vrdevice) throws Exception {
        if (vrdeviceMapper.insertNew(vrdevice) > 0) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean deleteVrDevice(Vrdevice vrdevice) throws Exception {
        if (vrdeviceMapper.deleteById(ConvUtil.convToInt(vrdevice.getId())) > 0) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean update(Vrdevice vrdevice) throws Exception {
        int ns = vrdeviceMapper.updateBySql(vrdevice);
        if (ns > 0) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public Vrdevice searchByDeviceId(String vrdeviceid) throws Exception {
        Vrdevice vrdevice = null;
        List<Vrdevice> list = vrdeviceMapper.selectBySql("select * from vrdevice where vrdeviceid = '" + vrdeviceid +"'");
        if (list.size() > 0) {
            vrdevice = list.get(0);
        }
        return vrdevice;
    }

    @Override
    public Vrdevice getById(Integer id) throws Exception {
        Vrdevice vrdevice = null;
        List<Vrdevice> list = vrdeviceMapper.selectBySql("select * from vrdevice where id = " + id);
        if (list.size() > 0) {
            vrdevice = list.get(0);
        }
        return vrdevice;
    }

    @Override
    public Integer getMaxCount(Map<String, Object> paramMap){
        String getallsql = "SELECT COUNT(id) FROM vrdevice ";
        String wheresql = getsql(paramMap);
        Integer target = vrdeviceMapper.countBySql(getallsql + wheresql);
        return target;
    }

    @Override
    public List<Vrdevice> getMaxList(Map<String, Object> paramMap) throws Exception {
        String getallsql = "SELECT DISTINCT * FROM vrdevice ";
        String wheresql = getsql(paramMap);
        List<Vrdevice> target = vrdeviceMapper.selectBySql(getallsql + wheresql);
        return target;
    }

    @Override
    public List<Vrdevice> searchvrdeviceforpage(Map<String, Object> paramMap, Integer page, Integer pageSize) throws Exception {
        //如果page，pageSize为null，则设为1
        if (page == null || page <= 0) {
            page = 1;
        }
        if (pageSize == null || pageSize <= 0) {
            pageSize = 10;
        }

        int startrow = pageSize * (page - 1);
        String getallsql = "SELECT * FROM vrdevice ";
        String limitsql = " order by id desc limit " + pageSize + " offset " + startrow;
        String wheresql = getsql(paramMap);

        List<Vrdevice> target = vrdeviceMapper.selectBySql(getallsql + wheresql + limitsql);
        return target;
    }

    /**
     * 公共方法：根据条件获取查询条件sql语句
     **/
    private String getsql(Map<String, Object> paramMap) {
        String wheresql = "where 1=1 ";

        wheresql += SqlUtil.getLikeSql(paramMap, "vrdeviceid");
        wheresql += SqlUtil.getLikeSql(paramMap, "name");
        wheresql += SqlUtil.getLikeSql(paramMap, "info");
        wheresql += SqlUtil.getLikeSql(paramMap, "remark");
        wheresql += SqlUtil.getLikeSql(paramMap, "status");

        wheresql += SqlUtil.getAsSql(paramMap, "id");
        wheresql += SqlUtil.getAsSql(paramMap, "type");
        wheresql += SqlUtil.getAsSql(paramMap, "version");

        return wheresql;
    }
}