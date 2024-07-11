package com.diode.lilypadoc.standard.domain.event;

import com.diode.lilypadoc.standard.api.ILilypadocComponent;
import com.diode.lilypadoc.standard.api.ITemplate;
import com.diode.lilypadoc.standard.api.plugin.AbstractPlugin;
import com.diode.lilypadoc.standard.domain.LilypadocContext;
import com.diode.lilypadoc.standard.domain.MPath;
import lombok.*;

import java.util.List;
import java.util.Map;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class PageSyncFinishEvent implements IEvent{
    private LilypadocContext lilypadocContext;
    private Map<AbstractPlugin, List<ILilypadocComponent>> componentMap;
    private ITemplate template;
    private ITemplate indexTemplate;
    private MPath htmlRootPath;
    private MPath htmlDocPath;
}