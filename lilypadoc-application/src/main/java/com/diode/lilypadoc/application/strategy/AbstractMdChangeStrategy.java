package com.diode.lilypadoc.application.strategy;

import com.diode.lilypadoc.application.common.enums.MdChangeTypeEnum;
import com.diode.lilypadoc.application.service.entity.MdChangeEntity;
import com.diode.lilypadoc.standard.common.ErrorCode;

import java.util.List;

public abstract class AbstractMdChangeStrategy {

    public abstract ErrorCode doHandle(List<MdChangeEntity> changeEntityList);

    public abstract MdChangeTypeEnum support();
}