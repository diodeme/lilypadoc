package com.diode.lilypadoc.standard.domain.event;

import com.diode.lilypadoc.standard.domain.LilypadocContext;
import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class PageInitFinishEvent implements IEvent{
    private LilypadocContext lilypadocContext;
}
