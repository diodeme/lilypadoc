package com.diode.lilypadoc.core.domain.template;

import com.diode.lilypadoc.standard.api.ILilypadocComponent;
import com.diode.lilypadoc.standard.api.ITemplate;
import com.diode.lilypadoc.standard.domain.MPath;
import com.diode.lilypadoc.core.config.ConfigurationManager;
import com.diode.lilypadoc.core.config.HtmlConfiguration;

public abstract class AbstractTemplate implements ILilypadocComponent, ITemplate {
    protected final MPath templatePath;

    public AbstractTemplate(){
        HtmlConfiguration htmlConfiguration = ConfigurationManager.getInstance().getConfiguration(HtmlConfiguration.class);
        templatePath = htmlConfiguration.getTemplatePath();
    }
}
