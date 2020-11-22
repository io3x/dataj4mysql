package com.github.io3x.app.libs.classes;

import com.github.io3x.app.Utils.CollectorUtils;
import com.github.io3x.app.func;
import com.github.io3x.app.libs.lock.dbTableLock;
import com.github.io3x.app.sboot.myLoader;
import com.github.io3x.syncd.adminController;
import com.liucf.dbrecord.Db;
import com.liucf.dbrecord.Record;
import org.apache.commons.lang.StringEscapeUtils;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Db
 */
public class db {
    /**
     * Map 2 record record
     *
     * @param map map
     * @return the record
     */
    public static Record map2record(Map map){
        Record record=new Record();
        return record.setColumns(map);
    }

    /**
     * Record 2 map map
     *
     * @param record record
     * @return the map
     */
    public static Map record2map(Record record){
        return record.getColumns();
    }

    /**
     * List 2 d array object [ ] [ ]
     *
     * @param list list
     * @return the object [ ] [ ]
     */
    public static Object[][] list2dArray(List<List<Object>> list){
        Object[][] strObj = new Object[list.size()][];
        for(int i=0;i<list.size();i++){
            List<Object> tmp = list.get(i);
            strObj[i] = tmp.toArray(new Object[tmp.size()]);
        }
        return strObj;
    }

