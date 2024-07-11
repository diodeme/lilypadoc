package com.diode.lilypadoc.standard.common;

public class StandardErrorCodes {

    public static final ErrorCode OK = new ErrorCode("0", "success");

    public static final ErrorCode VALIDATE_ERROR = new ErrorCode("4001", "校检失败");

    public static final ErrorCode BIZ_ERROR = new ErrorCode("5001", "业务异常");

    public static final ErrorCode TIMEOUT_ERROR = new ErrorCode("9002", "超时异常");

    public static final ErrorCode IO_ERROR = new ErrorCode("9002", "io异常");

    public static final ErrorCode SYS_ERROR = new ErrorCode("9001", "系统异常");

    public static final ErrorCode UNKNOWN_ERROR = new ErrorCode("9999", "未知错误");

    public static final ErrorCode DB_ERROR = new ErrorCode("SQL0001", "数据库异常");


}
