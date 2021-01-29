package com.routon.plcloud.device.api.utils;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;

/**
 * @author FireWang
 * @date 2020/5/18 14:40
 * 该工具类用于解析和生成带表头的Excel文件
 */
public class ExcelUtil {
    private static Logger logger = LoggerFactory.getLogger(ExcelUtil.class);

    /**
     * 解析Excel文件
     *
     * @param filepath   目标excel文件路径
     * @param sheetorder 文件表单顺位：即想要获取的sheet的顺位，原则上第一张表单就填1，不填默认解析第一张sheet
     **/
    public static List<List<String>> getSheetList(String filepath, int sheetorder) {
        /*创建返回List对象*/
        List<List<String>> target = new ArrayList<>();
        List<String> rowlist = new ArrayList<>();
        Workbook workbook = null;
        Sheet sheet = null;
        Row row = null;
        Cell cell = null;

        try {
            /*文件若不存在直接返回空list*/
            File targetfile = new File(filepath);
            if (!targetfile.exists()) {
                rowlist.add("fail");
                rowlist.add("文件不存在！");
                target.add(rowlist);
                return target;
            }

            /*读取文件，若文件格式不对直接返错误信息*/
            InputStream is = new FileInputStream(filepath);
            String format = filepath.substring(filepath.lastIndexOf("."));
            String xlsformat = ".xls", xlsxformat = ".xlsx";
            if (format.equals(xlsformat)) {
                workbook = new HSSFWorkbook(is);
            } else if (format.equals(xlsxformat)) {
                workbook = new XSSFWorkbook(is);
            } else {
                rowlist.add("fail");
                rowlist.add("文件格式错误：请上传.xlsx格式的文件！");
                target.add(rowlist);
                return target;
            }

            /*获取sheet*/
            if (sheetorder > 0) {
                sheet = workbook.getSheetAt(sheetorder - 1);
            } else {
                sheet = workbook.getSheetAt(0);
            }

            /*获取每行内容并写入目标集合*/
            //最大行数
            int maxrow = sheet.getPhysicalNumberOfRows();
            //最大列数
            int maxcell = sheet.getRow(0).getPhysicalNumberOfCells();
            for (int i = 0; i < maxrow; i++) {
                row = sheet.getRow(i);
                rowlist = new ArrayList<>();
                for (int j = 0; j < maxcell; j++) {
                    cell = row.getCell(j);
                    if (cell != null) {
                        rowlist.add(cell.toString());
                    } else {
                        rowlist.add("");
                    }
                }
                target.add(rowlist);
            }

        } catch (Exception e) {
            target = new ArrayList<>();
            rowlist = new ArrayList<>();
            rowlist.add("fail");
            rowlist.add("解析Excel异常！");
            target.add(rowlist);
            logger.info("解析Excel异常:{" + e.getMessage() + "}");
        }
        return target;
    }

    /**
     * 将数据列表转为Excel文件并输出
     *
     * @param response
     * @param target
     * @param filename
     * @return
     */
    public static boolean listToExcel(HttpServletResponse response, List<List<String>> target , String filename) {
        try {
            if (target != null && response != null) {
                //创建工作簿
                HSSFWorkbook workbook = new HSSFWorkbook();
                //创建工作表
                HSSFSheet wbSheet = workbook.createSheet("first");
                //按数据总数写入内容
                for (int i = 0; i < target.size(); i++) {
                    //创建工作行
                    HSSFRow rowdata = wbSheet.createRow(i);
                    //获取行数据
                    List<String> rowlist = target.get(i);
                    //写入行数据
                    for (int j = 0; j < rowlist.size(); j++) {
                        HSSFCell celldata = rowdata.createCell(j);
                        celldata.setCellValue(rowlist.get(j));
                    }
                }
                //输出文件
                response.setHeader("Content-Disposition", "attachment;filename=" + filename + ".xls");
                OutputStream outputStream = response.getOutputStream();
                workbook.write(outputStream);
                outputStream.close();
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
