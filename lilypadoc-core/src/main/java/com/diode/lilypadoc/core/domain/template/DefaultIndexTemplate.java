package com.diode.lilypadoc.core.domain.template;

import com.diode.lilypadoc.standard.api.ILilypadocComponent;
import com.diode.lilypadoc.standard.api.ITemplate;
import com.diode.lilypadoc.standard.common.Const;
import com.diode.lilypadoc.standard.common.Result;
import com.diode.lilypadoc.standard.common.StandardErrorCodes;
import com.diode.lilypadoc.standard.common.enums.PluginAreaEnum;
import com.diode.lilypadoc.standard.common.enums.PluginDomainEnum;
import com.diode.lilypadoc.standard.domain.MPath;
import com.diode.lilypadoc.standard.domain.PluginMeta;
import com.diode.lilypadoc.standard.domain.Resource;
import com.diode.lilypadoc.standard.domain.html.Html;
import com.diode.lilypadoc.standard.domain.html.Text;
import com.diode.lilypadoc.standard.exception.BizException;
import com.diode.lilypadoc.standard.utils.FileTool;
import com.diode.lilypadoc.core.config.ConfigurationManager;
import com.diode.lilypadoc.core.config.HtmlConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.diode.lilypadoc.core.domain.template.DefaultTemplate.BODY_INDEX;
import static com.diode.lilypadoc.core.domain.template.DefaultTemplate.RESOURCE_INDEX;

public class DefaultIndexTemplate implements ITemplate {

    private static final String TEMPLATE_INDEX_INDEX = "<!--@TEMPLATE INDEX AREA-->";

    private final String templateContent;

    public DefaultIndexTemplate(DefaultTemplateConfig config) {
        File templateFile = new File(getTemplatePath().toString());
        if (!templateFile.exists()) {
            throw new BizException(StandardErrorCodes.BIZ_ERROR.of("Index模版文件不存在。请检查!"));
        }
        Result<String> result = FileTool.readFileToString(templateFile);
        if (result.isFailed()) {
            throw new BizException(result.errorCode());
        }
        String fileContent = result.get();
        fileContent = DefaultTemplate.fusion(fileContent, TEMPLATE_INDEX_INDEX, config.getIndex());
        templateContent = fileContent;
    }

    @Override
    public Result<Html> inject(List<Resource> resourceList, Map<PluginMeta, List<ILilypadocComponent>> componentMap) {
        String inject = DefaultTemplate.inject(templateContent, RESOURCE_INDEX, resourceList.toArray(new ILilypadocComponent[0]));
        List<PluginMeta> bodyList = new ArrayList<>();
        componentMap.forEach((k, v) -> {
            PluginAreaEnum pluginAreaEnum = k.getDomains().get(PluginDomainEnum.INDEX);
            if (PluginAreaEnum.BODY.equals(pluginAreaEnum)) {
                bodyList.add(k);
            }
        });
        try {
            inject = DefaultTemplate.inject(inject, BODY_INDEX,
                    DefaultTemplate.getSortedComponents(bodyList, componentMap).toArray(new ILilypadocComponent[0]));
        } catch (BizException e) {
            return Result.fail(e.getErrorCode());
        }
        return Result.ok(new Html().element(new Text(inject)));
    }

    @Override
    public MPath getTemplatePath(){
        HtmlConfiguration configuration = ConfigurationManager.getInstance().getConfiguration(HtmlConfiguration.class);
        MPath rootPath = configuration.getTemplatePath();
        return rootPath.appendChild(Const.INDEX_TEMPLATE_NAME);
    }
}

