package com.risk.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 规则引擎评估请求参数
 * 
 * 用于评估单个指标是否触发预警
 */
@Data
public class RuleEvalRequest {

    /** 组合代码（基金代码） */
    private String portCode;

    /** 组合类型编码 */
    private String portTypeCode;

    /** 指标编码 */
    private String metricCode;

    /** 指标实际值 */
    private BigDecimal metricValue;

    /** 评估日期（默认当天） */
    private String evalDate;

    /** 细分类别编码（可选，用于按档位匹配阈值） */
    private String subCategoryCode;
}