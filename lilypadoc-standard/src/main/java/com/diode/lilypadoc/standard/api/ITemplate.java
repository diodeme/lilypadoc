package com.diode.lilypadoc.standard.api;

import com.diode.lilypadoc.standard.common.Result;
import com.diode.lilypadoc.standard.domain.MPath;
import com.diode.lilypadoc.standard.domain.PluginMeta;
import com.diode.lilypadoc.standard.domain.Resource;
import com.diode.lilypadoc.standard.domain.html.Html;

import java.util.List;
import java.util.Map;

public interface ITemplate {
    Result<Html> inject(List<Resource> resourceList, Map<PluginMeta, List<ILilypadocComponent>> componentMap);

    MPath getTemplatePath();
}
