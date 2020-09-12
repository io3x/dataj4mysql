package com.github.io3x.syncd.classes;

import com.github.io3x.app.func;
import com.liucf.dbrecord.Db;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class asyncHandleEvent {
    Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * 异步修复数据
     */
    @Async
    public void fixdb(String table){
        logger.info("{} fixdb 修复数据",table,func.datetime("yyyy-MM-dd HH:mm:ss"));
        synchronized (table) {
            logger.info("{} 开始异步修复数据 {}",table,func.datetime("yyyy-MM-dd HH:mm:ss"));
            try {
                Db.update(String.format("repair table `%s`",table));
                Db.update(String.format("optimize table `%s`",table));
            } catch (Exception e) {
                logger.info("{} 修复数据出错 {}",table,e.getMessage());
            }
            logger.info("{} 结束异步修复数据 {}",table,func.datetime("yyyy-MM-dd HH:mm:ss"));
        }
    }

}
