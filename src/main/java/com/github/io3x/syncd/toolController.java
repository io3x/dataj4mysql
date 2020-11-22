package com.github.io3x.syncd;

import com.github.io3x.app.Utils.CollectorUtils;
import com.github.io3x.app.Utils.RequestParamsToMap;
import com.github.io3x.app.Utils.StrUtils;
import com.github.io3x.app.func;
import com.github.io3x.app.libs.classes.db;
import com.github.io3x.app.sboot.R;
import com.liucf.dbrecord.Db;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/syncd/tool")
public class toolController {
    @Autowired
    Environment environment;

    Logger logger = LoggerFactory.getLogger(this.getClass());

    public String adminPassword(){
        return environment.getProperty("dataj4json.password");
    }
    /**
     *
     * 测试模式
     * @param request the request
     * @return the string
     */
    @RequestMapping(value = "/test",method = {RequestMethod.GET,RequestMethod.POST})
    public R test(HttpServletRequest request) {
        String ticket = func.randStr(16);
        Map<String, Object> data = RequestParamsToMap.getParameterMap(request);
        String table = String.valueOf(data.get("table"));
        String json = String.valueOf(data.get("json"));
        if(StrUtils.areNotEmpty(table,json)) {
            db.initTable(table);
            if(!db.schmema.containsKey(table)) return R.error(-1,"数据表不存在");
            db.json2mysql(table,json);
        } else {
            return R.error(-1,"参数错误");
        }
        return R.ok().put(ticket);
    }

    @RequestMapping(value = "/json2one",method = {RequestMethod.GET,RequestMethod.POST})
    public R json2one(HttpServletRequest request) {
        String ticket = func.randStr(16);
        Map<String, Object> data = RequestParamsToMap.getParameterMap(request);
        String table = String.valueOf(data.get("table"));
        String json = String.valueOf(data.get("json"));
        if(StrUtils.areNotEmpty(table,json)) {
            db.json2one(table,json);
        } else {
            return R.error(-1,"参数错误");
        }
        return R.ok().put(ticket);
    }

    @RequestMapping(value = "/json",method = {RequestMethod.GET,RequestMethod.POST})
    public R json(HttpServletRequest request) {
        String ticket = func.randStr(16);
        Map<String, Object> data = RequestParamsToMap.getParameterMap(request);
        String table = String.valueOf(data.get("table"));
        List<Map> r = new ArrayList<>();
        if(StrUtils.areNotEmpty(table)) {
            r = db.fetch_all(String.format("select * from %s",table));
        } else {
            return R.error(-1,"参数错误");
        }
        return R.ok().put(r);
    }

    @RequestMapping(value = "/schmema",method = {RequestMethod.GET,RequestMethod.POST})
    public R schmema(HttpServletRequest request) {
        //db.setScheme();
        return R.ok().put("schmema",db.schmema).put("FieldType",db.schmemaTableFieldType);
    }

    @RequestMapping(value = "/syncd",method = {RequestMethod.GET,RequestMethod.POST})
    public R syncd(HttpServletRequest request) {
        String ticket = func.randStr(16);
        Map<String, Object> data = RequestParamsToMap.getParameterMap(request);
        String table = String.valueOf(data.get("table"));
        String sync_key = String.valueOf(data.get("sync_key"));
        String json = String.valueOf(data.get("json"));
        if(StrUtils.areNotEmpty(table,sync_key,json)) {
            db.initTable(table);
            db.syncMetadata(table);
            db.json2mysql_syncd(table,json,sync_key);
        } else {
            return R.error(-1,"参数错误");
        }
        return R.ok().put(ticket).put("metadata",db.metadata);
    }

