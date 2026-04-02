package com.risk.dto;

import com.risk.vo.RuleConditions;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.math.BigDecimal;

/**
 * 规则配置请求参数（新增/更新通用）
 */
@Data
public class RuleConfigParams {

    /** 规则编码，业务唯一标识 */
    @NotBlank(message = "规则编码不能为空")
    private String ruleCode;

    /** 规则名称 */
    @NotBlank(message = "规则名称不能为空")
    private String ruleName;

    /** 组合类型编码（如：active_equity） */
    private String portTypeCode;

    /** 组合类型名称（如：主动权益） */
    private String portTypeName;

    /** 指标编码 */
    private String metricCode;

    /** 指标名称 */
    private String metricName;

    /** 细分类别 */
    private String subCategory;

    /**
     * 规则条件（JSON字符串，与 ruleConditionsObj 二选一）
     * 前端可直接传序列化好的JSON字符串
     */
    private String ruleConditions;

    /**
     * 规则条件（对象形式，与 ruleConditions 二选一）
     * 框架会自动序列化为JSON
     */
    private RuleConditions ruleConditionsObj;

    /** 层级1阈值 */
    private BigDecimal level1;

    /** 层级2阈值 */
    private BigDecimal level2;

    /** 层级3阈值 */
    private BigDecimal level3;

    /** 层级4阈值 */
    private BigDecimal level4;

    /** 操作人 */
    @NotBlank(message = "操作人不能为空")
    private String operator;
}
