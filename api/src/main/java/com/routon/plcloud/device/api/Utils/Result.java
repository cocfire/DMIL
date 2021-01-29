package com.routon.plcloud.device.api.utils;

import org.apache.commons.lang.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @ClassName:Result
 * @Description:返回结果类
 * @Author:shiquan
 * @Date:2019/3/22 17:28
 **/
public class Result {
    private static final long serialVersionUID = -1651614836984397356L;

    private String code;

    private Object result;

    private String message;

    public static final String SUCCESS_CODE = "0";

    public static final String FAIL_CODE = "1";

    private Result() {
    }

    private Result(String code, Object result, String message) {
        this.code = code;
        if (result != null && "".equals(String.valueOf(result).trim())) {
            this.result = new HashMap<String, Object>();
        } else {
            this.result = result;
        }
        this.message = message;
    }

    /**
     * 成功响应
     *
     * @param result
     * @return
     */
    public static Result successResult(Object result) {
        return result(SUCCESS_CODE, result, "");
    }


    /**
     * 成功响应, 默认result为map, 可调用<b>put(String key, Object value)</b>方法添加响应内容
     *
     * @return
     */
    public static Result succesMapResult() {
        return result(SUCCESS_CODE, new HashMap<String, String>(16), "");
    }


    /**
     * 失败响应
     *
     * @param errorMsg
     * @return
     */
    public static Result failResult(String errorMsg) {
        return result(FAIL_CODE, "", errorMsg);
    }

    public static Result failResult(String code, String errorMsg) {
        code = StringUtils.isBlank(code) ? FAIL_CODE : code;
        return result(code, "", errorMsg);
    }

    public static Map<String, Object> resultOfList(Object records, Long recordSum, Long rowsOfPage, Long page,
                                                   Object userData) {
        Map<String, Object> result = new HashMap<String, Object>(16);
        result.put("rows", records);
        if (null != userData) {
            result.put("userdata", userData);
        }
        result.put("records", recordSum);
        result.put("total", Math.round(Math.ceil(Double.parseDouble(recordSum.toString()) / rowsOfPage)));
        result.put("page", page);
        return result;
    }

    public static Result result(String code, Object result, String message) {
        return new Result(code, result, message);
    }

    public String getCode() {
        return code;
    }

    public Object getResult() {
        return result;
    }

    public String getMessage() {
        return message;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setResult(Object result) {
        this.result = result;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "Result{" +
                "code='" + code + '\'' +
                ", result=" + result +
                ", message='" + message + '\'' +
                '}';
    }
}
