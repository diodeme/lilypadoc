package com.diode.lilypadoc.application.controller.resolver.git.gitee;

import com.diode.lilypadoc.application.configuration.GitConfiguration;
import com.diode.lilypadoc.application.controller.request.MdChangeRequest;
import com.diode.lilypadoc.application.controller.resolver.git.IGitWebhookHandler;
import com.diode.lilypadoc.standard.utils.JsonTool;
import jakarta.annotation.Resource;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import javax.crypto.Mac;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Component;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Objects;

@Slf4j
@Component
public class GiteeWebhookRequestHandler implements IGitWebhookHandler {

    @Resource
    private GitConfiguration gitConfiguration;

    public MdChangeRequest handle(HttpServletRequest httpServletRequest){
        String giteeToken = httpServletRequest.getHeader("X-Gitee-Token");
        String giteeTimestamp = httpServletRequest.getHeader("X-Gitee-Timestamp");
        if (!validateToken(giteeToken, giteeTimestamp)) {
            log.info("gitee 验签失败 token:{}, timestamp:{}", giteeToken, giteeTimestamp);
            throw new RuntimeException("gitee 验签失败");
        }
        try(final ServletInputStream is = httpServletRequest.getInputStream()) {
            final String request = IOUtils.toString(is, StandardCharsets.UTF_8);
            com.diode.lilypadoc.application.controller.resolver.git.gitee.GiteeWebhookRequest giteeWebhookRequest = JsonTool.fromJson(request, com.diode.lilypadoc.application.controller.resolver.git.gitee.GiteeWebhookRequest.class);
            if (Objects.isNull(giteeWebhookRequest)) {
                throw new RuntimeException("反序列化gitee的webhook请求失败, request:" + request);
            }
            return giteeWebhookRequest.convertToMdChangeRequest();
        } catch (Exception e) {
            log.error("can't get real request request from request body", e);
            throw new RuntimeException("can't get real request request from request body", e);
        }
    }

    private boolean validateToken(String giteeToken, String giteeTimestamp) {
        String secret = gitConfiguration.getWebHookSecret();
        try {
            String stringToSign = giteeTimestamp + "\n" + secret;
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] signData = mac.doFinal(stringToSign.getBytes(StandardCharsets.UTF_8));
            String token = new String(Base64.getEncoder().encode(signData));
            return Objects.equals(giteeToken, token);
        } catch (Exception e) {
            log.error("gitee webhook 验签时出现异常", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean apply(HttpServletRequest httpServletRequest) {
        String userAgent = httpServletRequest.getHeader("User-Agent");
        return "git-oschina-hook".equals(userAgent);
    }
}
