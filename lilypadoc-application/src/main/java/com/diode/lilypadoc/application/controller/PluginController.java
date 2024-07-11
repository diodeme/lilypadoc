package com.diode.lilypadoc.application.controller;

import com.diode.lilypadoc.core.config.ConfigurationManager;
import com.diode.lilypadoc.core.config.HtmlConfiguration;
import com.diode.lilypadoc.core.plugin.PluginManager;
import com.diode.lilypadoc.standard.api.IHttpCall;
import com.diode.lilypadoc.standard.api.plugin.AbstractPlugin;
import com.diode.lilypadoc.standard.common.Result;
import com.diode.lilypadoc.standard.common.StandardErrorCodes;
import com.diode.lilypadoc.standard.domain.MPath;
import com.diode.lilypadoc.standard.domain.http.HttpCallContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Objects;

@Slf4j
@RestController("/customConfig")
@RequestMapping("/plugin")
public class PluginController {

    @PostMapping(value = "")
    public Result<Map<String, String>> pluginHttpCall(@RequestBody Map<String, String> paramMap) {
        log.info("收到插件调用请求:{}", paramMap);
        String name = paramMap.get("name");
        AbstractPlugin plugin = PluginManager.getPlugin(name);
        if (Objects.isNull(plugin)) {
            return Result.fail(StandardErrorCodes.BIZ_ERROR.of("插件未找到插件:" + name + "不存在!"));
        }
        if (!(plugin instanceof IHttpCall)) {
            return Result.fail(StandardErrorCodes.BIZ_ERROR.of("插件未找到插件:" + name + "不支持Http调用!"));
        }
        HttpCallContext httpCallContext = new HttpCallContext();
        HtmlConfiguration configuration = ConfigurationManager.getInstance().getConfiguration(HtmlConfiguration.class);
        httpCallContext.setHtmlRootPath(MPath.of(configuration.getRootPath()));
        httpCallContext.setHtmlDocRPath(MPath.of(configuration.getDocRePath()));
        return ((IHttpCall)plugin).httpCall(paramMap, httpCallContext);
    }
}