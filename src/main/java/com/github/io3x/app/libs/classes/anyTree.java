package com.github.io3x.app.libs.classes;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class anyTree {
    public Map<String,Object> mtree=new HashMap<>();
    public anyTree setmap(String a,Object b){
        this.mtree.put(a,b);
        return this;
    }


    public List<String> lTree=new ArrayList<>();
    public anyTree setstr(String a){
        this.lTree.add(a);
        return this;
    }

    public List<Integer> iTree=new ArrayList<>();
    public anyTree setint(int a){
        this.iTree.add(a);
        return this;
    }

    public List<Object> olTree=new ArrayList<>();
    public anyTree seto(Object a){
        this.olTree.add(a);
        return this;
    }

}
