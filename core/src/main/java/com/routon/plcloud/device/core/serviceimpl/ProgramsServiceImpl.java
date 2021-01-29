package com.routon.plcloud.device.core.serviceimpl;

import com.routon.plcloud.device.core.common.TreeBean;
import com.routon.plcloud.device.core.service.ProgramsService;
import com.routon.plcloud.device.core.utils.ConvUtil;
import com.routon.plcloud.device.data.entity.Menu;
import com.routon.plcloud.device.data.entity.Programs;
import com.routon.plcloud.device.data.mapper.MenuMapper;
import com.routon.plcloud.device.data.mapper.ProgramsMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author FireWang
 * @date 2020/5/07 15:09
 */
@Service
public class ProgramsServiceImpl implements ProgramsService {
    private Logger logger = LoggerFactory.getLogger(ProgramsServiceImpl.class);
    @Autowired
    private ProgramsMapper programsMapper;

    @Autowired
    private MenuMapper menuMapper;

    @Override
    public boolean insertProgram(Programs programs) throws Exception {
        if (programsMapper.insertNew(programs) > 0) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean deleteProgram(Programs programs) throws Exception {
        if (programsMapper.deleteById(ConvUtil.convToInt(programs.getId())) > 0) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean update(Programs programs) {
        programs.setModifytime(new Date());
        if (programsMapper.updateBySql(programs) > 0) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public List<TreeBean> getProgramTree(Integer companyId, Integer searchId) throws Exception {
        /**---------  构建树型目录  ----------*/
        //创建一级目录的树型结构，作为完整的返回的目录结构
        List<TreeBean> rootTree = new ArrayList<TreeBean>();
        try {
            /* 先获取公司信息，根据项目信息分级，该菜单只有两级 */
            String companysql = "SELECT DISTINCT id, name FROM menu where rank = 1 ";
            if (searchId == 0) {
                /** 若为管理员需要显示未分组的节目单*/
                TreeBean unBean = new TreeBean();
                //设置节点名称
                unBean.setName("未分组");
                //设置节点id
                unBean.setId(0L);
                //没有父节点，父节点id设置为0
                unBean.setPid(0L);
                //设置为父节点，改节点没有子节点，即不开启子节点
                unBean.setParent(true);
                //父节点需要展开
                unBean.setOpen(true);

                //创建二级目录的树形结构
                List<TreeBean> programTreeBeans = new ArrayList<TreeBean>();

                /*获取节目单列表，即该一级目录节点要显示的内容*/
                String programsql = "SELECT DISTINCT id, name FROM programs where companyid = 0 or companyid is null";
                List<Programs> programlist = programsMapper.selectBySql(programsql);
                //循环写入二级目录内容
                for (Programs program : programlist) {
                    //创建二级结点
                    TreeBean programBean = new TreeBean();
                    //设置节点名称
                    programBean.setName(program.getName());
                    //设置节点id
                    programBean.setId(ConvUtil.convToLong(program.getId()));
                    //设置父节点id
                    programBean.setPid(0L);
                    //将节点放入二级目录
                    programTreeBeans.add(programBean);

                }
                //将写好的二级目录放到一级节点上
                unBean.setChildren(programTreeBeans);

                //将写好的二级目录结构放到一级节点上，该目录节点内容完成
                rootTree.add(unBean);
            } else {
                companysql += " and id = " + searchId;
            }

            List<Menu> companylist = menuMapper.selectBySql(companysql);
            int companyid = 0;
            for (Menu company : companylist) {
                companyid = ConvUtil.convToInt(company.getId());
                if (companyid == 0) {
                    continue;
                } else {
                    /** 创建一级目录 **/
                    //创建一级节点：公司
                    TreeBean companyBean = new TreeBean();
                    //设置节点名称
                    companyBean.setName(company.getName());
                    //设置节点id
                    Long companyTreeId = ConvUtil.convToLong(companyid);
                    companyBean.setId(companyTreeId);
                    //设置为父节点，即开启子节点
                    companyBean.setParent(true);
                    //没有父节点，父节点id设置为0
                    companyBean.setPid(0L);
                    //判断是否需要展开父节点
                    if (companyid == ConvUtil.convToInt(companyId)) {
                        //设置节点展开
                        companyBean.setOpen(true);
                    }

                    /** 为了给一级目录的节点写入二级目录内容，需要构建二级目录的树形结构 */
                    //创建二级目录的树形结构
                    List<TreeBean> programTreeBeans = new ArrayList<TreeBean>();

                    /*获取节目单列表，即该一级目录节点要显示的内容*/
                    String programsql = "SELECT DISTINCT id, name FROM programs where companyid = " + companyid;
                    List<Programs> programlist = programsMapper.selectBySql(programsql);
                    //循环写入二级目录内容
                    for (Programs program : programlist) {
                        //创建二级结点
                        TreeBean programBean = new TreeBean();
                        //设置节点名称
                        programBean.setName(program.getName());
                        //设置节点id
                        programBean.setId(ConvUtil.convToLong(program.getId()));
                        //设置父节点id
                        programBean.setPid(companyTreeId);
                        //将节点放入二级目录
                        programTreeBeans.add(programBean);

                    }
                    //将写好的二级目录放到一级节点上
                    companyBean.setChildren(programTreeBeans);
                    //将写好的一级级目录结构放到根点上，该目录节点内容完成
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
    public Programs getProgramById(Integer id) {
        List<Programs> filelist = programsMapper.selectBySql("SELECT * FROM programs where id = " + id);
        if (filelist.size() > 0){
            return filelist.get(0);
        } else {
            return null;
        }
    }

    @Override
    public Programs getProgramByOnlyName(String name) {
        List<Programs> filelist = programsMapper.selectBySql("SELECT * FROM programs where name = '"+ name +"'");
        if (filelist.size() > 0){
            return filelist.get(0);
        } else {
            return null;
        }
    }

    @Override
    public Programs getProgramByName(String name, Integer companyid) {
        String sql = "SELECT * FROM programs where name = '"+ name +"' and companyid = "+ companyid;
        if (companyid == 0) {
            sql = "SELECT * FROM programs where name = '"+ name +"' and (companyid = 0 or companyid is null)";
        }
        List<Programs> filelist = programsMapper.selectBySql(sql);
        if (filelist.size() > 0) {
            return filelist.get(0);
        } else {
            return null;
        }
    }

    @Override
    public List<Programs> getProgramByNamecase(String name, Integer companyid) {
        String sql = "SELECT * FROM programs where name like '%"+ name +"%' and companyid = "+ companyid;
        if (companyid == 0) {
            sql = "SELECT * FROM programs where name like '%"+ name +"%'";
        }
        return programsMapper.selectBySql(sql);
    }

    @Override
    public List<Programs> getProgramByDeviceId(Integer deviceid, Integer companyid) {
        List<Programs> target = new ArrayList<>();

        String sql = "SELECT * FROM programs where companyid = "+ companyid;
        if (companyid == 0) {
            sql = "SELECT * FROM programs";
        }
        List<Programs> programlist = programsMapper.selectBySql(sql);
        for (Programs program : programlist) {
            String deviceids = "," + ConvUtil.convToStr(program.getRemark()) + ",";
            String targetid = "," + ConvUtil.convToStr(deviceid) + ",";
            if (deviceids.contains(targetid)) {
                target.add(program);
            }
        }

        return target;
    }

}