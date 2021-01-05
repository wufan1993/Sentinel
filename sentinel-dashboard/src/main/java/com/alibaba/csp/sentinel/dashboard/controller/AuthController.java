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
package com.alibaba.csp.sentinel.dashboard.controller;

import com.alibaba.csp.sentinel.dashboard.auth.AuthService;
import com.alibaba.csp.sentinel.dashboard.auth.FakeAuthServiceImpl;
import com.alibaba.csp.sentinel.dashboard.auth.SimpleWebAuthServiceImpl;
import com.alibaba.csp.sentinel.dashboard.config.DashboardConfig;
import com.alibaba.csp.sentinel.dashboard.domain.Result;
import com.alibaba.csp.sentinel.dashboard.h2.domain.UserInfo;
import com.alibaba.csp.sentinel.dashboard.h2.service.UserInfoService;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * @author cdfive
 * @since 1.6.0
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthController.class);

    @Value("${auth.username:sentinel}")
    private String authUsername;

    @Value("${auth.password:sentinel}")
    private String authPassword;

    @Resource
    private UserInfoService userInfoService;

    @Autowired
    private AuthService<HttpServletRequest> authService;

    @PostMapping("/login")
    public Result<AuthService.AuthUser> login(HttpServletRequest request, String username, String password) {
        if (StringUtils.isNotBlank(DashboardConfig.getAuthUsername())) {
            authUsername = DashboardConfig.getAuthUsername();
        }

        if (StringUtils.isNotBlank(DashboardConfig.getAuthPassword())) {
            authPassword = DashboardConfig.getAuthPassword();
        }

        /*
         * If auth.username or auth.password is blank(set in application.properties or VM arguments),
         * auth will pass, as the front side validate the input which can't be blank,
         * so user can input any username or password(both are not blank) to login in that case.
         */
        UserInfo userInfo = null;
        if (StringUtils.isNotBlank(authUsername) && !authUsername.equals(username)
                || StringUtils.isNotBlank(authPassword) && !authPassword.equals(password)) {
            userInfo = userInfoService.findUser(username);
            if (userInfo == null || !userInfo.getPassword().equals(password)) {
                LOGGER.error("Login failed: Invalid username or password, username=" + username);
                return Result.ofFail(-1, "Invalid username or password");
            }
        }
        if(authUsername.equals(username)){
            userInfo=new UserInfo();
            userInfo.setUsername(username);
            userInfo.setPassword(password);
            userInfo.setPrivilegeType("ALL");
        }
        //查询用户信息
        AuthService.AuthUser authUser = new FakeAuthServiceImpl.AuthUserImpl(userInfo);

        request.getSession().setAttribute(SimpleWebAuthServiceImpl.WEB_SESSION_KEY, authUser);
        return Result.ofSuccess(authUser);
    }

    @PostMapping(value = "/logout")
    public Result<?> logout(HttpServletRequest request) {
        request.getSession().invalidate();
        return Result.ofSuccess(null);
    }

    @PostMapping(value = "/check")
    public Result<?> check(HttpServletRequest request) {
        AuthService.AuthUser authUser = authService.getAuthUser(request);
        if (authUser == null) {
            return Result.ofFail(-1, "Not logged in");
        }
        return Result.ofSuccess(authUser);
    }
}
