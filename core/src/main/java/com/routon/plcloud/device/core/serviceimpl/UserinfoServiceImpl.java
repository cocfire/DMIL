package com.routon.plcloud.device.core.serviceimpl;

import com.routon.plcloud.device.core.common.TreeBean;
import com.routon.plcloud.device.core.service.UserinfoService;
import com.routon.plcloud.device.core.utils.ConvUtil;
import com.routon.plcloud.device.core.utils.SqlUtil;
import com.routon.plcloud.device.data.entity.Menu;
import com.routon.plcloud.device.data.entity.Userinfo;
import com.routon.plcloud.device.data.mapper.MenuMapper;
import com.routon.plcloud.device.data.mapper.UserinfoMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @author FireWang
 * @date 2020/5/6 13:25
 */
@Service
public class UserinfoServiceImpl implements UserinfoService {
    private Logger logger = LoggerFactory.getLogger(UserinfoServiceImpl.class);

    @Autowired
    private UserinfoMapper userinfoMapper;

    @Autowired
    private MenuMapper menuMapper;

    @Override
    public Userinfo getAdmin() {
        List<Userinfo> users = userinfoMapper.selectBySql("select * from userinfo where id = 888");
        Userinfo admin = new Userinfo();
        if (users.size() > 0) {
            admin = users.get(0);
        } else {
            admin = null;
        }
        return admin;
    }

    @Override
    public boolean saveAdmin(Userinfo user) {
        boolean succ = false;
        user.setModifytime(new Date());
        int ns = userinfoMapper.updateBySql(user);
        if (ns > 0) {
            succ = true;
        }
        return succ;
    }

