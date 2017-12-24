package com.mmall.task;

import com.mmall.common.Const;
import com.mmall.service.IOrderService;
import com.mmall.util.PropertiesUtil;
import com.mmall.util.RedisShardedPoolUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;

/**
 * @Author: Chou_meng
 * @Date: 2017/12/21
 */
@Component
@Slf4j
public class CloseOrderTask {

    @Autowired
    private IOrderService iOrderService;

    // tomcat平滑关闭的时候会在tomcat关闭之前执行@PreDestroy注解的方法，但是不适用于粗暴关闭tomcat比如找到tomcat进程来关闭tomcat
    @PreDestroy
    public void delLock() {
        RedisShardedPoolUtil.del(Const.RedisLock.CLOSE_ORDER_TASK_LOCK);
    }

//    @Scheduled(cron = "0 */1 * * * ?") // 每1分钟，每个1分钟的整数倍
    public void cloeOrderTaskV1() {
        log.info("关闭订单定时任务启动");
        int hour = Integer.parseInt(PropertiesUtil.getProperty("close.order.task.time.hour", "2"));
//        iOrderService.closeOrder(hour);
        log.info("关闭订单定时任务结束");
    }

//    @Scheduled(cron = "0 */1 * * * ?")
    public void closeOrderTaskV2() {
        log.info("关闭订单定时任务启动");
        long lockTimeout = Long.parseLong(PropertiesUtil.getProperty("lock.timeout", "5000"));
        Long setnxResult = RedisShardedPoolUtil.setnx(Const.RedisLock.CLOSE_ORDER_TASK_LOCK, String.valueOf(System.currentTimeMillis() + lockTimeout));
        if (setnxResult != null && setnxResult.intValue() == 1) {
            // 如果返回值是1，代表设置成功，获取锁
            closeOrder(Const.RedisLock.CLOSE_ORDER_TASK_LOCK);
        } else {
            log.info("没有获得分布式锁: {}", Const.RedisLock.CLOSE_ORDER_TASK_LOCK);
        }
        log.info("关闭订单定时任务结束");
    }

    @Scheduled(cron = "0 */1 * * * ?")
    public void closeOrderTaskV3() {
        log.info("关闭订单定时任务启动");
        long lockTimeout = Long.parseLong(PropertiesUtil.getProperty("lock.timeout", "5000"));
        Long setnxResult = RedisShardedPoolUtil.setnx(Const.RedisLock.CLOSE_ORDER_TASK_LOCK, String.valueOf(System.currentTimeMillis() + lockTimeout));
        if (setnxResult != null && setnxResult.intValue() == 1) {
            closeOrder(Const.RedisLock.CLOSE_ORDER_TASK_LOCK);
        } else {
            // 未获取到锁，继续判断，判断时间戳，看是否可以重置并获取到锁
            String lockValueStr = RedisShardedPoolUtil.get(Const.RedisLock.CLOSE_ORDER_TASK_LOCK);
            if (lockValueStr != null && System.currentTimeMillis() > Long.parseLong(lockValueStr)) {
                // 再次用当前时间戳getset
                // 返回给定的key的旧值，旧值判断，是否可以获取锁
                // 当key没有旧值时，即key不存在时，返回nil（null），获取锁
                // 这里我们set了一个新的value值，获取旧的值
                String getSetResult = RedisShardedPoolUtil.getSet(Const.RedisLock.CLOSE_ORDER_TASK_LOCK, String.valueOf(System.currentTimeMillis() + lockTimeout));
                if (getSetResult == null || (getSetResult != null && StringUtils.equals(lockValueStr, getSetResult))) {
                    // 真正获取到锁
                    closeOrder(Const.RedisLock.CLOSE_ORDER_TASK_LOCK);
                } else {
                    log.info("获取分布式锁失败: {}", Const.RedisLock.CLOSE_ORDER_TASK_LOCK);
                }
            } else {
                log.info("获取分布式锁失败: {}", Const.RedisLock.CLOSE_ORDER_TASK_LOCK);
            }
        }
        log.info("关闭订单定时任务结束");
    }

    private void closeOrder(String lockName) {
        RedisShardedPoolUtil.expire(lockName, 50); // 有效期50秒，防止死锁
        log.info("获取锁: {}, ThreadName: {}", Const.RedisLock.CLOSE_ORDER_TASK_LOCK, Thread.currentThread().getName());
        int hour = Integer.parseInt(PropertiesUtil.getProperty("close.order.task.time.hour", "2"));
//        iOrderService.closeOrder(hour);
        RedisShardedPoolUtil.del(lockName);
        log.info("释放锁: {}, ThreadName: {}", Const.RedisLock.CLOSE_ORDER_TASK_LOCK, Thread.currentThread().getName());
        log.info("=============================");
    }

}