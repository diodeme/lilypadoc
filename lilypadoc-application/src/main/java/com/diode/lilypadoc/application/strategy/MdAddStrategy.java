package com.diode.lilypadoc.application.strategy;

import com.diode.lilypadoc.application.common.enums.MdChangeTypeEnum;
import com.diode.lilypadoc.application.service.entity.MdChangeEntity;
import com.diode.lilypadoc.standard.common.ErrorCode;
import com.diode.lilypadoc.standard.common.StandardErrorCodes;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MdAddStrategy extends AbstractMdChangeStrategy {

    @Override
    public ErrorCode doHandle(List<MdChangeEntity> changeEntityList) {
        return StandardErrorCodes.OK;
    }

    @Override
    public MdChangeTypeEnum support() {
        return MdChangeTypeEnum.add;
    }
}