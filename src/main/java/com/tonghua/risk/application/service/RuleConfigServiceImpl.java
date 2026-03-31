package com.tonghua.risk.application.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tonghua.risk.domain.entity.WarningRuleConfig;
import com.tonghua.risk.domain.enums.RuleStatusEnum;
import com.tonghua.risk.domain.vo.RuleConditions;
import com.tonghua.risk.infrastructure.mapper.WarningRuleConfigMapper;
import com.tonghua.risk.interface.dto.RuleConfigCreateDTO;
import com.tonghua.risk.interface.dto.RuleConfigUpdateDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 预警规则配置 Service 实现
 */
@Slf4j
@Service
public class RuleConfigServiceImpl implements RuleConfigService {

    @Autowired
    private WarningRuleConfigMapper ruleConfigMapper;

    @Autowired
    private ObjectMapper objectMapper;

    // ==================== 新增 ====================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WarningRuleConfig create(RuleConfigCreateDTO dto) {
        WarningRuleConfig config = new WarningRuleConfig();

        // 基础信息
        config.setRuleCode(dto.getRuleCode());
        config.setRuleName(dto.getRuleName());

        // 维度信息
        config.setPortfolioTypeCode(dto.getPortfolioTypeCode());
        config.setPortfolioTypeName(dto.getPortfolioTypeName());
        config.setIndicatorCode(dto.getIndicatorCode());
        config.setIndicatorName(dto.getIndicatorName());
        config.setSubCategory(dto.getSubCategory());

        // 规则条件（校验并序列化为JSON）
        config.setRuleConditions(buildRuleConditionsJson(dto));

        // 4级阈值
        config.setThresholdL1(dto.getThresholdL1());
        config.setThresholdL2(dto.getThresholdL2());
        config.setThresholdL3(dto.getThresholdL3());
        config.setThresholdL4(dto.getThresholdL4());

        // 版本初始化
        config.setVersion(1);
        config.setStatus(RuleStatusEnum.DRAFT.getCode());
        config.setIsCurrentVersion(0);

        // 创建人
        config.setCreateBy(dto.getOperator());

