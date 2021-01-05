package com.alibaba.csp.sentinel.extension.resource;

import org.springframework.beans.factory.InitializingBean;

/**
 * 我本非凡
 * Date:2020-12-23
 * Time:17:12:53
 * Description:SentinelResourceRegister.java
 *
 * @author wufan02
 * @since JDK 1.8
 * Enjoy a grander sight By climbing to a greater height
 */
public class SentinelResourceRegister implements InitializingBean {


    private String sentinelServer;

    private String projectName;

    public void setSentinelServer(String sentinelServer) {
        this.sentinelServer = sentinelServer;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        //获取所有的配置 并且设置参数数据
        System.setProperty("csp.sentinel.dashboard.server", sentinelServer);
        System.out.println("设置sentinel流控熔断参数  csp.sentinel.dashboard.server" + sentinelServer);
        System.setProperty("project.name", projectName);
        System.out.println("设置sentinel流控熔断参数  project.name" + projectName);
    }
}
