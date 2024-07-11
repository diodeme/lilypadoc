package com.diode.lilypadoc.application.configuration;

import com.diode.lilypadoc.application.controller.resolver.git.GitWebhookHandlerManager;
import com.diode.lilypadoc.application.controller.resolver.git.GitWebhookResolver;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Resource
    private GitWebhookHandlerManager gitWebhookHandlerManager;

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
        argumentResolvers.add(new GitWebhookResolver(gitWebhookHandlerManager));
    }
}