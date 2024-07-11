package com.diode.lilypadoc.application.core;

import com.diode.lilypadoc.application.util.StringTool;
import com.diode.lilypadoc.core.config.ApplicationConfiguration;
import com.diode.lilypadoc.core.config.ConfigurationManager;
import com.diode.lilypadoc.core.config.HtmlConfiguration;
import com.diode.lilypadoc.core.config.IConfiguration;
import com.diode.lilypadoc.core.domain.Lilypadoc;
import com.diode.lilypadoc.core.domain.template.DefaultIndexTemplate;
import com.diode.lilypadoc.core.domain.template.DefaultTemplate;
import com.diode.lilypadoc.core.domain.template.DefaultTemplateConfig;
import com.diode.lilypadoc.standard.common.ErrorCode;
import com.diode.lilypadoc.standard.common.ErrorCodeWrapper;
import com.diode.lilypadoc.standard.common.Result;
import com.diode.lilypadoc.standard.common.StandardErrorCodes;
import com.diode.lilypadoc.standard.domain.MPath;
import com.diode.lilypadoc.standard.exception.BizException;
import com.diode.lilypadoc.standard.utils.FileTool;
import com.diode.lilypadoc.standard.utils.JsonTool;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.env.Environment;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.stereotype.Component;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Set;

@Slf4j
@Component
public class SpringLilypadoc implements DisposableBean {

    private static final String BASE_CONFIG_PACKAGE = "com.diode.lilypadoc.core.config";
    private static final String PROPERTY_NAME_TEMPLATE = "lilypadoc.spring.%s.%s";

    private final Lilypadoc lilypadoc;

    public SpringLilypadoc(Environment environment) {
        this.lilypadoc = new Lilypadoc() {

            @Override
            protected void initConfig() {
                ConfigurationManager instance = ConfigurationManager.getInstance();
                //处理底层框架的配置类
                ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
                scanner.addIncludeFilter(new AssignableTypeFilter(IConfiguration.class));
                Set<BeanDefinition> candidateComponents = scanner.findCandidateComponents(BASE_CONFIG_PACKAGE);

                try {
                    for (BeanDefinition beanDefinition : candidateComponents) {
                        Class<?> clazz = Class.forName(beanDefinition.getBeanClassName());
                        if (!IConfiguration.class.isAssignableFrom(clazz)) {
                            continue;
                        }

                        IConfiguration configuration = (IConfiguration) clazz.getDeclaredConstructor().newInstance();
                        Field[] declaredFields = clazz.getDeclaredFields();

                        for (Field field : declaredFields) {
                            int modifiers = field.getModifiers();
                            if (Modifier.isFinal(modifiers)) {
                                continue;
                            }

                            String property = environment.getProperty(
                                    String.format(PROPERTY_NAME_TEMPLATE, clazz.getSimpleName(), field.getName()));
                            if (StringUtils.isBlank(property)) {
                                continue;
                            }

                            Object value = StringTool.transformValue(field.getType(), property);
                            field.setAccessible(true);
                            field.set(configuration, value);
                        }

                        instance.injectConfiguration(configuration);
                    }
                } catch (Exception e) {
                    log.error("SpringLilypadoc 初始化配置失败", e);
                    throw new BizException(StandardErrorCodes.BIZ_ERROR.of("SpringLilypadoc 初始化配置失败"));
                }
                HtmlConfiguration configuration = instance.getConfiguration(HtmlConfiguration.class);
                ApplicationConfiguration applicationConfiguration = instance.getConfiguration(ApplicationConfiguration.class);
                String path = applicationConfiguration.getCustomConfigAbPath() + "/" + configuration.getTemplateConfig();
                Result<String> result = FileTool.readFileToString(
                        new File(path));
                if (result.isFailed()) {
                    log.error("SpringLilypadoc 获取模板配置失败{}, path:{}", result.errorCode(), path);
                    throw new BizException(StandardErrorCodes.BIZ_ERROR.of("SpringLilypadoc 获取模板配置失败"));
                }

                String configContent = result.get();
                DefaultTemplateConfig defaultTemplateConfig = JsonTool.fromJson(configContent, DefaultTemplateConfig.class);

                //初始化模板
                configuration.setTemplate(new DefaultTemplate(defaultTemplateConfig));
                configuration.setIndexTemplate(new DefaultIndexTemplate(defaultTemplateConfig));
                configuration.setCssTemplate(new TailwindcssTemplate());
            }

            @Override
            protected ErrorCodeWrapper customParseAll() {
                HtmlConfiguration configuration = ConfigurationManager.getInstance()
                        .getConfiguration(HtmlConfiguration.class);
                ErrorCodeWrapper errorCodeWrapper = new ErrorCodeWrapper();

                if (configuration.isCssPack()) {
                    TailwindcssPack cssPack = new TailwindcssPack();
                    Result<File> result = cssPack.parseAndSync();
                    if (result.isFailed()) {
                        return errorCodeWrapper.add(result.errorCode());
                    }
                }

                TemplateAfterFusion templateAfterFusion = new TemplateAfterFusion();
                Result<File> result = templateAfterFusion.parseAndSync();
                if (result.isFailed()) {
                    return errorCodeWrapper.add(result.errorCode());
                }

                return errorCodeWrapper;
            }
        };
    }

    public ErrorCode deleteAll(){
        HtmlConfiguration configuration = ConfigurationManager.getInstance().getConfiguration(HtmlConfiguration.class);
        MPath docRootPath = configuration.getDocRootPath();
        return FileTool.delete(new File(docRootPath.toString()));
    }

    public ErrorCodeWrapper parseAll() {
        return lilypadoc.parseAll();
    }

    public ErrorCodeWrapper parse(File file) {
        return lilypadoc.parseDoc(file);
    }

    @Override
    public void destroy() {
        lilypadoc.destroy();
    }
}