package com.highteam.router.common.util;

import com.highteam.router.common.m.BusinessException;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;

public class OssConfig {


    private final String CONFIG_PATH="conf/ossConfig.yml";

    private final String ENDPONIT="endpoint";
    private final String ENDPONIT_INTERNAL="endpoint_internal";
    private final String ACCESSKEYID="accessKeyId";
    private final String ACCESSKEYSECRET="accessKeySecret";
    private final String BUCKETNAME="bucketName";
    private final String ROOTPATH="rootPath";
    private final String SIGN="sign";

    private Map<String, Object> OSSCONFIG;

    private Map<String, String> map;


    private OssConfig() {}

    public OssConfig(String serverName) {
        InputStream in = OssConfig.class.getClassLoader().getResourceAsStream(CONFIG_PATH);
        Yaml yaml = new Yaml();
        OSSCONFIG = yaml.load(in);
        if(OSSCONFIG.containsKey(serverName)){
            map = (LinkedHashMap<String,String>)OSSCONFIG.get(serverName);
        }else{
            throw new BusinessException("不存在["+serverName+"]配置");
        }
    }

    public String getEndpoint() {
        return map.get(ENDPONIT);
    }

    public String getEndpointInternal() {
        return map.get(ENDPONIT_INTERNAL);
    }

    public String getAccessId() {
        return map.get(ACCESSKEYID);
    }

    public String getAccessKey() {
        return map.get(ACCESSKEYSECRET);
    }

    public String getBucket() {
        return map.get(BUCKETNAME);
    }

    public String getDir() {
        return map.get(ROOTPATH);
    }

    public String getSign() {
        return map.get(SIGN);
    }
}

