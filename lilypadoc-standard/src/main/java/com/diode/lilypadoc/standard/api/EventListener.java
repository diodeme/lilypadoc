package com.diode.lilypadoc.standard.api;

import com.diode.lilypadoc.standard.common.ErrorCode;
import com.diode.lilypadoc.standard.domain.event.IEvent;

public interface EventListener <T extends IEvent>{

    ErrorCode onEvent(T event);
}
