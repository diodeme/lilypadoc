package com.diode.lilypadoc.standard.api.plugin;

import lombok.Data;

import java.net.URLClassLoader;

@Data
public class InitContext {

    private URLClassLoader rootClassLoader;
    private boolean cssPack;
    private boolean jsPack;
    private String customConfPath;
}
