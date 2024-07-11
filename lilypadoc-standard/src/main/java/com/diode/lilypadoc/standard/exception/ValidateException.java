package com.diode.lilypadoc.standard.exception;

public class ValidateException extends RuntimeException{

    public ValidateException(String msg){
        super(msg);
    }

    public ValidateException(String msg, Throwable e){
        super(msg, e);
    }
}
