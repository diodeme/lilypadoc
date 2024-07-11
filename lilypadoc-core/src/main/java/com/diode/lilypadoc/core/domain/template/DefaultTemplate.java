package com.diode.lilypadoc.core.domain.template;

import com.diode.lilypadoc.standard.api.ILilypadocComponent;
import com.diode.lilypadoc.standard.api.ILilypadocFusionComponent;
import com.diode.lilypadoc.standard.common.Result;
import com.diode.lilypadoc.standard.common.StandardErrorCodes;
import com.diode.lilypadoc.standard.common.enums.PluginAreaEnum;
import com.diode.lilypadoc.standard.common.enums.PluginDomainEnum;
import com.diode.lilypadoc.standard.domain.MPath;
import com.diode.lilypadoc.standard.domain.PluginMeta;
import com.diode.lilypadoc.standard.domain.Resource;
import com.diode.lilypadoc.standard.domain.html.Html;
import com.diode.lilypadoc.standard.domain.html.IHtmlElement;
import com.diode.lilypadoc.standard.domain.html.Text;
import com.diode.lilypadoc.standard.exception.BizException;
import com.diode.lilypadoc.standard.utils.FileTool;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;

import java.io.File;
import java.lang.reflect.Field;
import java.util.*;

public class DefaultTemplate extends AbstractTemplate {

    private static final String TEMPLATE_FILE_NAME = "template.html";
    private final String templateContent;
    protected static final String RESOURCE_INDEX = "<!--@RESOURCE-->";
    protected static final String FINAL_INDEX = "<!--@END-->";
    protected static final String HEAD_INDEX = "<!--@HEADER AREA-->";
    protected static final String LEFT_ASIDE_INDEX = "<!--@LEFT ASIDE AREA-->";
    protected static final String RIGHT_ASIDE_AREA = "<!--@RIGHT ASIDE AREA-->";
    protected static final String BODY_INDEX = "<!--@BODY AREA-->";
    protected static final String FOOT_INDEX = "<!--@FOOTER AREA-->";
    protected static final String TEMPLATE_HEADER_INDEX = "<!--@TEMPLATE HEADER AREA-->";
    protected static final String TEMPLATE_FOOTER_INDEX = "<!--@TEMPLATE FOOTER AREA-->";
    protected static final String TEMPLATE_THEME_INDEX = "<!--@TEMPLATE THEME AREA-->";

    public DefaultTemplate(DefaultTemplateConfig config) {
        super();
        File templateFile = new File(getTemplatePath().toString());
        if (!templateFile.exists()) {
            throw new BizException(StandardErrorCodes.BIZ_ERROR.of("模版文件"+getTemplatePath()+"不存在。请检查!"));
        }
        Result<String> result = FileTool.readFileToString(templateFile);
        if (result.isFailed()) {
            throw new BizException(result.errorCode());
        }
        String fileContent = result.get();
        fileContent = fusion(fileContent, TEMPLATE_HEADER_INDEX, config.getHeader());
        fileContent = fusion(fileContent, TEMPLATE_FOOTER_INDEX, config.getFooter());
        fileContent = inject(fileContent, TEMPLATE_THEME_INDEX, config.getTheme());
        templateContent = fileContent;
    }

    @Override
    public Result<Html> inject(List<Resource> resourceList, Map<PluginMeta, List<ILilypadocComponent>> componentMap) {
        String inject = inject(templateContent, RESOURCE_INDEX, resourceList.toArray(new ILilypadocComponent[0]));
        List<PluginMeta> headList = new ArrayList<>();
        List<PluginMeta> leftAsideList = new ArrayList<>();
        List<PluginMeta> rightAsideList = new ArrayList<>();
        List<PluginMeta> bodyList = new ArrayList<>();
        List<PluginMeta> footList = new ArrayList<>();
        componentMap.forEach((k, v) -> {
            PluginAreaEnum pluginAreaEnum = k.getDomains().get(PluginDomainEnum.DOC);
            if (PluginAreaEnum.HEAD.equals(pluginAreaEnum)) {
                headList.add(k);
            }
            if (PluginAreaEnum.LEFT_ASIDE.equals(pluginAreaEnum)) {
                leftAsideList.add(k);
            }
            if (PluginAreaEnum.RIGHT_ASIDE.equals(pluginAreaEnum)) {
                rightAsideList.add(k);
            }
            if (PluginAreaEnum.BODY.equals(pluginAreaEnum)) {
                bodyList.add(k);
            }
            if (PluginAreaEnum.FOOT.equals(pluginAreaEnum)) {
                footList.add(k);
            }
        });
        try {
            inject = inject(inject, HEAD_INDEX, getSortedComponents(headList, componentMap).toArray(new ILilypadocComponent[0]));
            inject = inject(inject, LEFT_ASIDE_INDEX, getSortedComponents(leftAsideList, componentMap).toArray(new ILilypadocComponent[0]));
            inject = inject(inject, RIGHT_ASIDE_AREA, getSortedComponents(rightAsideList, componentMap).toArray(new ILilypadocComponent[0]));
            inject = inject(inject, BODY_INDEX, getSortedComponents(bodyList, componentMap).toArray(new ILilypadocComponent[0]));
            inject = inject(inject, FOOT_INDEX, getSortedComponents(footList, componentMap).toArray(new ILilypadocComponent[0]));
        } catch (BizException e) {
            return Result.fail(e.getErrorCode());
        }
        return Result.ok(new Html().element(new Text(inject)));
    }

