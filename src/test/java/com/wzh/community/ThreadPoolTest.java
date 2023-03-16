package com.wzh.community;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.util.Date;

import static java.lang.Thread.sleep;

/**
 * @author wzh
 * @data 2022/9/24 -13:17
 */
@SpringBootTest
public class ThreadPoolTest {
    // 因为已经配置好了，会自动创建
    // spring普通线程池
    @Autowired
    private ThreadPoolTaskExecutor taskExecutor;

    // spring定时任务线程池
    @Autowired
    private ThreadPoolTaskScheduler taskScheduler;

    @Test
    public void testThreadPoolTaskExecutor() throws InterruptedException {
       Runnable task =  new Runnable() {
           @Override
           public void run() {
               System.out.println(Thread.currentThread().getName() + "hello");
           }

        };
        for (int i = 0; i < 10; i++) {
            taskExecutor.submit(task);
        }

        sleep(1000);


    }


    @Test
    public void testThreadPoolTaskScheduler() throws InterruptedException {
        Runnable task = new Runnable() {

            @Override
            public void run() {
                System.out.println(Thread.currentThread().getName() + "hello");
            }
        };

        Date start = new Date(System.currentTimeMillis() + 10000);
        taskScheduler.scheduleAtFixedRate(task, start, 1000);
        sleep(30000);
    }


}
