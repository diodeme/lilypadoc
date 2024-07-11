package com.diode.lilypadoc.core.config;

import com.diode.lilypadoc.standard.api.ITemplate;
import com.diode.lilypadoc.standard.domain.MPath;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HtmlConfiguration implements IConfiguration {
    private String pluginPath;
    private String rootPath;
    private String pluginResourceRePath = null;
    private String templateRePath = "/template";
    private String docRePath = "/docs";
    private Integer threadPoolCoreSize = 24;
    private Integer threadPoolMaxSize = 72;
    private Integer threadQueueCapacity = 1000;
    private Integer threadAliveTime = 60*1000;
    private String templateConfig;
    private ITemplate template;
    private ITemplate indexTemplate;
    private ITemplate cssTemplate;
    private boolean cssPack;
    private boolean jsPack;

    public MPath getTemplatePath() { return MPath.of(rootPath + templateRePath); }
    public MPath getDocRootPath() { return MPath.of(rootPath + docRePath); }
}
