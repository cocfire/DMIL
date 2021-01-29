package com.routon.plcloud.device.core.serviceimpl;

import com.routon.plcloud.device.core.common.TreeBean;
import com.routon.plcloud.device.core.service.DeviceService;
import com.routon.plcloud.device.core.utils.ConvUtil;
import com.routon.plcloud.device.core.utils.SqlUtil;
import com.routon.plcloud.device.data.entity.Device;
import com.routon.plcloud.device.data.entity.Menu;
import com.routon.plcloud.device.data.mapper.DeviceMapper;
import com.routon.plcloud.device.data.mapper.MenuMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @author FireWang
 * @date 2020/5/07 15:09
 */
@Service
public class DeviceServiceImpl implements DeviceService {
    private Logger logger = LoggerFactory.getLogger(DeviceServiceImpl.class);
    @Autowired
    private DeviceMapper deviceMapper;

    @Autowired
    private MenuMapper menuMapper;

    @Override
    public Device addDevice(Map<String, Object> paramMap) throws Exception {
        Device device = new Device();
        device = getdevice(paramMap, device);
        device.setCreatetime(new Date());
        device.setModitytime(new Date());

        if (deviceMapper.insertNew(device) > 0) {
            List<Device> deviceList = getOtherList(paramMap);
            if (deviceList.size() > 0) {
                device = deviceList.get(0);
            }
        }
        return device;
    }