    @RequestMapping(value = "/syncd2",method = {RequestMethod.GET,RequestMethod.POST})
    public R syncd2(HttpServletRequest request) {
        String ticket = func.randStr(16);
        Map<String, Object> data = RequestParamsToMap.getParameterMap(request);
        String table = String.valueOf(data.get("table"));
        String sync_key = String.valueOf(data.get("sync_key"));
        String json = String.valueOf(data.get("json"));
        if(StrUtils.areNotEmpty(table,sync_key,json)) {
            db.initTable(table);
            db.json2mysql_syncd2(table,json,sync_key);
        } else {
            return R.error(-1,"参数错误");
        }
        return R.ok().put(ticket);
    }

    @RequestMapping(value = "/syncd3",method = {RequestMethod.GET,RequestMethod.POST})
    public R syncd3(String table,String json,String new_key,@RequestParam(value = "union_key[]") String[] union_key) {
        String ticket = func.randStr(16);
        if(union_key.length>0) {
            List<String> kk = new LinkedList<>();
            for (String key:union_key) {
                if(StrUtils.isNotEmpty(key)) kk.add(key);
            }
            if(kk.isEmpty()) {
                return R.error(-1,"联合键不能为空");
            }
            if(StrUtils.areNotEmpty(table,json,new_key)) {
                db.initTable(table);
                List<Map> newData = CollectorUtils.listMapAddnewkey(func.json_decode(json),new_key,kk.toArray(new String[kk.size()]));
                db.json2mysql_syncd2(table,func.json_encode(newData),new_key);
            }
        } else {
            return R.error(-1,"联合键错误");
        }

        return R.ok().put(ticket);
    }

    @RequestMapping(value = "/ddl",method = {RequestMethod.GET,RequestMethod.POST})
    public R ddl(HttpServletRequest request) {
        String ticket = func.randStr(16);
        Map<String, Object> data = RequestParamsToMap.getParameterMap(request);
        String sql = String.valueOf(data.get("sql"));
        String password = String.valueOf(data.get("password"));
        String r;
        if(StrUtils.areNotEmpty(password,sql)) {
            if(!password.equals(adminPassword())) {
                return R.error(-1,"密码错误");
            }
            try {
                r = String.valueOf(Db.update(sql));
            } catch (Exception e) {
                r = e.getMessage();
            }
        } else {
            return R.error(-1,"参数错误");
        }
        return R.ok().put(ticket).put("r",r);
    }

    @RequestMapping(value = "/query",method = {RequestMethod.GET,RequestMethod.POST})
    public R query(HttpServletRequest request) {
        String ticket = func.randStr(16);
        Map<String, Object> data = RequestParamsToMap.getParameterMap(request);
        String sql = String.valueOf(data.get("sql"));
        String password = String.valueOf(data.get("password"));
        Object r;
        if(StrUtils.areNotEmpty(password,sql)) {
            if(!password.equals(adminPassword())) {
                return R.error(-1,"密码错误");
            }
            try {
                r = db.fetch_all(sql);
            } catch (Exception e) {
                r = e.getMessage();
            }
        } else {
            return R.error(-1,"参数错误");
        }
        return R.ok().put(ticket).put("r",r);
    }



    @RequestMapping(value = "/struct",method = {RequestMethod.GET,RequestMethod.POST})
    public R sync_struct(HttpServletRequest request){
        String ticket = func.randStr(16);
        Map<String, Object> data = RequestParamsToMap.getParameterMap(request);
        String table = String.valueOf(data.get("table"));
        String table_to = String.valueOf(data.get("table_to"));
        String iscopy = String.valueOf(data.get("iscopy"));
        if(!StrUtils.areNotEmpty(table,table_to)) {
            return R.error(-1,"参数错误");
        }

        if(!db.schmema.containsKey(table)) {
            return R.error(-1,"来源表不存在");
        }

        db.setScheme();

        if(iscopy.equals("1")) {
            db.copyTable(table,table_to,true);
        } else {
            db.copyTable(table,table_to,false);
        }



        return R.ok().put(ticket).put(table,db.schmema.get(table)).put(table_to,db.schmema.get(table_to));
    }


}
