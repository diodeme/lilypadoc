package com.diode.lilypadoc.application.core;

import com.diode.lilypadoc.core.config.ConfigurationManager;
import com.diode.lilypadoc.core.config.HtmlConfiguration;
import com.diode.lilypadoc.standard.api.ILilypadocComponent;
import com.diode.lilypadoc.standard.api.ITemplate;
import com.diode.lilypadoc.standard.common.Result;
import com.diode.lilypadoc.standard.common.StandardErrorCodes;
import com.diode.lilypadoc.standard.common.enums.ResourceEnum;
import com.diode.lilypadoc.standard.domain.MPath;
import com.diode.lilypadoc.standard.domain.PluginMeta;
import com.diode.lilypadoc.standard.domain.Resource;
import com.diode.lilypadoc.standard.domain.html.Html;
import com.diode.lilypadoc.standard.domain.html.Text;
import com.diode.lilypadoc.standard.exception.BizException;
import com.diode.lilypadoc.standard.utils.FileTool;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class TailwindcssTemplate implements ITemplate {

    private static final String RESOURCE_INDEX = "/*@RESOURCE AREA*/";
    private static final String FINAL_INDEX = "/*@END*/";
    private static final String TAILWINDCSS_TEMPLATE_PATH = "tailwind.custom.css";

    private final String template;

    public TailwindcssTemplate() {
        File templateFile = new File(getTemplatePath().toString());
        if (!templateFile.exists()) {
            throw new BizException(StandardErrorCodes.BIZ_ERROR.of("Index文件不存在。请检查!"));
        }
        Result<String> result = FileTool.readFileToString(templateFile);
        if (result.isFailed()) {
            throw new BizException(result.errorCode());
        }
        template = result.get();
    }

    @Override
    public Result<Html> inject(List<Resource> resourceList, Map<PluginMeta, List<ILilypadocComponent>> componentMap) {
        int pos = template.indexOf(RESOURCE_INDEX) + RESOURCE_INDEX.length();
        int endPos = template.indexOf(RESOURCE_INDEX + FINAL_INDEX);

        if (pos < 0 || endPos < 0) {
            throw new BizException(StandardErrorCodes.BIZ_ERROR.of("css模版找不到tag:" + RESOURCE_INDEX + "或结束符"));
        }

        StringBuilder sb = new StringBuilder();
        for (Resource resource : resourceList) {
            List<File> syncResource = resource.getSyncResource(ResourceEnum.CSS);
            if (Objects.isNull(syncResource)) {
                continue;
            }

            String importPathFormat =
                    "@import \".." + resource.getCustomPath() + "/" + resource.getPluginName() + "/"
                            + ResourceEnum.CSS.getName() + "/%s\";\n";
            for (File file : syncResource) {
                sb.append("\n").append(String.format(importPathFormat, file.getName()));
            }
        }
        String content = template.substring(0, pos) + sb + template.substring(endPos);
        return Result.ok(new Html().element(new Text(content)));
    }

    @Override
    public MPath getTemplatePath() {
        HtmlConfiguration configuration = ConfigurationManager.getInstance().getConfiguration(HtmlConfiguration.class);
        return configuration.getTemplatePath().appendChild(TAILWINDCSS_TEMPLATE_PATH);
    }
}