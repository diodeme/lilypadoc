package com.diode.lilypadoc.core.plugin;

import com.diode.lilypadoc.core.config.ApplicationConfiguration;
import com.diode.lilypadoc.core.config.ConfigurationManager;
import com.diode.lilypadoc.core.config.HtmlConfiguration;
import com.diode.lilypadoc.core.config.PluginConfiguration;
import com.diode.lilypadoc.core.support.DAG;
import com.diode.lilypadoc.standard.api.EventListener;
import com.diode.lilypadoc.standard.api.plugin.AbstractPlugin;
import com.diode.lilypadoc.standard.api.plugin.InitContext;
import com.diode.lilypadoc.standard.common.ErrorCode;
import com.diode.lilypadoc.standard.common.Pair;
import com.diode.lilypadoc.standard.common.Result;
import com.diode.lilypadoc.standard.common.StandardErrorCodes;
import com.diode.lilypadoc.standard.common.enums.PluginAreaEnum;
import com.diode.lilypadoc.standard.common.enums.PluginDomainEnum;
import com.diode.lilypadoc.standard.common.enums.PluginStatusEnum;
import com.diode.lilypadoc.standard.domain.PluginMeta;
import com.diode.lilypadoc.standard.domain.Resource;
import com.diode.lilypadoc.standard.domain.event.IEvent;
import com.diode.lilypadoc.standard.utils.FileTool;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;

@Slf4j
public class PluginManager {

    private static Map<String, Pair<AbstractPlugin, URLClassLoader>> pluginMap;
    private static List<AbstractPlugin> sortedPlugins;

    public static Result<List<AbstractPlugin>> getSortedPlugins() {
        if (CollectionUtils.isEmpty(sortedPlugins)) {
            return Result.fail(StandardErrorCodes.BIZ_ERROR.of("插件列表为空，请先加载插件！"));
        }
        return Result.ok(sortedPlugins);
    }

