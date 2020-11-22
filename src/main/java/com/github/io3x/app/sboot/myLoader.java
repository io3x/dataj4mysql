package com.github.io3x.app.sboot;

import com.github.io3x.app.func;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class myLoader {
    public static Logger logger = LoggerFactory.getLogger(myLoader.class);

    public static Environment env=null;


    //静态代码块会在依赖注入后自动执行,并优先执行
    static{
        logger.info("myLoader staticed... {}", func.getCurdatetime());
    }

    @PostConstruct
    public static void init(){
        logger.info("已初始化 @PostConstruct myLoader init");
    }

}
