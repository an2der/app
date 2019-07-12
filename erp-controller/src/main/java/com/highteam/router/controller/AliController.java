package com.highteam.router.controller;

import com.alibaba.fastjson.JSON;
import com.aliyun.oss.OSSClient;
import com.aliyun.oss.common.utils.BinaryUtil;
import com.aliyun.oss.model.MatchMode;
import com.aliyun.oss.model.PolicyConditions;
import com.highteam.router.common.util.OssConfig;
import com.highteam.router.common.util.OssUtil;
import com.highteam.router.dao.AttachmentMapper;
import com.highteam.router.model.Attachment;
import com.highteam.router.oauth2.model.OAuth2Request;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Controller
@RequestMapping("/ali")
public class AliController {
    @Autowired
    private AttachmentMapper attachmentMapper;
    @RequestMapping("/getOssSign")
    public void getSign(HttpServletRequest request, HttpServletResponse response, String serverName, String businessId) {
        OAuth2Request oAuth2Request = new OAuth2Request();
        oAuth2Request.setStatus(false);
        if (StringUtils.isEmpty(serverName)) {
            oAuth2Request.setMsg("无效的serverName");
            oAuth2Request.setCode("INVALID_SERVER_NAME");
            OssUtil.response(request,response,JSON.toJSONString(oAuth2Request));
            return;
        }
        try {
            OssConfig ossConfig = new OssConfig(serverName);
            String endpoint = ossConfig.getEndpoint();
            String accessId = ossConfig.getAccessId();
            String accessKey = ossConfig.getAccessKey();
            String bucket = ossConfig.getBucket();
            String dir = ossConfig.getDir() + "/" + businessId + "/" + UUID.randomUUID() + "/";
            String host = "http://" + bucket + "." + endpoint.substring(7);
            OSSClient client = new OSSClient(endpoint, accessId, accessKey);
            long expireTime = 30;
            long expireEndTime = System.currentTimeMillis() + expireTime * 1000;
            Date expiration = new Date(expireEndTime);
            PolicyConditions policyConds = new PolicyConditions();
            policyConds.addConditionItem(PolicyConditions.COND_CONTENT_LENGTH_RANGE, 0, 1048576000);
            policyConds.addConditionItem(MatchMode.StartWith, PolicyConditions.COND_KEY, dir);
            String postPolicy = client.generatePostPolicy(expiration, policyConds);
            byte[] binaryData = postPolicy.getBytes("utf-8");
            String encodedPolicy = BinaryUtil.toBase64String(binaryData);
            String postSignature = client.calculatePostSignature(postPolicy);
            Map<String, String> respMap = new LinkedHashMap<String, String>();
            respMap.put("accessid", accessId);
            respMap.put("policy", encodedPolicy);
            respMap.put("signature", postSignature);
            respMap.put("dir", dir);
            respMap.put("host", host);
            respMap.put("expire", String.valueOf(expireEndTime / 1000));
            response.setHeader("Access-Control-Allow-Origin", "*");
            response.setHeader("Access-Control-Allow-Methods", "GET, POST");
            OssUtil.response(request, response, JSON.toJSONString(respMap));
        } catch (UnsupportedEncodingException e) {
            oAuth2Request.setMsg("编码转换错误");
            OssUtil.response(request,response,JSON.toJSONString(oAuth2Request));
        } catch (IOException e) {
            oAuth2Request.setMsg("文件流处理错误");
            OssUtil.response(request,response,JSON.toJSONString(oAuth2Request));
        }catch (Exception e){
            oAuth2Request.setMsg(e.getMessage());
            OssUtil.response(request,response,JSON.toJSONString(oAuth2Request));
        }
    }

    @RequestMapping("/downloadOssFile")
    public void download(HttpServletRequest request, HttpServletResponse response,Integer attachmentId){
        Attachment attachment = attachmentMapper.selectByPrimaryKey(attachmentId);
        OssUtil.downloadFile(attachment,response);
    }
}