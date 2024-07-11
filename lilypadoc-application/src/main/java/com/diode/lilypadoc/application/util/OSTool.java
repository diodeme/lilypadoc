package com.diode.lilypadoc.application.util;

import com.diode.lilypadoc.standard.common.StandardErrorCodes;
import com.diode.lilypadoc.standard.exception.BizException;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

@Slf4j
public class OSTool {

    public static boolean isWindows() {
        String os = System.getProperty("os.name").toLowerCase();
        return os.contains("win");
    }

    public static boolean isLinux() {
        String os = System.getProperty("os.name").toLowerCase();
        return os.contains("nux") || os.contains("nix") || os.contains("aix");
    }

    public static void execCommand(File workingDir, String[] command) {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.directory(workingDir);
            Process process = processBuilder.start();

            // 获取命令的输出信息
            BufferedReader stdInput = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String s;
            StringBuilder output = new StringBuilder();
            while ((s = stdInput.readLine()) != null) {
                output.append(s).append(System.lineSeparator());
            }
            // 获取命令的错误信息
            BufferedReader stdError = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            StringBuilder errorOutput = new StringBuilder();
            while ((s = stdError.readLine()) != null) {
                errorOutput.append(s).append(System.lineSeparator());
            }

            int exitVal = process.waitFor();
            // 命令执行完毕后返回的状态码
            log.info("执行命令, 返回值: {}, 标准输出: {}, 标准错误输出: {}", exitVal, output, errorOutput);
            if (exitVal != 0) {
                throw new BizException(StandardErrorCodes.BIZ_ERROR.of("执行命令失败"));
            }
        } catch (Exception e) {
            log.error("执行命令未知失败", e);
            throw new BizException(StandardErrorCodes.BIZ_ERROR.of("执行命令未知失败"));
        }
    }

    public static void main(String[] args) {
        if (isWindows()) {
            System.out.println("This is a Windows environment.");
        } else if (isLinux()) {
            System.out.println("This is a Linux environment.");
        } else {
            System.out.println("This is neither a Windows nor a Linux environment.");
        }
    }
}