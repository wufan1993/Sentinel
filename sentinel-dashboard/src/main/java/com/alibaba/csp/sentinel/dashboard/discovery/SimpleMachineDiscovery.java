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
package com.alibaba.csp.sentinel.dashboard.discovery;

import com.alibaba.csp.sentinel.dashboard.client.SentinelApiClient;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.*;
import com.alibaba.csp.sentinel.dashboard.domain.cluster.request.ClusterAppAssignMap;
import com.alibaba.csp.sentinel.dashboard.h2.model.ConfigConstants;
import com.alibaba.csp.sentinel.dashboard.h2.service.RuleConfigService;
import com.alibaba.csp.sentinel.dashboard.service.ClusterAssignService;
import com.alibaba.csp.sentinel.dashboard.util.JsonHelper;
import com.alibaba.csp.sentinel.util.AssertUtil;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author leyou
 */
@Component
public class SimpleMachineDiscovery implements MachineDiscovery {

    private static Logger logger = LoggerFactory.getLogger(SimpleMachineDiscovery.class);

    private final ConcurrentMap<String, AppInfo> apps = new ConcurrentHashMap<>();

    @Autowired
    private SentinelApiClient sentinelApiClient;

    @Autowired
    private ClusterAssignService clusterAssignService;

    @Resource
    private RuleConfigService ruleConfigService;


