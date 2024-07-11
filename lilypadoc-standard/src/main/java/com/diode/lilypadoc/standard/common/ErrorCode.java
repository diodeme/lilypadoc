package com.diode.lilypadoc.standard.common;

import java.io.Serializable;
import java.util.Objects;

public class ErrorCode implements Serializable {

    private static final long serialVersionUID = 4775717720589082047L;
    private final String code;
    private String message;

    /**
     * Instantiates a new Error code.
     *
     * @param code the code
     * @param message the message
     */
    public ErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public static ErrorCode of(String code, String message) {
        return new ErrorCode(code, message);
    }

    /**
     * Code string.
     *
     * @return the string
     */
    public String code() {
        return code;
    }

    /**
     * Message string.
     *
     * @return the string
     */
    public String message() {
        return message;
    }

    @Override
    public String toString() {
        return code() + " : " + message();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || !getClass().equals(o.getClass())) {
            return false;
        }
        ErrorCode errorCode = (ErrorCode) o;
        return Objects.equals(code, errorCode.code);
    }

    public boolean notEquals(Object o) {
        return !equals(o);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code);
    }

    public ErrorCode of(String message) {
        return new ErrorCode(code, message);
    }

    public synchronized ErrorCode append(String message) {
        this.message += "\n" + message;
        return this;
    }
}
