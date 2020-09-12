package com.github.io3x.syncd;

import com.github.io3x.app.func;
import com.github.io3x.app.libs.classes.db;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class Cron {
    /**
     * Logger
     */
    Logger logger = LoggerFactory.getLogger(this.getClass());
    /**
     * Model
     */

    /**
     * 每分钟同步元数据最大值
     */
    @Scheduled(cron = "0 */1 * * * ?")
    synchronized public void syncMetadata(){
        db.metadata.forEach((table,value)->{
            db.syncMetadata(table);
        });
        logger.info("每分钟同步元数据最大值:{}", func.json_encode(db.metadata));
    }
}