    @Override
    public boolean addUser(Userinfo user) {
        user.setCreatetime(new Date());
        user.setModifytime(new Date());
        user.setStatus(1);

        if (userinfoMapper.insertNew(user) > 0) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public Userinfo getUserById(Integer id) {
        List<Userinfo> userList = userinfoMapper.selectBySql("SELECT * FROM userinfo where id = " + id);
        if (userList.size() > 0) {
            return userList.get(0);
        } else {
            return null;
        }
    }

    @Override
    public boolean deleteUserById(Integer id) {
        boolean deletesign = false;
        if (userinfoMapper.deleteById(id) > 0) {
            deletesign = true;
        }
        return deletesign;
    }

    @Override
    public Userinfo getUserByUerName(String username) {
        List<Userinfo> userList = userinfoMapper.selectBySql("SELECT * FROM userinfo where username = '" + username + "'");
        if (userList.size() > 0) {
            return userList.get(0);
        } else {
            return null;
        }
    }

    @Override
    public Userinfo getUserByPhone(String phone) {
        List<Userinfo> userList = userinfoMapper.selectBySql("SELECT * FROM userinfo where phone = '" + phone + "'");
        if (userList.size() > 0) {
            return userList.get(0);
        } else {
            return null;
        }
    }

    @Override
    public Userinfo getUserByCompany(String company) {
        Userinfo userinfo = new Userinfo();
        List<Userinfo> userList = userinfoMapper.selectBySql("SELECT * FROM userinfo where company = '" + company + "'");
        if (userList.size() > 0) {
            userinfo = userList.get(0);
        } else {
            userinfo = null;
        }
        return userinfo;
    }

    @Override
    public Integer getMaxCount(Map<String, Object> paramMap){
        String getallsql = "SELECT COUNT(id) FROM userinfo a ";
        String wheresql = getsql(paramMap);
        Integer target = userinfoMapper.countBySql(getallsql + wheresql);
        return target;
    }

    @Override
    public List<Userinfo> getMaxList(Map<String, Object> paramMap) throws Exception {
        String getallsql = "SELECT * FROM userinfo a";
        String wheresql = getsql(paramMap);
        List<Userinfo> target = userinfoMapper.selectBySql(getallsql + wheresql);
        return target;
    }

    @Override
    public List<Userinfo> searchUserforpage(Map<String, Object> paramMap, Integer page, Integer pageSize) throws Exception {
        int startrow = pageSize * (page - 1);
        String limitsql = " order by a.id desc limit " + pageSize + " offset " + startrow;
        String getallsql = "SELECT a.id, a.username, a.phone, a.realname, a.status, a.id, b.name as company FROM userinfo a " +
                "left join menu b on a.company = CAST(b.id AS text) ";
        String wheresql = getsql(paramMap);

        List<Userinfo> target = userinfoMapper.selectBySql(getallsql + wheresql + limitsql);
        return target;
    }

    @Override
    public List<TreeBean> getCompanyTree() throws Exception {
        /**---------  构建树型目录  ----------*/
        //创建一级目录的树型结构，作为完整的返回的目录结构
        List<TreeBean> rootTree = new ArrayList<TreeBean>();
        try {
            /**目前一级目录只有两个，创建第一个目录：未分组*/
            TreeBean unBean = new TreeBean();
            //设置节点id
            unBean.setId(0L);
            //设置节点名称
            unBean.setName("未分组");
            //没有父节点，父节点id设置为0
            unBean.setPid(0L);
            //设置为父节点，改节点没有子节点，即不开启子节点
            unBean.setParent(true);
            //将写好的二级目录结构放到一级节点上，该目录节点内容完成
            rootTree.add(unBean);

            /**目前一级目录只有两个，创建第二个目录：公司*/
            TreeBean companyTreeBean = new TreeBean();
            //设置节点id
            companyTreeBean.setId(1L);
            //设置节点名称
            companyTreeBean.setName("公司");
            //没有父节点，父节点id设置为0
            companyTreeBean.setPid(0L);
            //设置为父节点，即开启子节点
            companyTreeBean.setParent(true);
            //设置节点展开
            companyTreeBean.setOpen(true);

            /**为了给一级目录的节点写入二级目录内容，需要构建二级目录的树形结构*/
            //创建二级目录的树形结构
            List<TreeBean> companyBeanList = new ArrayList<TreeBean>();
            /**获取系统软件列表，即该一级目录节点要显示的内容*/
            String companysql = "SELECT DISTINCT id, name FROM menu where rank = 1 and status = 1 ORDER BY id";
            List<Menu> menuList = menuMapper.selectBySql(companysql);

            //循环写入二级目录内容
            for (Menu menu : menuList) {
                //创建二级结点
                TreeBean companyBean = new TreeBean();
                //将erp设置为节点id
                companyBean.setId(ConvUtil.convToLong(menu.getId()));
                //将软件名称写入节点
                companyBean.setName(menu.getName());
                //设置父节点id，即“公司”的id
                companyBean.setPid(1L);
                //将节点放入二级目录
                companyBeanList.add(companyBean);
            }
            //将写好的二级目录放到一级节点上
            companyTreeBean.setChildren(companyBeanList);
            rootTree.add(companyTreeBean);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        /**返回构建完成的目录结构 the end*/
        return rootTree;
    }

    @Override
    public void delLinkedMenuId(Integer menuId) {
        String sql = "select * from userinfo a ";

        //删除公司信息，用户改为禁用
        Map<String, Object> companyMap = new HashMap<>(16);
        companyMap.put("company", menuId);
        List<Userinfo> users = userinfoMapper.selectBySql(sql + getsql(companyMap));
        for (Userinfo user : users) {
            user.setCompany("");
            user.setStatus(0);
            userinfoMapper.updateBySql(user);
        }
    }

    /**
     * 获取查询条件sql语句
     **/
    private String getsql(Map<String, Object> paramMap) {
        String wheresql = " where a.id != 888 ";

        wheresql += SqlUtil.getLikeSql(paramMap, "username");
        wheresql += SqlUtil.getLikeSql(paramMap, "realname");
        wheresql += SqlUtil.getLikeSql(paramMap, "phone");
        wheresql += SqlUtil.getLikeSql(paramMap, "remark");

        wheresql += SqlUtil.getAsSql(paramMap, "a.status");

        String company = ConvUtil.convToStr(paramMap.get("company"));
        String zero = "0", one = "1";
        if (zero.equals(company)) {
            wheresql += " and (company = '' or company is null)";
        } else if (one.equals(company)) {
            wheresql += " and (company != '' and company is not null)";
        } else {
            wheresql += SqlUtil.getAsSql(paramMap, "company");
        }

        return wheresql;
    }
}
