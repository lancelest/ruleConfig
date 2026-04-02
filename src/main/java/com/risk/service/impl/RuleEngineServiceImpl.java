package com.risk.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.risk.dto.BatchEvalRequest;
import com.risk.dto.RuleEvalRequest;
import com.risk.entity.MetricMetadataEntity;
import com.risk.entity.RuleConfigEntity;
import com.risk.entity.RuleExemptionEntity;
import com.risk.entity.RuleSubCategoryEntity;
import com.risk.enums.RuleStatusEnum;
import com.risk.exception.BusinessException;
import com.risk.mapper.MetricMetadataMapper;
import com.risk.mapper.RuleConfigMapper;
import com.risk.mapper.RuleExemptionMapper;
import com.risk.mapper.RuleSubCategoryMapper;
import com.risk.service.RuleEngineService;
import com.risk.vo.BatchEvalResult;
import com.risk.vo.RuleEvalResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 规则引擎服务实现
 * 
 * 核心逻辑：
 * 1. 根据 portTypeCode + metricCode 查找匹配的规则
 * 2. 检查该规则是否被豁免
 * 3. 如有细分类别，根据组合基准值匹配对应的阈值配置
 * 4. 比较 metricValue 与阈值，判定触发等级
 * 5. 返回评估结果
 */
@Slf4j
@Service
public class RuleEngineServiceImpl implements RuleEngineService {

    @Resource
    private RuleConfigMapper ruleConfigMapper;

    @Resource
    private RuleSubCategoryMapper subCategoryMapper;

    @Resource
    private RuleExemptionMapper exemptionMapper;

    @Resource
    private MetricMetadataMapper metricMetadataMapper;

    /**
     * 单次评估
     * 
     * 处理流程：
     * 1. 查找匹配规则（按组合类型 + 指标编码）
     * 2. 检查豁免配置
     * 3. 获取阈值（优先细分类别，没有则用主表）
     * 4. 判定等级
     * 5. 返回结果
     */
    @Override
    public RuleEvalResult evaluate(RuleEvalRequest request) {
        // 1. 查找匹配规则
        RuleConfigEntity rule = findMatchingRule(request.getPortTypeCode(), request.getMetricCode());
        if (rule == null) {
            return RuleEvalResult.notTriggered("未找到匹配规则");
        }

        // 2. 检查豁免（遍历4个层级，只要任一层级豁免开启就不触发）
        if (isExempted(rule.getRuleCode(), request.getPortTypeCode())) {
            return RuleEvalResult.notTriggered("该组合类型已被豁免");
        }

        // 3. 获取阈值配置（优先细分类别）
        Map<Integer, BigDecimal> thresholds = getThresholds(rule, request.getSubCategoryCode());
        if (thresholds == null || thresholds.isEmpty()) {
            return RuleEvalResult.notTriggered("规则未配置阈值");
        }

        // 4. 获取阈值类型（NEGATIVE 或 ABSOLUTE）
        String thresholdType = getThresholdType(request.getMetricCode());

        // 5. 判定触发等级
        int level = determineLevel(thresholds, request.getMetricValue(), thresholdType);
        if (level == 0) {
            return RuleEvalResult.notTriggered("指标值未达到触发阈值");
        }

        // 6. 返回结果
        BigDecimal thresholdValue = thresholds.get(level);
        String message = buildMessage(rule.getRuleName(), request.getMetricCode(), 
                                        request.getMetricValue(), thresholdValue, level);
        
        return RuleEvalResult.of(rule.getRuleCode(), rule.getRuleName(), level, 
                                  thresholdValue, request.getMetricValue(), message);
    }

