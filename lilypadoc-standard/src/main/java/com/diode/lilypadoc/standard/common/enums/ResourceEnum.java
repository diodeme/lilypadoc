package com.diode.lilypadoc.standard.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum ResourceEnum {

    CSS("css", ".+\\.css"),
    JS("js",".+\\.js"),
    HTML("html",".+\\.html"),
    IMG("img",".+\\.(ico|icon|jpg|jpeg|png|gif|svg|apng|webp|avif)"),
    ;

    @Getter
    private final String name;

    @Getter
    private final String extensionRegex;
}
