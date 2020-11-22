package com.github.io3x.app.sboot;

import com.github.io3x.app.libs.classes.db;
import com.liucf.dbrecord.Db;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class dbRecordRunner implements ApplicationRunner {
    @Autowired
    Environment environment;
    private static final Logger LOG = LoggerFactory.getLogger(dbRecordRunner.class);


    @Override
    public void run(ApplicationArguments args) throws Exception {
        myLoader.env = environment;
        /*启动mysql*/
        Db.init(environment.getProperty("mydb.url"),environment.getProperty("mydb.username"), environment.getProperty("mydb.password"));
        db.setScheme();
        db.syncMetadata();
        LOG.info("==========dbRecordRunner project===========");
    }
}