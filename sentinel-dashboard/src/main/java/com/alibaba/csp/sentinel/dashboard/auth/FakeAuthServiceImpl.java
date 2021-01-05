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
package com.alibaba.csp.sentinel.dashboard.auth;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.alibaba.csp.sentinel.dashboard.h2.domain.UserInfo;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Objects;

/**
 * A fake AuthService implementation, which will pass all user auth checking.
 *
 * @author Carpenter Lee
 * @since 1.5.0
 */
@Component
public class FakeAuthServiceImpl implements AuthService<HttpServletRequest> {


    @Override
    public AuthUser getAuthUser(HttpServletRequest request) {
        //注意 如果是多个机器搭建 请从缓存中去取
        if(request==null){
            request = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest();
        }
        HttpSession session = request.getSession();
        Object sentinelUserObj = session.getAttribute(SimpleWebAuthServiceImpl.WEB_SESSION_KEY);
        if (sentinelUserObj != null && sentinelUserObj instanceof AuthUser) {
            return (AuthUser) sentinelUserObj;
        }

        return null;
    }

    public static final class AuthUserImpl implements AuthUser {

        private UserInfo userInfo;

        public AuthUserImpl(UserInfo userInfo) {
            this.userInfo = userInfo;
        }

        @Override
        public boolean authTarget(String target, PrivilegeType privilegeType) {
            String privilegeTypes = userInfo.getPrivilegeType();
            if(StringUtils.isBlank(privilegeTypes) || privilegeTypes.contains(PrivilegeType.ALL.name())){
                return true;
            }
            if(privilegeTypes.contains(privilegeType.name())){
                return true;
            }
            return false;
        }

        @Override
        public boolean isSuperUser() {
            return true;
        }

        @Override
        public String getNickName() {
            return userInfo.getUsername();
        }

        @Override
        public String getLoginName() {
            return userInfo.getUsername();
        }

        @Override
        public String getId() {
            return userInfo.getUsername();
        }
    }

}
