package com.diode.lilypadoc.standard.api.plugin;

import com.diode.lilypadoc.standard.api.BaseCustomConfig;
import com.diode.lilypadoc.standard.common.ErrorCode;
import com.diode.lilypadoc.standard.common.Result;
import com.diode.lilypadoc.standard.common.StandardErrorCodes;
import com.diode.lilypadoc.standard.common.enums.PluginStatusEnum;
import com.diode.lilypadoc.standard.domain.PluginMeta;
import com.diode.lilypadoc.standard.domain.Resource;
import com.diode.lilypadoc.standard.exception.BizException;
import com.diode.lilypadoc.standard.exception.ValidateException;
import com.diode.lilypadoc.standard.utils.FileTool;
import com.diode.lilypadoc.standard.utils.JsonTool;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.reflections.Reflections;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

import java.io.File;
import java.net.URLClassLoader;
import java.util.Objects;
import java.util.Set;

/**
 * 请不要继承该抽象类，参考{@link FactoryPlugin,BehaviourPlugin,ProxyPlugin}
 */
@Slf4j
public abstract class AbstractPlugin {

    private static final String PLUGIN_INFO_PATH = "META-INF/plugin.info";
    public static final String OPEN = "01";
    public static final String CLOSE = "02";
    @Getter
    private PluginStatusEnum status = PluginStatusEnum.INIT;
    private PluginMeta meta;
    private Resource resource;
    private boolean validateBeforeInit;
    private boolean init;
    private URLClassLoader rootClassLoader;
    private URLClassLoader classLoader;
    private InitContext initContext;
    private Class<? extends BaseCustomConfig> configClass;

    public final synchronized ErrorCode init(URLClassLoader urlClassLoader, InitContext initContext) {
        if (init) {
            return StandardErrorCodes.OK;
        }
        if (!validateBeforeInit) {
            return StandardErrorCodes.BIZ_ERROR.of("插件未进行前置验证，请先进行前置验证");
        }
        this.classLoader = urlClassLoader;
        this.initContext = initContext;
        this.rootClassLoader = initContext.getRootClassLoader();
        try {
            initMeta();
            initResource();
            initCustomConfig();
            customInit();
        } catch (BizException e) {
            return e.getErrorCode();
        } catch (Exception e) {
            log.error("初始化插件失败", e);
            return StandardErrorCodes.UNKNOWN_ERROR.of("未知的初始化错误");
        }
        ErrorCode errorCode = validateAfterInit();
        if (errorCode.equals(StandardErrorCodes.OK)) {
            init = true;
        }
        return errorCode;
    }

    private void initMeta() {
        Result<String> result = FileTool.readResourceFlat(classLoader, PLUGIN_INFO_PATH);
        if (result.isFailed()) {
            throw new BizException(StandardErrorCodes.VALIDATE_ERROR.of("插件元信息读取失败，请检查"));
        }
        String pluginInfo = result.get();
        meta = JsonTool.fromJson(pluginInfo, PluginMeta.class);
        if (Objects.isNull(meta)) {
            throw new BizException(StandardErrorCodes.VALIDATE_ERROR.of("插件元信息不可为空"));
        }
    }

    private void initResource() {
        resource = new Resource(name(), classLoader, initContext);
        ErrorCode errorCode = resource.load();
        if (!StandardErrorCodes.OK.equals(errorCode)) {
            throw new BizException(StandardErrorCodes.BIZ_ERROR.of("插件资源加载失败: " + errorCode.message()));
        }
    }

