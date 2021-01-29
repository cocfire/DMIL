package com.routon.plcloud.device.core.serviceimpl;

import com.routon.plcloud.device.core.service.OfflinemsgService;
import com.routon.plcloud.device.core.utils.ConvUtil;
import com.routon.plcloud.device.data.entity.Offlinemsg;
import com.routon.plcloud.device.data.mapper.OfflinemsgMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author FireWang
 * @date 2020/5/07 15:09
 */
@Service
public class OfflinemsgServiceImpl implements OfflinemsgService {
    @Autowired
    private OfflinemsgMapper offlinemsgMapper;

    @Override
    public List<Offlinemsg> searchMsgByCid(String clientid) throws Exception {
        String sql = "select * from offlinemsg where status = 0 and clientid = '" + clientid + "' order by id";
        return offlinemsgMapper.selectBySql(sql);
    }

    @Override
    public boolean insert(Offlinemsg offlinemsg) throws Exception {
        if (offlinemsgMapper.insertNew(offlinemsg) > 0) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean update(Offlinemsg offlinemsg) throws Exception {
        boolean succ = false;
        int ns = offlinemsgMapper.updateBySql(offlinemsg);
        if (ns > 0) {
            succ = true;
        }
        return succ;
    }

    @Override
    public int insertAndRid(Offlinemsg offlinemsg) throws Exception {
        //查询即将生成的id
        int id = 0;
        //查存成功，插入数据，随后返回id，失败一律返回0
        if (insert(offlinemsg)) {
            List<Offlinemsg> list = offlinemsgMapper.selectBySql("select last_value id from msg_id_seq");
            if (list.size() > 0) {
                id = ConvUtil.convToInt(list.get(0).getId());
            }
        }
        return id;
    }

    @Override
    public Offlinemsg getMsgById(Integer id) {
        List<Offlinemsg> msglist = offlinemsgMapper.selectBySql("SELECT * FROM offlinemsg where id = " + id);
        if (msglist.size() > 0){
            return msglist.get(0);
        } else {
            return null;
        }
    }
}