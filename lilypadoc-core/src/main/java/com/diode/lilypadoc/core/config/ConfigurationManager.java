package com.diode.lilypadoc.core.config;

import com.diode.lilypadoc.standard.common.StandardErrorCodes;
import com.diode.lilypadoc.standard.exception.BizException;

import java.util.HashMap;
import java.util.Map;

public class ConfigurationManager {
    public Map<Class<? extends IConfiguration>, IConfiguration> map;

    private ConfigurationManager() { map = new HashMap<>(); }

    private static class ConfigurationManagerInstance {
        private static final ConfigurationManager INSTANCE = new ConfigurationManager();
    }

    public static ConfigurationManager getInstance() { return ConfigurationManagerInstance.INSTANCE; }

    public void injectConfiguration(IConfiguration configuration) {
        if (map.containsKey(configuration.getClass())) {
            throw new BizException(StandardErrorCodes.BIZ_ERROR.of("重复的配置: " + configuration.getClass()));
        }
        map.put(configuration.getClass(), configuration);
    }

    @SuppressWarnings({"unchecked"})
    public <T extends IConfiguration> T getConfiguration(Class<T> clazz) { return (T) map.get(clazz); }
}