    //todo 此处不重启应用 在安装、卸载接口中重启
    private void initCustomConfig() {

        // 创建Reflections实例并指定配置
        Reflections reflections = new Reflections(new ConfigurationBuilder()
                .addClassLoaders(rootClassLoader)
                .addUrls(ClasspathHelper.forPackage(getClass().getPackageName(), rootClassLoader)));

        // 获取指定父类的所有子类
        Set<Class<? extends BaseCustomConfig>> subTypes = reflections.getSubTypesOf(BaseCustomConfig.class);
        if(Objects.isNull(subTypes) || subTypes.isEmpty()){
            return;
        }
        configClass = subTypes.stream().findFirst().get();
        String customConfPath = initContext.getCustomConfPath();
        String customConfigPath = BaseCustomConfig.getCustomConfigPath(name());
        String path = customConfPath + customConfigPath;
        File file = new File(path);
        if(file.exists()){
            return;
        }
        ErrorCode errorCode = FileTool.writeStringToFile(path, "{}");
        if (!StandardErrorCodes.OK.equals(errorCode)) {
            throw new BizException(StandardErrorCodes.BIZ_ERROR.of("初始化插件配置文件异常: " + errorCode.message()));
        }
    }

    public abstract void customInit();

    public final Resource resource() {
        return resource;
    }

// TODO 刷新功能 刷新htmlComponent和meta

    public final void open() {
        this.status = PluginStatusEnum.OPEN;
        afterOpen();
    }

    protected void afterOpen() {
    }

    public final void close() {
        this.status = PluginStatusEnum.CLOSE;
        afterClose();
    }

    protected void afterClose() {
    }

    public final ErrorCode delete() {
        if (Objects.nonNull(resource)) {
            ErrorCode delete = resource.delete();
            if (StandardErrorCodes.OK.notEquals(delete)) {
                return delete;
            }
        }
        return StandardErrorCodes.OK;
    }

    public final String name() {
        if (Objects.isNull(meta)) {
            return "";
        }
        return meta.getName();
    }

    public final PluginMeta meta() {
        return meta;
    }

    public final ErrorCode validateBeforeInit(URLClassLoader urlClassLoader) {
        try {
            validatePlugin(urlClassLoader);
            validateCustomBeforeInit();
            validateBeforeInit = true;
            return StandardErrorCodes.OK;
        } catch (Exception e) {
            return StandardErrorCodes.VALIDATE_ERROR.of(e.getMessage());
        }
    }

    protected void validateCustomBeforeInit() {
    }

    public final ErrorCode validateAfterInit() {
        try {
            meta.validate();
            validateCustomAfterInit();
            return StandardErrorCodes.OK;
        } catch (ValidateException e) {
            return StandardErrorCodes.VALIDATE_ERROR.of(e.getMessage());
        }
    }

    protected void validateCustomAfterInit() {
    }

    private void validatePlugin(URLClassLoader urlClassLoader) {
        Result<String> result = FileTool.readResourceFlat(urlClassLoader, PLUGIN_INFO_PATH);
        if (result.isFailed()) {
            throw new ValidateException("验证插件合法性时出现异常，请稍后再试");
        }
        String pluginInfo = result.get();
        if (StringUtils.isBlank(pluginInfo)) {
            throw new ValidateException("插件元信息不能为空");
        }
    }


    /**
     * getCustomConfig 实时获取
     * @return
     * @param <T>
     */
    @SuppressWarnings({"unchecked"})
    public final <T extends BaseCustomConfig> Result<T> getCustomConfig() {
        String customConfPath = initContext.getCustomConfPath();
        String customConfigPath = BaseCustomConfig.getCustomConfigPath(name());
        String path = customConfPath + customConfigPath;
        File file = new File(path);
        Result<String> res = FileTool.readFileToString(file);
        if (res.isFailed()) {
            return Result.fail(res.errorCode());
        }
        log.info("插件{}获取到customConfig:{}", name(), res.get());
        T config = (T) JsonTool.fromJson(res.get(), configClass);
        return Result.ok(config);
    }


    public final boolean notOpen() {
        return !PluginStatusEnum.OPEN.equals(status);
    }

    public final boolean notClose() {
        return !PluginStatusEnum.CLOSE.equals(status);
    }

    public final boolean isOpen() {
        return PluginStatusEnum.OPEN.equals(status);
    }

    public final boolean isClose() {
        return PluginStatusEnum.CLOSE.equals(status);
    }
}