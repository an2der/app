package com.highteam.router.common.util;

import com.alibaba.fastjson.util.TypeUtils;
import com.highteam.router.common.m.BusinessException;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class ExcelUtil {
    /**
     * 读取excel数据，默认忽略第一行
     * @param request
     * @return
     * @throws IOException
     */
    public <T> List<T> readData(HttpServletRequest request, Class<T>  clazz){
        return readData(request,true,clazz);
    }

    /**
     * 读取excel数据
     * @param request
     * @param ignoreFirstRow 忽略第一行
     * @return
     * @throws IOException
     */
    public <T> List<T>  readData(HttpServletRequest request,boolean ignoreFirstRow,Class<T> clazz) {
        List<T> resultData = new ArrayList<>();
        ByteArrayInputStream byteArrayInputStream = null;
        try {
            CommonsMultipartResolver multipartResolver=new CommonsMultipartResolver(
                    request.getSession().getServletContext());
            if(multipartResolver.isMultipart(request)) {
                //将request变成多部分request
                MultipartHttpServletRequest multiRequest = (MultipartHttpServletRequest) request;
                //获取multiRequest 中所有的文件名
                Iterator iter = multiRequest.getFileNames();
                while(iter.hasNext())
                {
                    //一次遍历所有文件
                    MultipartFile file=multiRequest.getFile(iter.next().toString());
                    if(file!=null)
                    {
                        byteArrayInputStream = new ByteArrayInputStream(file.getBytes());
                        POIFSFileSystem poifsFileSystem = new POIFSFileSystem(byteArrayInputStream);
                        HSSFWorkbook hssfWorkbook = new HSSFWorkbook(poifsFileSystem);
                        HSSFCell cell;
                        Field [] fields = clazz.getDeclaredFields();
                        Constructor<T> constructor = clazz.getConstructor();
                        if(!constructor.isAccessible()){
                            constructor.setAccessible(true);
                        }
                        for(int sheetIndex = 0; sheetIndex < hssfWorkbook.getNumberOfSheets();sheetIndex++){
                            HSSFSheet hssfSheet = hssfWorkbook.getSheetAt(sheetIndex);
                            for(int rowIndex = ignoreFirstRow?1:0;rowIndex < hssfSheet.getLastRowNum()+1;rowIndex++){
                                HSSFRow hssfRow = hssfSheet.getRow(rowIndex);
                                if(hssfRow == null){
                                    continue;
                                }
                                if(hssfRow.getLastCellNum() > fields.length){
                                    throw new BusinessException("Excel数据格式错误！");
                                }
                                T obj = constructor.newInstance();
                                for (short cellIndex = 0;cellIndex < hssfRow.getLastCellNum();cellIndex++){
                                    String fieldName = fields[cellIndex].getName();
                                    Class<?> typeClass = fields[cellIndex].getType();
                                    String methodName = "set" + toFirstLetterUpperCase(fieldName);
                                    Method method = clazz.getMethod(methodName,new Class[]{typeClass});
                                    cell = hssfRow.getCell(cellIndex);
                                    if(cell != null){
                                        method.invoke(obj,new Object[]{TypeUtils.castToJavaBean(cell.getStringCellValue().trim(), typeClass)});
                                    }else{
                                        method.invoke(obj,new Object[]{null});
                                    }
                                }
                                resultData.add(obj);
                            }
                        }
                    }

                }
            }
        }catch (BusinessException e){
            throw e;
        } catch (Exception e) {
            throw new BusinessException("文件读取错误");
        }finally {
            try {
                byteArrayInputStream.close();
            } catch (IOException e) {
                return null;
            }
        }
        return resultData;
    }

    public <T> void exportExcel(String filename, Map<String,String> heads, List<T> data, HttpServletResponse response) {
        OutputStream outputStream = null;
        HSSFWorkbook hssfWorkbook = new HSSFWorkbook();
        try {
            outputStream = response.getOutputStream();
            String fileName = new String((filename + ".xls").getBytes("gb2312"), "iso-8859-1");
            response.setHeader("Content-Disposition", "attachment;filename=" + fileName);
            response.setContentType("application/octet-stream");
            HSSFSheet hssfSheet = hssfWorkbook.createSheet();
            //创建首行，并设置样式
            HSSFRow headRow = hssfSheet.createRow(0);
            HSSFFont headfont = hssfWorkbook.createFont();
            headfont.setFontName("宋体");
            headfont.setFontHeightInPoints((short) 11);
            headfont.setBold(true);
            HSSFCellStyle headStyle = hssfWorkbook.createCellStyle();
            headStyle.setFont(headfont);
            headStyle.setAlignment(HorizontalAlignment.CENTER);
            headStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            headStyle.setWrapText(true);
            headStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headStyle.setFillForegroundColor(HSSFColor.SEA_GREEN.index);
            HSSFCell headCell;
            int headIndex = 0;
            for(String head:heads.keySet()){
                headCell = headRow.createCell(headIndex++);
                headCell.setCellStyle(headStyle);
                headCell.setCellValue(head);
            }
            //创建数据行
            HSSFFont datafont = hssfWorkbook.createFont();
            datafont.setFontName("宋体");
            datafont.setFontHeightInPoints((short) 11);
            HSSFCellStyle dataStyle = hssfWorkbook.createCellStyle();
            dataStyle.setFont(datafont);
            dataStyle.setAlignment(HorizontalAlignment.CENTER);
            dataStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            dataStyle.setWrapText(true);
            HSSFRow dataRow;
            HSSFCell dataCell;
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            for (int i = 0;i < data.size();i++){
                dataRow = hssfSheet.createRow(i+1);
                T obj = data.get(i);
                Class objClass = obj.getClass();
                int dataIndex = 0;
                for(String head:heads.keySet()){
                    dataCell = dataRow.createCell(dataIndex++);
                    dataCell.setCellStyle(dataStyle);
                    String methodName = "get" + toFirstLetterUpperCase(heads.get(head));
                    Object value = objClass.getMethod(methodName).invoke(obj);
                    if(value != null){
                        if(("java.util.Date").equals(value.getClass().getName())){
                            dataCell.setCellValue(simpleDateFormat.format(value));
                        }else {
                            dataCell.setCellValue(value.toString());
                        }
                    }else {
                        dataCell.setCellValue("");
                    }
                }
            }
            hssfWorkbook.write(outputStream);
        } catch (Exception e) {
            throw new BusinessException("导出失败");
        }finally {
            try {
                outputStream.close();
                hssfWorkbook.close();
            } catch (IOException e) {
                return;
            }

        }
    }
    private String toFirstLetterUpperCase (String str){
        if (str == null || str.length() < 2) {
            return str;
        }
        String firstLetter = str.substring(0, 1).toUpperCase();
        return firstLetter + str.substring(1);
    }
}
