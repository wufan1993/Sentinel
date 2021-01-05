package com.alibaba.csp.sentinel.dashboard.task;

import com.alibaba.csp.sentinel.dashboard.datasource.entity.MetricEntity;
import com.alibaba.csp.sentinel.dashboard.discovery.AppInfo;
import com.alibaba.csp.sentinel.dashboard.discovery.AppManagement;
import com.alibaba.csp.sentinel.dashboard.domain.vo.MetricVo;
import com.alibaba.csp.sentinel.dashboard.repository.metric.MetricsRepository;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 我本非凡
 * Date:2020-12-24
 * Time:15:12:35
 * Description:MonitorTask.java
 *
 * @author wufan02
 * @since JDK 1.8
 * Enjoy a grander sight By climbing to a greater height
 */
@Component
public class MonitorTask {

    private static final Logger logger = LoggerFactory.getLogger(MonitorTask.class);

    @Autowired
    private AppManagement appManagement;

    @Autowired
    @Qualifier("inMemoryMetricsRepository")
    private MetricsRepository<MetricEntity> metricStore;

    /**
     * 每3分钟执行一次
     */
    @Scheduled(cron = "0 0/2 * * * ? ")
    public void process() {
        logger.debug("定时输出日志");

        //String app = "sentinel-dashboard";

        Set<AppInfo> briefApps = appManagement.getBriefApps();

        briefApps.forEach(appInfo -> {
            String app = appInfo.getApp();
            logger.debug("获取到系统编号" + app);

            List<String> resources = metricStore.listResourcesOfApp(app);
            logger.debug("queryTopResourceMetric(), resources.size()={}", resources.size());

            if (resources.isEmpty()) {
                logger.warn("当前无️资源");
                return;
            }

            long now = System.currentTimeMillis();
            long startTime = now - 120000;

            logger.debug("topResource={}", resources);

            Set<String> haveAlarm = new HashSet<>();

            for (final String resource : resources) {
                List<MetricEntity> entities = metricStore.queryByAppAndResourceBetween(app, resource, startTime, now);
                logger.debug("resource={}, entities.size()={}", resource, entities == null ? "null" : entities.size());
                List<MetricVo> vos = MetricVo.fromMetricEntities(entities, resource);
                vos.forEach(metricVo -> {
                    //logger.debug("获取详细资源信息=====》》》》》" + JsonHelper.toJson(metricVo));
                    //邮箱账号 //todo
                    String userListStr = ""/*config.getProperty(app, ConfigConstants.superEmail)*/;

                    String mailList = Arrays.stream(userListStr.split(",")).filter(StringUtils::isNotBlank).map(String::trim).distinct().collect(Collectors.joining(","));

                    String desc = null;
                    if (metricVo.getExceptionQps() > 0 && !haveAlarm.contains(metricVo.getResource())) {
                        desc = String.format("系统:%s  资源:%s 触发异常 数量%s", metricVo.getApp(), metricVo.getResource(), metricVo.getExceptionQps());
                    }
                    if (metricVo.getBlockQps() > 0 && !haveAlarm.contains(metricVo.getResource())) {
                        desc = String.format("系统:%s  资源:%s 触发流控降级 数量%s", metricVo.getApp(), metricVo.getResource(), metricVo.getBlockQps());
                    }
                    //报警短信功能发送不可用
                    if (StringUtils.isNotBlank(desc)) {
                        //Profiler.businessAlarm(app + "_sentinel", System.currentTimeMillis(), desc, "", mailList, "");
                        haveAlarm.add(metricVo.getResource());
                        logger.info("报警key {} 描述{} 发送邮箱人员{} ", app + "_sentinel", desc, mailList);
                    }

                });
            }
            logger.debug("queryTopResourceMetric() total query app({}) time={} ms", app, System.currentTimeMillis() - now);


        });


    }

}