package com.routon.plcloud.device.api.controller.otherdevice;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.routon.plcloud.device.api.utils.HttpClientUtil;
import com.routon.plcloud.device.core.utils.ConvUtil;
import com.routon.plcloud.device.core.utils.PropertiesUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author FireWang
 * @date 2020/5/11 17:28
 * 该类用于从轻量级平台获取的公共信息
 */
public class CommonForFace {
    private static Logger logger = LoggerFactory.getLogger(CommonForFace.class);

    /**
     * http请求客户端
     */
    private static HttpClientUtil httpClient = HttpClientUtil.getInstance();

    /**
     * 轻量级系统地址
     */
    private static String faceurl = PropertiesUtil.getDataFromPropertiseFile("otherDeviceCommon", "facelib_ip");

    /**
     * 轻量级系统端口
     */
    private static String faceport = PropertiesUtil.getDataFromPropertiseFile("otherDeviceCommon", "face_port");

    /**
     * 返回码
     */
    protected static String result = "result";

    /**
     * 成功返回码
     */
    protected static String success = "0";

    /**
     * 失败返回码
     */
    protected static String fail = "1";


    /**
     * 获取人脸库名称：不含参
     **/
    public static String getFaceLibName() {
        String facelibname = "plcloud";
        //创建查询对象
        JSONObject requsetlist = new JSONObject();
        requsetlist.put("command", "FaceLibraryQuery");
        requsetlist.put("version", 0x101);
        requsetlist.put("face_library_id", 0);

        JSONObject getrmsg = getRequstJson(requsetlist, "获取人脸库");
        String result = ConvUtil.convToStr(getrmsg.get("result"));
        if (success.equals(result)) {
            JSONArray faceliblist = JSONArray.parseArray(ConvUtil.convToStr(getrmsg.get("face_library_list")));
            JSONObject facelib = JSONObject.parseObject(ConvUtil.convToStr(faceliblist.get(0)));
            String name = ConvUtil.convToStr(facelib.get("name"));
            if (!"".equals(name)) {
                facelibname = ConvUtil.convToStr(facelib.get("name"));
            }
        }

        return facelibname;
    }


    /**
     * 注册终端信息：7个参数
     * String	face_library_name	人脸库名称
     * String	term_name	        终端名称
     * String	term_sn	            终端序列号
     * int	    term_business	    终端业务类型
     * String	soft_ver	        终端软件版本信息
     * String	term_ip	            终端IP地址
     * int	    term_model	        设备型号
     */
    public static JSONObject termInfoRegister(JSONObject jsonMap) {
        //创建请求对象
        JSONObject requsetlist = new JSONObject();
        requsetlist.put("command", "TermInfoRegister");
        requsetlist.put("version", 0x100);
        requsetlist.put("face_library_name", ConvUtil.convToStr(jsonMap.get("face_library_name")));
        requsetlist.put("term_name", ConvUtil.convToStr(jsonMap.get("term_name")));
        requsetlist.put("term_sn", ConvUtil.convToStr(jsonMap.get("term_sn")));
        requsetlist.put("term_business", ConvUtil.convToInt(jsonMap.get("term_business")));
        requsetlist.put("soft_ver", ConvUtil.convToStr(jsonMap.get("soft_ver")));
        requsetlist.put("term_ip", ConvUtil.convToStr(jsonMap.get("term_ip")));
        requsetlist.put("term_model", ConvUtil.convToInt(jsonMap.get("term_model")));

        JSONObject getrmsg = getRequstJson(requsetlist, "注册终端信息");

        return getrmsg;
    }


    /**
     * 查询数据状态： 2个参数
     * int	face_library_id	人脸库ID
     * int	term_id	        终端id
     */
    public static JSONObject dataStatusQuery(JSONObject jsonMap) {
        //创建请求对象
        JSONObject requsetlist = new JSONObject();
        requsetlist.put("command", "DataStatusQuery");
        requsetlist.put("version", 0x100);
        requsetlist.put("face_library_id", ConvUtil.convToInt(jsonMap.get("face_library_id")));
        requsetlist.put("term_id", ConvUtil.convToInt(jsonMap.get("term_id")));

        JSONObject getrmsg = getRequstJson(requsetlist, "查询数据状态");

        return getrmsg;
    }


