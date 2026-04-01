package com.risk.enums;

import lombok.Getter;

/**
 * 预警触发等级枚举
 */
@Getter
public enum TriggerLevelEnum {

    L1("L1", "正常边界"),
    L2("L2", "警告"),
    L3("L3", "错误"),
    L4("L4", "严重");

    /** 等级编码 */
    private final String code;
    /** 等级描述 */
    private final String desc;

    TriggerLevelEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
