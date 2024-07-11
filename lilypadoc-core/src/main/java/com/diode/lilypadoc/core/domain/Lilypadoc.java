package com.diode.lilypadoc.core.domain;

import com.diode.lilypadoc.standard.common.ErrorCode;
import com.diode.lilypadoc.standard.common.ErrorCodeWrapper;
import com.diode.lilypadoc.standard.common.Result;
import com.diode.lilypadoc.standard.common.StandardErrorCodes;
import com.diode.lilypadoc.standard.exception.BizException;
import com.diode.lilypadoc.core.config.ConfigurationManager;
import com.diode.lilypadoc.core.config.HtmlConfiguration;
import com.diode.lilypadoc.core.config.MarkdownConfiguration;
import com.diode.lilypadoc.core.plugin.PluginManager;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.Objects;
import java.util.concurrent.*;

@Slf4j
public abstract class Lilypadoc {
    private final ThreadPoolExecutor threadPoolExecutor;

    public Lilypadoc() {
        initConfig();
        HtmlConfiguration configuration = ConfigurationManager.getInstance().getConfiguration(HtmlConfiguration.class);
        BlockingQueue<Runnable> workQueue = new ArrayBlockingQueue<>(configuration.getThreadQueueCapacity());
        ThreadFactory threadFactory = Executors.defaultThreadFactory();
        threadPoolExecutor = new ThreadPoolExecutor(configuration.getThreadPoolCoreSize(),
                configuration.getThreadPoolMaxSize(),
                configuration.getThreadAliveTime(), TimeUnit.MILLISECONDS, workQueue, threadFactory,
                new ThreadPoolExecutor.CallerRunsPolicy());
        ErrorCode errorCode = PluginManager.loadAllPlugins();
        if (StandardErrorCodes.OK.notEquals(errorCode)) {
            throw new BizException(new ErrorCode(errorCode.code(), "lilypadoc加载插件失败" + errorCode.message()));
        }
    }

    protected abstract void initConfig();

    protected abstract ErrorCodeWrapper customParseAll();

    /**
     * 初始化全部
     */
    public ErrorCodeWrapper parseAll() {
        ConfigurationManager configurationManager = ConfigurationManager.getInstance();
        MarkdownConfiguration markdownConfiguration = configurationManager.getConfiguration(
                MarkdownConfiguration.class);
        //同步index
        ErrorCodeWrapper indexErrorCodeWrapper = parseIndex();
        File file = new File(markdownConfiguration.getRootDir());
        ErrorCodeWrapper docErrorCodeWrapper = parseDoc(file);
        ErrorCodeWrapper customErrorCodeWrapper = customParseAll();
        return indexErrorCodeWrapper.union(docErrorCodeWrapper.union(customErrorCodeWrapper));
    }

    public ErrorCodeWrapper parseIndex() {
        ErrorCodeWrapper errorCodeWrapper = new ErrorCodeWrapper();
        Index index = new Index();
        Result<File> result = index.parseAndSync();
        if (result.isFailed()) {
            return errorCodeWrapper.add(result.errorCode());
        }
        return errorCodeWrapper;
    }

    /**
     * 解析指定目录
     */
    public ErrorCodeWrapper parseDoc(File file) {
        ErrorCodeWrapper errorCodeWrapper = new ErrorCodeWrapper();
        if (!file.exists()) {
            return errorCodeWrapper.add(StandardErrorCodes.BIZ_ERROR.of("文件:"+ file.getPath() + "不存在"));
        }
        parseDoc(file, errorCodeWrapper);
        return errorCodeWrapper;
    }

    private void parseDoc(File file, ErrorCodeWrapper errorCodeWrapper) {
        if (file.isFile()) {
            if (!file.getName().endsWith(".md")) {
                return;
            }
            //TODO future等待完成
            threadPoolExecutor.submit(() -> {
                Doc doc = new Doc(file);
                Result<File> result = doc.parseAndSync();
                if (result.isFailed()) {
                    log.error("文件：{}解析时发生异常，errorcode:{}", file.getName(), result.errorCode());
                    errorCodeWrapper.add(result.errorCode());
                }
            });
            return;
        }
        File[] files = file.listFiles((dir, name) -> !name.endsWith(".git"));
        if (Objects.isNull(files)) {
            return;
        }
        for (File child : files) {
            parseDoc(child, errorCodeWrapper);
        }
    }

    public void destroy() { threadPoolExecutor.shutdown(); }
}