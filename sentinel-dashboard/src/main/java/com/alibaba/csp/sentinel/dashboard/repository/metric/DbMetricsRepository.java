/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.csp.sentinel.dashboard.repository.metric;

import com.alibaba.csp.sentinel.dashboard.datasource.entity.MetricEntity;
import com.alibaba.csp.sentinel.dashboard.h2.dao.MetricDetailMapper;
import com.alibaba.csp.sentinel.dashboard.h2.domain.MetricDetail;
import com.alibaba.csp.sentinel.dashboard.h2.domain.RuleConfig;
import com.alibaba.csp.sentinel.util.StringUtil;
import com.alibaba.csp.sentinel.util.TimeUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.time.Instant;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

/**
 * Caches metrics data in a period of time in memory.
 *
 * @author Carpenter Lee
 * @author Eric Zhao
 */
@Component("dbMetricsRepository")
public class DbMetricsRepository implements MetricsRepository<MetricEntity> {

    private final ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();

    @Resource
    private MetricDetailMapper metricDetailMapper;

    @Override
    public void save(MetricEntity entity) {
        if (entity == null || StringUtil.isBlank(entity.getApp())) {
            return;
        }
        readWriteLock.writeLock().lock();
        try {
            MetricDetail metricDetail=MetricDetail.transferMetricEntity(entity);
            metricDetailMapper.insert(metricDetail);
        } finally {
            readWriteLock.writeLock().unlock();
        }
    }

    @Override
    public void saveAll(Iterable<MetricEntity> metrics) {
        if (metrics == null) {
            return;
        }
        readWriteLock.writeLock().lock();
        try {
            metrics.forEach(this::save);
        } finally {
            readWriteLock.writeLock().unlock();
        }
    }

    @Override
    public List<MetricEntity> queryByAppAndResourceBetween(String app, String resource,
                                                           long startTime, long endTime) {
        List<MetricEntity> results = new ArrayList<>();
        if (StringUtil.isBlank(app)) {
            return results;
        }

        QueryWrapper<MetricDetail> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("app", app);
        queryWrapper.eq("resource", resource);
        queryWrapper.gt("timestamp", Date.from(Instant.ofEpochMilli(startTime)));
        queryWrapper.lt("timestamp", Date.from(Instant.ofEpochMilli(endTime)));
        List<MetricDetail> metricDetailList = metricDetailMapper.selectList(queryWrapper);

        readWriteLock.readLock().lock();
        try {
            return metricDetailList.stream().map(MetricDetail::transferMetricDetail).collect(Collectors.toList());
        } finally {
            readWriteLock.readLock().unlock();
        }
    }

    @Override
    public List<String> listResourcesOfApp(String app) {
        List<String> results = new ArrayList<>();
        if (StringUtil.isBlank(app)) {
            return results;
        }
        final long minTimeMs = System.currentTimeMillis() - 1000 * 60;

        readWriteLock.readLock().lock();
        try {
            QueryWrapper<MetricDetail> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("app", app);
            queryWrapper.gt("timestamp", Date.from(Instant.ofEpochMilli(minTimeMs)));
            List<MetricDetail> metricDetailList = metricDetailMapper.selectList(queryWrapper);

            List<MetricEntity > entityList=metricDetailList.stream().map(MetricDetail::transferMetricDetail).collect(Collectors.toList());

            // Order by last minute b_qps DESC.
            return entityList
                    .stream()
                    .sorted((e1, e2) -> {
                        int t = e2.getBlockQps().compareTo(e1.getBlockQps());
                        if (t != 0) {
                            return t;
                        }
                        return e2.getPassQps().compareTo(e1.getPassQps());
                    })
                    .map(MetricEntity::getResource)
                    .collect(Collectors.toList());
        } finally {
            readWriteLock.readLock().unlock();
        }
    }
}
