package com.diode.lilypadoc.application.controller.resolver.git.github;

import com.diode.lilypadoc.application.controller.request.MdChangeRequest;
import com.diode.lilypadoc.application.controller.resolver.git.IGitWebhookHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;

@Slf4j
@Component
public class GithubWebhookRequestHandler implements IGitWebhookHandler {
    @Override
    public MdChangeRequest handle(HttpServletRequest httpServletRequest) {
        return null;
    }

    @Override
    public boolean apply(HttpServletRequest httpServletRequest) {
        String userAgent = httpServletRequest.getHeader("User-Agent");
        return userAgent.startsWith("GitHub-Hookshot/");
    }
}
