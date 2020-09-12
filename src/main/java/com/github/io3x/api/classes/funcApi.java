package com.github.io3x.api.classes;

import com.github.io3x.app.func;
import com.github.io3x.app.libs.classes.anyTree;

import java.util.*;

public class funcApi {

    private static Map ops = new HashMap<>();
    /**
     * Docs map
     *
     * @param op op
     * @return the map
     */
    public static Map Docs(String op){
        if(ops.containsKey(op)) return (Map)ops.get(op);
        anyTree at = new anyTree();
        Map<String,Map<String,String>> a= func.yaml("doc");
        for (Map.Entry<String, Map<String,String>> entry : a.entrySet()) {
            anyTree at1 = new anyTree();
            String k1 = entry.getKey();
            Map<String,String> v1 = entry.getValue();
            //System.out.println(String.format("1 KEY %s VALUE %s \n",k1,func.json_encode(v1)));
            for (Map.Entry<String,String> entry2 : v1.entrySet()) {
                String k2 = entry2.getKey();
                String v2 = entry2.getValue().trim();
                //System.out.println(String.format("2 KEY %s VALUE %s \n",k2, netjoy.app.func.json_encode(v2)));

                if(func.in_array(k2,new String[]{"more","title","url"})) {
                    at1.setmap(k2,v2.trim());
                } else {
                    List<String> x = Arrays.asList(v2.split("\n"));
                    //System.out.println(String.format("3 %s \n",func.json_encode(x)));
                    anyTree at2 = new anyTree();
                    for (String xx : x) {
                        List<String> xxx = Arrays.asList(xx.split("\\|"));
                        List<String> tmp = new ArrayList<>();
                        tmp.addAll(xxx);
                        while (tmp.size()<=3) {
                            tmp.add("None");
                        }
                        //System.out.println(tmp.size());
                        //System.out.println(tmp);
                        at2.seto(tmp);
                    }
                    at1.setmap(k2,at2.olTree);
                }


            }
            at.setmap(k1,at1.mtree);
        }
        //System.out.println(at.mtree);
        //System.out.println(at.mtree.get(op));
        if(at.mtree.containsKey(op)) {
            ops.put(op,(Map)at.mtree.get(op));
        } else {
            ops.put(op,new HashMap<>());
        }
        return (Map)ops.get(op);
    }
}
