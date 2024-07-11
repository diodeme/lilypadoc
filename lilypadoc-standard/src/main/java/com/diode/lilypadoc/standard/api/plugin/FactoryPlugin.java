package com.diode.lilypadoc.standard.api.plugin;

import com.diode.lilypadoc.standard.api.ILilypadocComponent;
import com.diode.lilypadoc.standard.common.Result;
import com.diode.lilypadoc.standard.common.StandardErrorCodes;
import com.diode.lilypadoc.standard.domain.LilypadocContext;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

@Slf4j
public abstract class FactoryPlugin extends AbstractPlugin{

    public Result<List<ILilypadocComponent>> genComponent(LilypadocContext lilypadocContext, Map<String, List<ILilypadocComponent>> dependencies){
        try {
            return process(lilypadocContext, dependencies);
        }catch (Throwable e) {
            log.error("插件生成component异常", e);
            return Result.fail(StandardErrorCodes.SYS_ERROR.of("插件生成component异常"));
        }
    }

    protected abstract Result<List<ILilypadocComponent>> process(LilypadocContext lilypadocContext, Map<String, List<ILilypadocComponent>> dependencies);
}