    @Override
    public MPath getTemplatePath() { return templatePath.appendChild(TEMPLATE_FILE_NAME); }

    protected static List<ILilypadocComponent> getSortedComponents(List<PluginMeta> pluginMetaList,
                                                                Map<PluginMeta, List<ILilypadocComponent>> pluginMap) {
        List<PluginMeta> safePluginMetaList = ListUtils.emptyIfNull(pluginMetaList);
        safePluginMetaList.sort(Comparator.comparingInt(PluginMeta::getOrder));
        List<ILilypadocComponent> res = new ArrayList<>();
        for (PluginMeta pluginMeta : safePluginMetaList) {
            res.addAll(CollectionUtils.emptyIfNull(pluginMap.get(pluginMeta)));
        }
        return res;
    }

    protected static String inject(String template, String tag, ILilypadocComponent... components) {
        if (Objects.isNull(components) || components.length == 0) {
            return template;
        }
        int pos = template.indexOf(tag) + tag.length();
        int endPos = template.indexOf(tag + FINAL_INDEX);
        if (pos < 0 || endPos < 0){
            throw new BizException(StandardErrorCodes.BIZ_ERROR.of("找不到标签:" + tag+"或结束符"));
        }
        StringBuilder sb = new StringBuilder();
        for (ILilypadocComponent component : components) {
            sb.append(component.parse().parse());
        }
        return template.substring(0, pos) + sb + template.substring(endPos);
    }

    public static String fusion(String template, String tag, ILilypadocFusionComponent component) {
        if (Objects.isNull(component)) {
            return template;
        }
        int pos = template.indexOf(tag) + tag.length();
        int endPos = template.indexOf(tag + FINAL_INDEX);
        if (pos < 0 || endPos < 0) {
            throw new BizException(StandardErrorCodes.BIZ_ERROR.of("找不到标签:" + tag+"或结束符"));
        }
        String content = template.substring(pos, endPos);
        return template.substring(0, pos) + fusion(content, component, "") + template.substring(endPos);
    }

    private static String fusion(String content, ILilypadocFusionComponent component, String prefix) {
        Class<?> cls = component.getClass();

        Field[] fields = cls.getDeclaredFields();

        for (Field field : fields) {
            field.setAccessible(true);

            String fieldName = field.getName();

            Object value;
            try {
                value = field.get(component);
            } catch (IllegalAccessException e) {
                throw new BizException(StandardErrorCodes.BIZ_ERROR.of("字段"+ field +"进行fusion时出现反射错误"));
            }
            if (Objects.isNull(value)) {
                continue;
            }

            if (value instanceof String) {
                String fusionTag = ILilypadocFusionComponent.getFusionTag(prefix, fieldName);
                int pos = content.indexOf(fusionTag);
                if (pos < 0) {
                    continue;
                }
                int endPos = pos + fusionTag.length();
                content = content.substring(0, pos) + value + content.substring(endPos);
            }

            if (value instanceof IHtmlElement) {
                String fusionTag = ILilypadocFusionComponent.getFusionTag(prefix, fieldName);
                int pos = content.indexOf(fusionTag);
                if (pos < 0) {
                    continue;
                }
                int endPos = pos + fusionTag.length();
                IHtmlElement element = (IHtmlElement) value;
                content = content.substring(0, pos) + element.parse() + content.substring(endPos);
            }

            if (value instanceof ILilypadocFusionComponent) {
                ILilypadocFusionComponent fusionComponent = (ILilypadocFusionComponent) value;
                content = fusion(content, fusionComponent, fieldName);
            }
        }

        return content;
    }

    @Override
    public Html parse() {
        return new Html().element(new Text(templateContent));
    }

}