    /**
     * 获取终端权限： 6个参数
     * int	    face_library_id	人脸库ID
     * int	    term_id	        终端ID
     * int	    mode	        返回数据范围
     * String	revno	        当前版本号
     * int	    start	        起始索引号
     * int	    rec_num	        返回的记录总数
     */
    public static JSONObject termFaceQuery(JSONObject jsonMap) {
        //创建请求对象
        JSONObject requsetlist = new JSONObject();
        requsetlist.put("command", "TermFaceQuery");
        requsetlist.put("version", 0x102);
        requsetlist.put("face_library_id", ConvUtil.convToInt(jsonMap.get("face_library_id")));
        requsetlist.put("term_id", ConvUtil.convToInt(jsonMap.get("term_id")));
        requsetlist.put("mode", ConvUtil.convToInt(jsonMap.get("mode")));
        requsetlist.put("revno", ConvUtil.convToStr(jsonMap.get("revno")));
        requsetlist.put("start", ConvUtil.convToInt(jsonMap.get("start")));
        requsetlist.put("rec_num", ConvUtil.convToInt(jsonMap.get("rec_num")) == 0 ? (-1) : ConvUtil.convToInt(jsonMap.get("rec_num")));

        JSONObject getrmsg = getRequstJson(requsetlist, "获取终端权限");

        return getrmsg;
    }


    /**
     * 获取人员信息： 15个参数
     * int	    face_library_id	人脸库ID
     * int	    face_id	        人脸ID
     * int	    depart_id	    部门ID
     * int	    attribute	    属性
     * String	openid
     * String	tel_no	        手机号
     * String	id	            身份证号
     * int	    term_id	        终端ID
     * int	    status	        人员状态
     * int	    picture	        照片条件
     * String	query_info	    查询条件
     * List	    depart_list	    部门列表
     * int	    include_child_depart	是否包含子部门
     * int	    start	        起始索引号
     * int	    face_num	    返回的记录总数
     */
    public static JSONObject faceInfoQuery(JSONObject jsonMap) {
        //创建请求对象
        JSONObject requsetlist = new JSONObject();
        requsetlist.put("command", "FaceInfoQuery");
        requsetlist.put("version", 0x106);
        requsetlist.put("face_library_id", ConvUtil.convToInt(jsonMap.get("face_library_id")));
        requsetlist.put("face_id", ConvUtil.convToInt(jsonMap.get("face_id")));
        requsetlist.put("depart_id", ConvUtil.convToInt(jsonMap.get("depart_id")));
        requsetlist.put("attribute", ConvUtil.convToInt(jsonMap.get("attribute")));
        requsetlist.put("openid", ConvUtil.convToStr(jsonMap.get("openid")));
        requsetlist.put("tel_no", ConvUtil.convToStr(jsonMap.get("tel_no")));
        requsetlist.put("id", ConvUtil.convToStr(jsonMap.get("id")));
        requsetlist.put("term_id", ConvUtil.convToInt(jsonMap.get("term_id")));
        requsetlist.put("status", ConvUtil.convToInt(jsonMap.get("status")));
        requsetlist.put("picture", ConvUtil.convToInt(jsonMap.get("picture")));
        requsetlist.put("query_info", ConvUtil.convToStr(jsonMap.get("query_info")));
        requsetlist.put("depart_list", jsonMap.get("depart_list"));
        requsetlist.put("include_child_depart", ConvUtil.convToInt(jsonMap.get("include_child_depart")));
        requsetlist.put("start", ConvUtil.convToInt(jsonMap.get("start")));
        requsetlist.put("face_num", ConvUtil.convToInt(jsonMap.get("face_num")) == 0 ? (-1) : ConvUtil.convToInt(jsonMap.get("face_num")));

        JSONObject getrmsg = getRequstJson(requsetlist, "获取人员信息");

        return getrmsg;
    }


    /**
     * 获取人脸照片： 7个参数
     * int	face_library_id	人脸库ID
     * int	attribute	    属性
     * int	image_num	    人脸照片数量
     * List	image_list	    人脸照片列表
     * int	face_num	    人员数量
     * int	image_type	    返回的照片类型
     * List	face_list	    人员列表
     */
    public static JSONObject facePhotoQuery(JSONObject jsonMap) {
        //创建请求对象
        JSONObject requsetlist = new JSONObject();
        requsetlist.put("command", "FacePhotoQuery");
        requsetlist.put("version", 0x101);
        requsetlist.put("face_library_id", ConvUtil.convToInt(jsonMap.get("face_library_id")));
        requsetlist.put("attribute", ConvUtil.convToInt(jsonMap.get("attribute")));
        requsetlist.put("image_num", ConvUtil.convToInt(jsonMap.get("image_num")));
        requsetlist.put("image_list", jsonMap.get("image_list"));
        requsetlist.put("face_num", ConvUtil.convToInt(jsonMap.get("face_num")));
        requsetlist.put("image_type", ConvUtil.convToInt(jsonMap.get("image_type")));
        requsetlist.put("face_list", jsonMap.get("face_list"));

        JSONObject getrmsg = getRequstJson(requsetlist, "获取人脸照片");

        return getrmsg;
    }