    /**
     * 批量评估
     * 
     * 遍历每个指标，逐个调用单次评估，汇总结果
     */
    @Override
    public BatchEvalResult batchEvaluate(BatchEvalRequest request) {
        List<BatchEvalResult.EvalDetail> details = new ArrayList<>();
        int triggeredCount = 0;

        for (BatchEvalRequest.MetricItem item : request.getMetrics()) {
            // 构造单次评估请求
            RuleEvalRequest evalRequest = new RuleEvalRequest();
            evalRequest.setPortCode(item.getPortCode());
            evalRequest.setPortTypeCode(item.getPortTypeCode());
            evalRequest.setMetricCode(item.getMetricCode());
            evalRequest.setMetricValue(item.getMetricValue());
            evalRequest.setSubCategoryCode(item.getSubCategoryCode());
            evalRequest.setEvalDate(request.getEvalDate());

            // 执行评估
            RuleEvalResult result = evaluate(evalRequest);

            // 汇总结果
            BatchEvalResult.EvalDetail detail = new BatchEvalResult.EvalDetail();
            detail.setPortCode(item.getPortCode());
            detail.setMetricCode(item.getMetricCode());
            detail.setTriggered(result.getTriggered());
            detail.setLevel(result.getLevel());
            detail.setRuleCode(result.getRuleCode());
            detail.setActualValue(result.getActualValue());
            detail.setThresholdValue(result.getThresholdValue());
            detail.setMessage(result.getMessage());
            details.add(detail);

            if (result.getTriggered()) {
                triggeredCount++;
            }
        }

        BatchEvalResult result = new BatchEvalResult();
        result.setTotal(details.size());
        result.setTriggered(triggeredCount);
        result.setResults(details);
        return result;
    }

    // ==================== 内部方法 ====================

    /**
     * 查找匹配规则
     * 
     * 条件：portTypeCode 匹配 + metricCode 匹配 + 状态为生效 + 当前版本
     */
    private RuleConfigEntity findMatchingRule(String portTypeCode, String metricCode) {
        LambdaQueryWrapper<RuleConfigEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RuleConfigEntity::getPortTypeCode, portTypeCode)
               .eq(RuleConfigEntity::getMetricCode, metricCode)
               .eq(RuleConfigEntity::getStatus, RuleStatusEnum.ACTIVE.getCode())
               .eq(RuleConfigEntity::getDeleted, 0);
        
        // 时间有效性检查（如果配置了生效/失效时间）
        LocalDateTime now = LocalDateTime.now();
        wrapper.and(w -> w.isNull(RuleConfigEntity::getEffectiveTime)
                         .or().le(RuleConfigEntity::getEffectiveTime, now));
        wrapper.and(w -> w.isNull(RuleConfigEntity::getExpireTime)
                         .or().ge(RuleConfigEntity::getExpireTime, now));

