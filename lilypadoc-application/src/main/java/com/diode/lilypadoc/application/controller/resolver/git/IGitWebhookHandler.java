package com.diode.lilypadoc.application.controller.resolver.git;

import com.diode.lilypadoc.application.controller.request.MdChangeRequest;

import jakarta.servlet.http.HttpServletRequest;

public interface IGitWebhookHandler {
    MdChangeRequest handle(HttpServletRequest httpServletRequest);

    boolean apply(HttpServletRequest httpServletRequest);
}
