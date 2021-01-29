package com.routon.plcloud.device.api.controller.webservice;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.routon.plcloud.device.api.utils.DateUtils;
import com.routon.plcloud.device.core.service.DatastatsService;
import com.routon.plcloud.device.core.service.DeviceService;
import com.routon.plcloud.device.core.service.FileinfoService;
import com.routon.plcloud.device.core.utils.ConvUtil;
import com.routon.plcloud.device.data.entity.Datastats;
import com.routon.plcloud.device.data.entity.Device;
import com.routon.plcloud.device.data.entity.Fileinfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * @author FireWang
 * @date 2020/12/9 14:45
 * 播放记录数据统计类
 */
@Controller
@RequestMapping(value = "/WebService/playrecord")
public class PlayRecord {
    private static Logger logger = LoggerFactory.getLogger(PlayRecord.class);

    @Autowired
    DeviceService deviceService;

    @Autowired
    FileinfoService fileinfoService;

    @Autowired
    DatastatsService datastatsService;

    /**
     * 播放记录上报
     *
     * @param dataObject
     * @return
     */
    @RequestMapping(value = "/report", method = RequestMethod.POST)
    public @ResponseBody
    JSONObject report(@RequestBody JSONObject dataObject) {
        //创建应答对象
        JSONObject responseOb = new JSONObject();
        int transCode = 1001, result = 0;
        String message = "SUCCESS";
        try {
            String termSn = ConvUtil.convToStr(dataObject.get("term_sn"));
            int tCode = ConvUtil.convToInt(dataObject.get("trans_code"));
            JSONArray dataList = dataObject.getJSONArray("data_list");
            if (termSn != null && tCode == transCode) {
                logger.info("收到设备{" + termSn + "}播放记录上报请求：" + dataObject);

                for (Object filedata : dataList) {
                    JSONObject fileData = (JSONObject) filedata;
                    String filename = ConvUtil.convToStr(fileData.get("file_name"));
                    String playDate = ConvUtil.convToStr(fileData.get("play_date"));
                    //修正错误时间，统一改为前一天
                    if (ConvUtil.convToInt(playDate.substring(0,4)) < 2020) {
                        SimpleDateFormat formatter= new SimpleDateFormat("yyyy-MM-dd");
                        Date today = new Date();
                        playDate = DateUtils.getPreDateByDate(formatter.format(today));
                    }
                    int playTimes = ConvUtil.convToInt(fileData.get("play_times"));

                    // 获取关联信息，插入记录
                    Device device = deviceService.searchDeviceBySn(termSn);
                    Fileinfo fileinfo = fileinfoService.getFileByFileName(filename);
                    if (device != null && fileinfo != null) {
                        Datastats data = datastatsService.getByLink(ConvUtil.convToInt(fileinfo.getId()), termSn, playDate);
                        if (data == null) {
                            data = new Datastats();
                            data.setLinktable("fileinfo");
                            data.setLinkid(ConvUtil.convToInt(fileinfo.getId()));
                            data.setLinkinfo(termSn);
                            data.setInfoa(playTimes);
                            data.setInfob(playDate);
                            data.setCreatetime(new Date());
                            data.setModifytime(new Date());
                            datastatsService.insert(data);
                        } else {
                            data.setInfoa(ConvUtil.convToInt(data.getInfoa()) + playTimes);
                            data.setModifytime(new Date());
                            datastatsService.update(data);
                        }
                    }
                }

            } else {
                result = -1;
                message = "缺少必要的字段或业务代码有误！";
            }

        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e.getMessage());
            result = -3;
            message = "参数格式错误，参数类型或长度不正确！";
        }
        //组织应答报文
        responseOb.put("result", result);
        responseOb.put("trans_code", transCode);
        responseOb.put("message", message);

        logger.info("设备播放记录上报响应：" + responseOb);
        return responseOb;
    }

}
