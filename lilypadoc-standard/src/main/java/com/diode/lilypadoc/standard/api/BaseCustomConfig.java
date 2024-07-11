package com.diode.lilypadoc.standard.api;

public class BaseCustomConfig {
    public static final String CUSTOM_CONFIG_PATH = "/%s/custom.config";

    public static String getCustomConfigPath(String pluginName){
        return String.format(BaseCustomConfig.CUSTOM_CONFIG_PATH, pluginName);
    }

}
