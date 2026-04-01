package com.risk.enums;

import lombok.Getter;

/**
 * 规则状态枚举
 */
@Getter
public enum RuleStatusEnum {

    DRAFT("DRAFT", "草稿"),
    ACTIVE("ACTIVE", "生效"),
    INACTIVE("INACTIVE", "停用"),
    ARCHIVED("ARCHIVED", "归档");

    /** 状态编码 */
    private final String code;
    /** 状态描述 */
    private final String desc;

    RuleStatusEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
