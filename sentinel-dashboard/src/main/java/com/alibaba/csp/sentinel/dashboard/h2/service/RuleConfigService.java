package com.alibaba.csp.sentinel.dashboard.h2.service;

import java.util.List;

/**
 * 我本非凡
 * Date:2020-12-09
 * Time:14:12:09
 * Description:AgentCommandServerService.java
 *
 * @author wufan02
 * @since JDK 1.8
 * Enjoy a grander sight By climbing to a greater height
 */
public interface RuleConfigService {


    void publishRules(String flowRuleKey, String app, List<?> rules);

    String getRulesByKey(String s);
}
