package com.diode.lilypadoc.core.config;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApplicationConfiguration implements IConfiguration{
    private String projectRootPath;
    private String customConfigPath;

    public String getCustomConfigAbPath(){
        return projectRootPath + "/" + customConfigPath;
    }
}
