package com.diode.lilypadoc.standard.common;

import lombok.Builder;

@Builder
public class Result<T> {

    private final ErrorCode errorCode;
    private final T result;

    public Result(ErrorCode errorCode){
        this.errorCode = errorCode;
        this.result = null;
    }

    public Result(ErrorCode errorCode, T result){
        this.errorCode = errorCode;
        this.result = result;
    }

    public static <T> Result<T> fail(final ErrorCode errorCode){ return new Result<>(errorCode);}
    public static <T> Result<T> ok(){ return new Result<>(StandardErrorCodes.OK);}
    public static <T> Result<T> ok(T result){ return new Result<>(StandardErrorCodes.OK, result);}

    public boolean isSuccess(){
        return errorCode.equals(StandardErrorCodes.OK);
    }

    public boolean isFailed(){
        return !isSuccess();
    }

    public String code(){
        return errorCode.code();
    }

    public String message(){
        return errorCode.message();
    }

    public ErrorCode errorCode(){
        return errorCode;
    }

    public T get(){
        return result;
    }

}
