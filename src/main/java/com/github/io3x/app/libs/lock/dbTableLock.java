package com.github.io3x.app.libs.lock;

import com.github.io3x.app.libs.classes.db;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class dbTableLock {
    public static Logger logger = LoggerFactory.getLogger(dbTableLock.class);
    private static Map<String,ReentrantLock> locklist = new ConcurrentHashMap<>();
    private static Map<String,Condition> conditionlist = new ConcurrentHashMap<>();
    private static ExecutorService es = Executors.newSingleThreadExecutor();

    /**
     * Create lock. 创建资源的线程不能被回收
     *
     * @param a the a
     */
    /*创建资源*/
    private static void createLock(String a){
        if(!db.schmema.containsKey(a)) return;
        try {
            es.submit(()->{
                if(locklist.containsKey(a)&&conditionlist.containsKey(a)) {
                } else {
                    logger.info("TableLock创建锁 {}",a);
                    ReentrantLock lock = new ReentrantLock();
                    locklist.put(a,lock);
                    conditionlist.put(a,lock.newCondition());
                }
            }).get();
        }catch (InterruptedException e){
            e.printStackTrace();
        }catch (ExecutionException e){
            e.printStackTrace();
        } finally {
        }

    }
    /**
     * Clean lock *
     *
     * @param a a
     */
    public synchronized static void clean_lock(String a){
        try {
            if(locklist.containsKey(a)) {
                locklist.remove(a);
            }
            if(conditionlist.containsKey(a)) {
                conditionlist.remove(a);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Await * 如果对象被锁定,则等待
     *
     * @param a a
     * @throws InterruptedException interrupted exception
     */
    /*全局锁定*/
    public static void await(String a,Integer sec)  {
        if(!db.schmema.containsKey(a)) return;
        createLock(a);
        ReentrantLock lock = locklist.get(a);

        if(lock.isLocked()) {
            try {
                //logger.info("TableLock await 准备 {}",a);
                lock.lock();
                //logger.info("TableLock await 苏醒  {}",a);
            } finally {
                lock.unlock();
            }
        }

    }

    /**
     * Lock.
     * 锁定执行代码块 两次锁定解锁
     *
     * @param a     the a
     * @param block the block
     */
    public static void lock(Runnable block,String a) {
        if(!db.schmema.containsKey(a)) {
            new Thread(block).run();
            return;
        }
        createLock(a);
        if(!locklist.containsKey(a)||!conditionlist.containsKey(a)) {
            return;
        }
        ReentrantLock lock = locklist.get(a);
        Condition condition = conditionlist.get(a);
        //logger.info("TableLock 执行锁定  {}",a);
        lock.lock();
        try {
            new Thread(block).run();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }

        lock.lock();
        try {
            condition.signalAll();
            //func.println("解除锁定",lock.getHoldCount(),lock.getQueueLength(),lock.getWaitQueueLength(condition));
        } catch (Exception e){
            e.printStackTrace();
        } finally {
            lock.unlock();
            logger.info("TableLock 解除锁定  {}",a);
        }
    }
}
