package com.diode.lilypadoc.application.strategy;

import com.diode.lilypadoc.application.common.enums.MdChangeTypeEnum;
import com.diode.lilypadoc.application.service.entity.MdChangeEntity;
import com.diode.lilypadoc.core.config.ConfigurationManager;
import com.diode.lilypadoc.core.config.HtmlConfiguration;
import com.diode.lilypadoc.standard.common.ErrorCode;
import com.diode.lilypadoc.standard.common.StandardErrorCodes;
import com.diode.lilypadoc.standard.domain.MPath;
import com.diode.lilypadoc.standard.utils.FileTool;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.ListUtils;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.List;

@Slf4j
@Component
public class MdDeleteStrategy extends AbstractMdChangeStrategy {

    @Override
    public ErrorCode doHandle(List<MdChangeEntity> changeEntityList) {
        HtmlConfiguration configuration = ConfigurationManager.getInstance().getConfiguration(HtmlConfiguration.class);
        for (MdChangeEntity mdChangeEntity : ListUtils.emptyIfNull(changeEntityList)) {
            String filePath = mdChangeEntity.getFilePath();
            File htmlFile = new File(MPath.ofHtml(configuration.getDocRootPath().appendChild(filePath)).toString());
            ErrorCode errorCode = FileTool.deleteIfExist(htmlFile);
            if (StandardErrorCodes.OK.notEquals(errorCode)) {
                return errorCode;
            }
        }
        return StandardErrorCodes.OK;
    }

    @Override
    public MdChangeTypeEnum support() {
        return MdChangeTypeEnum.delete;
    }
}