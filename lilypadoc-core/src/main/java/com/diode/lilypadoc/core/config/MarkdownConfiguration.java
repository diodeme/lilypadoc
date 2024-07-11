package com.diode.lilypadoc.core.config;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class MarkdownConfiguration implements IConfiguration{
    private String rootDir;
    private Integer categoryDepth;
}
