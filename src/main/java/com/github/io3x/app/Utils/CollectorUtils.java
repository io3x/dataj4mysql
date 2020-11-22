package com.github.io3x.app.Utils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Collector utils
 */
public class CollectorUtils {
    /**
     * https://blog.csdn.net/a_limingfei/article/details/81559744
     * 将一组数据固定分组，每组n个元素
     *
     * @param <T>    parameter
     * @param source 要分组的数据源
     * @param n      每组n个元素
     * @return list list
     */
    public static <T> List<List<T>> fixedGrouping(List<T> source, int n) {
        if (null == source || source.size() == 0 || n <= 0)
            return null;
        List<List<T>> result = new ArrayList<List<T>>();
        int sourceSize = source.size();
        int size = (source.size() / n) + 1;
        for (int i = 0; i < size; i++) {
            List<T> subset = new ArrayList<T>();
            for (int j = i * n; j < (i + 1) * n; j++) {
                if (j < sourceSize) {
                    subset.add(source.get(j));
                }
            }
            result.add(subset);
        }
        return result;
    }

    /**
     * List map 2 map.
     *
     * @param listMap list map
     * @param from    from
     * @param to      to
     * @return the map
     */
    public static Map<String,String>  listMap2Map(List<Map> listMap,String from,String to){
        Map<String,String> r = new TreeMap<>();
        listMap.forEach(map->{
            r.put(String.valueOf(map.get(from)),String.valueOf(map.get(to)));
        });
        return r;
    }

    public static Map<String,Map>  listMap2keyListMap(List<Map> listMap,String from){
        Map<String,Map> r = new TreeMap<>();
        listMap.forEach(map->{
            r.put(String.valueOf(map.get(from)),map);
        });
        return r;
    }

    /**
     * List map addnewkey list listMap增加key
     *
     * @param data    data
     * @param keyName key name
     * @param Fields  fields
     * @return the list
     */
    public static List<Map> listMapAddnewkey(List<Map> data,String keyName,String... Fields){
        List<Map> newData = new LinkedList<>();
        if(!data.isEmpty()&&data.size()>0) {
            data.forEach(map->{
                Map<String,String>  tmp = new LinkedHashMap<>();
                tmp.putAll(map);
                List<String> ls = new LinkedList<>();
                for (String item : Fields) {
                    ls.add(String.valueOf(map.get(item)));
                }
                tmp.put(keyName,String.join("_",ls));
                newData.add(tmp);
            });
        }
        return newData;
    }

    /**
     * List mapre movekey list
     *
     * @param data     data
     * @param keyNames key names 移除多个listmap key
     * @return the list
     */
    public static List<Map> listMapreMovekey(List<Map> data,String... keyNames){
        List<Map> newData = new LinkedList<>();
        if(!data.isEmpty()&&data.size()>0) {
            data.forEach(map->{
                Map tmp = map;
                for (String keyName : keyNames) {
                    if(tmp.containsKey(keyName)) {
                        tmp.remove(keyName);
                    }
                }
                newData.add(tmp);
            });
        }
        return newData;
    }

    /**
     * List mapre namekey list 重命名listmap key
     *
     * @param data       data
     * @param keyName    key name
     * @param newkeyName newkey name
     * @return the list
     */
    public static List<Map> listMapreNamekey(List<Map> data,String keyName,String newkeyName){
        List<Map> newData = new LinkedList<>();
        if(!data.isEmpty()&&data.size()>0) {
            data.forEach(map->{
                Map tmp = map;
                if(tmp.containsKey(keyName)) {
                    tmp.remove(keyName);
                    tmp.put(newkeyName,map.get(keyName));
                }
                newData.add(tmp);
            });
        }
        return newData;
    }

    /**
     * 获取列数据
     * Array column list
     *
     * @param data data
     * @param a    a
     * @return the list
     */
    public static List<String> arrayColumn(List<Map> data,String a){
        return data.stream().map(map->String.valueOf(map.get(a))).collect(Collectors.toList());
    }
}
