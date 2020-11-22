package com.github.io3x.app;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONPath;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.util.ResourceUtils;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class func {
    public static String json_encode(Map<String,Object> data) {
        return JSON.toJSONString(data,SerializerFeature.DisableCircularReferenceDetect);
    }

    public static String json_encode(Object data){
        return JSON.toJSONString(data,SerializerFeature.DisableCircularReferenceDetect);
    }

    /**
     * Json path t.
     * 通过json字符串路径形式解析json成泛型
     *
     * @param <T>  the type parameter
     * @param json the json
     * @param path the path
     * @return the t
     */
    public static <T> T json_path(String json,String path){
        try {
            return (T)JSONPath.read(json,path);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static <T> T json_decode(String json) {
        return (T)JSON.parse(json);
    }

    public static String randStr(int len) {
        return RandomStringUtils.random(len,"1234567890qwertyuiopasdfghjklzxcvbnm").toUpperCase();
    }

    /**
     * Get curdatetime string.
     *
     * @return the string
     */
    public static String getCurdatetime(){
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
    }

    /**
     * In array boolean. 判断数组中是否存在值
     *
     * @param a   the a
     * @param arr the arr
     * @return the boolean
     */
    public static boolean in_array(String a,String[] arr){
        return Arrays.asList(arr).contains(a);
    }

    /*
     *
     * 全局 yaml 配置文件
     *
     * */
    private static Map<String,Map> p= new ConcurrentHashMap();
    public static Map yaml(String f){
        if(!p.containsKey(f)) {
            //ClassPathResource classPathResource = new ClassPathResource("templates"+File.separator+f+".yaml");
            try {
                File cnf = ResourceUtils.getFile("classpath:"+"templates"+File.separator+f+".yaml");
                //File cnf = classPathResource.getFile();
                if(cnf.isFile()) {
                    Yaml yaml = new Yaml();
                    try {
                        Map tmpMap = (Map) yaml.load(new FileInputStream(cnf));
                        p.put(f,tmpMap);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } catch ( Exception e) {
                e.printStackTrace();
            }
        }
        return p.get(f);
    }


    /**
     * Datetime string
     * yyyy-MM-dd HH:mm:ss
     *
     * @param format format
     * @return the string
     */
    public static String datetime(String format){
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern(format));
    }

    public static void sleep(int sec){
        try {
            int sL = sec * 1000;
            Thread.sleep(sL);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void sleep_ms(int sec){
        try {
            Thread.sleep(sec);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void println(Object... ss){
        StringBuffer sb = new StringBuffer();
        for(Object i:ss) {
            sb.append(String.format("  %s",i));
        }
        System.out.println(String.format("%s %s %s",datetime("yyyy-MM-dd HH:mm:ss"),Thread.currentThread().getName(),sb.toString()));
    }

    /**
     * 显示 func.CurrentLineInfo() 代码所在行信息
     *
     * @return the string
     */
    public static String CurrentLineInfo(){
        int originStackIndex = 2;
        StackTraceElement ste = Thread.currentThread().getStackTrace()[originStackIndex];
        return String.format("%s/%s/%s/%s",ste.getFileName(),ste.getClassName(),ste.getMethodName(),ste.getLineNumber());
    }
}
