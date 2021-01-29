package com.routon.plcloud.device.core.serviceimpl;

import com.alibaba.fastjson.JSONObject;
import com.routon.plcloud.device.core.utils.ConvUtil;
import com.routon.plcloud.device.core.utils.PropertiesUtil;
import com.routon.plcloud.device.core.common.TreeBean;
import com.routon.plcloud.device.core.service.SoftwareService;
import com.routon.plcloud.device.core.utils.SqlUtil;
import com.routon.plcloud.device.data.entity.Device;
import com.routon.plcloud.device.data.entity.Software;
import com.routon.plcloud.device.data.mapper.SoftwareMapper;
import org.apache.ibatis.annotations.Mapper;
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
public class SoftwareServiceImpl implements SoftwareService {
    private Logger logger = LoggerFactory.getLogger(SoftwareServiceImpl.class);

    @Autowired
    private SoftwareMapper softwareMapper;

    @Override
    public Software getSoftwareById(Integer id) {
        Software software = new Software();
        List<Software> softwareList = softwareMapper.selectBySql("SELECT * FROM software where id = " + id);
        if (softwareList.size() > 0) {
            software = softwareList.get(0);
        }
        return software;
    }

    @Override
    public Software getSoftwareByName(String name) {
        Software software = new Software();
        String sql = "SELECT * FROM software where softwarename = '" + name + "'";
        String order = " order by id desc ";
        List<Software> softwareList = softwareMapper.selectBySql(sql + order);
        if (softwareList.size() > 0) {
            software = softwareList.get(0);
        }
        return software;
    }

    @Override
    public boolean update(Software software) {
        boolean succ = false;
        software.setModitytime(new Date());
        int ns = softwareMapper.updateBySql(software);
        if (ns > 0) {
            succ = true;
        }
        return succ;
    }