    public static ErrorCode loadAllPlugins() {
        List<AbstractPlugin> plugins = new ArrayList<>();
        HtmlConfiguration htmlConfiguration = ConfigurationManager.getInstance().getConfiguration(
                HtmlConfiguration.class);
        PluginConfiguration pluginConfiguration = ConfigurationManager.getInstance()
                .getConfiguration(PluginConfiguration.class);
        ApplicationConfiguration applicationConfiguration = ConfigurationManager.getInstance().getConfiguration(ApplicationConfiguration.class);
        Result<URL[]> result = FileTool.listAllFilesByExtension(htmlConfiguration.getPluginPath(), ".jar");
        if (result.isFailed()) {
            return result.errorCode();
        }
        URL[] urls = result.get();

        if (pluginMap == null) {
            pluginMap = new HashMap<>(urls.length);
        }
        URLClassLoader parentUrlClassLoader = new URLClassLoader(urls);
        InitContext initContext = new InitContext();
        initContext.setCssPack(htmlConfiguration.isCssPack());
        initContext.setJsPack(htmlConfiguration.isJsPack());
        initContext.setCustomConfPath(applicationConfiguration.getCustomConfigAbPath());
        initContext.setRootClassLoader(parentUrlClassLoader);
        ServiceLoader<AbstractPlugin> serviceLoader = ServiceLoader.load(AbstractPlugin.class, parentUrlClassLoader);
        Iterator<AbstractPlugin> iterator = serviceLoader.iterator();
        // 使用 ServiceLoader 以SPI的方式加载插件包中的 IPluginService 实现类
        for (URL url : urls) {
            //TODO 这里需要测试清楚 urlClassLoader如果根据url新建一个，会不会影响插件的resource的获取？
            //TODO 如果影响，那就在delete方法里去close，如果不影响，那就在close方法里
            URLClassLoader urlClassLoader = new URLClassLoader(new URL[]{url});
            if (!iterator.hasNext()) {
                log.error("{}中不存在合法插件", url.getPath());
                continue;
            }
            AbstractPlugin plugin = iterator.next();
            PluginStatusEnum pluginStatus = pluginConfiguration.getPluginStatus(plugin.name());
            log.info("插件:{}初始化状态:{}", plugin.name(), pluginStatus);
            if (PluginStatusEnum.CLOSE.equals(pluginStatus)) {
                plugin.close();
                pluginMap.put(plugin.name(), new Pair<>(plugin, urlClassLoader));
                continue;
            }
            ErrorCode errorCode = plugin.validateBeforeInit(urlClassLoader);
            if (StandardErrorCodes.OK.notEquals(errorCode)) {
                log.error("路径{}, 插件验证失败, {}", url.getPath(), errorCode);
                continue;
            }
            if (pluginMap.containsKey(plugin.name())) {
                log.error("路径{}, 插件{}已存在", url.getPath(), plugin.meta().getName());
                continue;
            }
            ErrorCode initErrorCode = plugin.init(urlClassLoader, initContext);
            if (StandardErrorCodes.OK.notEquals(initErrorCode)) {
                return initErrorCode;
            }
            Resource resource = plugin.resource();
            if (Objects.nonNull(resource) && !resource.isEmpty()) {
                ErrorCode syncCode = resource.sync(htmlConfiguration.getRootPath(),
                        htmlConfiguration.getPluginResourceRePath());
                if (StandardErrorCodes.OK.notEquals(syncCode)) {
                    return StandardErrorCodes.BIZ_ERROR.of("插件resource同步失败:" + syncCode.message());
                }
            }
            openPlugin(plugin.name());
            plugins.add(plugin);
            pluginMap.put(plugin.name(), new Pair<>(plugin, urlClassLoader));
        }
        pluginConfiguration.sync();
        Map<String, List<PluginMeta>> countMap = new HashMap<>();
        for (Map.Entry<String, Pair<AbstractPlugin, URLClassLoader>> pluginMapEntry : pluginMap.entrySet()) {
            AbstractPlugin key = pluginMapEntry.getValue().getKey();
            PluginMeta pluginMeta = key.meta();
            for (Map.Entry<PluginDomainEnum, PluginAreaEnum> entry : pluginMeta.getDomains().entrySet()) {
                PluginDomainEnum domain = entry.getKey();
                PluginAreaEnum area = entry.getValue();
                String index = "" + domain + "_" + area + "_" + pluginMeta.getOrder();
                List<PluginMeta> count = countMap.compute(index, (k, v) -> Objects.isNull(v) ? new ArrayList<>() : v);
                count.add(pluginMeta);
                if (count.size() > 1) {
                    log.error("插件index重复, 重复插件{}", count);
                    return StandardErrorCodes.BIZ_ERROR.of("插件index重复, 请检查");
                }
            }
        }
        sortedPlugins = sortPlugins(plugins);
        return StandardErrorCodes.OK;
    }

    private static List<AbstractPlugin> sortPlugins(List<AbstractPlugin> plugins) {
        DAG<AbstractPlugin> dag = new DAG<>();
        for (AbstractPlugin plugin : plugins) {
            List<String> dependencies = plugin.meta().getDependencies();
            if (Objects.isNull(dependencies)) {
                continue;
            }
            for (String dependency : dependencies) {
                Pair<AbstractPlugin, URLClassLoader> pair = pluginMap.get(dependency);
                if (Objects.isNull(pair)) {
                    continue;
                }
                DAG.Edge<AbstractPlugin> edge = new DAG.Edge<>(pair.getKey(), plugin);
                dag.addEdge(edge);
            }
        }
        List<AbstractPlugin> sort = dag.sort();
        //能运行到插件的初始化阶段,说明没有相互依赖
        List<AbstractPlugin> subtract = ListUtils.subtract(plugins, sort);
        sort.addAll(subtract);
        return sort;
    }

