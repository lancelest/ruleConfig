package com.risk.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.risk.dto.RuleConfigDTO;
import com.risk.entity.WarningRuleConfigEntity;
import com.risk.enums.RuleStatusEnum;
import com.risk.mapper.WarningRuleConfigMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
    public WarningRuleConfigEntity create(RuleConfigDTO dto) {
        WarningRuleConfigEntity config = new WarningRuleConfigEntity();

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
    public WarningRuleConfigEntity update(RuleConfigDTO dto) {
        // 1. 查询当前规则的最大版本号
        LambdaQueryWrapper<WarningRuleConfigEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WarningRuleConfigEntity::getRuleCode, dto.getRuleCode())
               .orderByDesc(WarningRuleConfigEntity::getVersion)
               .last("LIMIT 1");
        WarningRuleConfigEntity latest = ruleConfigMapper.selectOne(wrapper);

        // 2. 计算新版本号
        int newVersion = (latest != null) ? latest.getVersion() + 1 : 1;

        // 3. 插入新版本记录（旧版本保留不动）
        WarningRuleConfigEntity config = new WarningRuleConfigEntity();
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
    public void delete(String ruleCode) {
        LambdaUpdateWrapper<WarningRuleConfigEntity> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(WarningRuleConfigEntity::getRuleCode, ruleCode)
               .set(WarningRuleConfigEntity::getDeleted, 1);

        ruleConfigMapper.update(null, wrapper);
        log.info("伪删除规则成功，ruleCode={}，所有版本已标记删除", ruleCode);
    }

    // ==================== 生效/停用 ====================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void activate(Long id) {
        WarningRuleConfigEntity current = ruleConfigMapper.selectById(id);
        if (current == null) {
            throw new RuntimeException("规则不存在，id=" + id);
        }

        // 将同 ruleCode 下所有旧版本的 isCurrentVersion 置为 0，status 改为 ARCHIVED
        LambdaUpdateWrapper<WarningRuleConfigEntity> archiveWrapper = new LambdaUpdateWrapper<>();
        archiveWrapper.eq(WarningRuleConfigEntity::getRuleCode, current.getRuleCode())
                      .set(WarningRuleConfigEntity::getIsCurrentVersion, 0)
                      .set(WarningRuleConfigEntity::getStatus, RuleStatusEnum.ARCHIVED.getCode());
        ruleConfigMapper.update(null, archiveWrapper);

        // 将当前版本设为生效
        WarningRuleConfigEntity update = new WarningRuleConfigEntity();
        update.setId(id);
        update.setIsCurrentVersion(1);
        update.setStatus(RuleStatusEnum.ACTIVE.getCode());
        ruleConfigMapper.updateById(update);

        log.info("规则生效成功，ruleCode={}，version={}", current.getRuleCode(), current.getVersion());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deactivate(Long id) {
        WarningRuleConfigEntity current = ruleConfigMapper.selectById(id);
        if (current == null) {
            throw new RuntimeException("规则不存在，id=" + id);
        }

        WarningRuleConfigEntity update = new WarningRuleConfigEntity();
        update.setId(id);
        update.setIsCurrentVersion(0);
        update.setStatus(RuleStatusEnum.INACTIVE.getCode());
        ruleConfigMapper.updateById(update);

        log.info("规则停用成功，ruleCode={}，version={}", current.getRuleCode(), current.getVersion());
    }

    // ==================== 查询 ====================

    @Override
    public WarningRuleConfigEntity getById(Long id) {
        return ruleConfigMapper.selectById(id);
    }

    @Override
    public List<WarningRuleConfigEntity> listByPortfolioType(String portfolioTypeCode) {
        LambdaQueryWrapper<WarningRuleConfigEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WarningRuleConfigEntity::getPortfolioTypeCode, portfolioTypeCode)
               .eq(WarningRuleConfigEntity::getIsCurrentVersion, 1)
               .eq(WarningRuleConfigEntity::getStatus, RuleStatusEnum.ACTIVE.getCode())
               .orderByAsc(WarningRuleConfigEntity::getCreateTime);
        return ruleConfigMapper.selectList(wrapper);
    }

    @Override
    public List<WarningRuleConfigEntity> listByIndicator(String indicatorCode) {
        LambdaQueryWrapper<WarningRuleConfigEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WarningRuleConfigEntity::getIndicatorCode, indicatorCode)
               .eq(WarningRuleConfigEntity::getIsCurrentVersion, 1)
               .eq(WarningRuleConfigEntity::getStatus, RuleStatusEnum.ACTIVE.getCode())
               .orderByAsc(WarningRuleConfigEntity::getCreateTime);
        return ruleConfigMapper.selectList(wrapper);
    }

    @Override
    public List<WarningRuleConfigEntity> listVersions(String ruleCode) {
        LambdaQueryWrapper<WarningRuleConfigEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WarningRuleConfigEntity::getRuleCode, ruleCode)
               .orderByDesc(WarningRuleConfigEntity::getVersion);
        return ruleConfigMapper.selectList(wrapper);
    }

    @Override
    public List<WarningRuleConfigEntity> listVersionsIncludeDeleted(String ruleCode) {
        return ruleConfigMapper.selectVersionsIncludeDeleted(ruleCode);
    }

    // ==================== 私有方法 ====================

    /**
     * 构建规则条件JSON字符串
     *
     * 优先使用前端传入的 ruleConditions（已经序列化好的JSON字符串），
     * 如果前端传入的是 RuleConditions 对象，则序列化为JSON
     */
    private String buildRuleConditionsJson(RuleConfigDTO dto) {
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
