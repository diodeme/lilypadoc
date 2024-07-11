package com.diode.lilypadoc.standard.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum PluginDomainEnum implements BaseEnum{

    DOC("doc","文章"),
    INDEX("index", "主页"),
    ;

    @Getter
    private final String code;

    @Getter
    private final String value;
}
