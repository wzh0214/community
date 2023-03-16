package com.wzh.community.config;

import com.wzh.community.quartz.PostScoreRefreshJob;
import org.springframework.context.annotation.Configuration;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;
import org.springframework.scheduling.quartz.SimpleTriggerFactoryBean;


/**
 * @author wzh
 * @data 2022/9/24 -14:41
 */
// 配置->数据库->调用 只在第一次调用，以后访问直接找数据库
@Configuration
public class QuartzConfig {

    //FactoryBean 简化Bean实例化过程
    // 通过FactoryBean封装bean的实例化过程；-> 将FactoryBean装配到Spring容器里
    // ->将FactoryBean注入给其他的bean ->该bean得到的是FactoryBean所管理的对象实例

    //刷新帖子分数任务
    @Bean
    public JobDetailFactoryBean postScoreRefreshJobDetail() {
        JobDetailFactoryBean factoryBean = new JobDetailFactoryBean();
        factoryBean.setJobClass(PostScoreRefreshJob.class);
        factoryBean.setName("postScoreRefreshJob");
        factoryBean.setGroup("communityJobGroup");
        factoryBean.setDurability(true);
        factoryBean.setRequestsRecovery(true);
        return factoryBean;
    }

    @Bean
    public SimpleTriggerFactoryBean postScoreRefreshTrigger(JobDetail postScoreRefreshJobDetail) {
        SimpleTriggerFactoryBean factoryBean = new SimpleTriggerFactoryBean();
        factoryBean.setJobDetail(postScoreRefreshJobDetail);
        factoryBean.setName("postScoreRefreshTrigger");
        factoryBean.setGroup("communityTriggerGroup");
        factoryBean.setRepeatInterval(1000 * 60 * 5); // 五分钟刷新一遍
        factoryBean.setJobDataMap(new JobDataMap());
        return factoryBean;
    }
}
