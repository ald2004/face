package com.face.sdk.util;

import com.face.sdk.main.FaceUserEntity;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * project : face-web-server
 * Code Create : 2019/4/28
 * Class : com.face.sdk.util.ExcelUtil
 *
 * @author wangxiaoming
 * @author a345566462@163.com
 * @version 1.0.1
 * @since 1.0.1 April 2019
 */
public class ExcelUtil {
    public ExcelUtil() {
    }

    public static List<FaceUserEntity> readExcel(InputStream inputStream, boolean isXlsx) throws IOException, InvalidFormatException {

        Workbook wb;
        //根据文件后缀（xls/xlsx）进行判断
        if (isXlsx) {
            wb = new XSSFWorkbook(inputStream);
        } else {
            wb = new HSSFWorkbook(inputStream);
        }
        //开始解析
        Sheet sheet = wb.getSheetAt(0);     //读取sheet 0

        int firstRowIndex = sheet.getFirstRowNum() + 1;   //第一行是列名，所以不读
        int lastRowIndex = sheet.getLastRowNum();
        //姓名    证件号    联系方式    状态    图片    附加信息

        List<FaceUserEntity> faceUserEntityList = new ArrayList<>();

        for (int rIndex = firstRowIndex; rIndex <= lastRowIndex; rIndex++) {   //遍历行
            Row row = sheet.getRow(rIndex);
            if (row != null) {
                String name = row.getCell(0).toString().trim();
                String idCard = row.getCell(1).toString().trim();
                String phone = row.getCell(2).toString().trim();
                String statusStr = row.getCell(3).toString().trim();
                String photo = row.getCell(4).toString().trim();
                String des = row.getCell(5).toString().trim();

                FaceUserEntity entity = new FaceUserEntity();
                entity.setName(name);
                entity.setIdCard(idCard);
                entity.setPhone(phone);
                entity.setPhoto(photo);
                entity.setDes(des);
                int status = 0;
                switch (statusStr ) {
                    case "禁用":
                        status = 0;
                        break;
                    case "黑名单":
                        status = 1;
                        break;
                    case "白名单":
                        status = 2;
                        break;
                }
                entity.setStatus(status);
                faceUserEntityList.add(entity);
            }
        }
        return faceUserEntityList;
    }

    public static List<FaceUserEntity> readExcel(String filePath) throws IOException, InvalidFormatException {
        File excel = new File(filePath);
        String[] split = excel.getName().split("\\.");  //.是特殊字符，需要转义！！！！！

        boolean isXlsx;
        //根据文件后缀（xls/xlsx）进行判断
        if ("xls".equals(split[1])) {
            isXlsx = false;
        } else if ("xlsx".equals(split[1])) {
            isXlsx = true;
        } else {
            throw new IllegalArgumentException("文件类型错误!");
        }
        return readExcel(new FileInputStream(excel), isXlsx);
    }
}
