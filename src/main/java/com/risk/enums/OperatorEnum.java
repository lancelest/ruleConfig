package com.risk.enums;

import lombok.Getter;

/**
 * 比较运算符枚举
 */
@Getter
public enum OperatorEnum {

    GT("GT", "大于"),
    GTE("GTE", "大于等于"),
    LT("LT", "小于"),
    LTE("LTE", "小于等于"),
    ABS_GT("ABS_GT", "绝对值大于"),
    ABS_LT("ABS_LT", "绝对值小于");

    /** 运算符编码 */
    private final String code;
    /** 运算符描述 */
    private final String desc;

    OperatorEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