        return ruleConfigMapper.selectOne(wrapper);
    }

    /**
     * 检查是否被豁免
     * 
     * 条件：ruleCode + portTypeCode 存在豁免配置且 isEnabled=1
     */
    private boolean isExempted(String ruleCode, String portTypeCode) {
        LambdaQueryWrapper<RuleExemptionEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RuleExemptionEntity::getRuleCode, ruleCode)
               .eq(RuleExemptionEntity::getPortTypeCode, portTypeCode)
               .eq(RuleExemptionEntity::getIsEnabled, 1)
               .eq(RuleExemptionEntity::getDeleted, 0);
        
        Long count = exemptionMapper.selectCount(wrapper);
        return count != null && count > 0;
    }

    /**
     * 获取阈值配置
     * 
     * 优先从细分类别表获取，没有则从规则主表获取
     */
    private Map<Integer, BigDecimal> getThresholds(RuleConfigEntity rule, String subCategoryCode) {
        Map<Integer, BigDecimal> thresholds = new HashMap<>();

        // 如果有细分类别编码，优先查细分类别表
        if (subCategoryCode != null && !subCategoryCode.isEmpty()) {
            LambdaQueryWrapper<RuleSubCategoryEntity> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(RuleSubCategoryEntity::getRuleCode, rule.getRuleCode())
                   .eq(RuleSubCategoryEntity::getSubCategoryCode, subCategoryCode)
                   .eq(RuleSubCategoryEntity::getDeleted, 0);
            
            RuleSubCategoryEntity subCategory = subCategoryMapper.selectOne(wrapper);
            if (subCategory != null) {
                putIfNotNull(thresholds, 1, subCategory.getLevel1());
                putIfNotNull(thresholds, 2, subCategory.getLevel2());
                putIfNotNull(thresholds, 3, subCategory.getLevel3());
                putIfNotNull(thresholds, 4, subCategory.getLevel4());
                return thresholds;
            }
        }

        // 没有细分类别或未匹配到，用规则主表的阈值
        putIfNotNull(thresholds, 1, rule.getLevel1());
        putIfNotNull(thresholds, 2, rule.getLevel2());
        putIfNotNull(thresholds, 3, rule.getLevel3());
        putIfNotNull(thresholds, 4, rule.getLevel4());

        return thresholds;
    }

    /**
     * 辅助方法：仅当值不为 null 时才放入 Map
     */
    private void putIfNotNull(Map<Integer, BigDecimal> map, int key, BigDecimal value) {
        if (value != null) {
            map.put(key, value);
        }
    }

    /**
     * 获取阈值类型
     * 
     * 从指标元数据表获取 thresholdType 字段
     * 默认返回 ABSOLUTE（绝对值偏离）
     */
    private String getThresholdType(String metricCode) {
        LambdaQueryWrapper<MetricMetadataEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MetricMetadataEntity::getMetricCode, metricCode);
        
        MetricMetadataEntity metadata = metricMetadataMapper.selectOne(wrapper);
        if (metadata != null && metadata.getThresholdType() != null) {
            return metadata.getThresholdType();
        }
        return "ABSOLUTE";
    }

    /**
     * 判定触发等级
     * 
     * 比较逻辑：
     * - NEGATIVE（负向）：value <= threshold 触发，值越负等级越高（level4 阈值最负）
     * - ABSOLUTE（绝对值）：|value| >= threshold 触发，偏离越大等级越高（level4 阈值最大）
     * 
     * @param thresholds 各层级阈值 Map
     * @param value 指标实际值
     * @param thresholdType 阈值类型 NEGATIVE/ABSOLUTE
     * @return 触发等级 1/2/3/4，未触发返回 0
     */
    private int determineLevel(Map<Integer, BigDecimal> thresholds, BigDecimal value, String thresholdType) {
        if (value == null) {
            return 0;
        }

        // 按等级从高到低判断（level4 最严格，优先判断）
        if ("NEGATIVE".equals(thresholdType)) {
            // 负向阈值：value <= threshold 触发，阈值越负等级越高
            // 例如：level4=-0.10, level3=-0.07, level2=-0.05
            if (thresholds.containsKey(4) && value.compareTo(thresholds.get(4)) <= 0) {
                return 4;
            }
            if (thresholds.containsKey(3) && value.compareTo(thresholds.get(3)) <= 0) {
                return 3;
            }
            if (thresholds.containsKey(2) && value.compareTo(thresholds.get(2)) <= 0) {
                return 2;
            }
            if (thresholds.containsKey(1) && value.compareTo(thresholds.get(1)) <= 0) {
                return 1;
            }
        } else {
            // 绝对值偏离：|value| >= threshold 触发，阈值越大等级越高
            // 例如：level4=0.25, level3=0.20, level2=0.15
            BigDecimal absValue = value.abs();
            if (thresholds.containsKey(4) && absValue.compareTo(thresholds.get(4)) >= 0) {
                return 4;
            }
            if (thresholds.containsKey(3) && absValue.compareTo(thresholds.get(3)) >= 0) {
                return 3;
            }
            if (thresholds.containsKey(2) && absValue.compareTo(thresholds.get(2)) >= 0) {
                return 2;
            }
            if (thresholds.containsKey(1) && absValue.compareTo(thresholds.get(1)) >= 0) {
                return 1;
            }
        }

        return 0;
    }

    /**
     * 构建结果说明信息
     */
    private String buildMessage(String ruleName, String metricCode, 
                                 BigDecimal actualValue, BigDecimal thresholdValue, int level) {
        return String.format("规则[%s] 指标[%s] 实际值=%.4f 触发L%d级预警（阈值=%.4f）",
                ruleName, metricCode, actualValue, level, thresholdValue);
    }
}