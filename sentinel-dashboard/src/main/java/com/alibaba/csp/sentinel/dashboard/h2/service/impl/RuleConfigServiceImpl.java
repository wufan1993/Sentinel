package com.alibaba.csp.sentinel.dashboard.h2.service.impl;

import com.alibaba.csp.sentinel.dashboard.auth.AuthService;
import com.alibaba.csp.sentinel.dashboard.h2.dao.RuleConfigMapper;
import com.alibaba.csp.sentinel.dashboard.h2.domain.RuleConfig;
import com.alibaba.csp.sentinel.dashboard.h2.service.RuleConfigService;
import com.alibaba.csp.sentinel.dashboard.util.JsonHelper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
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
@Service
public class RuleConfigServiceImpl implements RuleConfigService {

    private final Logger logger = LoggerFactory.getLogger(RuleConfigServiceImpl.class);

    @Resource
    private RuleConfigMapper ruleConfigMapper;

    @Autowired
    private AuthService<HttpServletRequest> authService;

    @Override
    public void publishRules(String ruleKey, String app, List<?> rules) {

        //获取session username
        AuthService.AuthUser authUser = authService.getAuthUser(null);

        RuleConfig entity = new RuleConfig();
        entity.setDictCode(app + "_" + ruleKey);
        entity.setDictExt("dashboard-control");
        entity.setDictValue(JsonHelper.toJson(rules));
        entity.setPin(authUser.getLoginName());

        //先查询 后更新
        QueryWrapper<RuleConfig> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("dict_code", entity.getDictCode());
        List<RuleConfig> ruleConfigList = ruleConfigMapper.selectList(queryWrapper);
        if (ruleConfigList != null && ruleConfigList.size() == 1) {
            //走更新策略
            RuleConfig oldConfig = ruleConfigList.get(0);
            logger.info("操作人{} 变更前 变更后{}", entity.getPin(), oldConfig.getDictValue(), entity.getDictValue());
            entity.setId(oldConfig.getId());
            ruleConfigMapper.updateById(entity);
        } else {
            //走新增策略
            ruleConfigMapper.insert(entity);
        }
    }

    @Override
    public String getRulesByKey(String ruleKey) {
        if(StringUtils.isNotBlank(ruleKey)){
            QueryWrapper<RuleConfig> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("dict_code", ruleKey);
            List<RuleConfig> ruleConfigList = ruleConfigMapper.selectList(queryWrapper);
            if (ruleConfigList != null && ruleConfigList.size() == 1) {
                return ruleConfigList.get(0).getDictValue();
            }
        }
        return "";
    }
}
