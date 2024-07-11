package com.diode.lilypadoc.standard.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum PluginStatusEnum {
    INIT("00", "初始化"),
    OPEN("01", "开启"),
    CLOSE("02", "关闭"),
    ;

    @Getter
    private final String code;

    @Getter
    private final String value;
}