    /**
     * Max index int. 获取当期数据集合的最大值最小值
     *
     * @param list     the list
     * @param indexKey the index key
     * @return the int
     */
    public static Map<String,Integer> maxminIndex(List<Map<String, Object>> list,String indexKey){
        Map<String,Integer> r =  new HashMap<>();
        try {
            List<Integer> rr = new ArrayList<>();
            list.forEach(map->{
                rr.add(Integer.valueOf(String.valueOf(map.get(indexKey))));
            });
            r.put("max",Collections.max(rr));
            r.put("min",Collections.max(rr));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return r;
    }

    /**
     * Schmema
     */
    public static Map<String,List<String>> schmema=null;
    public static Map<String,Map<String,String>> schmemaTableFieldType=null;

    /**
     * Addfield * 更快速的操作对象增加值
     *
     * @param table table
     * @param field field
     */
    synchronized public  static void addfield(String table,String field){
        try {
            if(!schmema.containsKey(table)) {
                schmema.put(table,new ArrayList<>());
            }
            schmema.get(table).add(field);

            if(!schmemaTableFieldType.containsKey(table)) {
                schmemaTableFieldType.put(table,new HashMap<>());
            }
            schmemaTableFieldType.get(table).put(field,"text");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Set scheme
     */
    synchronized public static void setScheme(){
        List<String> tables = Db.query("show tables");
        Map<String,List<Map>> info = new HashMap<>();
        Map<String,Map<String,String>> infoFieldTypes = new HashMap<>();
        tables.forEach(table->{
            List<Map> tmp = fetch_all(String.format("show full columns from %s",table));
            info.put(table,tmp);
            /*记录表字段类型 开始*/
            Map<String,String> FieldTypes = new HashMap<>();
            tmp.forEach(map->{
                FieldTypes.put(String.valueOf(map.get("Field")),String.valueOf(map.get("Type")));
            });
            infoFieldTypes.put(table,FieldTypes);
            /*记录表字段类型 结束*/
        });
        Map<String,List<String>> r = new HashMap<>();
        info.forEach((k,v)->{
            List<String> x = new ArrayList<>();
            v.forEach(item->{
                x.add(String.valueOf(item.get("Field")));
            });
            r.put(k,x);
        });
        schmemaTableFieldType = infoFieldTypes;
        schmema = r;
    }

    /**
     * The constant metadata.
     */
    public static Map<String,Integer> metadata=new ConcurrentHashMap<>();

    /**
     * Sync metadata.
     */
    synchronized public static void syncMetadata(){
        String engine;
        try {
            engine = myLoader.env.getProperty("dataj4json.table_engine");
        } catch (Exception e) {
            engine = "ENGINE=MyISAM DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci";
        }
        if(!schmema.containsKey("metadata")) {
            try {
                Db.update("CREATE TABLE `metadata` (\n" +
                        "  `table` varchar(255) NOT NULL DEFAULT '',\n" +
                        "  `sync_key` varchar(255) NOT NULL DEFAULT '',\n" +
                        "  `sync_value` bigint(11) NOT NULL DEFAULT '0',\n" +
                        "  `synd_datetime` datetime DEFAULT NULL\n" +
                        ") "+engine+" COMMENT='增量数据标记表'\n" +
                        "\n");
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            List<Map> x = fetch_all("select * from metadata");
            if(x!=null&&x.size()>0) {
                x.forEach(map->{
                    metadata.put(String.valueOf(map.get("table")),Integer.valueOf(String.valueOf(map.get("sync_value"))));
                });
            }
        }
    }

    /**
     * Sync metadata.
     *
     * @param table the table
     */
    public static void syncMetadata(String table){
        String obj = new StringBuffer().append("#syncMetadata").append(table).toString();
        synchronized (obj.intern()) {
            if(metadata!=null&&metadata.size()>0&&metadata.containsKey(table)) {
                int sync_value = metadata.get(table);
                if(sync_value>=0) {
                    try {
                        int count = Db.update("update metadata set sync_value = ?,synd_datetime=? where `table` = ? and sync_value<?",sync_value,func.datetime("yyyy-MM-dd HH:mm:ss"),table,sync_value);
                    } catch (Exception e) {
                    }
                }
            } else {
                Map md = fetch(String.format("select * from metadata where `table`='%s'",table));
                int sync_value;
                if(md==null||md.size()==0) {
                    Db.save("metadata",new Record().set("table",table).set("sync_value","0").set("synd_datetime",func.datetime("yyyy-MM-dd HH:mm:ss")));
                    sync_value = 0;
                } else {
                    sync_value = Integer.valueOf(String.valueOf(md.get("sync_value")));
                }

                metadata.put(table,sync_value);
            }
        }
    }

    public static void copyTable(String table,String table_to,boolean isCopyData){
        /*如果目标表不存在,则新建表*/
        if(!db.schmema.containsKey(table_to)) {
            db.initTable(table_to);
            /*修复索引类型*/
            try {
                if(db.schmemaTableFieldType.get(table_to).get("xxx3id").equals("bigint(11)")) {

                } else {
                    Db.use().update(String.format("alter table %s modify xxx3id bigint(11)",table_to));
                    Db.use().update(String.format("alter table %s drop primary key",table_to));
                    Db.use().update(String.format("ALTER TABLE %s ADD INDEX nk_xxx3id(`xxx3id`)",table_to));
                }
            } catch (Exception e) {

            }
        }

        dbTableLock.lock(()->{
            /*处理表字段 开始*/
            Map<String,String> defaultTypes = adminController.defaultTypes();
            db.schmema.get(table).forEach(field->{
                /*存在字段*/
                if(db.schmema.get(table_to).contains(field)) {

                } else {
                    String fk = db.schmemaTableFieldType.get(table).get(field);
                    if(defaultTypes.containsKey(fk)) {
                        String atSql = defaultTypes.get(fk).replace("modify column","add column");
                        Db.use().update(String.format(atSql,table_to,field));
                    } else {
                        Db.use().update(String.format("ALTER TABLE %s ADD  `%s` text COMMENT ''",table_to,field));
                    }
                }
            });
            db.setScheme();
            /*删除多余的字段*/
            db.schmema.get(table_to).forEach(field->{
                if(!db.schmema.get(table).contains(field)) {
                    Db.use().update(String.format("ALTER TABLE `%s` drop COLUMN `%s`",table_to,field));
                }
            });
            db.setScheme();
            /*处理表字段 结束*/

            /*复制数据*/
            if(isCopyData) {
                Db.use().update(String.format("insert into %s select * from %s where xxx3id not in (select xxx3id from %s)",table_to,table,table_to));
            }
        },table_to);
    }

    /**
     * Fetch all list 查询所有数据
     *
     * @param sql sql
     * @return the list
     */
    public static List<Map> fetch_all(String sql){
        List<Record> tmp = Db.find(sql);
        List<Map> r = new ArrayList<>();
        if(tmp!=null&&tmp.size()>0) {
            tmp.forEach(record->{
                r.add(record.getColumns());
            });
        }
        return r;
    }

    /**
     * Fetch all strval list 非严格模式,所有返回结果以字符串格式展示 如果返回null则使用0置换 不能存在datetime字段情况
     * 查询非本工具自动维护的表推荐使用fetch_all_strval
     * @param sql sql
     * @return the list
     */
    public static List<Map> fetch_all_strval(String sql){
        List<Record> tmp = Db.find(sql);
        List<Map> r = new LinkedList<>();
        if(tmp!=null&&tmp.size()>0) {
            tmp.forEach(record->{
                Map<String,String> xtmp = new TreeMap<>();
                record.getColumns().forEach((k,v)->{
                    if(v==null) {
                        xtmp.put(k,"0");
                    } else {
                        xtmp.put(k,String.valueOf(v));
                    }
                });
                r.add(xtmp);
            });
        }
        return r;
    }

    public static List<List<Map>> fetch_all_trunk(String sql,int num){
        List<List<Map>> ls = new LinkedList<>();
        int page=1;
        for(;;){
            int startNum = (page-1)*num;
            List<Map> pageData = fetch_all_strval(sql+" LIMIT "+String.valueOf(startNum)+","+String.valueOf(num));
            if(pageData.isEmpty()) break;
            if(pageData.size()<num) {
                ls.add(pageData);
                break;
            }
            ls.add(pageData);
            ++page;
        }
        return ls;
    }

    /**
     * Fetch map.
     *
     * @param sql the sql
     * @return the map
     */
    public static Map fetch(String sql){
        try {
            Record record = Db.findFirst(sql);
            return record2map(record);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Init table *
     *
     * @param table table
     */
    public static void initTable(String table){
        String engine;
        try {
            engine = myLoader.env.getProperty("dataj4json.table_engine");
        } catch (Exception e) {
            engine = "ENGINE=MyISAM DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci";
        }
        String obj = new StringBuffer().append("app.libs.classes.db.initTable").append(table).toString();
        synchronized (obj.intern()){
            if(!schmema.containsKey(table)) {
                Db.use().update(String.format("CREATE TABLE `%s` ( `xxx3id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键', PRIMARY KEY (`xxx3id`) ) %s",table,engine));
                addfield(table,"xxx3id");
            }
        }

    }

    /**
     * Json 2 mysql * 必须保证每个list的map都具有相同的字段
     *
     * @param table table
     * @param json  json
     */
    public static void json2mysql(String table,String json){
        List<Map<String, Object>> arrJson = func.json_decode(json);
        map2mysql(table,arrJson);
    }

    private static void map2mysql(String table,List<Map<String, Object>> arrJson){
        /*写入表数据时判断是否锁定,最多等待10分钟*/
        dbTableLock.await(table,600);
        if(arrJson!=null&&arrJson.size()>0) {
            List<String> fields = new ArrayList<>();
            List<String> values = new ArrayList<>();
            Set<String> arrJsonKeys = new LinkedHashSet<>();
            /*循环找出所有key*/
            arrJson.forEach(map->{
                map.forEach((k,v)->{
                    arrJsonKeys.add(k);
                });
            });

            /*再次循环定义字段是否存在*/
            arrJsonKeys.forEach(k->{
                fields.add(k);
                values.add("?");
                /*判断字段是否存在*/
                List<String> curTable = db.schmema.get(table);
                if(!curTable.contains(k)) {
                    String lastField = curTable.get(curTable.size()-1);
                    try {
                        Db.update(String.format("ALTER TABLE %s ADD  `%s` text COMMENT '' AFTER `%s`",table,k,lastField));
                        db.addfield(table,k);
                    } catch (Exception e) {
                        LoggerFactory.getLogger(db.class).info("ALTER TABLE Error {}",e.getMessage());
                    }
                }
            });

            String strfields = "`"+String.join("`,`",fields)+"`";
            String strvalus = String.join(",",values);
            String sql = String.format("insert into %s (%s) values (%s)",table,strfields,strvalus);
            List<List<Object>> data = new LinkedList<>();
            arrJson.forEach(map->{
                List<Object> oblist = new LinkedList<>();
                /*根据arrJsonKeys索引增加数据*/
                arrJsonKeys.forEach(k->{
                    /*判断已存在的字段类型*/
                    String fieldType;
                    try {
                        fieldType = db.schmemaTableFieldType.get(table).get(k);
                    } catch (Exception e) {
                        fieldType = "";
                    }

                    if(map.containsKey(k)) {
                        String tmp = String.valueOf(map.get(k));
                        /*有值且是时间日期类型,判断是否匹配*/
                        if(fieldType.equals("datetime")) {
                            if(tmp.matches("[0-9]{4}-[0-9]{2}-[0-9]{2} [0-9]{2}:[0-9]{2}:[0-9]{2}[0-9\\.]{0,}")){
                                oblist.add(tmp);
                            } else {
                                oblist.add(null);
                            }
                        } else {
                            if(tmp.matches("[0-9\\.-]+")){
                                oblist.add(map.get(k));
                            } else if(tmp.equals("null")){
                                oblist.add("0");
                            } else {
                                oblist.add(tmp);
                            }
                        }
                    } else {
                        if(fieldType.equals("datetime")) {
                            oblist.add(null);
                        } else {
                            oblist.add("0");
                        }
                    }
                });
                data.add(oblist);

            });
            try {
                Db.batch(sql,list2dArray(data),500);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    /**
     * Json 2 mysql syncd * 默认有maxid控制 需要需要解除控制 则sync_key给个比索引大的值
     *
     * @param table    table
     * @param json     json
     * @param sync_key sync key
     */
    public static void json2mysql_syncd(String table,String json,String sync_key){
        List<Map<String, Object>> arrJson = func.json_decode(json);
        int cmax = maxminIndex(arrJson,sync_key).get("max");
        if(cmax<metadata.get(table)) {
            return;
        }
        List<List<Map<String, Object>>> x = CollectorUtils.fixedGrouping(arrJson,1000);
        x.forEach(arr->{
            List<String> newSK = arr.stream().map(m->String.valueOf(m.get(sync_key))).collect(Collectors.toList());
            if(!newSK.isEmpty()) {
                String strvalue = "'"+String.join("','",newSK)+"'";
                List<String> ls = new ArrayList<>();
                try {
                    String sql = String.format("select DISTINCT %s from %s where `%s` in (%s)",sync_key,table,sync_key,strvalue);
                    LoggerFactory.getLogger(db.class).info(sql);
                    List<Map> listmap= fetch_all(sql);
                    ls = listmap.stream().map(m->String.valueOf(m.get(sync_key))).collect(Collectors.toList());
                } catch (Exception e) {
                    e.printStackTrace();
                    ls = new ArrayList<>();
                }
                List<Map<String, Object>> newArr = new ArrayList<>();
                List<String> finalLs = ls;
                arr.forEach(item->{
                    String tmp = String.valueOf(item.get(sync_key));
                    if(finalLs.contains(tmp)) {

                    } else {
                        newArr.add(item);
                    }
                });
                if(!newArr.isEmpty()) {
                    //int min = maxminIndex(newArr,sync_key).get("min");
                    int max = maxminIndex(newArr,sync_key).get("max");
                    //int dbMax = Db.queryInt(String.format("select max(%s) as maxi from %s",sync_key,table));
                    int value = metadata.get(table);
                    metadata.put(table,max);
                    try {
                        map2mysql(table,newArr);
                    } catch (Exception e) {
                        e.printStackTrace();
                        metadata.put(table,value);
                    }
                }
            }
        });

    }


    /**
     * Json 2 mysql syncd 2 * 字符索引无数字递增值
     *
     * @param table    table
     * @param json     json
     * @param sync_key sync key
     */
    public static void json2mysql_syncd2(String table,String json,String sync_key){
        List<Map<String, Object>> arrJson = func.json_decode(json);
        List<List<Map<String, Object>>> x = CollectorUtils.fixedGrouping(arrJson,1000);
        x.forEach(arr->{
            List<String> newSK = arr.stream().map(m->String.valueOf(m.get(sync_key))).collect(Collectors.toList());
            if(!newSK.isEmpty()) {
                String strvalue = "'"+String.join("','",newSK)+"'";
                List<String> ls = new ArrayList<>();
                try {
                    String sql = String.format("select DISTINCT %s from %s where `%s` in (%s)",sync_key,table,sync_key,strvalue);
                    List<Map> listmap= fetch_all(sql);
                    ls = listmap.stream().map(m->String.valueOf(m.get(sync_key))).collect(Collectors.toList());
                } catch (Exception e) {
                    e.printStackTrace();
                    ls = new ArrayList<>();
                }
                List<Map<String, Object>> newArr = new ArrayList<>();
                List<String> finalLs = ls;
                arr.forEach(item->{
                    String tmp = String.valueOf(item.get(sync_key));
                    if(finalLs.contains(tmp)) {

                    } else {
                        newArr.add(item);
                    }
                });
                if(!newArr.isEmpty()) {
                    try {
                        map2mysql(table,newArr);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });

    }

    /**
     * Json 2 one * 单条数据写入
     *
     * @param table table
     * @param json  json
     */
    public static void json2one(String table,String json){
        initTable(table);
        /*写入表数据时判断是否锁定,最多等待10分钟
        dbTableLock.await(table,600);*/
        Map<String, Object> arrJson = func.json_decode(json);
        if (arrJson!=null&&arrJson.size()>0) {
            arrJson.forEach((k,v)->{
                /*判断字段是否存在*/
                List<String> curTable = db.schmema.get(table);
                if(!curTable.contains(k)) {
                    String lastField = curTable.get(curTable.size()-1);
                    Db.update(String.format("ALTER TABLE %s ADD  `%s` text COMMENT '' AFTER %s",table,k,lastField));
                    db.addfield(table,k);
                }
            });
            Db.save(table,map2record(arrJson));
        }
    }

    /**
     * Update int 更新数据方法
     *
     * @param table table
     * @param data  data
     * @param where where
     * @return the int
     */
    public static int update(String table,Map<String, Object> data,String where){
        StringBuffer sb = new StringBuffer();
        if(data.size()>0) {
            data.forEach((k,v)->{
                sb.append(String.format(" `%s`='%s' ,",k,StringEscapeUtils.escapeSql(String.valueOf(v))));
            });
        }
        String sets = sb.toString();
        sets = sets.substring(0, sets.length() - 1);

        String sql = String.format("update %s set %s where %s",table,sets,where);
        LoggerFactory.getLogger(db.class).info(sql);
        return Db.update(sql);
    }

    /**
     * Uniq sql string
     * 生成去重sql语句及执行操作
     *
     * @param table           table
     * @param numberFieldName number field name
     * @param ww              ww
     * @return the string
     */
    public static String uniqSql(boolean isDelete,String table,String numberFieldName,String ...ww){
        List<String> wwArr=(List<String>)Arrays.asList(ww);
        String groupbySql = String.join(",",wwArr);
        String sql = " select m.xxx3id from %s m, ( select a.max_fee,substring_index(substring_index(a.ids,',',b.help_topic_id+1),',',-1) as id from  (select max(%s) as max_fee,count(DISTINCT %s) as num,GROUP_CONCAT(xxx3id) as ids from %s  GROUP BY %s HAVING num>1) a join mysql.help_topic b on b.help_topic_id < (length(a.ids) - length(replace(a.ids,',',''))+1) ) n WHERE m.xxx3id=n.id and m.%s < n.max_fee ";
        String deSql = String.format(sql,table,numberFieldName,numberFieldName,table,groupbySql,numberFieldName);
        if(isDelete) {
            int delCount = Db.use().update(String.format("delete from %s WHERE xxx3id in ( select xxx3id from (%s) zzz )",table,deSql));
            return String.valueOf(delCount);
        }
        return deSql;
    }
}
