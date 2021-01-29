package com.routon.plcloud.device.core.serviceimpl;

import com.routon.plcloud.device.core.common.TreeBean;
import com.routon.plcloud.device.core.service.ConfigService;
import com.routon.plcloud.device.core.utils.ConvUtil;
import com.routon.plcloud.device.core.utils.SqlUtil;
import com.routon.plcloud.device.data.entity.Config;
import com.routon.plcloud.device.data.entity.Paraminfo;
import com.routon.plcloud.device.data.mapper.ConfigMapper;
import com.routon.plcloud.device.data.mapper.ParaminfoMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author FireWang
 * @date 2020/12/10 10:00
 */
@Service
public class ConfigServiceImpl implements ConfigService {
    private Logger logger = LoggerFactory.getLogger(ConfigServiceImpl.class);

    @Autowired
    private ConfigMapper configMapper;

    @Autowired
    private ParaminfoMapper paraminfoMapper;

    @Override
    public boolean insert(Config config) {
        if (configMapper.insertNew(config) > 0) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean delete(Config config) {
        if (configMapper.deleteById(ConvUtil.convToInt(config.getId())) > 0) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean update(Config config) {
        boolean succ = false;
        int ns = configMapper.updateBySql(config);
        if (ns > 0) {
            succ = true;
        }
        return succ;
    }

    @Override
    public Config getById(Integer id) {
        List<Config> list = configMapper.selectBySql("SELECT * FROM config where id = " + id);
        if (list.size() > 0) {
            return list.get(0);
        } else {
            return null;
        }
    }

    @Override
    public Config getByConfigid(Integer configid) {
        List<Config> list = configMapper.selectBySql("SELECT * FROM config where configid = " + configid);
        if (list.size() > 0) {
            return list.get(0);
        } else {
            return null;
        }
    }

    @Override
    public List<TreeBean> getConfigTree() throws Exception {
        /**---------  构建树型目录  ----------*/
        //创建一级目录的树型结构，作为完整的返回的目录结构
        List<TreeBean> rootTree = new ArrayList<TreeBean>();
        try {
            /**目前一级目录只有一个*/
            //创建一级节点：配置类型
            TreeBean configBean = new TreeBean();
            //设置节点id
            configBean.setId(1L);
            //设置节点名称
            configBean.setName("配置类型");
            //没有父节点，父节点id设置为0
            configBean.setPid(0L);
            //设置为父节点，即开启子节点
            configBean.setParent(true);
            //设置节点展开
            configBean.setOpen(true);

            /**为了给一级目录的节点写入二级目录内容，需要构建二级目录的树形结构*/
            //创建二级目录的树形结构
            List<TreeBean> appSoftwareNameTreeBeans = new ArrayList<TreeBean>();

            //写入二级目录内容，目前只有两个：系统参数，设备参数

            //系统参数
            //创建二级结点
            TreeBean sysBean = new TreeBean();
            //将type设置为节点id
            sysBean.setId(ConvUtil.convToLong(1));
            //将操作类型名称写入节点
            sysBean.setName("系统参数");
            //设置父节点id，即“日志类型”的id
            sysBean.setPid(1L);
            //将节点放入二级目录
            appSoftwareNameTreeBeans.add(sysBean);

            //设备参数
            //创建二级结点
            TreeBean deviceBean = new TreeBean();
            //将type设置为节点id
            deviceBean.setId(ConvUtil.convToLong(2));
            //将操作类型名称写入节点
            deviceBean.setName("设备参数");
            //设置父节点id，即“日志类型”的id
            deviceBean.setPid(1L);
            //将节点放入二级目录
            appSoftwareNameTreeBeans.add(deviceBean);

            //将写好的二级目录放到一级节点上
            configBean.setChildren(appSoftwareNameTreeBeans);

            //将写好的二级目录结构放到一级节点上，该目录节点内容完成
            rootTree.add(configBean);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        /**返回构建完成的目录结构 the end*/
        return rootTree;
    }

    @Override
    public Integer getMaxCount(Map<String, Object> paramMap) {
        String getallsql = "SELECT COUNT(id) FROM config ";
        String wheresql = getsql(paramMap);
        Integer target = configMapper.countBySql(getallsql + wheresql);
        return target;
    }

    @Override
    public List<Config> getMaxList(Map<String, Object> paramMap) {
        String getallsql = "SELECT * FROM config ";
        String limitsql = " order by id desc";
        String wheresql = getsql(paramMap);
        List<Config> target = configMapper.selectBySql(getallsql + wheresql + limitsql);
        return target;
    }

    @Override
    public List<Config> searchforpage(Map<String, Object> paramMap, Integer page, Integer pageSize) {
        int startrow = pageSize * (page - 1);
        String getallsql = "SELECT * FROM config ";
        String wheresql = getsql(paramMap);
        String limitsql = " order by id desc limit " + pageSize + " offset " + startrow;

        List<Config> target = configMapper.selectBySql(getallsql + wheresql + limitsql);
        return target;
    }

    @Override
    public boolean checkDefultParam(Map<String, Object> paramMap) {
        Integer configid = ConvUtil.convToInt(paramMap.get("configid"));
        Integer companyid = ConvUtil.convToInt(paramMap.get("companyid"));
        Integer configinfo = ConvUtil.convToInt(paramMap.get("configinfo"));
        String value =  ConvUtil.convToStr(paramMap.get("value"));
        String dsql = "SELECT DISTINCT a.termsn from device a " +
                "LEFT JOIN devicesw b ON b.deviceid = a.id " +
                "LEFT JOIN config c ON c.configinfo = b.erpcode " +
                "WHERE c.configid = "+ configid +" AND b.status = 1";
        if (companyid != 0) {
            dsql += " AND a.companyid = " + companyid;
        }

        String psql = "SELECT * from paraminfo " +
                "WHERE paramid = "+ configid;

        List<Paraminfo> dlist = paraminfoMapper.selectBySql(dsql);
        for (Paraminfo device : dlist) {
            List<Paraminfo> plist = paraminfoMapper.selectBySql(psql + " AND termsn = '"+ device.getTermsn() +"'");
            if (plist.size() == 0) {
                Paraminfo newp = new Paraminfo();
                newp.setLinkid(configinfo);
                newp.setTermsn(device.getTermsn());
                newp.setParamid(configid);
                newp.setParaminfo(value);
                newp.setModifytime(new Date());
                newp.setCreatetime(new Date());
                newp.setStatus(1);
                paraminfoMapper.insertNew(newp);
            }
        }

        return true;
    }

    @Override
    public List<Config> getVaildList(String termsn) {
        String sql = "SELECT DISTINCT a.* FROM config a " +
                "LEFT JOIN devicesw b ON b.erpcode = a.configinfo " +
                "LEFT JOIN device c ON c.id = b.deviceid " +
                "WHERE b.status = 1 AND a.type = 2 AND a.status = 1 " +
                "AND c.termsn = '"+ termsn +"'";
        return configMapper.selectBySql(sql);
    }

    /**
     * 公共方法：根据条件获取查询条件sql语句
     **/
    private String getsql(Map<String, Object> paramMap) {
        String wheresql = "where 1=1 ";
        wheresql += SqlUtil.getLikeSql(paramMap, "remark");
        wheresql += SqlUtil.getLikeSql(paramMap, "name");

        wheresql += SqlUtil.getAsSql(paramMap, "id");
        wheresql += SqlUtil.getAsSql(paramMap, "type");
        wheresql += SqlUtil.getAsSql(paramMap, "status");
        wheresql += SqlUtil.getAsSql(paramMap, "configinfo");

        String configid = "configid";
        if (!"".equals(ConvUtil.convToStr(paramMap.get(configid)))) {
            wheresql += " and CAST(configid as text) like '%" + ConvUtil.convToStr(paramMap.get("configid")) + "%'";
        }

        String start = ConvUtil.convToStr(paramMap.get("start")), end = ConvUtil.convToStr(paramMap.get("end"));
        if (!"".equals(start)) {
            wheresql = wheresql + " and createtime >= '" + start + "' ";
        }
        if (!"".equals(end)) {
            wheresql = wheresql + " and createtime <= '" + end + "' ";
        }

        return wheresql;
    }
}