    public static AbstractPlugin getPlugin(String name) {
        if (StringUtils.isBlank(name)) {
            return null;
        }
        Pair<AbstractPlugin, URLClassLoader> pair = pluginMap.get(name);
        if (Objects.nonNull(pair)) {
            return pair.getKey();
        }
        return null;
    }

    private static URLClassLoader getClassLoader(String name) {
        if (StringUtils.isBlank(name)) {
            return null;
        }
        Pair<AbstractPlugin, URLClassLoader> pair = pluginMap.get(name);
        if (Objects.nonNull(pair)) {
            return pair.getValue();
        }
        return null;
    }

    private static ErrorCode openPlugin(String name) {
        AbstractPlugin plugin = getPlugin(name);
        if (Objects.isNull(plugin)) {
            return StandardErrorCodes.BIZ_ERROR.of("插件" + name + "开启，但是插件不存在");
        }
        if (plugin.isOpen()) {
            return StandardErrorCodes.BIZ_ERROR.of("插件" + name + "已开启");
        }
        Pair<AbstractPlugin, URLClassLoader> pair = pluginMap.get(name);
        URLClassLoader value = pair.getValue();
        //TODO test 如果关闭了，则classLoader也关闭了，开始时需要重新打开
        if (plugin.isClose()) {
            pluginMap.put(name, new Pair<>(plugin, new URLClassLoader(value.getURLs())));
        }
        plugin.open();
        PluginConfiguration pluginConfiguration = ConfigurationManager.getInstance()
                .getConfiguration(PluginConfiguration.class);
        pluginConfiguration.changeStatus(name, PluginStatusEnum.OPEN);
        return StandardErrorCodes.OK;
    }

    //TODO 关闭, 打开, 安装, 卸载
//TODO 关闭时需要判断依赖
    private static ErrorCode closePlugin(String name) {
        URLClassLoader classLoader = getClassLoader(name);
        AbstractPlugin plugin = getPlugin(name);
        if (Objects.isNull(plugin) || Objects.isNull(classLoader)) {
            return StandardErrorCodes.OK;
        }
        try {
            classLoader.close();
        } catch (IOException e) {
            log.error("关闭插件{}时出现错误, 请稍后再试", name, e);
            return StandardErrorCodes.SYS_ERROR.of("关闭插件" + name + "发生错误");
        }
        plugin.close();
        PluginConfiguration pluginConfiguration = ConfigurationManager.getInstance()
                .getConfiguration(PluginConfiguration.class);
        pluginConfiguration.changeStatus(name, PluginStatusEnum.CLOSE);
        pluginConfiguration.sync();
        return StandardErrorCodes.OK;
    }

    public static ErrorCode deletePlugin(String pluginName) {
        //TODO 关闭 去掉map 然后根据plugin地址前缀删除
        closePlugin(pluginName);
        Pair<AbstractPlugin, URLClassLoader> pair = pluginMap.get(pluginName);
        AbstractPlugin plugin = pair.getKey();
        return plugin.delete();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static ErrorCode sendEvent(IEvent event) {
        Result<List<AbstractPlugin>> result = getSortedPlugins();
        if (result.isFailed()) {
            return result.errorCode();
        }
        List<AbstractPlugin> plugins = result.get();
        for (AbstractPlugin plugin : plugins) {
            if (!(plugin instanceof EventListener)) {
                continue;
            }
            Class<IEvent> eventType = (Class<IEvent>) ((ParameterizedType) plugin.getClass()
                    .getGenericSuperclass()).getActualTypeArguments()[0];
            if (eventType.isInstance(event)) {
                ErrorCode errorCode = ((EventListener) plugin).onEvent(event);
                if (StandardErrorCodes.OK.notEquals(errorCode)) {
                    log.error("发送消息处理失败. event:{} errorCode:{}", event, errorCode);
                    return errorCode;
                }
            }
        }
        return StandardErrorCodes.OK;
    }
}
