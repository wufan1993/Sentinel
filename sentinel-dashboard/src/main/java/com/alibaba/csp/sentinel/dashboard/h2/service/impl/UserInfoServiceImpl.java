package com.alibaba.csp.sentinel.dashboard.h2.service.impl;

import com.alibaba.csp.sentinel.dashboard.auth.AuthService;
import com.alibaba.csp.sentinel.dashboard.h2.dao.UserAppMapper;
import com.alibaba.csp.sentinel.dashboard.h2.dao.UserInfoMapper;
import com.alibaba.csp.sentinel.dashboard.h2.domain.UserApp;
import com.alibaba.csp.sentinel.dashboard.h2.domain.UserInfo;
import com.alibaba.csp.sentinel.dashboard.h2.service.UserInfoService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;

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
@Service
public class UserInfoServiceImpl implements UserInfoService {

    private final Logger logger = LoggerFactory.getLogger(UserInfoServiceImpl.class);

    @Resource
    private UserInfoMapper userInfoMapper;

    @Resource
    private UserAppMapper userAppMapper;

    @Autowired
    private AuthService<HttpServletRequest> authService;

    @Override
    public UserInfo findUser(String username) {
        QueryWrapper<UserInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);
        List<UserInfo> userInfoList = userInfoMapper.selectList(queryWrapper);
        if (userInfoList != null && userInfoList.size() > 0) {
            return userInfoList.get(0);
        }
        return null;
    }

    @Override
    public List<String> findUserApp() {
        //获取session username
        AuthService.AuthUser authUser = authService.getAuthUser(null);
        if (authUser != null) {
            QueryWrapper<UserApp> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("username", authUser.getLoginName());
            List<UserApp> userInfoList = userAppMapper.selectList(queryWrapper);
            if (userInfoList != null) {
                return userInfoList.stream().map(UserApp::getAppName).collect(Collectors.toList());
            }
        }
        return null;
    }
}
