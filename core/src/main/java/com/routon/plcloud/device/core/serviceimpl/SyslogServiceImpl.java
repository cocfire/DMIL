package com.routon.plcloud.device.core.serviceimpl;

import com.routon.plcloud.device.core.utils.ConvUtil;
import com.routon.plcloud.device.core.common.TreeBean;
import com.routon.plcloud.device.core.service.SyslogService;
import com.routon.plcloud.device.core.utils.SqlUtil;
import com.routon.plcloud.device.data.entity.Syslog;
import com.routon.plcloud.device.data.mapper.SyslogMapper;
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
 * @date 2020/5/07 15:09
 */
@Service
public class SyslogServiceImpl implements SyslogService {
    private Logger logger = LoggerFactory.getLogger(SyslogServiceImpl.class);

    @Autowired
    private SyslogMapper syslogMapper;

    @Override
    public boolean save(Syslog syslog) {
        boolean succ = false;
        syslog.setTimestamp(new Date());
        int ns = syslogMapper.updateBySql(syslog);
        if (ns > 0) {
            succ = true;
        }
        return succ;
    }

    @Override
    public List<TreeBean> getSyslogTree(Integer userid) throws Exception {
        /**---------  构建树型目录  ----------*/
        //创建一级目录的树型结构，作为完整的返回的目录结构
        List<TreeBean> rootTree = new ArrayList<TreeBean>();
        int admin = 888;
        try {
            /**目前一级目录只有一个*/
            //创建一级节点：日志类型
            TreeBean syslogBean = new TreeBean();
            //设置节点id
            syslogBean.setId(1L);
            //设置节点名称
            syslogBean.setName("日志类型");
            //没有父节点，父节点id设置为0
            syslogBean.setPid(0L);
            //设置为父节点，即开启子节点
            syslogBean.setParent(true);
            //设置节点展开
            syslogBean.setOpen(true);

            /**为了给一级目录的节点写入二级目录内容，需要构建二级目录的树形结构*/
            //创建二级目录的树形结构
            List<TreeBean> appSoftwareNameTreeBeans = new ArrayList<TreeBean>();

            /**获取应用软件列表，即该一级目录节点要显示的内容*/
            String systreesql = "SELECT DISTINCT option, type FROM syslog where userid = "+ userid +" ORDER BY type";
            if (userid == admin) {
                systreesql = "SELECT DISTINCT option, type FROM syslog ORDER BY type";
            }

            List<Syslog> syslogList = syslogMapper.selectBySql(systreesql);

            //循环写入二级目录内容
            for (Syslog syslog : syslogList) {
                //创建二级结点
                TreeBean sysBean = new TreeBean();
                //将type设置为节点id
                sysBean.setId(ConvUtil.convToLong(syslog.getType()));
                //将操作类型名称写入节点
                sysBean.setName(syslog.getOption());
                //设置父节点id，即“日志类型”的id
                sysBean.setPid(1L);
                //将节点放入二级目录
                appSoftwareNameTreeBeans.add(sysBean);
            }
            //将写好的二级目录放到一级节点上
            syslogBean.setChildren(appSoftwareNameTreeBeans);
            //将写好的二级目录结构放到一级节点上，该目录节点内容完成
            rootTree.add(syslogBean);

        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        /**返回构建完成的目录结构 the end*/
        return rootTree;
    }

    @Override
    public Integer getMaxCount(Map<String, Object> paramMap){
        String getallsql = "SELECT COUNT(id) FROM syslog ";
        String wheresql = getsql(paramMap);
        Integer target = syslogMapper.countBySql(getallsql + wheresql);
        return target;
    }

    @Override
    public List<Syslog> getMaxList(Map<String, Object> paramMap) throws Exception {
        String getallsql = "SELECT id FROM syslog ";
        String wheresql = getsql(paramMap);
        List<Syslog> target = syslogMapper.selectBySql(getallsql + wheresql);
        return target;
    }

    @Override
    public List<Syslog> searchsyslogforpage(Map<String, Object> paramMap, Integer page, Integer pageSize) throws Exception {
        int startrow = pageSize * (page - 1);
        String limitsql = " order by id DESC limit " + pageSize + " offset " + startrow;
        String getallsql = "SELECT * FROM syslog ";
        String wheresql = getsql(paramMap);

        List<Syslog> target = syslogMapper.selectBySql(getallsql + wheresql + limitsql);
        return target;
    }

    @Override
    public boolean addSyslog(Map<String, Object> paramMap) throws Exception {
        Syslog syslog = new Syslog();
        syslog.setOption(Syslog.SYS_TYPE.get(ConvUtil.convToInt(paramMap.get("type"))));
        syslog.setType(ConvUtil.convToInt(paramMap.get("type")));
        syslog.setLoginfo(ConvUtil.convToStr(paramMap.get("loginfo")));
        syslog.setTimestamp(new Date());
        syslog.setUserid(ConvUtil.convToInt(paramMap.get("userid")));
        syslog.setUserip(ConvUtil.convToStr(paramMap.get("userip")));
        syslog.setRemark(ConvUtil.convToString(paramMap.get("remark")));

        if (syslogMapper.insertNew(syslog) > 0) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 获取查询条件sql语句
     **/
    private String getsql(Map<String, Object> paramMap) {
        String wheresql = " where 1=1 ";

        String start = ConvUtil.convToStr(paramMap.get("start")), end = ConvUtil.convToStr(paramMap.get("end"));
        if (!"".equals(start)) {
            wheresql = wheresql + " and timestamp >= '" + start + "' ";
        }
        if (!"".equals(end)) {
            wheresql = wheresql + " and timestamp <= '" + end + "' ";
        }

        wheresql += SqlUtil.getLikeSql(paramMap, "loginfo");
        wheresql += SqlUtil.getAsSql(paramMap, "type");
        wheresql += SqlUtil.getAsSql(paramMap, "userid");

        return wheresql;
    }
}