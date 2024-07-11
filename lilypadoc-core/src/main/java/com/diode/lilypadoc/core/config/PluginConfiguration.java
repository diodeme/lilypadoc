package com.diode.lilypadoc.core.config;

import com.diode.lilypadoc.standard.common.ErrorCode;
import com.diode.lilypadoc.standard.common.Result;
import com.diode.lilypadoc.standard.common.StandardErrorCodes;
import com.diode.lilypadoc.standard.common.enums.PluginStatusEnum;
import com.diode.lilypadoc.standard.exception.BizException;
import com.diode.lilypadoc.standard.utils.FileTool;
import com.diode.lilypadoc.standard.utils.JsonTool;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Slf4j
public class PluginConfiguration implements IConfiguration {

    private static final String PLUGIN_STATUS_PATH = "plugin.status";

    /**
     * key:pluginName value:pluginStatus
     */
    private final Map<String, PluginStatusEnum> pluginStatusMap;

    public PluginConfiguration() {
        //TODO 改成sql-lite
        Result<String> result = FileTool.readResource(getClass().getClassLoader(), PLUGIN_STATUS_PATH);
        if (result.isFailed()) {
            throw new BizException(StandardErrorCodes.IO_ERROR.of("读取插件状态文件时出现异常"));
        }
        String pluginConfiguration = result.get();
        Map<String, PluginStatusEnum> map = JsonTool.fromJson(pluginConfiguration, new HashMap<String, String>() {
        }.getClass().getGenericSuperclass());
        pluginStatusMap = Objects.isNull(map) ? new HashMap<>() : map;
    }

    public PluginStatusEnum getPluginStatus(String pluginName) {
        return pluginStatusMap.get(pluginName);
    }

    public void changeStatus(String name, PluginStatusEnum status) {
        PluginStatusEnum pluginStatusEnum = pluginStatusMap.get(name);
        if (Objects.isNull(pluginStatusEnum)) {
            log.warn("changeStatus 指定的name:{}不存在", name);
            return;
        }
        pluginStatusMap.put(name, status);
    }

    public void sync() {
        String content = JsonTool.toJson(pluginStatusMap);
        ErrorCode errorCode = FileTool.writeExistResource(getClass().getClassLoader(), PLUGIN_STATUS_PATH, content);
        if (StandardErrorCodes.OK.notEquals(errorCode)) {
            throw new BizException(StandardErrorCodes.IO_ERROR.of("写入插件状态文件时出现异常，请稍后再试"));
        }
    }
}

