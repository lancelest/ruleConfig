package com.risk.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 规则引擎评估结果
 * 
 * 用于返回单个指标的评估结果
 */
@Data
public class RuleEvalResult {

    /** 是否触发预警 */
    private Boolean triggered;

    /** 规则编码 */
    private String ruleCode;

    /** 规则名称 */
    private String ruleName;

    /** 触发等级 1/2/3/4，未触发时为 null */
    private Integer level;

    /** 触发阈值（命中的那个阈值） */
    private BigDecimal thresholdValue;

    /** 指标实际值 */
    private BigDecimal actualValue;

    /** 结果说明 */
    private String message;

    /**
     * 创建未触发结果
     */
    public static RuleEvalResult notTriggered(String message) {
        RuleEvalResult result = new RuleEvalResult();
        result.setTriggered(false);
        result.setMessage(message);
        return result;
    }

    /**
     * 创建已触发结果
     */
    public static RuleEvalResult of(String ruleCode, String ruleName, Integer level,
                                     BigDecimal thresholdValue, BigDecimal actualValue, String message) {
        RuleEvalResult result = new RuleEvalResult();
        result.setTriggered(true);
        result.setRuleCode(ruleCode);
        result.setRuleName(ruleName);
        result.setLevel(level);
        result.setThresholdValue(thresholdValue);
        result.setActualValue(actualValue);
        result.setMessage(message);
        return result;
    }
}