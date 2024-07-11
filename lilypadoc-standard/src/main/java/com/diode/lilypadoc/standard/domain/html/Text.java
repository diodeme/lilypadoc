package com.diode.lilypadoc.standard.domain.html;

public class Text implements IHtmlElement{

    private final String val;

    public Text(String val){
        this.val = val;
    }

    @Override
    public String parse(){
        return val+ "\n";
    }
}
