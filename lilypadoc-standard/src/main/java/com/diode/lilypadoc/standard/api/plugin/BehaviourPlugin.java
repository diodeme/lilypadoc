package com.diode.lilypadoc.standard.api.plugin;

import com.diode.lilypadoc.standard.api.EventListener;
import com.diode.lilypadoc.standard.common.ErrorCode;
import com.diode.lilypadoc.standard.common.StandardErrorCodes;
import com.diode.lilypadoc.standard.domain.event.IEvent;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class BehaviourPlugin<T extends IEvent> extends AbstractPlugin implements EventListener<T> {

    protected abstract ErrorCode process(T event);

    @Override
    public ErrorCode onEvent(T event){
        try{
            return process(event);
        }catch (Throwable e){
            log.error("插件执行行为异常", e);
            return StandardErrorCodes.SYS_ERROR.of("插件执行行为异常");
        }
    }
}
