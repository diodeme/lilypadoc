package com.diode.lilypadoc.standard.exception;

import com.diode.lilypadoc.standard.common.ErrorCode;

public class BizException extends RuntimeException{

    protected ErrorCode errorCode;

    public BizException(ErrorCode errorCode){
        super(errorCode.toString());
        this.errorCode = errorCode;
    }

    public BizException(ErrorCode errorCode, Throwable e){
        super(errorCode.toString(), e);
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode(){
        return errorCode;
    }
}
