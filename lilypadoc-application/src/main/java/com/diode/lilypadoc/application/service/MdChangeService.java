package com.diode.lilypadoc.application.service;

import com.diode.lilypadoc.application.common.enums.MdChangeTypeEnum;
import com.diode.lilypadoc.application.configuration.GitConfiguration;
import com.diode.lilypadoc.application.core.SpringLilypadoc;
import com.diode.lilypadoc.application.service.entity.MdChangeEntity;
import com.diode.lilypadoc.application.strategy.AbstractMdChangeStrategy;
import com.diode.lilypadoc.core.config.ConfigurationManager;
import com.diode.lilypadoc.core.config.MarkdownConfiguration;
import com.diode.lilypadoc.standard.common.ErrorCode;
import com.diode.lilypadoc.standard.common.ErrorCodeWrapper;
import com.diode.lilypadoc.standard.common.StandardErrorCodes;
import com.diode.lilypadoc.standard.domain.MPath;
import com.diode.lilypadoc.standard.utils.ListTool;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.io.File;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
public class MdChangeService {

    @Resource
    private GitConfiguration gitConfiguration;

    @Resource
    private SpringLilypadoc springLilypadoc;

    @Resource
    private GitService gitService;

    private Map<MdChangeTypeEnum, AbstractMdChangeStrategy> strategyMap;

    public MdChangeService(List<AbstractMdChangeStrategy> mdChangeStrategyList) {
        strategyMap = ListUtils.emptyIfNull(mdChangeStrategyList).stream().collect(
                Collectors.toMap(AbstractMdChangeStrategy::support, Function.identity(), (s1, s2) -> s2));
    }

    public void parseAndSync(List<MdChangeEntity> entityList) {
        if(CollectionUtils.isEmpty(entityList)) {
            return;
        }

        gitService.cloneOrPullApiRepo();
        Map<MdChangeTypeEnum, List<MdChangeEntity>> changeEntityMap = ListUtils.emptyIfNull(entityList).stream()
                .collect(Collectors.groupingBy(MdChangeEntity::getType));

        for (Map.Entry<MdChangeTypeEnum, List<MdChangeEntity>> entry : changeEntityMap.entrySet()) {
            MdChangeTypeEnum k = entry.getKey();
            List<MdChangeEntity> v = entry.getValue();
            AbstractMdChangeStrategy abstractMdChangeStrategy = strategyMap.get(k);

            if(Objects.isNull(abstractMdChangeStrategy)) {
                throw new RuntimeException("存在不支持的mdChange类型, type: " + k);
            }

            ErrorCode errorCode = abstractMdChangeStrategy.doHandle(v);
            if(StandardErrorCodes.OK.notEquals(errorCode)) {
                log.error("消息类型, {}执行对应操作失败. code:{}", k, errorCode);
            }
        }
        //refresh
        Set<String> refreshCateSet = new HashSet<>();
        String refreshCateStr = gitConfiguration.getRefreshCate();
        if(StringUtils.isBlank(refreshCateStr)){
            return;
        }
        int refreshCate = Integer.parseInt(refreshCateStr);
        for (MdChangeEntity mdChangeEntity : ListTool.safeArrayList(entityList)) {
            MPath targetCate = MPath.of(mdChangeEntity.getFilePath()).getTargetCate(refreshCate);
            if(Objects.nonNull(targetCate)) {
                refreshCateSet.add(targetCate.toString());
            }
        }

        MarkdownConfiguration configuration = ConfigurationManager.getInstance()
                .getConfiguration(MarkdownConfiguration.class);
        for (String refreshCatePath : refreshCateSet) {
            File file = new File(configuration.getRootDir() + refreshCatePath);
            ErrorCodeWrapper errorCodeWrapper = springLilypadoc.parse(file);
            if(errorCodeWrapper.notEmpty()){
                log.error("解析文件: {} 异常: {}", file.getPath(), errorCodeWrapper);
            }
        }
    }
}