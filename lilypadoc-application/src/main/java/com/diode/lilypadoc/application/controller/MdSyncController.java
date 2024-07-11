package com.diode.lilypadoc.application.controller;

import com.diode.lilypadoc.application.controller.request.MdChangeRequest;
import com.diode.lilypadoc.application.service.MdChangeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;

@Slf4j
@ConditionalOnProperty(name = "git.properties.enable", havingValue = "true")
@RestController("/md")
@RequestMapping("/md")
public class MdSyncController {

    @Resource
    private MdChangeService mdChangeService;

    @PostMapping(value = "/change")
    public void change(MdChangeRequest request) {
        log.info("mdChange收到请求:{}", request);
        try {
            mdChangeService.parseAndSync(request.toEntity());
        } catch (Exception e) {
            log.error("转换并同步md文件出现异常, request:{}", request, e);
        }
    }

}