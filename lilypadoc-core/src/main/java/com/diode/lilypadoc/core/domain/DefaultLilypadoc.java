package com.diode.lilypadoc.core.domain;

import com.diode.lilypadoc.standard.common.ErrorCodeWrapper;
import com.diode.lilypadoc.core.config.ConfigurationManager;
import com.diode.lilypadoc.core.config.HtmlConfiguration;
import com.diode.lilypadoc.core.config.MarkdownConfiguration;
import com.diode.lilypadoc.core.config.PluginConfiguration;

public class DefaultLilypadoc extends Lilypadoc{

    public DefaultLilypadoc() { super(); }

    @Override
    public void initConfig() {
        HtmlConfiguration htmlConfiguration = new HtmlConfiguration();
        MarkdownConfiguration markdownConfiguration = new MarkdownConfiguration();
        PluginConfiguration pluginConfiguration = new PluginConfiguration();
        ConfigurationManager configurationManager = ConfigurationManager.getInstance();
        configurationManager.injectConfiguration(htmlConfiguration);
        configurationManager.injectConfiguration(markdownConfiguration);
        configurationManager.injectConfiguration(pluginConfiguration);
    }

    @Override
    protected ErrorCodeWrapper customParseAll() { return new ErrorCodeWrapper(); }
}