        ruleConfigMapper.insert(config);
        log.info("新增规则成功，ruleCode={}，version=1", dto.getRuleCode());
        return config;
    }

    // ==================== 更新（新版本） ====================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WarningRuleConfig update(RuleConfigUpdateDTO dto) {
        // 1. 查询当前规则的最大版本号
        LambdaQueryWrapper<WarningRuleConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WarningRuleConfig::getRuleCode, dto.getRuleCode())
               .orderByDesc(WarningRuleConfig::getVersion)
               .last("LIMIT 1");
        WarningRuleConfig latest = ruleConfigMapper.selectOne(wrapper);

        // 2. 计算新版本号
        int newVersion = (latest != null) ? latest.getVersion() + 1 : 1;

        // 3. 插入新版本记录（旧版本保留不动）
        WarningRuleConfig config = new WarningRuleConfig();
        config.setRuleCode(dto.getRuleCode());
        config.setRuleName(dto.getRuleName());

        config.setPortfolioTypeCode(dto.getPortfolioTypeCode());
        config.setPortfolioTypeName(dto.getPortfolioTypeName());
        config.setIndicatorCode(dto.getIndicatorCode());
        config.setIndicatorName(dto.getIndicatorName());
        config.setSubCategory(dto.getSubCategory());

        config.setRuleConditions(buildRuleConditionsJson(dto));

        config.setThresholdL1(dto.getThresholdL1());
        config.setThresholdL2(dto.getThresholdL2());
        config.setThresholdL3(dto.getThresholdL3());
        config.setThresholdL4(dto.getThresholdL4());

        config.setVersion(newVersion);
        config.setStatus(RuleStatusEnum.DRAFT.getCode());
        config.setIsCurrentVersion(0);

        config.setCreateBy(dto.getOperator());

        ruleConfigMapper.insert(config);
        log.info("更新规则成功，ruleCode={}，新版本={}", dto.getRuleCode(), newVersion);
        return config;
    }

    // ==================== 删除（伪删除） ====================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteByRuleCode(String ruleCode) {
        // 将该 ruleCode 下所有版本的 deleted 标记为 1
        LambdaUpdateWrapper<WarningRuleConfig> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(WarningRuleConfig::getRuleCode, ruleCode)
               .set(WarningRuleConfig::getDeleted, 1);

        ruleConfigMapper.update(null, wrapper);
        log.info("伪删除规则成功，ruleCode={}，所有版本已标记删除", ruleCode);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteById(Long id) {
        // MyBatis-Plus @TableLogic 自动将此操作转为 UPDATE SET deleted=1
        ruleConfigMapper.deleteById(id);
        log.info("伪删除规则成功，id={}", id);
    }

    // ==================== 生效/停用 ====================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void activate(Long id) {
        // 1. 查询当前规则
        WarningRuleConfig current = ruleConfigMapper.selectById(id);
        if (current == null) {
            throw new RuntimeException("规则不存在，id=" + id);
        }

        // 2. 将同 ruleCode 下所有旧版本的 isCurrentVersion 置为 0，status 改为 ARCHIVED
        LambdaUpdateWrapper<WarningRuleConfig> archiveWrapper = new LambdaUpdateWrapper<>();
        archiveWrapper.eq(WarningRuleConfig::getRuleCode, current.getRuleCode())
                      .set(WarningRuleConfig::getIsCurrentVersion, 0)
                      .set(WarningRuleConfig::getStatus, RuleStatusEnum.ARCHIVED.getCode());
        ruleConfigMapper.update(null, archiveWrapper);

        // 3. 将当前版本设为生效
        WarningRuleConfig update = new WarningRuleConfig();
        update.setId(id);
        update.setIsCurrentVersion(1);
        update.setStatus(RuleStatusEnum.ACTIVE.getCode());
        ruleConfigMapper.updateById(update);

        log.info("规则生效成功，ruleCode={}，version={}", current.getRuleCode(), current.getVersion());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deactivate(Long id) {
        WarningRuleConfig current = ruleConfigMapper.selectById(id);
        if (current == null) {
            throw new RuntimeException("规则不存在，id=" + id);
        }

        WarningRuleConfig update = new WarningRuleConfig();
        update.setId(id);
        update.setIsCurrentVersion(0);
        update.setStatus(RuleStatusEnum.INACTIVE.getCode());
        ruleConfigMapper.updateById(update);

        log.info("规则停用成功，ruleCode={}，version={}", current.getRuleCode(), current.getVersion());
    }

    // ==================== 查询 ====================

    @Override
    public WarningRuleConfig getById(Long id) {
        return ruleConfigMapper.selectById(id);
    }

    @Override
    public List<WarningRuleConfig> listByPortfolioType(String portfolioTypeCode) {
        LambdaQueryWrapper<WarningRuleConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WarningRuleConfig::getPortfolioTypeCode, portfolioTypeCode)
               .eq(WarningRuleConfig::getIsCurrentVersion, 1)
               .eq(WarningRuleConfig::getStatus, RuleStatusEnum.ACTIVE.getCode())
               .orderByAsc(WarningRuleConfig::getCreateTime);
        return ruleConfigMapper.selectList(wrapper);
    }

    @Override
    public List<WarningRuleConfig> listByIndicator(String indicatorCode) {
        LambdaQueryWrapper<WarningRuleConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WarningRuleConfig::getIndicatorCode, indicatorCode)
               .eq(WarningRuleConfig::getIsCurrentVersion, 1)
               .eq(WarningRuleConfig::getStatus, RuleStatusEnum.ACTIVE.getCode())
               .orderByAsc(WarningRuleConfig::getCreateTime);
        return ruleConfigMapper.selectList(wrapper);
    }

    @Override
    public List<WarningRuleConfig> listVersions(String ruleCode) {
        LambdaQueryWrapper<WarningRuleConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WarningRuleConfig::getRuleCode, ruleCode)
               .orderByDesc(WarningRuleConfig::getVersion);
        return ruleConfigMapper.selectList(wrapper);
    }

    // ==================== 私有方法 ====================

    /**
     * 构建规则条件JSON字符串
     *
     * 优先使用前端传入的 ruleConditions（已经序列化好的JSON字符串），
     * 如果前端传入的是 RuleConditions 对象，则序列化为JSON
     */
    private String buildRuleConditionsJson(RuleConfigCreateDTO dto) {
        // 如果前端直接传了JSON字符串，直接用
        if (StringUtils.hasText(dto.getRuleConditions())) {
            return dto.getRuleConditions();
        }
        // 如果前端传了 RuleConditions 对象，序列化为JSON
        if (dto.getRuleConditionsObj() != null) {
            try {
                return objectMapper.writeValueAsString(dto.getRuleConditionsObj());
            } catch (JsonProcessingException e) {
                throw new RuntimeException("规则条件序列化失败", e);
            }
        }
        return null;
    }

    /**
     * 构建规则条件JSON字符串（Update版本）
     */
    private String buildRuleConditionsJson(RuleConfigUpdateDTO dto) {
        if (StringUtils.hasText(dto.getRuleConditions())) {
            return dto.getRuleConditions();
        }
        if (dto.getRuleConditionsObj() != null) {
            try {
                return objectMapper.writeValueAsString(dto.getRuleConditionsObj());
            } catch (JsonProcessingException e) {
                throw new RuntimeException("规则条件序列化失败", e);
            }
        }
        return null;
    }
}
