package com.diode.lilypadoc.application.controller.resolver.git.gitee;

import com.diode.lilypadoc.application.common.enums.MdChangeTypeEnum;
import com.diode.lilypadoc.application.controller.request.MdChangeRequest;
import com.google.gson.annotations.SerializedName;
import lombok.Data;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class GiteeWebhookRequest {

    @SerializedName("commits")
    private List<Commit> commitList;

    static class Commit {

        @SerializedName("added")
        private List<String> added;

        @SerializedName("removed")
        private List<String> removed;

        @SerializedName("modified")
        private List<String> modified;
    }

    public MdChangeRequest convertToMdChangeRequest() {
        MdChangeRequest mdChangeRequest = new MdChangeRequest();
        List<MdChangeRequest.MdChangeMsg> mdChangeMsgList = new ArrayList<>();
        ListUtils.emptyIfNull(commitList).forEach(e -> {
            mdChangeMsgList.addAll(ListUtils.emptyIfNull(e.added).stream()
                    .map(add -> new MdChangeRequest.MdChangeMsg(add, MdChangeTypeEnum.add.name()))
                    .collect(Collectors.toList()));
            mdChangeMsgList.addAll(ListUtils.emptyIfNull(e.removed).stream()
                    .map(remove -> new MdChangeRequest.MdChangeMsg(remove, MdChangeTypeEnum.delete.name()))
                    .collect(Collectors.toList()));
            mdChangeMsgList.addAll(ListUtils.emptyIfNull(e.modified).stream()
                    .map(modify -> new MdChangeRequest.MdChangeMsg(modify, MdChangeTypeEnum.modify.name()))
                    .collect(Collectors.toList()));
        });
        mdChangeRequest.setMsgList(mdChangeMsgList.stream().filter(e -> StringUtils.endsWith(e.getFilePath(), ".md"))
            .collect(Collectors.toList()));
        return mdChangeRequest;
    }
}