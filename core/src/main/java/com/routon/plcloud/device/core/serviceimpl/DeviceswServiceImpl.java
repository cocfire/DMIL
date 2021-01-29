package com.routon.plcloud.device.core.serviceimpl;

import com.routon.plcloud.device.core.service.DeviceswService;
import com.routon.plcloud.device.core.utils.ConvUtil;
import com.routon.plcloud.device.data.entity.Devicesw;
import com.routon.plcloud.device.data.mapper.DeviceswMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author FireWang
 * @date 2020/5/07 15:09
 */
@Service
public class DeviceswServiceImpl implements DeviceswService {
    @Autowired
    private DeviceswMapper deviceswMapper;


    @Override
    public boolean insertDevicesw(Devicesw devicesw) throws Exception {
        if (deviceswMapper.insertNew(devicesw) > 0) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean deleteDevicesw(Devicesw devicesw) throws Exception {
        if (deviceswMapper.deleteById(ConvUtil.convToInt(devicesw.getId())) > 0) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean update(Devicesw devicesw) throws Exception {
        int ns = deviceswMapper.updateBySql(devicesw);
        if (ns > 0) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public List<Devicesw> searchByDeviceId(Integer deviceid) throws Exception {
        return deviceswMapper.selectBySql("select * from devicesw where deviceid = " + deviceid);
    }

    @Override
    public Devicesw getById(Integer id) throws Exception {
        Devicesw devicesw = null;
        List<Devicesw> list = deviceswMapper.selectBySql("select * from devicesw where id = " + id);
        if (list.size() > 0) {
            devicesw = list.get(0);
        }
        return devicesw;
    }

    @Override
    public List<Devicesw> getByDeviceId(Integer deviceid) throws Exception {
        return deviceswMapper.selectBySql("select DISTINCT a.id, a.softwarename, a.softwareversion, a.erpcode, " +
                "COALESCE(b.customername, '未知软件名') as remark from devicesw a" +
                " left join software b on (a.erpcode = b.erpcode) " +
                "where status = 1 and deviceid = " + deviceid + " order by id");
    }

    @Override
    public List<Devicesw> canTheyUpdateByDeviceId(Map idmap) throws Exception {
        List<Devicesw> target = new ArrayList<>();

        //获取idmap的sql语句字符串
        StringBuilder ids = new StringBuilder();
        for (Object value : idmap.values()) {
            ids.append(value + ",");
        }
        if (ids.length() > 1) {
            String idsql = "where status = 1 and deviceid in (" + ids.substring(0, ids.length() - 1) + ")";

            //获取这些设备中运行的软件的全集，即这些设备中运行的所有种类的软件
            String erpsql = "select distinct erpcode, softwarename from devicesw " + idsql;
            List<Devicesw> erpListAll = deviceswMapper.selectBySql(erpsql);

            //根据软件列表全集，分别查询deviceid的集，如果他的size与idmap一样大，则说明这些设备都运行着该软件，则加入结果集target中
            for (Devicesw erp : erpListAll) {
                String deviceidsql = "select distinct deviceid from devicesw " + idsql + " and erpcode = '"+ erp.getErpcode() +"'";
                List<Devicesw> deviceidList = deviceswMapper.selectBySql(deviceidsql);
                if (deviceidList.size() == idmap.size()) {
                    target.add(erp);
                }
            }
        }

        return target;
    }

    @Override
    public Devicesw getUpdateByDeviceId(Integer deviceid) throws Exception {
        Devicesw devicesw = null;
        List<Devicesw> list = deviceswMapper.selectBySql("select * from devicesw where status = 2 and deviceid = " + deviceid);
        if (list.size() > 0) {
            devicesw = list.get(0);
        }
        return devicesw;
    }

    @Override
    public Devicesw getByDeviceIdAndName(Integer deviceid, String softwarename) throws Exception {
        return getByStatus(deviceid, softwarename, 1);
    }

    @Override
    public Devicesw getByStatus(Integer deviceid, String softwarename, int status) {
        Devicesw devicesw = null;
        String sql = "select * from devicesw ";
        String whr = "where status = "+ ConvUtil.convToInt(status);
        String order = " order by id desc ";
        if (deviceid != null && !"".equals(ConvUtil.convToStr(softwarename))) {
            whr += " and softwarename = '"+ softwarename +"'" + " and deviceid = " + deviceid;
            List<Devicesw> list = deviceswMapper.selectBySql(sql + whr + order);
            if (list.size() > 0) {
                devicesw = list.get(0);
            }
        }
        return devicesw;
    }

    @Override
    public Devicesw getByDeviceIdAndErpcode(Integer deviceid, String erpcode) throws Exception {
        Devicesw devicesw = null;
        String sql = "select * from devicesw ";
        String whr = "where status = 1";
        String order = " order by id desc ";
        if (deviceid != null && !"".equals(ConvUtil.convToStr(erpcode))) {
            whr += " and erpcode = '"+ erpcode +"'" + " and deviceid = " + deviceid;
            List<Devicesw> list = deviceswMapper.selectBySql(sql + whr + order);
            if (list.size() > 0) {
                devicesw = list.get(0);
            }
        }
        return devicesw;
    }

    @Override
    public int getUpdateCount() throws Exception {
        List<Devicesw> list = deviceswMapper.selectBySql("select id from devicesw where status = 2");
        return list.size();
    }
}