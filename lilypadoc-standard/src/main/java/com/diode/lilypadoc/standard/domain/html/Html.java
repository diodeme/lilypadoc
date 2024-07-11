package com.diode.lilypadoc.standard.domain.html;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Html implements IHtmlElement{

    @Getter
    private final List<IHtmlElement> elements;

    public Html() {
        elements = new ArrayList<>();
    }

    public Html element(IHtmlElement element){
        elements.add(element);
        return this;
    }

    public Html union(Html html){
        if(Objects.nonNull(html)){
            elements.addAll(html.elements);
        }
        return this;
    }

    @Override
    public String parse(){
        if(elements.isEmpty()){
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for(IHtmlElement element: elements){
            sb.append(element.parse());
        }
        return sb.toString();
    }
}
