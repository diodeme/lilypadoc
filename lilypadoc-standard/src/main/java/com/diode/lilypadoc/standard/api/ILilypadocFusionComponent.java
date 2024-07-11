package com.diode.lilypadoc.standard.api;

import org.apache.commons.lang3.StringUtils;

public interface ILilypadocFusionComponent {
    static String getFusionTag(String prefix, String path){
        return "@" + getPath(prefix, path);
    }

    static String getPath(String prefix, String field){
        if(StringUtils.isBlank(prefix)){
            return field;
        }
        return prefix + "." + field;
    }
}
