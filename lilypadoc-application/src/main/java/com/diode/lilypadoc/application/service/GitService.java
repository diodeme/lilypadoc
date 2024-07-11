package com.diode.lilypadoc.application.service;

import com.diode.lilypadoc.application.configuration.GitConfiguration;
import com.diode.lilypadoc.standard.common.ErrorCode;
import com.diode.lilypadoc.standard.common.StandardErrorCodes;
import com.diode.lilypadoc.standard.exception.BizException;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.SshSessionFactory;
import org.eclipse.jgit.transport.SshTransport;
import org.eclipse.jgit.transport.ssh.jsch.JschConfigSessionFactory;
import org.eclipse.jgit.transport.ssh.jsch.OpenSshConfig;
import org.eclipse.jgit.util.FS;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

@Slf4j
@Service
public class GitService {

    private final GitConfiguration gitConfiguration;

    private SshSessionFactory sshSessionFactory = null;

    public GitService(GitConfiguration gitConfiguration) {
        this.gitConfiguration = gitConfiguration;
        //todo:ssh or http
        if (gitConfiguration.isUseSsh()) {
            if (gitConfiguration.getPrivateKeyPath() == null) {
                log.error("gitProperties.privateKeyPath not be null");
                throw new BizException(StandardErrorCodes.VALIDATE_ERROR.of("use git ssh, must set privateKeyPath"));
            }
            sshSessionFactory = new JschConfigSessionFactory() {
                @Override
                protected void configure(OpenSshConfig.Host hc, Session session) {
                    session.setConfig("StrictHostKeyChecking", "no");
                }

                @Override
                protected JSch createDefaultJSch(FS fs) throws JSchException {
                    JSch jsch = super.createDefaultJSch(fs);
                    jsch.addIdentity(gitConfiguration.getPrivateKeyPath());
                    return jsch;
                }
            };
        }
    }

    public ErrorCode cloneOrPullApiRepo() {
        if(!gitConfiguration.isEnable()){
            log.debug("git功能关闭");
            return StandardErrorCodes.OK.of("git功能关闭");
        }
        return this.cloneOrPullRepo(gitConfiguration.getRemoteRepoPath(), gitConfiguration.getLocalRepoPath(), gitConfiguration.getBranch());
    }

    public ErrorCode cloneOrPullRepo(String gitPath, String localDir, String branchName) {
        if(!gitConfiguration.isEnable()){
            log.debug("git功能关闭");
            return StandardErrorCodes.OK;
        }
        File rootFile = Paths.get(localDir).toFile();
        try (Git git = Git.open(rootFile)) {
            // 如果本地仓库存在，则 open 本地仓库
            // 如果本地仓库存在则拉取代码
            return checkoutAndPull(git, branchName);
        } catch (Exception e) {
            log.info("本地仓库不存在，将clone仓库, local repo:{}, remote repo:{}, branch:{}",
                    localDir, gitPath, branchName);
            try {
                FileUtils.forceDelete(rootFile);
            } catch (IOException ex) {
                log.error("删除目录失败", ex);
            }
            return this.cloneRepo(gitPath, localDir, branchName);
        }
    }

    private ErrorCode checkoutAndPull(Git git, String branch) throws Exception {
        git.fetch().setRefSpecs("refs/heads/" + branch).call();

        // 切换分支
        git.checkout().setName(branch).setCreateBranch(true).setForceRefUpdate(true).call();

        // 拉取代码
        PullResult pullResult = git.pull().setTransportConfigCallback(transport -> {
            SshTransport sshTransport = (SshTransport) transport;
            sshTransport.setSshSessionFactory(sshSessionFactory);
        }).call();

        // 检查拉取是否成功
        if (pullResult.isSuccessful()) {
            return StandardErrorCodes.OK;
        } else {
            return StandardErrorCodes.IO_ERROR.of("拉取git代码失败," + pullResult);
        }
    }

    private ErrorCode cloneRepo(String gitPath, String localDir, String branchName) {
        try {
            Git git = Git.cloneRepository()
                    .setTransportConfigCallback(transport -> {
                        SshTransport sshTransport = (SshTransport) transport;
                        sshTransport.setSshSessionFactory(sshSessionFactory);
                    })
                    .setURI(gitPath)
                    .setBranch(branchName)
                    .setDirectory(Paths.get(localDir).toFile())
                    .call();
            log.debug("Clone done!, local repo:{}, remote repo:{}, branch:{}",
                    localDir, gitPath, branchName);
            git.close();
            return StandardErrorCodes.OK;
        } catch (GitAPIException e) {
            log.error("Clone error!, local repo:" + localDir
                    + ", remote repo:" + gitPath + ", branch:" + branchName, e);

            return StandardErrorCodes.IO_ERROR.of("git克隆仓库失败");
        }
    }
}