package com.diode.lilypadoc.application.controller.request;

import com.diode.lilypadoc.application.common.enums.MdChangeTypeEnum;
import com.diode.lilypadoc.application.service.entity.MdChangeEntity;
import com.google.gson.annotations.SerializedName;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.collections4.ListUtils;

/**
 * @author:diodehe
 * @createDate:2023/10/26
 */
@Data
public class MdChangeRequest {

    @SerializedName("msgList")
    private List<MdChangeMsg> msgList;

    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    public static class MdChangeMsg {

        @SerializedName("filePath")
        private String filePath;

        @SerializedName("type")
        private String type;
    }

    public List<MdChangeEntity> toEntity() {
        return ListUtils.emptyIfNull(msgList).stream().map(e -> {
            MdChangeEntity mdChangeEntity = new MdChangeEntity();
            mdChangeEntity.setFilePath(e.filePath.replace("\\", "/"));
            mdChangeEntity.setType(MdChangeTypeEnum.valueOf(e.type));
            return mdChangeEntity;
        }).collect(Collectors.toList());
    }
}