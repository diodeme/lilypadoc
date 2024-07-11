package com.diode.lilypadoc.core.domain;

import com.diode.lilypadoc.standard.api.ILilypadocComponent;
import com.diode.lilypadoc.standard.api.ITemplate;
import com.diode.lilypadoc.standard.api.plugin.AbstractPlugin;
import com.diode.lilypadoc.standard.common.Const;
import com.diode.lilypadoc.standard.common.ErrorCode;
import com.diode.lilypadoc.standard.common.Result;
import com.diode.lilypadoc.standard.common.StandardErrorCodes;
import com.diode.lilypadoc.standard.common.enums.PluginDomainEnum;
import com.diode.lilypadoc.standard.domain.LilypadocContext;
import com.diode.lilypadoc.standard.domain.MPath;
import com.diode.lilypadoc.standard.domain.PluginMeta;
import com.diode.lilypadoc.standard.domain.Resource;
import com.diode.lilypadoc.standard.domain.html.Html;
import com.diode.lilypadoc.core.config.ConfigurationManager;
import com.diode.lilypadoc.core.config.HtmlConfiguration;
import com.diode.lilypadoc.core.plugin.PluginManager;
import com.diode.lilypadoc.core.support.PluginSupport;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public class Index implements Page {

    private final ITemplate template;
    private final MPath indexPath;

    public Index() {
        HtmlConfiguration configuration = ConfigurationManager.getInstance().getConfiguration(HtmlConfiguration.class);
        template = configuration.getIndexTemplate();
        indexPath = MPath.of(configuration.getRootPath()).appendChild(Const.INDEX_NAME);
    }

    @Override
    public Result<File> parseAndSync() {
        try {
            Result<Html> parse = parse();
            if (parse.isFailed()) {
                return Result.fail(parse.errorCode());
            }
            Result<File> syncResult = sync(parse.get());
            if (syncResult.isFailed()) {
                return Result.fail(syncResult.errorCode());
            }
            return syncResult;
        } catch (Exception e) {
            log.error("index页面生成未知异常", e);
            return Result.fail(StandardErrorCodes.UNKNOWN_ERROR.of("index页面生成时发生未知异常"));
        }
    }

    public Result<Html> parse() {
        log.info("开始解析Index");
        List<Resource> resourceList = new ArrayList<>();
        Result<List<AbstractPlugin>> result = PluginManager.getSortedPlugins();
        if (result.isFailed()) {
            return Result.fail(result.errorCode());
        }
        List<AbstractPlugin> plugins = result.get();
        //执行插件
        Map<AbstractPlugin, List<ILilypadocComponent>> htmlComponentMap = new HashMap<>();
        ErrorCode errorCode = PluginSupport.executePlugin(plugins, new LilypadocContext(), resourceList, htmlComponentMap, PluginDomainEnum.INDEX);
        if (StandardErrorCodes.OK.notEquals(errorCode)) {
            return Result.fail(errorCode);
        }
        Map<PluginMeta, List<ILilypadocComponent>> pluginMetaListMap = htmlComponentMap.entrySet().stream()
                .collect(Collectors.toMap(e -> e.getKey().meta(), Map.Entry::getValue, (v1, v2) -> v2));
        return template.inject(resourceList, pluginMetaListMap);
    }

    public Result<File> sync(Html html) {
        String path = indexPath.toString();
        log.info("开始同步index: {}", path);
        File file = null;
        try {
            file = new File(path);
            String content = html.parse();
            FileUtils.writeStringToFile(file, content, StandardCharsets.UTF_8);
            return Result.ok(file);
        } catch (IOException e) {
            log.error("同步index发生IO异常, file:{}", file, e);
            return Result.fail(StandardErrorCodes.IO_ERROR.of("同步index " + path + "发生io异常"));
        }
    }
}