    @Override
    public boolean insertDevice(Device device) throws Exception {
        if (deviceMapper.insertNew(device) > 0) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean deleteDevice(Device device) throws Exception {
        if (deviceMapper.deleteById(ConvUtil.convToInt(device.getId())) > 0) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean update(Device device) throws Exception {
        boolean succ = false;
        int ns = deviceMapper.updateBySql(device);
        if (ns > 0) {
            succ = true;
        }
        return succ;
    }

    @Override
    public Device getDeviceById(Integer id) throws Exception {
        List<Device> deivcelist = deviceMapper.selectBySql("select * from device where id = " + id);
        if (deivcelist.size() > 0) {
            return deivcelist.get(0);
        } else {
            return null;
        }
    }

    @Override
    public Device getDeviceByDeviceId(String deviceid) throws Exception {
        List<Device> deivcelist = deviceMapper.selectBySql("select * from device where deviceid = '" + deviceid + "'");
        if (deivcelist.size() > 0) {
            return deivcelist.get(0);
        } else {
            return null;
        }
    }

    @Override
    public Device searchDeviceById(Integer id) throws Exception {
        Device device = new Device();
        List<Device> deivcelist = deviceMapper.selectBySql("select * from device where id = " + id);
        if (deivcelist.size() > 0) {
            device = deivcelist.get(0);
        }
        return device;
    }

    @Override
    public Device searchDeviceBySn(String termsn) throws Exception {
        Device device = new Device();
        Map<String, Object> paramMap = new HashMap<>(16);
        paramMap.put("termsn", termsn);
        List<Device> target = searchotherforpage(paramMap, null, null);
        if (target.size() > 0) {
            device = target.get(0);
        }
        return device;
    }

    @Override
    public Device searchDeviceByCid(String clientid) throws Exception {
        Device device = null;
        List<Device> deivcelist = deviceMapper.selectBySql("select * from device where clientid = '" + clientid + "'");
        if (deivcelist.size() > 0) {
            device = deivcelist.get(0);
        }
        return device;
    }

    @Override
    public List<Device> searchDeviceListByCid(String clientid) throws Exception {
        Map<String, Object> paramMap = new HashMap<>(16);
        paramMap.put("clientid", clientid);
        List<Device> target = searchdeviceforpage(paramMap, null, null);
        return target;
    }

    @Override
    public Device searchDeviceByUuid(String uuid) throws Exception {
        Device device = new Device();
        Map<String, Object> paramMap = new HashMap<>(16);
        paramMap.put("uuid", uuid);
        List<Device> target = searchotherforpage(paramMap, null, null);
        if (target.size() > 0) {
            device = target.get(0);
        }
        return device;
    }

    @Override
    public List<TreeBean> getDviceTree(Integer companyId,Integer searchId) throws Exception {
        /**---------  构建树型目录  ----------*/
        //创建一级目录的树型结构，作为完整的返回的目录结构
        List<TreeBean> rootTree = new ArrayList<TreeBean>();
        try {

            /* 先获取项目信息，根据项目信息分级，该菜单只有两级 */
            String companysql = "SELECT DISTINCT id, name FROM menu where rank = 1";
            if (searchId != 0) {
                companysql += " and id = " + searchId;
            }
            List<Menu> companylist = menuMapper.selectBySql(companysql);
            int companyid = 0;
            for (Menu company : companylist) {
                companyid = ConvUtil.convToInt(company.getId());
                if (companyid == 0) {
                    continue;
                } else {
                    /* 创建一级目录 */
                    //创建一级节点：应用软件
                    TreeBean companyBean = new TreeBean();
                    //设置节点id
                    Long companyTreeId = ConvUtil.convToLong(companyid);
                    companyBean.setId(companyTreeId);
                    //设置节点名称
                    companyBean.setName(company.getName());
                    //没有父节点，父节点id设置为0
                    companyBean.setPid(0L);
                    //设置为父节点，即开启子节点
                    companyBean.setParent(true);
                    //判断是否需要展开父节点
                    if (companyid == ConvUtil.convToInt(companyId)) {
                        //设置节点展开
                        companyBean.setOpen(true);
                    }

                    /*为了给一级目录的节点写入二级目录内容，需要构建二级目录的树形结构*/
                    //创建二级目录的树形结构
                    List<TreeBean> projectTreeBeans = new ArrayList<TreeBean>();

                    /*获取应用软件列表，即该一级目录节点要显示的内容*/
                    String projectsql = "SELECT DISTINCT id, name FROM menu where rank = 2 and pid = " + companyid;
                    List<Menu> projectlist = menuMapper.selectBySql(projectsql);
                    //循环写入二级目录内容
                    for (Menu project : projectlist) {
                        //创建二级结点
                        TreeBean projectBean = new TreeBean();
                        //将erp设置为节点id
                        projectBean.setId(ConvUtil.convToLong(project.getId()));
                        //将软件名称写入节点
                        projectBean.setName(project.getName());
                        //设置父节点id，即“应用软件”的id
                        projectBean.setPid(companyTreeId);
                        //将节点放入二级目录
                        projectTreeBeans.add(projectBean);

                    }
                    //将写好的二级目录放到一级节点上
                    companyBean.setChildren(projectTreeBeans);
                    //将写好的二级目录结构放到一级节点上，该目录节点内容完成
                    rootTree.add(companyBean);
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        /*返回构建完成的目录结构 the end*/
        return rootTree;
    }

    @Override
    public int getProjectidByname(String projectname) throws Exception {
        int projectid = 0;
        String getNum = "SELECT DISTINCT projectid FROM device where projectname like '%" + projectname + "%'";
        List<Device> deviceList = deviceMapper.selectBySql(getNum);
        if (deviceList.size() == 1) {
            projectid = ConvUtil.convToInt(deviceList.get(0).getProjectid());
        }
        return projectid;
    }

    @Override
    public Integer getMaxCount(Map<String, Object> paramMap){
        String getallsql = "SELECT COUNT(a.id) FROM device a " +
                " left join devicesw b on (a.id = b.deviceid and b.status = 2) ";
        String wheresql = getsql(paramMap, true);
        Integer target = deviceMapper.countBySql(getallsql + wheresql);
        return target;
    }

    @Override
    public List<Device> getMaxList(Map<String, Object> paramMap) throws Exception {
        String getallsql = "SELECT DISTINCT * FROM device a ";
        String wheresql = getsql(paramMap, true);
        List<Device> target = deviceMapper.selectBySql(getallsql + wheresql);
        return target;
    }

    @Override
    public List<Device> getOtherMaxList(Map<String, Object> paramMap) throws Exception {
        String getallsql = "SELECT DISTINCT * FROM device a ";
        String wheresql = " where clientid is null or clientid = ''";

        wheresql += SqlUtil.getLikeSql(paramMap, "termsn");
        wheresql += SqlUtil.getLikeSql(paramMap, "remark");
        wheresql += SqlUtil.getAsSql(paramMap, "termmodel");
        wheresql += SqlUtil.getAsSql(paramMap, "status");
        wheresql += SqlUtil.getSqlByGroup(paramMap);
        List<Device> target = deviceMapper.selectBySql(getallsql + wheresql);
        return target;
    }

    /**
     * 非emq接入的设备
     *
     * @param paramMap
     * @return
     * @throws Exception
     */
    public List<Device> getOtherList(Map<String, Object> paramMap) throws Exception {
        String getallsql = "SELECT * FROM device ";
        String wheresql = getsql(paramMap, false);
        List<Device> target = deviceMapper.selectBySql(getallsql + wheresql);
        return target;
    }

    @Override
    public List<Device> searchdeviceforpage(Map<String, Object> paramMap, Integer page, Integer pageSize) throws Exception {
        int startrow = pageSize * (page - 1);
        String limitsql = " order by id desc limit " + pageSize + " offset " + startrow;
        String getallsql = "SELECT a.termsn, a.id, a.remark, termmodel, a.moditytime, a.status, a.deviceid," +
                " b.status as termtype FROM device a";
        String joinsql = " left join devicesw b on (a.id = b.deviceid and b.status = 2) ";
        String wheresql = getsql(paramMap, true);

        List<Device> target = deviceMapper.selectBySql(getallsql + joinsql + wheresql + limitsql);
        return target;
    }

    @Override
    public List<Device> searchotherdeviceforpage(Map<String, Object> paramMap, Integer page, Integer pageSize) throws Exception {
        int startrow = pageSize * (page - 1);
        String limitsql = " order by id desc limit " + pageSize + " offset " + startrow;
        String getallsql = "SELECT * FROM device";
        String wheresql = " where (clientid is null or clientid = '')";

        wheresql += SqlUtil.getLikeSql(paramMap, "termsn");
        wheresql += SqlUtil.getLikeSql(paramMap, "remark");
        wheresql += SqlUtil.getAsSql(paramMap, "status");
        wheresql += SqlUtil.getLikeSql(paramMap, "termmodel");
        wheresql += SqlUtil.getSqlByGroup(paramMap);

        List<Device> target = deviceMapper.selectBySql(getallsql + wheresql + limitsql);
        return target;
    }

    /**
     * 非emq接入的设备
     *
     * @param paramMap
     * @param page
     * @param pageSize
     * @return
     * @throws Exception
     */
    public List<Device> searchotherforpage(Map<String, Object> paramMap, Integer page, Integer pageSize) throws Exception {
        //如果page，pageSize为null，则设为1
        if (page == null || page <= 0) {
            page = 1;
        }
        if (pageSize == null || pageSize <= 0) {
            pageSize = 10;
        }

        int startrow = pageSize * (page - 1);
        String limitsql = " order by id desc limit " + pageSize + " offset " + startrow;
        String getallsql = "SELECT * FROM device ";
        String wheresql = getsql(paramMap, false);

        List<Device> target = deviceMapper.selectBySql(getallsql + wheresql + limitsql);
        return target;
    }

    @Override
    public List<Device> searchDeviceClounmByCondition(String clounm, Map<String, Object> condition) throws Exception {
        String getNum = "SELECT DISTINCT " + clounm + " FROM device ";
        String wheresql = " where clientid is not null and clientid != '' and emqversion !=''";
        if (condition != null) {
            wheresql = getsql(condition, true);
        }
        String sql = getNum + wheresql + " and " + clounm + " is not null and " + clounm + " != ''";
        List<Device> deviceList = deviceMapper.selectBySql(sql);
        return deviceList;
    }

    @Override
    public List<Device> searchOtherDeviceClounmByCondition(String clounm, Map<String, Object> condition) throws Exception {
        String getNum = "SELECT DISTINCT " + clounm + " FROM device ";
        String wheresql = " where clientid is null or clientid = ''";
        if (condition != null) {
            wheresql += SqlUtil.getSqlByGroup(condition);
        }
        String sql = getNum + wheresql + " and " + clounm + " is not null and " + clounm + " != ''";
        List<Device> deviceList = deviceMapper.selectBySql(sql);
        return deviceList;
    }

    @Override
    public List<Device> canTheyUpdateById(Map idmap) throws Exception {

        return getClounmById(idmap, "erpcode");
    }

    @Override
    public List<Device> getClounmById(Map idmap, String clounm) throws Exception {
        StringBuilder ids = new StringBuilder();
        for (Object value : idmap.values()) {
            ids.append(value + ",");
        }
        String sql;
        String erpsql = "select distinct "+ clounm +" from device ";
        if (ids.length() > 1) {
            sql = erpsql + "where id in (" + ids.substring(0, ids.length() - 1) + ")";
        } else {
            sql = erpsql;
        }
        return deviceMapper.selectBySql(sql);
    }

    @Override
    public List<Device> canTheyUpdateByParam(Map<String, Object> paramMap) throws Exception {
        String erpsql = "select distinct erpcode from device ";
        String wheresql = getsql(paramMap, true);
        List<Device> target = deviceMapper.selectBySql(erpsql + wheresql);
        return target;
    }

    @Override
    public void delLinkedMenuId(Integer menuId) {
        String sql = "select * from device ";

        //删除公司信息
        Map<String, Object> companyMap = new HashMap<>(16);
        companyMap.put("companyid", menuId);
        List<Device> companys = deviceMapper.selectBySql(sql + getsql(companyMap, true));
        for (Device device : companys) {
            //解除绑定公司和绑定账户
            device.setCompanyid(0);
            device.setClientcode("");
            deviceMapper.updateBySql(device);
        }

        //删除分组信息
        Map<String, Object> projectMap = new HashMap<>(16);
        projectMap.put("projectid", menuId);
        List<Device> projects = deviceMapper.selectBySql(sql + getsql(projectMap, true));
        for (Device device : projects) {
            device.setProjectid(0);
            deviceMapper.updateBySql(device);
        }
    }

    /**
     * 公共方法：根据条件获取Device
     * 该方法不修改关于时间的参数：createtime  moditytime  updatetime
     **/
    private Device getdevice(Map<String, Object> paramMap, Device device) {
        device.setClientid(ConvUtil.convToStr(paramMap.get("clientid")));
        device.setTermsn(ConvUtil.convToStr(paramMap.get("termsn")));
        device.setTermid(ConvUtil.convToStr(paramMap.get("termid")));
        device.setTermcode(ConvUtil.convToStr(paramMap.get("termcode")));
        device.setTermtype(ConvUtil.convToInt(paramMap.get("termtype")));
        device.setTerminfo(ConvUtil.convToStr(paramMap.get("terminfo")));
        device.setMacaddress(ConvUtil.convToStr(paramMap.get("macaddress")));
        device.setHardwarename(ConvUtil.convToStr(paramMap.get("hardwarename")));
        device.setMachinetype(ConvUtil.convToStr(paramMap.get("machinetype")));
        device.setLicencecode(ConvUtil.convToStr(paramMap.get("licencecode")));

        device.setSoftwarename(ConvUtil.convToStr(paramMap.get("softwarename")));
        device.setErpcode(ConvUtil.convToStr(paramMap.get("erpcode")));
        device.setSoftwareversion(ConvUtil.convToStr(paramMap.get("softwareversion")));
        device.setOrderid(ConvUtil.convToStr(paramMap.get("orderid")));
        device.setProjectid(ConvUtil.convToInt(paramMap.get("projectid")));
        device.setProjectname(ConvUtil.convToStr(paramMap.get("projectname")));
        device.setCompanyname(ConvUtil.convToStr(paramMap.get("companyname")));
        device.setClientname(ConvUtil.convToStr(paramMap.get("clientname")));
        device.setClientcode(ConvUtil.convToStr(paramMap.get("clientcode")));
        device.setContact(ConvUtil.convToStr(paramMap.get("contact")));

        device.setAddress(ConvUtil.convToStr(paramMap.get("address")));
        device.setTelno(ConvUtil.convToStr(paramMap.get("telno")));
        device.setRemark(ConvUtil.convToStr(paramMap.get("remark")));
        device.setStatus(ConvUtil.convToInt(paramMap.get("status")));
        device.setLink(ConvUtil.convToStr(paramMap.get("link")));
        device.setTermmodel(ConvUtil.convToStr(paramMap.get("termmodel")));
        device.setLanip(ConvUtil.convToStr(paramMap.get("lanip")));

        device.setWifiip(ConvUtil.convToStr(paramMap.get("wifiip")));
        device.setEmqversion(ConvUtil.convToStr(paramMap.get("emqversion")));
        device.setDeviceid(ConvUtil.convToStr(paramMap.get("deviceid")));
        device.setImei(ConvUtil.convToStr(paramMap.get("imei")));
        device.setUdid(ConvUtil.convToStr(paramMap.get("udid")));
        device.setUuid(ConvUtil.convToStr(paramMap.get("uuid")));
        device.setBarcode(ConvUtil.convToStr(paramMap.get("barcode")));
        device.setFacelibrid(ConvUtil.convToInt(paramMap.get("facelibrid")));

        return device;
    }

    /**
     * 公共方法：根据条件获取查询条件sql语句
     **/
    private String getsql(Map<String, Object> paramMap, boolean emqflag) {
        String wheresql = "where 1=1 ";
        //是否由EMQ接入的设备
        if (emqflag) {
            wheresql = " where clientid is not null and clientid != '' and emqversion !=''";
        }
        wheresql += SqlUtil.getLikeSql(paramMap, "clientid");
        wheresql += SqlUtil.getLikeSql(paramMap, "termsn");
        wheresql += SqlUtil.getLikeSql(paramMap, "a.deviceid");
        wheresql += SqlUtil.getLikeSql(paramMap, "terminfo");
        wheresql += SqlUtil.getLikeSql(paramMap, "projectname");
        wheresql += SqlUtil.getLikeSql(paramMap, "companyname");
        wheresql += SqlUtil.getLikeSql(paramMap, "remark");
        wheresql += SqlUtil.getLikeSql(paramMap, "a.remark");
        wheresql += SqlUtil.getLikeSql(paramMap, "macaddress");
        wheresql += SqlUtil.getLikeSql(paramMap, "machinetype");
        wheresql += SqlUtil.getLikeSql(paramMap, "lanip");
        wheresql += SqlUtil.getLikeSql(paramMap, "udid");
        wheresql += SqlUtil.getLikeSql(paramMap, "uuid");
        wheresql += SqlUtil.getLikeSql(paramMap, "barcode");

        wheresql += SqlUtil.getAsSql(paramMap, "softwarename");
        wheresql += SqlUtil.getAsSql(paramMap, "orderid");
        wheresql += SqlUtil.getAsSql(paramMap, "termmodel");
        wheresql += SqlUtil.getAsSql(paramMap, "termid");
        wheresql += SqlUtil.getAsSql(paramMap, "termcode");
        wheresql += SqlUtil.getAsSql(paramMap, "termtype");
        wheresql += SqlUtil.getAsSql(paramMap, "erpcode");
        wheresql += SqlUtil.getAsSql(paramMap, "softwareversion");
        wheresql += SqlUtil.getAsSql(paramMap, "status");
        wheresql += SqlUtil.getAsSql(paramMap, "a.status");
        wheresql += SqlUtil.getAsSql(paramMap, "link");
        wheresql += SqlUtil.getAsSql(paramMap, "emqversion");
        wheresql += SqlUtil.getAsSql(paramMap, "deviceid");
        wheresql += SqlUtil.getAsSql(paramMap, "facelibrid");
        wheresql += SqlUtil.getAsSql(paramMap, "b.erpcode");
        wheresql += SqlUtil.getSqlByGroup(paramMap);

        return wheresql;
    }
}