package com.xxy.blog.config;

import com.xxy.blog.quartz.RefreshBlogScoreJob;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;
import org.springframework.scheduling.quartz.SimpleTriggerFactoryBean;

@Configuration
public class QuartzConfig {

    @Bean
    public JobDetailFactoryBean blogScoreRefreshJobDetail(){
        JobDetailFactoryBean factoryBean = new JobDetailFactoryBean();
        factoryBean.setJobClass(RefreshBlogScoreJob.class);
        factoryBean.setName("blogScoreRefreshJob");
        factoryBean.setGroup("blogJobGroup");
        factoryBean.setDurability(true);
        factoryBean.setRequestsRecovery(true);
        return factoryBean;
    }

    @Bean
    public SimpleTriggerFactoryBean postScoreRefreshTrigger(JobDetail blogScoreRefreshJobDetail){
        SimpleTriggerFactoryBean factoryBean = new SimpleTriggerFactoryBean();
        factoryBean.setJobDetail(blogScoreRefreshJobDetail);
        factoryBean.setName("blogScoreRefreshTrigger");
        factoryBean.setGroup("blogTriggerGroup");
        /** 五分钟执行一次 */
        factoryBean.setRepeatInterval(1000 * 60 * 5);
        factoryBean.setJobDataMap(new JobDataMap());
        return factoryBean;
    }

}
