package com.diode.lilypadoc.standard.domain;

import com.diode.lilypadoc.standard.api.ILilypadocComponent;
import com.diode.lilypadoc.standard.api.plugin.InitContext;
import com.diode.lilypadoc.standard.common.ErrorCode;
import com.diode.lilypadoc.standard.common.Result;
import com.diode.lilypadoc.standard.common.StandardErrorCodes;
import com.diode.lilypadoc.standard.common.enums.ResourceEnum;
import com.diode.lilypadoc.standard.domain.html.Html;
import com.diode.lilypadoc.standard.domain.html.Tag;
import com.diode.lilypadoc.standard.utils.FileTool;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

import static com.diode.lilypadoc.standard.common.Const.DEFAULT_PLUGIN_RESOURCE_SYNC_PATH;

@Slf4j
public class Resource implements ILilypadocComponent {
    @Getter
    private final String pluginName;

    private final Map<ResourceEnum, JarFile> resourceMap;
    private final Map<ResourceEnum, List<File>> syncResourceMap;

    private boolean hasSync;

    @Getter
    private String customPath = DEFAULT_PLUGIN_RESOURCE_SYNC_PATH;

    private final URLClassLoader urlClassLoader;
    private final InitContext initContext;

    public Resource(String pluginName, URLClassLoader urlClassLoader, InitContext initContext) {
        this.pluginName = pluginName;
        this.resourceMap = new HashMap<>();
        this.urlClassLoader = urlClassLoader;
        this.syncResourceMap = new HashMap<>();
        this.initContext = initContext;
    }

    public boolean isEmpty() {
        return resourceMap.isEmpty();
    }

    public ErrorCode load() {
        Result<Optional<JarFile>> css = load(ResourceEnum.CSS.getName());
        if (css.isFailed()) {
            return css.errorCode();
        }
        Result<Optional<JarFile>> js = load(ResourceEnum.JS.getName());
        if (js.isFailed()) {
            return js.errorCode();
        }
        Result<Optional<JarFile>> html = load(ResourceEnum.HTML.getName());
        if (html.isFailed()) {
            return html.errorCode();
        }
        Result<Optional<JarFile>> img = load(ResourceEnum.IMG.getName());
        if (img.isFailed()) {
            return img.errorCode();
        }
        putResource(ResourceEnum.CSS, css.get());
        putResource(ResourceEnum.JS, js.get());
        putResource(ResourceEnum.HTML, html.get());
        putResource(ResourceEnum.IMG, img.get());
        return StandardErrorCodes.OK;
    }

    private Result<Optional<JarFile>> load(String dir) {
        URL resource = urlClassLoader.findResource(dir);
        if (Objects.isNull(resource)) {
            return Result.ok(Optional.empty());
        }
        Result<JarFile> result = FileTool.getJarFileFromUrl(resource);
        if (result.isFailed()) {
            return Result.fail(result.errorCode());
        }
        return Result.ok(Optional.of(result.get()));
    }

    @Override
    public Html parse() {
        Html html = new Html();
        String path = hasSync ? this.customPath : DEFAULT_PLUGIN_RESOURCE_SYNC_PATH;
        //如果静态资源打包，则不在此处注入，在初始化网站时统一注入
        if (!initContext.isCssPack()) {
            List<File> cssFileList = getSyncResource(ResourceEnum.CSS);
            if (Objects.nonNull(cssFileList)) {
                for (File file : cssFileList) {
                    Tag tag = new Tag("link");
                    tag.property("rel", "stylesheet").property("href", path + "/" + pluginName + "/" + ResourceEnum.CSS.getName() + "/" + file.getName());
                    html.element(tag);
                }
            }
        }
        // Check if the context is for JS and if so, add script tags
        if (!initContext.isJsPack()) {
            List<File> jsFileList = getSyncResource(ResourceEnum.JS);
            if (Objects.nonNull(jsFileList)) {
                for (File file : jsFileList) {
                    Tag tag = new Tag("script");
                    tag.property("src", path + "/" + pluginName + "/" + ResourceEnum.JS.getName() + "/" + file.getName());
                    html.element(tag);
                }
            }
        }
        return html;
    }

    public ErrorCode sync(String rootDir) {
        return sync(rootDir, null);
    }

    public ErrorCode sync(String rootDir, String customPath) {
        if (Objects.nonNull(customPath)) {
            this.customPath = customPath;
        }
        String path = this.customPath;
        String destinationPath = rootDir + path + "/" + pluginName;
        for (Map.Entry<ResourceEnum, JarFile> entry : resourceMap.entrySet()) {
            JarFile jarFile = entry.getValue();
            ResourceEnum resourceEnum = entry.getKey();
            if (Objects.isNull(jarFile)) {
                continue;
            }
            Result<List<File>> syncFileResult = FileTool.copyJarFile(jarFile, resourceEnum.getName(), destinationPath, true);
            if (syncFileResult.isFailed()) {
                return syncFileResult.errorCode();
            }
            List<File> fileList = syncFileResult.get();
            if (Objects.nonNull(fileList) && !fileList.isEmpty())
                syncResourceMap.put(resourceEnum, fileList);
        }
        this.hasSync = true;
        return StandardErrorCodes.OK;
    }

    public ErrorCode delete() {
        if (!hasSync) {
            log.warn("插件: {} resource还未同步, 无需删除", pluginName);
            return StandardErrorCodes.OK;
        }
        List<File> syncFileList = syncResourceMap.values().stream().flatMap(List::stream).collect(Collectors.toList());
        List<File> failFileList = new ArrayList<>();
        for (File file : syncFileList) {
            ErrorCode errorCode = FileTool.deleteIfExist(file);
            if (StandardErrorCodes.OK.notEquals(errorCode)) {
                failFileList.add(file);
            }
        }
        if (!failFileList.isEmpty()) {
            return StandardErrorCodes.BIZ_ERROR.of("删除resource失败, 失败列表:" + failFileList.stream().map(File::getName).collect(Collectors.toList()));
        }
        return StandardErrorCodes.OK;
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private void putResource(ResourceEnum k, Optional<JarFile> v) {
        if (Objects.isNull(v) || !v.isPresent()) {
            return;
        }
        resourceMap.put(k, v.get());
    }

    public List<File> getSyncResource(ResourceEnum k) {
        return syncResourceMap.get(k);
    }

}
