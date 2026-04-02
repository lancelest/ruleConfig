package com.risk.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 规则条件集合 - 整个规则的条件定义
 *
 * 示例：
 * 单指标：{"logic":"SINGLE","conditions":[{"metricCode":"excess_return_1y","operator":"GT","threshold":0}]}
 * 多指标AND：{"logic":"AND","conditions":[...]}
 */
@Data
public class RuleConditions implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 逻辑类型
     * SINGLE - 单指标条件
     * AND - 多指标同时满足
     */
    private String logic;

    /** 条件列表 */
    private List<RuleCondition> conditions;
}
