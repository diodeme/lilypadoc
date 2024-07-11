package com.diode.lilypadoc.application.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "git.properties")
public class GitConfiguration {
    private boolean enable;
    private String branch = "master";
    private boolean useSsh = true;
    private String localRepoPath;
    private String remoteRepoPath;
    private String privateKeyPath;
    private String refreshCate;
    private String webHookSecret;
}