    /**
     * 上报刷脸日志： 24个参数
     * int	    face_library_id	人脸库ID
     * int	    attribute	    属性
     * int	    face_id	        人脸ID
     * String	name	        姓名
     * String	idcard	        身份证号
     * int	    term_id	        终端ID
     * int	    compare_result	比对结果
     * float	compare_score	比对分数
     * String	temperature	    体温
     * int	    mask	        口罩
     * String	image	        现场照
     * String	preview_photo_path	刷脸照片路径
     * DateTime	create_time	    刷脸时间
     * int	    conf_id	        会议ID
     * int	    business	    人脸业务类型ID
     * int	    rec_id	        平台记录ID
     * int	    rec_type	    记录类型
     * int	    gender	        性别
     * String	idcard_img	        身份证照片
     * String	nation	        民族
     * String	addr	        地址
     * String	birth	        出生日期
     * String	agency	        身份证签发机关
     * String	valid_date	    身份证有效期
     */
    public static JSONObject faceRecordUpload(JSONObject jsonMap) {
        //创建请求对象
        JSONObject requsetlist = new JSONObject();
        requsetlist.put("command", "FaceRecordUpload");
        requsetlist.put("version", 0x102);
        requsetlist.put("face_library_id", ConvUtil.convToInt(jsonMap.get("face_library_id")));
        requsetlist.put("attribute", ConvUtil.convToInt(jsonMap.get("attribute")));
        requsetlist.put("face_id", ConvUtil.convToInt(jsonMap.get("face_id")));
        requsetlist.put("name", ConvUtil.convToStr(jsonMap.get("name")));
        requsetlist.put("idcard", ConvUtil.convToStr(jsonMap.get("idcard")));
        requsetlist.put("term_id", ConvUtil.convToInt(jsonMap.get("term_id")));
        requsetlist.put("compare_result", ConvUtil.convToInt(jsonMap.get("compare_result")));
        requsetlist.put("compare_score", ConvUtil.convToDouble(jsonMap.get("compare_score")));
        requsetlist.put("temperature", ConvUtil.convToStr(jsonMap.get("temperature")));
        requsetlist.put("mask", ConvUtil.convToInt(jsonMap.get("mask")));
        requsetlist.put("image", ConvUtil.convToStr(jsonMap.get("image")));
        requsetlist.put("preview_photo_path", ConvUtil.convToStr(jsonMap.get("preview_photo_path")));
        requsetlist.put("create_time", ConvUtil.convToStr(jsonMap.get("create_time")));
        requsetlist.put("conf_id", ConvUtil.convToInt(jsonMap.get("conf_id")));
        requsetlist.put("business", ConvUtil.convToInt(jsonMap.get("business")));
        requsetlist.put("rec_id", ConvUtil.convToInt(jsonMap.get("rec_id")));
        requsetlist.put("rec_type", ConvUtil.convToInt(jsonMap.get("rec_type")));
        requsetlist.put("gender", ConvUtil.convToInt(jsonMap.get("gender")));
        requsetlist.put("idcard_img", ConvUtil.convToStr(jsonMap.get("idcard_img")));
        requsetlist.put("nation", ConvUtil.convToStr(jsonMap.get("nation")));
        requsetlist.put("addr", ConvUtil.convToStr(jsonMap.get("addr")));
        requsetlist.put("birth", ConvUtil.convToStr(jsonMap.get("birth")));
        requsetlist.put("agency", ConvUtil.convToStr(jsonMap.get("agency")));
        requsetlist.put("valid_date", ConvUtil.convToStr(jsonMap.get("valid_date")));

        JSONObject getrmsg = getRequstJson(requsetlist, "上报刷脸日志");

        return getrmsg;
    }


    /**
     * 获取目标对象的实体
     *
     * @param jsonObject
     * @param paramName
     * @return
     */
    public static JSONObject getParamByName(JSONObject jsonObject, String paramName) {
        JSONObject target = new JSONObject();
        if (jsonObject.getJSONObject(paramName) != null) {
            target = jsonObject.getJSONObject(paramName);
        }
        return target;
    }

    /**
     * 公共获取返参方法
     *
     * @param requsetMap 请求参数
     * @param apiInfo    请求接口名称
     */
    private static JSONObject getRequstJson(JSONObject requsetMap, String apiInfo) {

        logger.info("向轻量级人脸发送" + apiInfo + "请求：" + JSONObject.toJSONString(requsetMap));
        JSONObject getrmsg = JSONObject.parseObject(httpClient.doPostForJsonParam("http://" + faceurl + ":" + faceport + "/", requsetMap));
        logger.info("收到轻量级人脸的" + apiInfo + "返回：" + JSONObject.toJSONString(getrmsg));

        if (getrmsg == null) {
            getrmsg = new JSONObject();
        }
        return getrmsg;
    }

}
