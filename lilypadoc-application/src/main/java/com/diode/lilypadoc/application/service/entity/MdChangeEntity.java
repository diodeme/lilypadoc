package com.diode.lilypadoc.application.service.entity;

import com.diode.lilypadoc.application.common.enums.MdChangeTypeEnum;
import lombok.Data;

@Data
public class MdChangeEntity {
    private String filePath;
    private MdChangeTypeEnum type;
}
