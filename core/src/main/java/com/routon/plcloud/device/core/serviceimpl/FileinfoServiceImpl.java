package com.routon.plcloud.device.core.serviceimpl;

import com.routon.plcloud.device.core.service.FileinfoService;
import com.routon.plcloud.device.core.utils.ConvUtil;
import com.routon.plcloud.device.core.utils.SqlUtil;
import com.routon.plcloud.device.data.entity.Fileinfo;
import com.routon.plcloud.device.data.mapper.FileinfoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * @author FireWang
 * @date 2020/5/07 15:09
 */
@Service
public class FileinfoServiceImpl implements FileinfoService {
    @Autowired
    private FileinfoMapper fileinfoMapper;

    @Override
    public boolean insertFile(Fileinfo fileinfo) throws Exception {
        if (fileinfoMapper.insertNew(fileinfo) > 0) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean deleteFile(Fileinfo fileinfo) throws Exception {
        if (fileinfoMapper.deleteById(ConvUtil.convToInt(fileinfo.getId())) > 0) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean update(Fileinfo fileinfo) {
        boolean succ = false;
        int ns = fileinfoMapper.updateBySql(fileinfo);
        if (ns > 0) {
            succ = true;
        }
        return succ;
    }

    @Override
    public Integer getMaxCount(Map<String, Object> paramMap){
        String getallsql = "SELECT COUNT(id) FROM fileinfo ";
        String wheresql = getsql(paramMap);
        Integer target = fileinfoMapper.countBySql(getallsql + wheresql);
        return target;
    }

    @Override
    public List<Fileinfo> getMaxList(Map<String, Object> paramMap) throws Exception {
        String getallsql = "SELECT * FROM fileinfo ";
        String limitsql = " order by id desc";
        String wheresql = getsql(paramMap);
        List<Fileinfo> target = fileinfoMapper.selectBySql(getallsql + wheresql + limitsql);
        return target;
    }

    @Override
    public List<Fileinfo> searchFileforpage(Map<String, Object> paramMap, Integer page, Integer pageSize) throws Exception {
        int startrow = pageSize * (page - 1);
        String headsql = "SELECT a.id, a.remark, a.filetype, a.uploadtime, SUM(infoa) downstatus from (SELECT * from fileinfo ";
        String wheresql = getsql(paramMap);
        String joinsql = ") a LEFT JOIN (SELECT * FROM datastats where linktable = 'fileinfo') b on a.id = b.linkid";
        String limitsql = " GROUP BY a.id, a.remark, a.filetype, a.uploadtime ORDER BY id DESC limit " + pageSize + " offset " + startrow;

        List<Fileinfo> target = fileinfoMapper.selectBySql(headsql + wheresql + joinsql + limitsql);
        return target;
    }

    @Override
    public Fileinfo getFileById(Integer id) {
        List<Fileinfo> filelist = fileinfoMapper.selectBySql("SELECT * FROM fileinfo where id = " + id);
        if (filelist.size() > 0){
            return filelist.get(0);
        } else {
            return null;
        }
    }

    @Override
    public Fileinfo getFileByFileName(String filename) {
        List<Fileinfo> filelist = fileinfoMapper.selectBySql("SELECT * FROM fileinfo where filename = '"+ filename +"'");
        if (filelist.size() > 0){
            return filelist.get(0);
        } else {
            return null;
        }
    }

    @Override
    public Fileinfo getFileByUploadName(String uploadname) {
        List<Fileinfo> filelist = fileinfoMapper.selectBySql("SELECT * FROM fileinfo where uploadname = '"+ uploadname +"'");
        if (filelist.size() > 0){
            return filelist.get(0);
        } else {
            return null;
        }
    }

    /**
     * 公共方法：根据条件获取查询条件sql语句
     **/
    private String getsql(Map<String, Object> paramMap) {
        String wheresql = "where 1=1 ";
        wheresql += SqlUtil.getLikeSql(paramMap, "filename");
        wheresql += SqlUtil.getLikeSql(paramMap, "remark");
        wheresql += SqlUtil.getAsSql(paramMap, "userid");
        wheresql += SqlUtil.getAsSql(paramMap, "filetype");

        String start = ConvUtil.convToStr(paramMap.get("start")), end = ConvUtil.convToStr(paramMap.get("end"));
        if (!"".equals(start)) {
            wheresql = wheresql + " and uploadtime >= '" + start + "' ";
        }
        if (!"".equals(end)) {
            wheresql = wheresql + " and uploadtime <= '" + end + "' ";
        }

        int programid = ConvUtil.convToInt(paramMap.get("programid" ));
        if (programid > 0) {
            wheresql += "and id in (select fileids from programs where id = "+ programid + ")";
        }

        return wheresql;
    }
}