package com.routon.plcloud.device.api.controller.commom;

import com.alibaba.fastjson.JSONObject;
import com.routon.plcloud.device.api.config.emq.EmqClient;
import com.routon.plcloud.device.core.service.OfflinemsgService;
import com.routon.plcloud.device.core.service.ParaminfoService;
import com.routon.plcloud.device.core.utils.ConvUtil;
import com.routon.plcloud.device.data.entity.Device;
import com.routon.plcloud.device.data.entity.Offlinemsg;
import com.routon.plcloud.device.data.entity.Paraminfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author FireWang
 * @date 2020/12/17 16:18
 * 设备数据信息公共方法集合
 */
public class CommonForDevice {
    private static Logger logger = LoggerFactory.getLogger(CommonForDevice.class);

    /**
     * 是否支持EMQ消息反馈机制，从v1.2开始支持
     *
     * @param version
     * @return
     */
    public static boolean isSuportEmqRet(String version) {
        Double startVersion = 1.2;
        //EMQ协议版本格式：v1.2，String类型
        if (!"".equals(ConvUtil.convToStr(version))) {
            Double verSize = ConvUtil.convToDouble(version.substring(1));
            if (verSize >= startVersion) {
                return true;
            }
        }
        return false;
    }

    /**
     * 消息发布之前进行离线确认机制处理，从v1.2开始支持
     * 注：该方法是为了格式化新版的发布消息，不能用于储存离线消息
     *
     * @param topic
     * @param emqmsg
     * @param device
     * @param remark
     * @return
     */
    public static JSONObject emqRetPrase(OfflinemsgService offlinemsgService, String topic, JSONObject emqmsg, Device device, String remark) {
        try {
            //协议支持的（即EMQ协议v1.2及以上的），消息添加message_id并储存
            if (isSuportEmqRet(device.getEmqversion())) {
                Offlinemsg offlinemsg = new Offlinemsg(topic, JSONObject.toJSONString(emqmsg), device.getClientid(), remark);

                //插入离线消息并返回id
                int msgid = offlinemsgService.insertAndRid(offlinemsg);
                if (msgid > 0) {
                    //messageid生成规则，“offlinemsg”拼接当前id
                    emqmsg.put("message_id", "offlinemsg" + msgid);
                }

                //更新消息内容：增加message_id
                offlinemsg = offlinemsgService.getMsgById(msgid);
                if (offlinemsg != null) {
                    offlinemsg.setMessage(JSONObject.toJSONString(emqmsg));
                    offlinemsgService.update(offlinemsg);
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
            e.printStackTrace();
            return emqmsg;
        }
        return emqmsg;
    }

    /**
     * 直接进行消息确认机制处理并发布，从v1.2开始使用
     *
     * @param topic
     * @param emqmsg
     * @param device
     * @param remark
     * @return
     */
    public static boolean emqRetPub(EmqClient emqClient, OfflinemsgService offlinemsgService, String topic, JSONObject emqmsg, Device device, String remark) {
        try {
            emqmsg = emqRetPrase(offlinemsgService, topic, emqmsg, device, remark);
            //设备在线直接发布，不在线则转存离线消息
            if (ConvUtil.convToInt(device.getStatus()) == 1) {
                //发布参数到设备
                emqClient.pubTopic(topic, JSONObject.toJSONString(emqmsg));
            } else {
                if (!isSuportEmqRet(device.getEmqversion())) {
                    Offlinemsg offlinemsg = new Offlinemsg(topic, JSONObject.toJSONString(emqmsg), device.getClientid(), remark);
                    offlinemsgService.insert(offlinemsg);
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * 不进行离线确认机制处理，直接走发布流程
     *
     * @param topic
     * @param emqmsg
     * @param device
     * @param remark
     * @return
     */
    public static boolean emqDirctPub(EmqClient emqClient, OfflinemsgService offlinemsgService, String topic, JSONObject emqmsg, Device device, String remark) {
        try {
            //设备在线直接发布，不在线则转存离线消息
            if (ConvUtil.convToInt(device.getStatus()) == 1) {
                //发布参数到设备
                emqClient.pubTopic(topic, JSONObject.toJSONString(emqmsg));
            } else {
                Offlinemsg offlinemsg = new Offlinemsg(topic, JSONObject.toJSONString(emqmsg), device.getClientid(), remark);
                offlinemsgService.insert(offlinemsg);
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * 版本一获取参数信息
     *
     * @param termsn
     */
    public static Paraminfo getVersionOne(ParaminfoService paraminfoService, String termsn) {
        //获取参数信息
        Map<String, Object> condition = new HashMap<>(16);
        condition.put("termsn", termsn);
        condition.put("paramid", 1100);
        List<Paraminfo> paramlists = paraminfoService.getMaxList(condition);
        Paraminfo param = null;
        if (paramlists.size() > 0) {
            param = (Paraminfo) paramlists.get(0);
        }
        return param;
    }
}
