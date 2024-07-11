package com.diode.lilypadoc.core.domain;

import com.diode.lilypadoc.core.config.ConfigurationManager;
import com.diode.lilypadoc.core.config.HtmlConfiguration;
import com.diode.lilypadoc.core.config.MarkdownConfiguration;
import com.diode.lilypadoc.core.plugin.PluginManager;
import com.diode.lilypadoc.core.support.PluginSupport;
import com.diode.lilypadoc.standard.api.ILilypadocComponent;
import com.diode.lilypadoc.standard.api.ITemplate;
import com.diode.lilypadoc.standard.api.plugin.AbstractPlugin;
import com.diode.lilypadoc.standard.common.ErrorCode;
import com.diode.lilypadoc.standard.common.Result;
import com.diode.lilypadoc.standard.common.StandardErrorCodes;
import com.diode.lilypadoc.standard.common.enums.PluginDomainEnum;
import com.diode.lilypadoc.standard.domain.LilypadocContext;
import com.diode.lilypadoc.standard.domain.MPath;
import com.diode.lilypadoc.standard.domain.PluginMeta;
import com.diode.lilypadoc.standard.domain.Resource;
import com.diode.lilypadoc.standard.domain.event.PageSyncFinishEvent;
import com.diode.lilypadoc.standard.domain.html.Html;
import com.diode.lilypadoc.standard.exception.BizException;
import com.diode.lilypadoc.standard.utils.FileTool;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public class Doc implements Page {

    /**
     * page定义开始
     */
    private final File file;

    private final MPath docRootDir;

    private final MPath htmlRootDir;

    private final MPath htmlDocRPath;

    private final MPath htmlDocRootDir;
    /**
     * @忽略此区域
     */
    private final MPath rPath;

    private final LilypadocContext lilypadocContext;

    private final Map<AbstractPlugin, List<ILilypadocComponent>> htmlComponentMap;

    private final ITemplate template;

    public Doc(File file) {
        if (file.isDirectory()) {
            throw new BizException(StandardErrorCodes.BIZ_ERROR.of("不可使用目录" + file.getName() + "来初始化page"));
        }
        this.file = file;
        MarkdownConfiguration markdownConfiguration = ConfigurationManager.getInstance()
                .getConfiguration(MarkdownConfiguration.class);
        docRootDir = MPath.of(markdownConfiguration.getRootDir());
        rPath = MPath.of(file.getPath()).remove(docRootDir);
        HtmlConfiguration configuration = ConfigurationManager.getInstance().getConfiguration(HtmlConfiguration.class);
        htmlRootDir = MPath.of(configuration.getRootPath());
        htmlDocRPath = MPath.of(configuration.getDocRePath());
        htmlDocRootDir = configuration.getDocRootPath();
        lilypadocContext = new LilypadocContext();
        htmlComponentMap = new HashMap<>();
        template = ConfigurationManager.getInstance().getConfiguration(HtmlConfiguration.class).getTemplate();
    }

    public Result<File> parseAndSync() {
        try {
            Result<Html> parse = parse();
            if (parse.isFailed()) {
                return Result.fail(parse.errorCode());
            }
            Html html = parse.get();
            Result<File> syncResult = sync(html);
            if (syncResult.isFailed()) {
                return Result.fail(syncResult.errorCode());
            }
            PageSyncFinishEvent pageSyncFinishEvent = PageSyncFinishEvent.builder().lilypadocContext(lilypadocContext)
                    .componentMap(htmlComponentMap).template(template)
                    .indexTemplate(ConfigurationManager.getInstance().getConfiguration(HtmlConfiguration.class).getIndexTemplate())
                    .htmlRootPath(htmlRootDir).htmlDocPath(htmlDocRootDir).build();
            ErrorCode sendErrorCode = PluginManager.sendEvent(pageSyncFinishEvent);
            if (StandardErrorCodes.OK.notEquals(sendErrorCode)) {
                ErrorCode delete = delete();
                if (StandardErrorCodes.OK.notEquals(delete)) {
                    return Result.fail(StandardErrorCodes.SYS_ERROR.of("发送同步文件信息失败后删除同步文件时发生异常,异常原因:" + delete.message()));
                }
                return Result.fail(sendErrorCode);
            }
            return syncResult;
        } catch (Exception e) {
            String name = FileTool.removeExtension(file.getName());
            log.error("生成页面:{}, 失败. 异常信息如下:{}", name, e);
            return Result.fail(StandardErrorCodes.UNKNOWN_ERROR.of("页面" + name + "生成时发生未知异常"));
        }
    }

    public Result<Html> parse() {
        log.info("开始解析page, {}", rPath);
        List<Resource> resourceList = new ArrayList<>();
        MarkdownConfiguration markdownConfiguration = ConfigurationManager.getInstance()
                .getConfiguration(MarkdownConfiguration.class);
        Integer categoryDepth = markdownConfiguration.getCategoryDepth();
        Result<File> lastCategoryDirResult = FileTool.getCategoryDir(file, docRootDir, categoryDepth);
        if (lastCategoryDirResult.isFailed()) {
            return Result.fail(StandardErrorCodes.BIZ_ERROR.of("doc: " + file.getName() + "发生错误: " + lastCategoryDirResult.message()));
        }
        File lastCategoryDir = lastCategoryDirResult.get();
        lilypadocContext.setDocRootDir(docRootDir);
        lilypadocContext.setLastCategory(MPath.of(lastCategoryDir.getPath()));
        lilypadocContext.setDoc(file);
        lilypadocContext.setDocRPath(rPath);
        lilypadocContext.setCategoryDepth(categoryDepth);
        lilypadocContext.setHtmlDocRPath(htmlDocRPath);
        Result<List<AbstractPlugin>> result = PluginManager.getSortedPlugins();
        if (result.isFailed()) {
            return Result.fail(result.errorCode());
        }
        List<AbstractPlugin> plugins = result.get();
        //执行插件
        ErrorCode errorCode = PluginSupport.executePlugin(plugins, lilypadocContext, resourceList, htmlComponentMap, PluginDomainEnum.DOC);
        if (StandardErrorCodes.OK.notEquals(errorCode)) {
            return Result.fail(errorCode);
        }
        Map<PluginMeta, List<ILilypadocComponent>> pluginMetaListMap = htmlComponentMap.entrySet().stream()
                .collect(Collectors.toMap(e -> e.getKey().meta(), Map.Entry::getValue, (s1, s2) -> s2));
        //按区域和顺序插入htmlComponent到resource
        return template.inject(resourceList, pluginMetaListMap);
    }

    public Result<File> sync(Html html) {
        String path = htmlDocRootDir.appendChild(MPath.ofHtml(rPath)).toString();
        log.info("开始同步page: {}", path);
        File file = null;
        try {
            file = new File(path);
            String content = html.parse();
            FileUtils.writeStringToFile(file, content, StandardCharsets.UTF_8);
            return Result.ok(file);
        } catch (IOException e) {
            log.error("同步page时发生IO异常, file:{}", file, e);
            return Result.fail(StandardErrorCodes.IO_ERROR.of("同步page:" + rPath + "发生io异常"));
        }
    }

    public ErrorCode delete() {
        String path = htmlDocRootDir.appendChild(MPath.ofHtml(rPath)).toString();
        log.info("开始删除page, {}", path);
        return FileTool.deleteIfExist(new File(path));
    }
}