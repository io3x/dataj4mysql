package com.github.io3x.syncd;

import com.github.io3x.app.Utils.CollectorUtils;
import com.github.io3x.app.Utils.RequestParamsToMap;
import com.github.io3x.app.Utils.StrUtils;
import com.github.io3x.app.func;
import com.github.io3x.app.libs.classes.db;
import com.github.io3x.app.libs.lock.dbTableLock;
import com.github.io3x.syncd.classes.asyncHandleEvent;
import com.liucf.dbrecord.Db;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/syncd/admin")
public class adminController {
    Logger logger = LoggerFactory.getLogger(this.getClass());

    /*自动加载异步处理方法*/
    @Autowired
    asyncHandleEvent Event;

    @Autowired
    Environment environment;

    private static Map<String,Map> databaseInfo(){
        Map<String,Map> mapInfo = new TreeMap();
        db.schmema.keySet().forEach(table->{
            try {
                List<Map> tableIndex =  db.fetch_all(String.format("show index from `%s`",table));
                List<String> indexs = tableIndex.stream().map(map->String.valueOf(map.get("Column_name"))).collect(Collectors.toList());
                List<Map> tableType = db.fetch_all(String.format("desc `%s`",table));
                String tableComment = Db.queryStr(String.format("select table_comment from information_schema.tables  where table_schema=(select database()) and table_name='%s'",table));
                Map tmp = new HashMap(){{
                    put("fields",db.schmema.get(table));
                    put("indexs",indexs);
                    put("Comment",tableComment);
                    put("types",CollectorUtils.listMap2Map(tableType,"Field","Type"));
                }};
                mapInfo.put(table,tmp);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        return mapInfo;
    }

    public static Map<String,String> defaultTypes(){
        Map<String,String> x =  new LinkedHashMap<>();
        x.put("tinyint(4)","alter table `%s` modify column `%s` tinyint(4) NOT NULL DEFAULT '0' COMMENT ''");
        x.put("int(10)","alter table `%s` modify column  `%s` int(10) NOT NULL DEFAULT '0' COMMENT ''");
        x.put("bigint(11)","alter table `%s` modify column  `%s` bigint(11) NOT NULL DEFAULT '0' COMMENT ''");
        x.put("decimal(10,2)","alter table `%s` modify column  `%s` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT ''");
        x.put("decimal(10,4)","alter table `%s` modify column  `%s` decimal(10,4) NOT NULL DEFAULT '0.0000' COMMENT ''");
        x.put("decimal(16,4)","alter table `%s` modify column  `%s` decimal(16,4) NOT NULL DEFAULT '0.0000' COMMENT ''");
        x.put("varchar(36)","alter table `%s` modify column  `%s` varchar(36) NOT NULL DEFAULT '' COMMENT ''");
        x.put("varchar(128)","alter table `%s` modify column  `%s` varchar(128) NOT NULL DEFAULT '' COMMENT ''");
        x.put("varchar(255)","alter table `%s` modify column  `%s` varchar(255) NOT NULL DEFAULT '' COMMENT ''");
        x.put("varchar(2048)","alter table `%s` modify column  `%s` varchar(2048) NOT NULL DEFAULT '' COMMENT ''");
        x.put("datetime","alter table `%s` modify column  `%s` datetime DEFAULT NULL COMMENT ''");
        x.put("mediumtext","alter table `%s` modify column  `%s` mediumtext COMMENT ''");
        return x;
    }


    @RequestMapping(value = "/index",method = {RequestMethod.GET,RequestMethod.POST})
    public String index(Model model, HttpServletRequest request) {
        Map<String,Map> dbInfo = databaseInfo();
        model.addAttribute("dbInfo",dbInfo);
        Map<String,String> defaultTypes = defaultTypes();
        model.addAttribute("defaultTypes",defaultTypes);
        Map<String, String> info = RequestParamsToMap.getParameterStringMap(request);
        String table = info.get("table");
        String Comment = info.get("Comment");
        if(StrUtils.areNotEmpty(table)) {
            if(!dbInfo.get(table).get("Comment").equals(Comment)) {
                Db.update(String.format("ALTER TABLE `%s` COMMENT '%s'",table,Comment));
            }
            /*判断是否更新类型*/
            Map<String,String> rInfo = new HashMap<>();
            try {
                List<String> fields = (List<String>)dbInfo.get(table).get("fields");
                Map<String,String> types = (Map<String,String>)dbInfo.get(table).get("types");
                fields.forEach(field->{
                    String key = String.format("fields[%s]",field);
                    if(info.containsKey(key)) {
                        String value = info.get(key);
                        if(value.equals("None")) {

                        } else if(value.equals(types.get(field))){

                        } else if(defaultTypes.containsKey(info.get(key))){

                            try {
                                /*修改字段类型前,先保持历史数据格式一致*/
                                String nullNum = db.fetch(String.format("select count(*) as num from  %s WHERE `%s` is null or `%s` = '' or  `%s` ='0' ",table,field,field,field)).get("num").toString();
                                if(Integer.valueOf(nullNum)>0) {
                                    String defaultSqlTmp = defaultTypes.get(info.get(key));
                                    String fixsql;
                                    if(defaultSqlTmp.contains("DEFAULT ''")) {
                                        fixsql = String.format("update %s set `%s`='' where xxx3id in (select xxx3id from (select xxx3id from %s WHERE `%s` is null) a)",table,field,table,field);
                                    } else if(defaultSqlTmp.contains("DEFAULT '0")) {
                                        fixsql = String.format("update %s set `%s`=0 where xxx3id in (select xxx3id from (select xxx3id from %s WHERE `%s` is null) a)",table,field,table,field);
                                    } else if(defaultSqlTmp.contains("datetime DEFAULT NULL")) {
                                        fixsql = String.format("delete from `%s` where xxx3id in (select xxx3id from (select xxx3id from %s WHERE `%s` = '0' or `%s` = '' ) a)",table,table,field,field);
                                    } else {
                                        fixsql = " ";
                                    }
                                    dbTableLock.lock(()->{
                                        Db.update(fixsql);
                                    },table);
                                }
                                /*执行修改字段类型*/
                                String updateSql = String.format(defaultTypes.get(info.get(key)),table,field);
                                int rInt = Db.update(updateSql);
                                rInfo.put("EditField-"+field,String.valueOf(rInt));
                            } catch (Exception e) {
                                rInfo.put("EditFieldError-"+field,e.getMessage());
                            }
                        }
                    }
                });
                /*修改完字段类型后,重置系统记录的数据表类型*/
                db.setScheme();
            } catch (Exception e) {
                rInfo.put("e1",e.getMessage());
            }


            /*判断是否更改索引*/
            try {
                List<String> fields = (List<String>)dbInfo.get(table).get("fields");
                Map<String,String> types = (Map<String,String>)dbInfo.get(table).get("types");
                List<String> indexs = (List<String>)dbInfo.get(table).get("indexs");
                fields.forEach(field->{
                    String key = String.format("indexs[%s]",field);
                    if(info.containsKey(key)&&String.valueOf(info.get(key)).equals("1")) {
                        /*判断是否需要添加该索引*/
                        if(!indexs.contains(field)) {
                            try {
                                Db.update(String.format("alter table `%s` add index nk_%s(`%s`)",table,field,field));
                            } catch (Exception e2_1) {
                                rInfo.put("EditIndex-"+field,String.format("索引索引 %s 出错 %s",field,e2_1.getMessage()));
                            }
                        }
                    } else {
                        /*判断是否需要删除该索引*/
                        if(indexs.contains(field)&&!field.equals("xxx3id")) {
                            try {
                                Db.update(String.format("alter table `%s` drop index nk_%s",table,field));
                            } catch (Exception e2_2) {
                                rInfo.put("EditIndex-"+field,String.format("删除索引 %s 出错 %s",field,e2_2.getMessage()));
                            }
                        }
                    }
                });
            } catch (Exception e) {
                rInfo.put("e2",e.getMessage());
            }

            Event.fixdb(table);


            model.addAttribute("R",func.json_encode(rInfo));
            return "/syncd/R";
        } else {
            return "/syncd/admin/index";
        }
    }

    @RequestMapping(value = "/metadata",method = {RequestMethod.GET,RequestMethod.POST})
    public String metadata(Model model, HttpServletRequest request){
        Map<String, Object> info = RequestParamsToMap.getParameterMap(request);
        if(info.containsKey("dosubmit")) {
            Map<String,String> rInfo = new HashMap<>();
            String[] tables = request.getParameterValues("table[]");
            String[] sync_keys = request.getParameterValues("sync_key[]");
            String[] sync_values = request.getParameterValues("sync_value[]");

            /*如果存在数据表*/
            if(tables.length>0) {
                for(int i=0;i<tables.length;i++){
                    String table = tables[i];
                    int tmpi = i;
                    try {
                        int r = db.update("metadata",new HashMap<String,Object>(){{
                            put("sync_key",sync_keys[tmpi]);
                            put("sync_value",sync_values[tmpi]);
                        }},String.format(" `table` = '%s' ",table));
                        db.metadata.put(table,Integer.parseInt(sync_values[tmpi]));
                        rInfo.put(table,String.valueOf(r));
                    } catch (Exception e) {
                        rInfo.put(table,e.getMessage());
                    }
                }
            }
            model.addAttribute("R",func.json_encode(rInfo));
            return "/syncd/R";
        } else {
            /*先同步数据*/
            List<Map> x = db.fetch_all("select * from metadata");
            if(!x.isEmpty()) {
                x.forEach(map->{
                    db.syncMetadata(String.valueOf(map.get("table")));
                });
            }

            List<Map> metadata = db.fetch_all_strval("select * from metadata");
            model.addAttribute("table","metadata");
            model.addAttribute("metadata",metadata);

            return "/syncd/admin/metadata";
        }
    }
}
