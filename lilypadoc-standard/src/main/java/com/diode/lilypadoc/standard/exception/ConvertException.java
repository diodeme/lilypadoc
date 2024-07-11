package com.diode.lilypadoc.standard.exception;

public class ConvertException extends RuntimeException{
    public ConvertException(String msg){
        super(msg);
    }

    public ConvertException(Throwable e){
        super(e);
    }

    public ConvertException(String msg, Throwable e){
        super(msg, e);
    }
}
