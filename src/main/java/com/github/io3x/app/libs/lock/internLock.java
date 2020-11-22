package com.github.io3x.app.libs.lock;

import com.github.io3x.app.func;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
public class internLock {
    private static Logger logger = LoggerFactory.getLogger(internLock.class);

    public static void lockWait(Runnable r,String... strs){
        StringBuffer sb = new StringBuffer();
        for (String str:strs) {
            sb.append(str);
        }
        synchronized (sb.toString().intern()) {
            func.println(sb.toString());
            new Thread(r).run();
        }
    }
    private static Map<String,AtomicInteger> mapA = new ConcurrentHashMap<>();
    public static void lockLose(Runnable r,String... strs){
        StringBuffer sb = new StringBuffer();
        for (String str:strs) {
            sb.append(str);
        }
        String strKey = sb.toString();
        AtomicInteger ai;
        synchronized (strKey.intern()) {
            if(!mapA.containsKey(strKey)) {
                ai = new AtomicInteger(0);
                mapA.put(strKey,ai);
            } else {
                ai = mapA.get(strKey);
            }
        }
        //incrementAndGet 非线程安全
        ai.incrementAndGet();

        //get 线程安全
        if(ai.get()==1) {
            new Thread(r).run();
            if(mapA.containsKey(strKey)) {
                mapA.remove(strKey);
            }
        } else {
            logger.info("{} 已锁定,忽略该执行过程",strKey);
        }
    }

    public static void  main(String[] args)  {
        /*for (int i=0;i<10;i++) {
            String tmp = String.valueOf(i);
            new Thread(()->{
                lockWait(()->{
                    func.println(tmp);
                },func.CurrentLineInfo(),"a","b","11");
            }).start();
        }*/


        for (int i=0;i<20;i++) {
            String tmp = String.valueOf(i);
            new Thread(()->{
                lockLose(()->{
                    func.sleep(2);
                    func.println(tmp);
                },"a111","a","b","11");
            }).start();
        }

        func.sleep(1);

        for (int i=0;i<20;i++) {
            String tmp = String.valueOf(i);
            new Thread(()->{
                lockLose(()->{
                    func.sleep(2);
                    func.println(tmp);
                },"a111","a","b","11");
            }).start();
        }


    }
}
