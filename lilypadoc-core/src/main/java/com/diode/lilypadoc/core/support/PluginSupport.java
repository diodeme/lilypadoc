package com.diode.lilypadoc.core.support;

import com.diode.lilypadoc.standard.api.ILilypadocComponent;
import com.diode.lilypadoc.standard.api.plugin.AbstractPlugin;
import com.diode.lilypadoc.standard.api.plugin.BehaviourPlugin;
import com.diode.lilypadoc.standard.api.plugin.FactoryPlugin;
import com.diode.lilypadoc.standard.api.plugin.ProxyPlugin;
import com.diode.lilypadoc.standard.common.ErrorCode;
import com.diode.lilypadoc.standard.common.Result;
import com.diode.lilypadoc.standard.common.StandardErrorCodes;
import com.diode.lilypadoc.standard.common.enums.PluginDomainEnum;
import com.diode.lilypadoc.standard.domain.LilypadocContext;
import com.diode.lilypadoc.standard.domain.Resource;
import com.diode.lilypadoc.standard.utils.ListTool;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

public class PluginSupport {

    public static ErrorCode executePlugin(List<AbstractPlugin> pluginList, LilypadocContext LilypadocContext,
                                          List<Resource> resourceList, Map<AbstractPlugin, List<ILilypadocComponent>> htmlComponentMap, PluginDomainEnum pluginDomain) {
        List<AbstractPlugin> plugins = pluginList;
        if(Objects.nonNull(pluginDomain)){
            plugins = pluginList.stream().filter(e -> e.meta().getDomains().containsKey(pluginDomain)).collect(Collectors.toList());
        }
        //key: 被代理的插件名称 value: 代理插件
        Map<String, AbstractPlugin> proxyMap = new HashMap<>();
        for (AbstractPlugin plugin : plugins) {
            String proxy = plugin.meta().getProxy();
            if (StringUtils.isBlank(proxy)) {
                continue;
            }
            proxyMap.put(proxy, plugin);
        }
        Set<String> executedPlugin = new HashSet<>(plugins.size());
        for (AbstractPlugin plugin : plugins) {
            if (executedPlugin.contains(plugin.name())) {
                continue;
            }
            AbstractPlugin realExecutePlugin = plugin;
            Map<String, List<ILilypadocComponent>> dependencyMap = new HashMap<>();
            List<String> dependencies = plugin.meta().getDependencies();
            if (CollectionUtils.isNotEmpty(dependencies)) {
                dependencyMap = htmlComponentMap.entrySet().stream()
                        .filter(e -> dependencies.contains(e.getKey().name()))
                        .collect(Collectors.toMap(e -> e.getKey().name(), Map.Entry::getValue, (s1, s2) -> s2));
            }
            AbstractPlugin proxyPlugin = proxyMap.get(plugin.name());
            //执行代理类逻辑优先
            if (Objects.nonNull(proxyPlugin)) {
                if (!(proxyPlugin instanceof ProxyPlugin)) {
                    return StandardErrorCodes.BIZ_ERROR.of("非代理插件使用了代理行为");
                }
                Result<List<ILilypadocComponent>> proxyResult = ((ProxyPlugin) proxyPlugin).genComponent(plugin,
                        LilypadocContext, dependencyMap);
                if (proxyResult.isFailed()) {
                    return proxyResult.errorCode();
                }
                List<ILilypadocComponent> componentList = ListTool.safeArrayList(proxyResult.get());
                //将代理类计算结果合并到总结果
                htmlComponentMap.put(plugin, componentList);
                realExecutePlugin = proxyPlugin;
            } else {
                //代理插件
                if (plugin instanceof ProxyPlugin) {
                    //do nothing
                }
                //行为插件
                else if (plugin instanceof BehaviourPlugin) {
                    //do nothing
                    //行为插件真正的执行依靠event
                }
                //生成插件
                else if (plugin instanceof FactoryPlugin) {
                    Result<List<ILilypadocComponent>> lilypadocComponentsResult = ((FactoryPlugin) plugin).genComponent(
                            LilypadocContext, dependencyMap);
                    if (lilypadocComponentsResult.isFailed()) {
                        return lilypadocComponentsResult.errorCode();
                    }
                    List<ILilypadocComponent> componentList = ListTool.safeArrayList(lilypadocComponentsResult.get());
                    htmlComponentMap.put(plugin, componentList);
                }
            }
            Resource resource = realExecutePlugin.resource();
            if (!resource.isEmpty()) {
                resourceList.add(resource);
            }
            executedPlugin.add(realExecutePlugin.name());
            executedPlugin.add(plugin.name());
        }
        return StandardErrorCodes.OK;
    }
}