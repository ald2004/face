package com.face.sdk.main;

import static com.face.sdk.util.ExcelUtil.readExcel;

/**
 * project : face-web-server
 * Code Create : 2019/4/28
 * Class : com.face.sdk.main.ImportExcel
 *
 * @author wangxiaoming
 * @author a345566462@163.com
 * @version 1.0.1
 * @since 1.0.1 April 2019
 */
public class ImportExcel {
    public ImportExcel() {
    }


    public static void main(String[] args) throws Exception {
        String filePath = "D:\\Downloads\\table-list.xlsx";
        readExcel(filePath).forEach(System.out::println);
    }
}
