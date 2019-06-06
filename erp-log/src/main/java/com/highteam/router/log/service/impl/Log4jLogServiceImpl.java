package com.highteam.router.log.service.impl;

import com.highteam.router.log.model.Log;
import com.highteam.router.log.service.LogService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;

@Service("log4jLogService")
public class Log4jLogServiceImpl implements LogService {
    private Logger logger = LogManager.getLogger("RollingLogger");

    @Override
    public void recordLog(Log log) {
        Class c = log.getClass();
        try {
            logger.info("--------------------Log Start--------------------");
            for(Field field : c.getDeclaredFields()){
                field.setAccessible(true);
                logger.info(field.getName()+" : "+field.get(log));
            }
            logger.info("--------------------Log End  --------------------");
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}
