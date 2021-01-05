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
package com.alibaba.csp.sentinel.extension.resource.http;

import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.transport.HeartbeatSender;
import com.alibaba.csp.sentinel.transport.config.TransportConfig;
import com.alibaba.csp.sentinel.transport.heartbeat.client.SimpleHttpClient;
import com.alibaba.csp.sentinel.transport.heartbeat.client.SimpleHttpRequest;
import com.alibaba.csp.sentinel.transport.heartbeat.client.SimpleHttpResponse;
import com.alibaba.csp.sentinel.util.function.Tuple2;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * 我本非凡
 * Date:2021-01-05
 * Time:10:01:53
 * Description:替换 HeartbeatMessage 为 BoostHeartbeatMessage 发送机器心跳
 *
 * @author wufan02
 * @since JDK 1.8
 * Enjoy a grander sight By climbing to a greater height
 */
public class BoostHttpHeartbeatSender implements HeartbeatSender {

    private static final int OK_STATUS = 200;

    private static final long DEFAULT_INTERVAL = 1000 * 10;

    private final BoostHeartbeatMessage heartBeat = new BoostHeartbeatMessage();
    private final SimpleHttpClient httpClient = new SimpleHttpClient();

    private final List<Tuple2<String, Integer>> addressList;

    private int currentAddressIdx = 0;

    public BoostHttpHeartbeatSender() {
        // Retrieve the list of default addresses.
        List<Tuple2<String, Integer>> newAddrs = TransportConfig.getConsoleServerList();
        if (newAddrs.isEmpty()) {
            RecordLog.warn("[BoostHttpHeartbeatSender] Dashboard server address not configured or not available");
        } else {
            RecordLog.info("[BoostHttpHeartbeatSender] Default console address list retrieved: " + newAddrs);
        }
        this.addressList = newAddrs;
    }

    @Override
    public boolean sendHeartbeat() throws Exception {
        if (TransportConfig.getRuntimePort() <= 0) {
            RecordLog.info("[BoostHttpHeartbeatSender] Command server port not initialized, won't send heartbeat");
            return false;
        }
        Tuple2<String, Integer> addrInfo = getAvailableAddress();
        if (addrInfo == null) {
            return false;
        }

        InetSocketAddress addr = new InetSocketAddress(addrInfo.r1, addrInfo.r2);
        SimpleHttpRequest request = new SimpleHttpRequest(addr, TransportConfig.getHeartbeatApiPath());
        request.setParams(heartBeat.generateCurrentMessage());
        try {
            SimpleHttpResponse response = httpClient.post(request);
            if (response.getStatusCode() == OK_STATUS) {
                return true;
            } else if (clientErrorCode(response.getStatusCode()) || serverErrorCode(response.getStatusCode())) {
                RecordLog.warn("[BoostHttpHeartbeatSender] Failed to send heartbeat to " + addr
                    + ", http status code: " + response.getStatusCode());
            }
        } catch (Exception e) {
            RecordLog.warn("[BoostHttpHeartbeatSender] Failed to send heartbeat to " + addr, e);
        }
        return false;
    }

    @Override
    public long intervalMs() {
        return DEFAULT_INTERVAL;
    }

    private Tuple2<String, Integer> getAvailableAddress() {
        if (addressList == null || addressList.isEmpty()) {
            return null;
        }
        if (currentAddressIdx < 0) {
            currentAddressIdx = 0;
        }
        int index = currentAddressIdx % addressList.size();
        return addressList.get(index);
    }

    private boolean clientErrorCode(int code) {
        return code > 399 && code < 500;
    }

    private boolean serverErrorCode(int code) {
        return code > 499 && code < 600;
    }
}
