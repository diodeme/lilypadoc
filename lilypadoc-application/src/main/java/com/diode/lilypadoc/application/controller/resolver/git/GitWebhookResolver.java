package com.diode.lilypadoc.application.controller.resolver.git;

import com.diode.lilypadoc.application.controller.request.MdChangeRequest;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import jakarta.servlet.http.HttpServletRequest;

@Slf4j
@AllArgsConstructor
public class GitWebhookResolver implements HandlerMethodArgumentResolver {

    private final GitWebhookHandlerManager gitWebhookHandlerManager;

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return MdChangeRequest.class.isAssignableFrom(parameter.getParameterType());
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
        try {
            final Object nativeRequest = webRequest.getNativeRequest();
            if (BooleanUtils.isFalse(nativeRequest instanceof HttpServletRequest)) {
                log.error("request attributes is not found in the request");
                throw new RuntimeException("request attributes is not found in the request");
            }
            assert nativeRequest instanceof HttpServletRequest;
            final HttpServletRequest httpServletRequest = (HttpServletRequest) nativeRequest;
            return gitWebhookHandlerManager.handle(httpServletRequest);
        }catch (Exception e){
            log.error("转换git hook异常", e);
            throw new RuntimeException("解析git请求异常");
        }
    }
}