package com.diode.lilypadoc.application.controller.resolver.git;

import com.diode.lilypadoc.application.controller.request.MdChangeRequest;
import com.diode.lilypadoc.standard.utils.ListTool;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class GitWebhookHandlerManager {
    @Resource
    private List<IGitWebhookHandler> handlerList;

    public MdChangeRequest handle(HttpServletRequest httpServletRequest){
        Optional<IGitWebhookHandler> handlerOptional = ListTool.safeArrayList(handlerList).stream().filter(e -> e.apply(httpServletRequest)).findFirst();
        if(handlerOptional.isEmpty()){
            throw new RuntimeException("git webhook接入 但是没有对应的适配器");
        }
        IGitWebhookHandler handler = handlerOptional.get();
        return handler.handle(httpServletRequest);
    }
}