    @Override
    public long addMachine(MachineInfo machineInfo) {
        AssertUtil.notNull(machineInfo, "machineInfo cannot be null");
        AtomicBoolean flag=new AtomicBoolean(false);
        AppInfo appInfo = apps.computeIfAbsent(machineInfo.getApp(), o -> new AppInfo(machineInfo.getApp(), machineInfo.getAppType()));

        //判断是否需要进行发送
        Optional<MachineInfo> machine = appInfo.getMachine(machineInfo.getIp(), machineInfo.getPort());
        if (!machine.isPresent() || machineInfo.getMcVersion()==0) {
            flag.set(true);
        } else {
            machine.ifPresent(ms -> {
                if (machineInfo.isHealthy() && !ms.isHealthy()) {
                    flag.set(true);
                }
            });
        }

        appInfo.addMachine(machineInfo);

        //改成从数据库中获取数据
        if(flag.get()){
            logger.info("已完成新机器的添加"+JsonHelper.toJson(machineInfo));
            //需要获取zk中的配置 并更新到这台机器中
            String flowRuleValue = ruleConfigService.getRulesByKey(machineInfo.getApp() + "_" + ConfigConstants.flowRuleKey);
            String deGradeRuleValue =ruleConfigService.getRulesByKey(machineInfo.getApp() + "_" + ConfigConstants.degradeRuleKey);
            String configFlowRuleValue = ruleConfigService.getRulesByKey(machineInfo.getApp() + "_" + ConfigConstants.flowRuleConfigKey);
            String paramFlowRuleValue = ruleConfigService.getRulesByKey(machineInfo.getApp() + "_" + ConfigConstants.paramFlowRuleKey);
            String authorityFlowRuleValue = ruleConfigService.getRulesByKey(machineInfo.getApp() + "_" + ConfigConstants.authorityRuleKey);
            String systemFlowRuleValue = ruleConfigService.getRulesByKey(machineInfo.getApp() + "_" + ConfigConstants.systemRuleKey);

            try {
                //更新热点规则
                if (StringUtils.isNotBlank(paramFlowRuleValue)) {
                    List<ParamFlowRuleEntity> ruleList = JsonHelper.fromJsonArray(paramFlowRuleValue, ParamFlowRuleEntity.class);
                    sentinelApiClient.setParamFlowRuleOfMachine(machineInfo.getApp(), machineInfo.getIp(), machineInfo.getPort(), ruleList);
                    logger.info("完成热点规则刷新"+paramFlowRuleValue);
                }
                //更新授权规则
                if (StringUtils.isNotBlank(authorityFlowRuleValue)) {
                    List<AuthorityRuleEntity> ruleList = JsonHelper.fromJsonArray(authorityFlowRuleValue, AuthorityRuleEntity.class);
                    sentinelApiClient.setAuthorityRuleOfMachine(machineInfo.getApp(), machineInfo.getIp(), machineInfo.getPort(), ruleList);
                    logger.info("完成授权规则刷新"+authorityFlowRuleValue);
                }
                //更新系统规则
                if (StringUtils.isNotBlank(systemFlowRuleValue)) {
                    List<SystemRuleEntity> ruleList = JsonHelper.fromJsonArray(systemFlowRuleValue, SystemRuleEntity.class);
                    sentinelApiClient.setSystemRuleOfMachine(machineInfo.getApp(), machineInfo.getIp(), machineInfo.getPort(), ruleList);
                    logger.info("完成系统规则刷新"+systemFlowRuleValue);
                }
                //更新流控规则
                if (StringUtils.isNotBlank(flowRuleValue)) {
                    List<FlowRuleEntity> lowRuleList = JsonHelper.fromJsonArray(flowRuleValue, FlowRuleEntity.class);
                    sentinelApiClient.setFlowRuleOfMachine(machineInfo.getApp(), machineInfo.getIp(), machineInfo.getPort(), lowRuleList);
                    logger.info("完成流控规则刷新"+flowRuleValue);
                }

                //更新降级规则
                if (StringUtils.isNotBlank(deGradeRuleValue)) {
                    List<DegradeRuleEntity> deGradeRuleList = JsonHelper.fromJsonArray(deGradeRuleValue, DegradeRuleEntity.class);
                    sentinelApiClient.setDegradeRuleOfMachine(machineInfo.getApp(), machineInfo.getIp(), machineInfo.getPort(), deGradeRuleList);
                    logger.info("完成降级规则刷新"+deGradeRuleValue);
                }

                //更新集群规则身份
                if (StringUtils.isNotBlank(configFlowRuleValue)) {
                    List<ClusterAppAssignMap> clusterAppAssignMapList = JsonHelper.fromJsonArray(configFlowRuleValue, ClusterAppAssignMap.class);
                    //判断刚刚启动的机器的身份
                    String machineId=machineInfo.getIp()+"@"+machineInfo.getPort();
                    for(ClusterAppAssignMap appAssignMap:clusterAppAssignMapList){
                        if(appAssignMap.getMachineId().equals(machineId)){
                            //说明是服务端 取消所有客户端
                            appAssignMap.setClientSet(new HashSet<>());
                            break;
                        }
                        Optional.ofNullable(appAssignMap.getClientSet()).ifPresent(client->{
                            if(client.equals(machineId)){
                                //说明是客户端身份 取消服务端身份
                                appAssignMap.setBelongToApp(false);
                                Set<String> clientSingle=new HashSet<>();
                                clientSingle.add(machineId);
                                appAssignMap.setClientSet(clientSingle);
                            }
                        });
                    }
                    clusterAssignService.applyAssignToApp(machineInfo.getApp(),clusterAppAssignMapList,null);
                    logger.info("完成集群流控配置刷新"+configFlowRuleValue);
                }

            } catch (Exception e) {
                logger.error("更新机器规则失败",e);
                //Profiler.businessAlarm("sentinel_dashboard_exception", System.currentTimeMillis(), "更新机器规则失败"+JsonHelper.toJson(machineInfo));
            }
        }
        return 1;
    }

    @Override
    public boolean removeMachine(String app, String ip, int port) {
        AssertUtil.assertNotBlank(app, "app name cannot be blank");
        AppInfo appInfo = apps.get(app);
        if (appInfo != null) {
            return appInfo.removeMachine(ip, port);
        }
        return false;
    }

    @Override
    public List<String> getAppNames() {
        return new ArrayList<>(apps.keySet());
    }

    @Override
    public AppInfo getDetailApp(String app) {
        AssertUtil.assertNotBlank(app, "app name cannot be blank");
        return apps.get(app);
    }

    @Override
    public Set<AppInfo> getBriefApps() {
        return new HashSet<>(apps.values());
    }

    @Override
    public void removeApp(String app) {
        AssertUtil.assertNotBlank(app, "app name cannot be blank");
        apps.remove(app);
    }

}
