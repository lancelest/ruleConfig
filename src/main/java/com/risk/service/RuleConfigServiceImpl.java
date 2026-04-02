package com.risk.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.risk.dto.RuleConfigParams;
import com.risk.entity.RuleConfigEntity;
import com.risk.enums.RuleStatusEnum;
import com.risk.exception.BusinessException;
import com.risk.mapper.RuleConfigMapper;
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
    private RuleConfigMapper ruleConfigMapper;

    @Autowired
    private ObjectMapper objectMapper;

    // ==================== 新增 ====================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RuleConfigEntity create(RuleConfigParams dto) {
        RuleConfigEntity config = new RuleConfigEntity();

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
    public RuleConfigEntity update(RuleConfigParams dto) {
        // 1. 查询当前规则的最大版本号
        LambdaQueryWrapper<RuleConfigEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RuleConfigEntity::getRuleCode, dto.getRuleCode())
               .orderByDesc(RuleConfigEntity::getVersion)
               .last("LIMIT 1");
        RuleConfigEntity latest = ruleConfigMapper.selectOne(wrapper);

        if (latest == null) {
            throw new BusinessException("规则不存在，ruleCode=" + dto.getRuleCode());
        }

        // 2. 计算新版本号
        int newVersion = latest.getVersion() + 1;

        // 3. 插入新版本记录（旧版本保留不动）
        RuleConfigEntity config = new RuleConfigEntity();
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
        // 先查询该规则是否存在
        LambdaQueryWrapper<RuleConfigEntity> checkWrapper = new LambdaQueryWrapper<>();
        checkWrapper.eq(RuleConfigEntity::getRuleCode, ruleCode);
        Long count = ruleConfigMapper.selectCount(checkWrapper);
        if (count == null || count == 0) {
            throw new BusinessException("规则不存在，ruleCode=" + ruleCode);
        }

        LambdaUpdateWrapper<RuleConfigEntity> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(RuleConfigEntity::getRuleCode, ruleCode)
               .set(RuleConfigEntity::getDeleted, 1);

        ruleConfigMapper.update(null, wrapper);
        log.info("伪删除规则成功，ruleCode={}，所有版本已标记删除", ruleCode);
    }

    // ==================== 生效/停用 ====================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void activate(Long id) {
        RuleConfigEntity current = ruleConfigMapper.selectById(id);
        if (current == null) {
            throw new BusinessException("规则不存在，id=" + id);
        }
        if (RuleStatusEnum.ACTIVE.getCode().equals(current.getStatus())) {
            throw new BusinessException("规则已是生效状态，无需重复操作");
        }

        // 将同 ruleCode 下所有旧版本的 isCurrentVersion 置为 0，status 改为 ARCHIVED
        LambdaUpdateWrapper<RuleConfigEntity> archiveWrapper = new LambdaUpdateWrapper<>();
        archiveWrapper.eq(RuleConfigEntity::getRuleCode, current.getRuleCode())
                      .set(RuleConfigEntity::getIsCurrentVersion, 0)
                      .set(RuleConfigEntity::getStatus, RuleStatusEnum.ARCHIVED.getCode());
        ruleConfigMapper.update(null, archiveWrapper);

        // 将当前版本设为生效
        RuleConfigEntity update = new RuleConfigEntity();
        update.setId(id);
        update.setIsCurrentVersion(1);
        update.setStatus(RuleStatusEnum.ACTIVE.getCode());
        ruleConfigMapper.updateById(update);

        log.info("规则生效成功，ruleCode={}，version={}", current.getRuleCode(), current.getVersion());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deactivate(Long id) {
        RuleConfigEntity current = ruleConfigMapper.selectById(id);
        if (current == null) {
            throw new BusinessException("规则不存在，id=" + id);
        }
        if (RuleStatusEnum.INACTIVE.getCode().equals(current.getStatus())) {
            throw new BusinessException("规则已是停用状态，无需重复操作");
        }

        RuleConfigEntity update = new RuleConfigEntity();
        update.setId(id);
        update.setIsCurrentVersion(0);
        update.setStatus(RuleStatusEnum.INACTIVE.getCode());
        ruleConfigMapper.updateById(update);

        log.info("规则停用成功，ruleCode={}，version={}", current.getRuleCode(), current.getVersion());
    }

    // ==================== 查询 ====================

    @Override
    public RuleConfigEntity getById(Long id) {
        return ruleConfigMapper.selectById(id);
    }

    @Override
    public List<RuleConfigEntity> listByPortType(String portTypeCode) {
        LambdaQueryWrapper<RuleConfigEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RuleConfigEntity::getPortfolioTypeCode, portTypeCode)
               .eq(RuleConfigEntity::getIsCurrentVersion, 1)
               .eq(RuleConfigEntity::getStatus, RuleStatusEnum.ACTIVE.getCode())
               .orderByAsc(RuleConfigEntity::getCreateTime);
        return ruleConfigMapper.selectList(wrapper);
    }

    @Override
    public List<RuleConfigEntity> listByMetric(String metricCode) {
        LambdaQueryWrapper<RuleConfigEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RuleConfigEntity::getIndicatorCode, metricCode)
               .eq(RuleConfigEntity::getIsCurrentVersion, 1)
               .eq(RuleConfigEntity::getStatus, RuleStatusEnum.ACTIVE.getCode())
               .orderByAsc(RuleConfigEntity::getCreateTime);
        return ruleConfigMapper.selectList(wrapper);
    }

    @Override
    public List<RuleConfigEntity> listVersions(String ruleCode) {
        LambdaQueryWrapper<RuleConfigEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RuleConfigEntity::getRuleCode, ruleCode)
               .orderByDesc(RuleConfigEntity::getVersion);
        return ruleConfigMapper.selectList(wrapper);
    }

    @Override
    public List<RuleConfigEntity> listVersionsIncludeDeleted(String ruleCode) {
        return ruleConfigMapper.selectVersionsIncludeDeleted(ruleCode);
    }

    // ==================== 私有方法 ====================

    /**
     * 构建规则条件JSON字符串
     *
     * 优先使用前端传入的 ruleConditions（已经序列化好的JSON字符串），
     * 如果前端传入的是 RuleConditions 对象，则序列化为JSON
     */
    private String buildRuleConditionsJson(RuleConfigParams dto) {
        if (StringUtils.hasText(dto.getRuleConditions())) {
            return dto.getRuleConditions();
        }
        if (dto.getRuleConditionsObj() != null) {
            try {
                return objectMapper.writeValueAsString(dto.getRuleConditionsObj());
            } catch (JsonProcessingException e) {
                throw new BusinessException("规则条件序列化失败");
            }
        }
        return null;
    }
}
