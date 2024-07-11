package com.diode.lilypadoc.standard.domain.http;

import com.diode.lilypadoc.standard.domain.MPath;
import lombok.Data;

@Data
public class HttpCallContext {

    private MPath htmlRootPath;
    private MPath htmlDocRPath;
}
