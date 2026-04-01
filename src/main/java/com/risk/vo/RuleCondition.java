package com.risk.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 规则条件 - 单个条件
 *
 * 示例：超额收益率大于0 → {indicatorCode:"excess_return_1y", operator:"GT", threshold:0}
 */
@Data
public class RuleCondition implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 指标编码，关联 indicator_metadata.indicator_code */
    private String indicatorCode;

    /** 指标名称（冗余存储，方便展示） */
    private String indicatorName;

    /**
     * 比较运算符
     * 取值：GT(大于), GTE(大于等于), LT(小于), LTE(小于等于), ABS_GT(绝对值大于)
     * 使用英文标识，避免JSON转义问题
     */
    private String operator;

    /** 阈值 */
    private BigDecimal threshold;
}
