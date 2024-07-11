package com.diode.lilypadoc.standard.common;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

public class ErrorCodeWrapper {

    @Getter
    private final List<ErrorCode> errorCodeList;

    public ErrorCodeWrapper(){
        this.errorCodeList = new ArrayList<>();
    }

    public synchronized ErrorCodeWrapper add(ErrorCode errorCode){
        errorCodeList.add(errorCode);
        return this;
    }

    public synchronized ErrorCodeWrapper addAll(List<ErrorCode> errorCodeList){
        this.errorCodeList.addAll(errorCodeList);
        return this;
    }

    public synchronized ErrorCodeWrapper union(ErrorCodeWrapper errorCodeWrapper){
        addAll(errorCodeWrapper.getErrorCodeList());
        return this;
    }

    public synchronized boolean notEmpty(){
        return !errorCodeList.isEmpty();
    }

    @Override
    public String toString(){
        if(!notEmpty()){
            return "OK";
        }
        StringBuilder stringBuilder = new StringBuilder();
        for(ErrorCode errorCode: errorCodeList){
            stringBuilder.append(errorCode.toString()).append("\n");
        }
        return stringBuilder.toString();
    }
}
