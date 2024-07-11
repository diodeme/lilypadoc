package com.diode.lilypadoc.application.core;

import com.diode.lilypadoc.core.config.ConfigurationManager;
import com.diode.lilypadoc.core.config.HtmlConfiguration;
import com.diode.lilypadoc.core.domain.Page;
import com.diode.lilypadoc.standard.api.ITemplate;
import com.diode.lilypadoc.standard.common.Result;
import com.diode.lilypadoc.standard.common.StandardErrorCodes;
import com.diode.lilypadoc.standard.domain.MPath;
import com.diode.lilypadoc.standard.domain.html.Html;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;

@Slf4j
public class TemplateAfterFusion implements Page {

    private final ITemplate template;
    private final MPath templatePath;

    private final static String TEMPLATE_AFTER_FUSION_PATH = "/template_after_fusion.html";

    public TemplateAfterFusion() {
        template = ConfigurationManager.getInstance().getConfiguration(HtmlConfiguration.class).getTemplate();
        HtmlConfiguration htmlConfiguration = ConfigurationManager.getInstance()
                .getConfiguration(HtmlConfiguration.class);
        templatePath = htmlConfiguration.getTemplatePath().appendChild(TEMPLATE_AFTER_FUSION_PATH);
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
            log.error("TemplateAfterFusion生成未知异常", e);
            return Result.fail(StandardErrorCodes.UNKNOWN_ERROR.of("TemplateAfterFusion生成时发生未知异常"));
        }
    }

    public Result<Html> parse() {
        log.info("开始解析TemplateAfterFusion");
        return template.inject(new ArrayList<>(), new HashMap<>());
    }

    public Result<File> sync(Html html) {
        String path = templatePath.toString();
        log.info("同步TemplateAfterFusion: {}", path);
        File file = null;
        try {
            file = new File(path);
            String content = html.parse();
            FileUtils.writeStringToFile(file, content, StandardCharsets.UTF_8);
            return Result.ok(file);
        } catch (IOException e) {
            log.error("同步TemplateAfterFusion发生IO异常，file: {}", file, e);
            return Result.fail(StandardErrorCodes.IO_ERROR.of("同步TemplateAfterFusion: " + path + " 发生io异常"));
        }
    }
}