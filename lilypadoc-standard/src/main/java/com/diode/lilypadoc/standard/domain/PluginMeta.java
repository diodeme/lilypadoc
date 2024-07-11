package com.diode.lilypadoc.standard.domain;

import com.diode.lilypadoc.standard.common.enums.PluginAreaEnum;
import com.diode.lilypadoc.standard.common.enums.PluginDomainEnum;
import com.diode.lilypadoc.standard.exception.ValidateException;
import lombok.Data;
import org.apache.commons.lang3.Validate;

import java.util.List;
import java.util.Map;

@Data
public class PluginMeta {

    private String name;
    private Map<PluginDomainEnum, PluginAreaEnum> domains;
    private Integer order;
    private List<String> dependencies;
    private String proxy;

    public void validate(){
        try {
            Validate.notBlank(name);
            Validate.notNull(order);
            Validate.notEmpty(domains);
        }catch (IllegalArgumentException e){
            throw new ValidateException("插件元信息格式非法", e);
        }
    }
}
