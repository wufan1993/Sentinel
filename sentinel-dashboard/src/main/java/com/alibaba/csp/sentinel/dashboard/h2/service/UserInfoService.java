package com.alibaba.csp.sentinel.dashboard.h2.service;

import com.alibaba.csp.sentinel.dashboard.h2.domain.UserInfo;

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
public interface UserInfoService {


    /**
     * 根据用户名称获取用户信息
     * @param username
     * @return
     */
    UserInfo findUser(String username);

    List<String> findUserApp();
}
