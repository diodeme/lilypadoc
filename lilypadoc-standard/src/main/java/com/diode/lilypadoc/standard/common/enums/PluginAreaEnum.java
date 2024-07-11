package com.diode.lilypadoc.standard.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum PluginAreaEnum implements BaseEnum{

    HEAD("head", "头部区域"),
    LEFT_ASIDE("left_aside", "左侧边区域"),
    RIGHT_ASIDE("right_aside", "右侧边区域"),
    BODY("body", "主体区域"),
    FOOT("foot", "底部区域"),
    ;

    @Getter
    private final String code;

    @Getter
    private final String value;
}
