package com.routon.plcloud.device.core.utils;

import java.util.Map;

/**
 * @author FireWang
 * @date 2020/6/1 10:18
 */
public class SqlUtil {
    /**
     * 获得确定查询条件sql
     *
     * @param paramMap
     * @param param
     * @return
     */
    public static String getAsSql(Map<String, Object> paramMap, String param) {
        String target = "";
        if (!"".equals(ConvUtil.convToStr(paramMap.get(param)))) {
            target = " and " + param + " = '" + ConvUtil.convToStr(paramMap.get(param)) + "'";
        } else {

        }
        return target;
    }

    /**
     * 获得模糊查询条件sql
     *
     * @param paramMap
     * @param param
     * @return
     */
    public static String getLikeSql(Map<String, Object> paramMap, String param) {
        String target = "";
        if (!"".equals(ConvUtil.convToStr(paramMap.get(param)))) {
            target = " and " + param + " like '%" + ConvUtil.convToStr(paramMap.get(param)) + "%'";
        } else {

        }
        return target;
    }

    /**
     * 公共方法：处理分组相关sql
     **/
    public static String getSqlByGroup(Map<String, Object> paramMap) {
        String wheresql = "";

        String zero = "0", one = "1";
        String companyid = ConvUtil.convToStr(paramMap.get("companyid"));
        if (zero.equals(companyid)) {
            wheresql += " and (companyid is null or companyid = 0)";
        } else if (one.equals(companyid)) {
            wheresql += " and (companyid is not null and companyid != 0)";
        } else {
            wheresql += SqlUtil.getAsSql(paramMap, "companyid");
        }

        String projectid = ConvUtil.convToStr(paramMap.get("projectid"));
        if (zero.equals(projectid)) {
            wheresql += " and (projectid is null or projectid = 0)";
        } else if (one.equals(projectid)) {
            wheresql += " and (projectid is not null and projectid != 0)";
        } else {
            wheresql += SqlUtil.getAsSql(paramMap, "projectid");
        }

        return wheresql;
    }
}