    @Override
    public List<TreeBean> getSoftwareNameTree(Integer userid) throws Exception {
        /**---------  构建树型目录  ----------*/
        //创建一级目录的树型结构，作为完整的返回的目录结构
        List<TreeBean> rootTree = new ArrayList<TreeBean>();
        try {
            /*目前一级目录只有两个，创建第一个目录*/
            //创建一级节点：应用软件
            TreeBean appBean = new TreeBean();
            //设置节点id
            appBean.setId(1L);
            //设置节点名称
            appBean.setName("应用软件");
            //没有父节点，父节点id设置为0
            appBean.setPid(0L);
            //设置为父节点，即开启子节点
            appBean.setParent(true);
            //设置节点展开
            appBean.setOpen(true);

            /**为了给一级目录的节点写入二级目录内容，需要构建二级目录的树形结构*/
            //创建二级目录的树形结构
            List<TreeBean> appSoftwareNameTreeBeans = new ArrayList<TreeBean>();

            /*获取应用软件列表，即该一级目录节点要显示的内容*/
            String treesql = "SELECT DISTINCT customername, erpcode FROM software ";
            String wherea;
            int admin = 888;
            if (userid == admin) {
                wherea = "where softwaretype = '1' ORDER BY erpcode";
            } else {
                wherea = "where softwaretype = '1' and downstatus = "+ userid +"ORDER BY erpcode";
            }

            List<Software> appSoftwareList = softwareMapper.selectBySql(treesql + wherea);
            //循环写入二级目录内容
            for (Software software : appSoftwareList) {
                //创建二级结点
                TreeBean softwareNameBean = new TreeBean();
                //将erp设置为节点id
                softwareNameBean.setId(ConvUtil.convToLong(software.getErpcode()));
                //将软件名称写入节点
                softwareNameBean.setName(software.getCustomername());
                //设置父节点id，即“应用软件”的id
                softwareNameBean.setPid(1L);
                //将节点放入二级目录
                appSoftwareNameTreeBeans.add(softwareNameBean);
            }
            //将写好的二级目录放到一级节点上
            appBean.setChildren(appSoftwareNameTreeBeans);
            //将写好的二级目录结构放到一级节点上，该目录节点内容完成
            rootTree.add(appBean);


            /**目前一级目录只有两个，创建第二个目录*/
            //创建一级节点：系统软件
            TreeBean sysBean = new TreeBean();
            //设置节点id
            sysBean.setId(2L);
            //设置节点名称
            sysBean.setName("系统软件");
            //没有父节点，父节点id设置为0
            sysBean.setPid(0L);
            //设置为父节点，即开启子节点
            sysBean.setParent(true);
            //设置节点展开
            sysBean.setOpen(true);

            /**为了给一级目录的节点写入二级目录内容，需要构建二级目录的树形结构*/
            //创建二级目录的树形结构
            List<TreeBean> sysSoftwareTreeBeans = new ArrayList<TreeBean>();
            /**获取系统软件列表，即该一级目录节点要显示的内容*/
            String wheres;
            if (userid == admin) {
                wheres = "where softwaretype = '2' ORDER BY erpcode";
            } else {
                wheres = "where softwaretype = '2' and downstatus = "+ userid +"ORDER BY erpcode";
            }

            List<Software> sysSoftwareList = softwareMapper.selectBySql(treesql + wheres);
            //循环写入二级目录内容
            for (Software software : sysSoftwareList) {
                //创建二级结点
                TreeBean softwareNameBean = new TreeBean();
                //将erp设置为节点id
                softwareNameBean.setId(ConvUtil.convToLong(software.getErpcode()));
                //将软件名称写入节点
                softwareNameBean.setName(software.getSoftwarename());
                //设置父节点id，即“应用软件”的id
                softwareNameBean.setPid(2L);
                //将节点放入二级目录
                sysSoftwareTreeBeans.add(softwareNameBean);
            }
            //将写好的二级目录放到一级节点上
            sysBean.setChildren(sysSoftwareTreeBeans);
            if (sysSoftwareList.size() != 0) {
                //将写好的二级目录结构放到一级节点上，该目录节点内容完成
                rootTree.add(sysBean);
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        /**返回构建完成的目录结构 the end*/
        return rootTree;
    }

    @Override
    public Integer getMaxCount(Map<String, Object> paramMap){
        String getallsql = "SELECT COUNT(id) FROM software ";
        String wheresql = getsql(paramMap);
        Integer target = softwareMapper.countBySql(getallsql + wheresql);
        return target;
    }

    @Override
    public List<Software> getMaxList(Map<String, Object> paramMap) throws Exception {
        String getallsql = "SELECT * FROM software ";
        String wheresql = getsql(paramMap);
        List<Software> target = softwareMapper.selectBySql(getallsql + wheresql);
        return target;
    }

    @Override
    public List<Software> searchsoftwareforpage(Map<String, Object> paramMap, Integer page, Integer pageSize) throws Exception {
        int startrow = pageSize * (page - 1);
        String limitsql = " order by id desc limit " + pageSize + " offset " + startrow;
        String getallsql = "SELECT * FROM software ";
        String wheresql = getsql(paramMap);

        List<Software> target = softwareMapper.selectBySql(getallsql + wheresql + limitsql);
        return target;
    }

    @Override
    public boolean isExist(Map<String, Object> paramMap) {
        String getsql = "SELECT * FROM software ";
        String erpcode = ConvUtil.convToStr(paramMap.get("erpcode"));
        String softwareversion = ConvUtil.convToStr(paramMap.get("softwareversion"));
        if (!"".equals(erpcode) && !"".equals(softwareversion)) {
            getsql = getsql + " where erpcode = '" + paramMap.get("erpcode") + "' and softwareversion = '" + paramMap.get("softwareversion") + "'";
        }
        List<Software> target = softwareMapper.selectBySql(getsql);
        if (target.size() != 0) {
            return true;
        }
        return false;
    }

    @Override
    public Software addSoftwarePro(Map<String, Object> paramMap) throws Exception {
        Software software = new Software();
        software.setErpcode(ConvUtil.convToStr(paramMap.get("erpcode")));
        software.setSoftwarename(ConvUtil.convToStr(paramMap.get("softwarename")));
        software.setCustomername(ConvUtil.convToStr(paramMap.get("customername")));
        software.setSoftwareversion(ConvUtil.convToStr(paramMap.get("softwareversion")));
        software.setSoftwaretype("1");
        software.setSize(ConvUtil.convToStr(paramMap.get("size")));
        software.setSignflag(ConvUtil.convToStr(paramMap.get("signflag")));
        software.setFiletimestamp(ConvUtil.convToStr(paramMap.get("filetimestamp")));
        software.setUploadsoftwarename(ConvUtil.convToStr(paramMap.get("uploadsoftwarename")));
        software.setFitmodel(ConvUtil.convToStr(paramMap.get("fitmodel")));
        software.setPubflag(ConvUtil.convToInt(paramMap.get("pubflag")));

        //添加userid（v1.4.0版本将userid添加到downstatus）
        software.setDownstatus(paramMap.get("downstatus"));

        software.setUploadtime(new Date());
        software.setCreatetime(new Date());
        software.setModitytime(new Date());

        if (softwareMapper.insertNew(software) > 0) {
            List<Software> softwareList = getMaxList(paramMap);
            if (softwareList.size() > 0) {
                software = softwareList.get(0);
            }
        }
        return software;
    }

    @Override
    public boolean updateSameSoftwareCustName(String erpcode, String customername) throws Exception {
        //获取同款软件列表
        List<Software> softwareList = softwareMapper.selectBySql("SELECT * FROM software where erpcode = '" + erpcode + "'");

        //依次更新中文描述
        for (Software software : softwareList) {
            software.setCustomername(customername);
            update(software);
        }

        return false;
    }

    @Override
    public boolean deleteSoftware(Software software) {
        boolean deletesign = false;
        if (softwareMapper.deleteById((Integer) software.getId()) > 0) {
            deletesign = true;
        }
        return deletesign;
    }

    @Override
    public int getNumByErp(Software software) {
        String getNum = "SELECT id FROM software where erpcode = '" + ConvUtil.convToStr(software.getErpcode()) + "' order by id";
        List<Software> softwareList = softwareMapper.selectBySql(getNum);
        int target = 1;
        for (int i = 0; i < softwareList.size(); i++) {
            if (ConvUtil.convToInt(softwareList.get(i).getId()) == ConvUtil.convToInt(software.getId())) {
                target = i + 1;
            }
        }
        return target;
    }

    @Override
    public String getErpBySoftware(String softwarename) throws Exception {
        String erp = "";
        String getNum = "SELECT erpcode FROM software where softwarename like '%" + softwarename + "%'";
        List<Software> erpcodeList = softwareMapper.selectBySql(getNum);
        if (erpcodeList.size() == 1) {
            erp = erpcodeList.get(0).getErpcode().toString();
        }
        return erp;
    }

    @Override
    public List<JSONObject> getVersionByErp(String erpcode) throws Exception {
        String sql = "SELECT * FROM software where erpcode = '" + erpcode + "'";
        List<Software> softwareList = softwareMapper.selectBySql(sql);

        List<JSONObject> target = new ArrayList<>();
        for (int i = 0; i < softwareList.size(); i++) {
            Software software = softwareList.get(i);
            JSONObject version = getVersion(software);
            target.add(version);
        }
        return target;
    }

    @Override
    public List<JSONObject> getVersionByErpAndTermmodel(String erpcode, List<Device> termmodels) throws Exception {
        List<JSONObject> target = new ArrayList<>();

        String sql = "SELECT * FROM software where erpcode = '" + erpcode + "'";
        String order = " order by id desc";
        List<Software> softwareList = softwareMapper.selectBySql(sql + order);
        for (int i = 0; i < softwareList.size(); i++) {
            Software software = softwareList.get(i);
            if ("".equals(ConvUtil.convToStr(software.getFitmodel())) || ifFitAllTerm(software.getFitmodel(), termmodels)) {
                JSONObject version = getVersion(software);
                target.add(version);
            }
        }
        return target;
    }

    /**
     * 判断该类型是否适配所有设备
     *
     * @param fitmodel
     * @param termmodels
     * @return
     */
    private boolean ifFitAllTerm(String fitmodel, List<Device> termmodels) {
        String fitmodels = "," + ConvUtil.convToStr(fitmodel) + ",";
        for (Device device : termmodels) {
            String termmodel = "," + ConvUtil.convToStr(device.getTermmodel()) + ",";
            if (!fitmodels.contains(termmodel)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public JSONObject getVersion(Software software) throws Exception {
        JSONObject version = new JSONObject();
        //获取服务连接头
        String url = PropertiesUtil.getDataFromPropertiseFile("deviceCommon", "hostrul") + "/DMIL/device/softwaredownload";
        version.put("filename", software.getUploadsoftwarename());
        version.put("totalsize", software.getSize());
        version.put("softwarename", software.getSoftwarename());
        version.put("customername", software.getCustomername());
        version.put("softwareversion", software.getSoftwareversion());
        version.put("url", url + "?index=" + software.getId());
        if (ConvUtil.convToBool(software.getSignflag())) {
            version.put("sign_flag", "1");
        } else {
            version.put("sign_flag", "0");
        }
        return version;
    }

    @Override
    public Software getSoftwareByQr(String qrcode) throws Exception {
        Software software = new Software();
        List<Software> softwareList = softwareMapper.selectBySql("SELECT * FROM software where qrcode = '" + qrcode + "'");
        if (softwareList.size() > 0) {
            software = softwareList.get(0);
        }
        return software;
    }

    @Override
    public List<Software> getDirctList() throws Exception {
        String sql = "SELECT distinct erpcode, softwarename FROM software where pubflag = 1";
        return softwareMapper.selectBySql(sql);
    }

    @Override
    public List<Software> getSoftwareByCondition(Map<String, Object> condition) throws Exception {
        String sql = "SELECT DISTINCT a.erpcode, a.customername FROM software a" +
                " LEFT JOIN devicesw b ON b.erpcode = a.erpcode " +
                " LEFT JOIN device c ON c.id = b.deviceid ";

        String companyid = ConvUtil.convToStr(condition.get("companyid"));
        if (!"".equals(companyid)) {
            sql += " where b.status = 1 and c.companyid = '" + companyid + "'";
        }
        return softwareMapper.selectBySql(sql);
    }

    /**
     * 获取查询条件sql语句
     **/
    private String getsql(Map<String, Object> paramMap) {
        String wheresql = " where 1=1 ";

        wheresql += SqlUtil.getLikeSql(paramMap, "softwarename");
        wheresql += SqlUtil.getLikeSql(paramMap, "softwareversion");
        wheresql += SqlUtil.getLikeSql(paramMap, "customername");
        wheresql += SqlUtil.getLikeSql(paramMap, "uploadsoftwarename");

        wheresql += SqlUtil.getAsSql(paramMap, "erpcode");
        wheresql += SqlUtil.getAsSql(paramMap, "softwaretype");
        wheresql += SqlUtil.getAsSql(paramMap, "qrcode");
        wheresql += SqlUtil.getAsSql(paramMap, "downstatus");
        wheresql += SqlUtil.getAsSql(paramMap, "pubflag");

        return wheresql;
    }
}