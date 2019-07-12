package com.highteam.router.common.util;

import com.alibaba.fastjson.JSONObject;
import com.aliyun.oss.OSSClient;
import com.aliyun.oss.model.DeleteObjectsRequest;
import com.aliyun.oss.model.DeleteObjectsResult;
import com.aliyun.oss.model.OSSObject;
import com.highteam.router.model.Attachment;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.ArrayList;
import java.util.List;


public class OssUtil {
    public static void downloadFile(Attachment attachment,HttpServletResponse response){
        JSONObject oAuth2Request = new JSONObject();
        oAuth2Request.put("status",false);
        if(attachment == null){
            oAuth2Request.put("code","ATTACHMENT_DOES_NOT_EXIST");
            oAuth2Request.put("msg","文件不存在");
            response(null,response, oAuth2Request.toJSONString());
            return;
        }
        try {

            OssConfig ossConfig = new OssConfig(attachment.getServerName());
            // Endpoint以杭州为例，其它Region请按实际情况填写。
            String endpoint = ossConfig.getEndpoint();
            // 阿里云主账号AccessKey拥有所有API的访问权限，风险很高。强烈建议您创建并使用RAM账号进行API访问或日常运维，请登录 https://ram.console.aliyun.com 创建RAM账号。
            String accessKeyId = ossConfig.getAccessId();
            String accessKeySecret = ossConfig.getAccessKey();
            String bucketName = ossConfig.getBucket();
            String objectName = attachment.getFilepath();


            // 创建OSSClient实例。
            OSSClient client = new OSSClient(endpoint, accessKeyId, accessKeySecret);

            // ossObject包含文件所在的存储空间名称、文件名称、文件元信息以及一个输入流。
            OSSObject ossObject = client.getObject(bucketName, objectName);
            InputStream inputStream = ossObject.getObjectContent();
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len;
            while ((len = inputStream.read(buffer)) != -1 ) {
                byteArrayOutputStream.write(buffer, 0, len);
            }
            // 数据读取完成后，获取的流必须关闭，否则会造成连接泄漏，导致请求无连接可用，程序无法正常工作。
            String fileName = new String(attachment.getFilename().getBytes("gb2312"),"iso-8859-1");
            response.setHeader("Content-Disposition", "attachment;filename=" + fileName);
            response.setContentType("application/octet-stream");
            OutputStream outputStream = response.getOutputStream();
            outputStream.write(byteArrayOutputStream.toByteArray());

            outputStream.close();
            byteArrayOutputStream.close();
            // 关闭OSSClient。
            client.shutdown();
        } catch (UnsupportedEncodingException e) {
            oAuth2Request.put("code","UnsupportedEncodingException");
            oAuth2Request.put("msg","文件名编码转换错误");
            response(null,response, oAuth2Request.toJSONString());
        } catch (IOException e) {
            oAuth2Request.put("code","STREAM_EXCEPTION");
            oAuth2Request.put("msg","获取输出流错误");
            response(null,response, oAuth2Request.toJSONString());
        }catch (Exception e){
            oAuth2Request.put("code","EXCEPTION");
            oAuth2Request.put("msg",e.getMessage());
            response(null,response, oAuth2Request.toJSONString());
        }
    }
    public static void deleteFile(Attachment attachment){
        JSONObject oAuth2Request = new JSONObject();
        oAuth2Request.put("status",false);
        if(attachment == null){
            return;
        }
        OssConfig ossConfig = new OssConfig(attachment.getServerName());
        // Endpoint以杭州为例，其它Region请按实际情况填写。
        String endpoint = ossConfig.getEndpoint();
        // 阿里云主账号AccessKey拥有所有API的访问权限，风险很高。强烈建议您创建并使用RAM账号进行API访问或日常运维，请登录 https://ram.console.aliyun.com 创建RAM账号。
        String accessKeyId = ossConfig.getAccessId();
        String accessKeySecret = ossConfig.getAccessKey();
        String bucketName = ossConfig.getBucket();
        String objectName = attachment.getFilepath();


        // 创建OSSClient实例。
        OSSClient client = new OSSClient(endpoint, accessKeyId, accessKeySecret);

        // 删除文件。
        client.deleteObject(bucketName, objectName);

        // 关闭OSSClient。
        client.shutdown();
    }
    public static void deleteFiles(List<Attachment> attachments){
        if(attachments == null||attachments.size()==0){
            return;
        }
        OssConfig ossConfig = new OssConfig(attachments.get(0).getServerName());
        // Endpoint以杭州为例，其它Region请按实际情况填写。
        String endpoint = ossConfig.getEndpoint();
        // 阿里云主账号AccessKey拥有所有API的访问权限，风险很高。强烈建议您创建并使用RAM账号进行API访问或日常运维，请登录 https://ram.console.aliyun.com 创建RAM账号。
        String accessKeyId = ossConfig.getAccessId();
        String accessKeySecret = ossConfig.getAccessKey();
        String bucketName = ossConfig.getBucket();


        // 创建OSSClient实例。
        OSSClient client = new OSSClient(endpoint, accessKeyId, accessKeySecret);

        // 删除文件。
        List<String> keys = new ArrayList<String>();
        for(Attachment attachment : attachments) {
            keys.add(attachment.getFilepath());
        }
        DeleteObjectsResult deleteObjectsResult = client.deleteObjects(new DeleteObjectsRequest(bucketName).withKeys(keys));
        List<String> deletedObjects = deleteObjectsResult.getDeletedObjects();

        // 关闭OSSClient。
        client.shutdown();
    }
    public static void response(HttpServletRequest request, HttpServletResponse response, String results){
        response.setContentType("application/json; charset=UTF-8");
        String callbackFunName = request != null ? request.getParameter("callback"):null;
        try {
            if (callbackFunName == null || callbackFunName.equalsIgnoreCase("")) response.getWriter().println(results);
            else response.getWriter().println(callbackFunName + "( " + results + " )");
            response.setStatus(HttpServletResponse.SC_OK);
            response.flushBuffer();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
