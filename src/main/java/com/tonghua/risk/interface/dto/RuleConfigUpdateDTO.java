package com.tonghua.risk.interface.dto;

import com.tonghua.risk.domain.vo.RuleConditions;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.math.BigDecimal;

/**
 * 更新规则请求参数（生成新版本）
 */
@Data
public class RuleConfigUpdateDTO {

    /** 规则编码（用于定位要更新的规则） */
    @NotBlank(message = "规则编码不能为空")
    private String ruleCode;

    /** 规则名称 */
    @NotBlank(message = "规则名称不能为空")
    private String ruleName;

    /** 组合类型编码 */
    private String portfolioTypeCode;

    /** 组合类型名称 */
    private String portfolioTypeName;

    /** 指标编码 */
    private String indicatorCode;

    /** 指标名称 */
    private String indicatorName;

    /** 细分类别 */
    private String subCategory;

    /** 规则条件（JSON字符串，与 ruleConditionsObj 二选一） */
    private String ruleConditions;

    /** 规则条件（对象形式，与 ruleConditions 二选一） */
    private RuleConditions ruleConditionsObj;

    /** 层级1阈值 */
    private BigDecimal thresholdL1;

    /** 层级2阈值 */
    private BigDecimal thresholdL2;

    /** 层级3阈值 */
    private BigDecimal thresholdL3;

    /** 层级4阈值 */
    private BigDecimal thresholdL4;

    /** 操作人 */
    @NotBlank(message = "操作人不能为空")
    private String operator;
}
