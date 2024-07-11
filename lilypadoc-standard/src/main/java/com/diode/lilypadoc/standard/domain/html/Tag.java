package com.diode.lilypadoc.standard.domain.html;

import java.util.*;

public class Tag implements IHtmlElement{

    private final String name;
    private final Map<String, String> properties;
    private final List<IHtmlElement> children;
    private boolean closeable = true;

    public Tag(String name){
        this.name = name;
        children = new ArrayList<>();
        properties = new HashMap<>();
    }

    public Tag(String name, boolean closeable){
        this.name = name;
        this.closeable = closeable;
        children = new ArrayList<>();
        properties = new HashMap<>();
    }

    public Tag child(IHtmlElement... htmlElements){
        children.addAll(Arrays.asList(htmlElements));
        return this;
    }

    public Tag property(String key, String value){
        properties.put(key, value);
        return this;
    }

    @Override
    public String parse(){
        StringBuilder sb = new StringBuilder();
        sb.append("<").append(name).append(" ").append(properties()).append(">").append("\n");
        for (IHtmlElement child: children){
            sb.append(child.parse());
        }
        if(closeable){
            sb.append("</").append(name).append(">");
        }
        sb.append("\n");
        return sb.toString();
    }

    private String properties(){
        StringBuilder stringBuilder = new StringBuilder();
        properties.forEach((k,v) -> {
            stringBuilder.append(k).append("=").append("\"").append(v).append("\"").append(" ");
        });
        return stringBuilder.toString();
    }
}
