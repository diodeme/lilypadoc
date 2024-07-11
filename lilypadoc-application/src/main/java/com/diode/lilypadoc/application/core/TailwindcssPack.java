package com.diode.lilypadoc.application.core;

import com.diode.lilypadoc.application.util.OSTool;
import com.diode.lilypadoc.core.config.ConfigurationManager;
import com.diode.lilypadoc.core.config.HtmlConfiguration;
import com.diode.lilypadoc.core.domain.Page;
import com.diode.lilypadoc.core.plugin.PluginManager;
import com.diode.lilypadoc.standard.api.ITemplate;
import com.diode.lilypadoc.standard.api.plugin.AbstractPlugin;
import com.diode.lilypadoc.standard.common.ErrorCode;
import com.diode.lilypadoc.standard.common.Result;
import com.diode.lilypadoc.standard.common.StandardErrorCodes;
import com.diode.lilypadoc.standard.domain.MPath;
import com.diode.lilypadoc.standard.domain.Resource;
import com.diode.lilypadoc.standard.domain.html.Html;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Tailwind CSS打包类
 */
public class TailwindcssPack implements Page {

    private static final Logger log = LoggerFactory.getLogger(TailwindcssPack.class);

    private final ITemplate template;

    private static final String WINDOWS_BAT_PATH = "/bin/tailwind.bat";
    private static final String LINUX_SH_PATH = "/bin/tailwind.sh";

    public TailwindcssPack() {
        template = ConfigurationManager.getInstance().getConfiguration(HtmlConfiguration.class).getCssTemplate();
    }

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
            // 执行打包命令，执行前端构建css
            ErrorCode errorCode = executeBash();
            if (StandardErrorCodes.OK.notEquals(errorCode)) {
                return Result.fail(errorCode);
            }
            return syncResult;
        } catch (Exception e) {
            log.error("CssPack生成时发生未知异常", e);
            return Result.fail(StandardErrorCodes.UNKNOWN_ERROR.of("CssPack生成时发生未知异常"));
        }
    }

    public Result<Html> parse() {
        log.info("开始解析css");
        List<Resource> resourceList = new ArrayList<>();
        Result<List<AbstractPlugin>> result = PluginManager.getSortedPlugins();
        if (result.isFailed()) {
            return Result.fail(result.errorCode());
        }
        List<AbstractPlugin> plugins = result.get();
        for (AbstractPlugin plugin : plugins) {
            resourceList.add(plugin.resource());
        }
        return template.inject(resourceList, new HashMap<>());
    }

    public Result<File> sync(Html html) {
        String path = template.getTemplatePath().toString();
        log.info("开始同步cssPack: {}", path);
        File file = null;
        try {
            file = new File(path);
            String content = html.parse();
            FileUtils.writeStringToFile(file, content, StandardCharsets.UTF_8);
            return Result.ok(file);
        } catch (IOException e) {
            log.error("同步cssPack发生IO异常, file:{}", file, e);
            return Result.fail(StandardErrorCodes.IO_ERROR.of("同步cssPack:"+path+"发生IO异常"));
        }
    }

    public ErrorCode executeBash() {
        try {
            String[] command;
            HtmlConfiguration configuration = ConfigurationManager.getInstance()
                    .getConfiguration(HtmlConfiguration.class);
            MPath templatePath = configuration.getTemplatePath();
            if (OSTool.isWindows()) {
                command = new String[]{templatePath + WINDOWS_BAT_PATH, templatePath.toString()};
            } else if (OSTool.isLinux()) {
                command = new String[]{"sh", templatePath + LINUX_SH_PATH, templatePath.toString()};
            } else {
                return StandardErrorCodes.BIZ_ERROR.of("暂不支持的操作系统");
            }
            OSTool.execCommand(new File(configuration.getRootPath()), command);
        } catch (Exception e) {
            log.error("执行css pack脚本失败", e);
            return StandardErrorCodes.BIZ_ERROR.of("执行css pack脚本失败");
        }
        return StandardErrorCodes.OK;
    }

}