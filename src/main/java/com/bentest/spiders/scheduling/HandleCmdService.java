package com.bentest.spiders.scheduling;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.TriggerContext;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.scheduling.support.CronTrigger;

import com.bentest.spiders.config.SystemConfig;
 
/**
 * 上报模板到第三方运营商服务
 *
 */
@Configuration
@EnableScheduling
public class HandleCmdService implements SchedulingConfigurer {
 
    //private static Logger log = LoggerFactory.getLogger(PushTmplService.class);
    
    @Autowired
    private SystemConfig systemConfig;
    
    @Autowired
    private DealCmdTask dealCmdTask;
    
    
    @Override
    public void configureTasks(ScheduledTaskRegistrar scheduledTaskRegistrar) {
        scheduledTaskRegistrar.addTriggerTask(new Runnable() {
            @Override
            public void run() {
                // 任务逻辑
            	dealCmdTask.run();
            }
        }, new Trigger() {
            @Override
            public Date nextExecutionTime(TriggerContext triggerContext) {
                String s = systemConfig.getScanCmdCron();
                // 任务触发，可修改任务的执行周期
                CronTrigger trigger = new CronTrigger(s);
                Date nextExec = trigger.nextExecutionTime(triggerContext);
                return nextExec;
            }
        });
    }
}