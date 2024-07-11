package com.diode.lilypadoc.application.util;

import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;

@UtilityClass
public class StringTool {

    /**
     * 将value转换为对应的class类型的 fieldType
     *
     * @param fieldType class类型
     * @param value     转换的值
     * @return Object
     * @throws Exception 反射获取类名 ClassNotFoundException 反射会有安全异常
     */
    public Object transformValue(Class<?> fieldType, String value) throws Exception {
        if (StringUtils.isBlank(value)) {
            return null;
        }
        if (fieldType == String.class) {
            return value;
        } else if (fieldType == Boolean.class || fieldType == boolean.class) {
            return Boolean.valueOf(value);
        } else if (fieldType == Byte.class || fieldType == byte.class) {
            return Byte.valueOf(value);
        } else if (fieldType == Double.class || fieldType == double.class) {
            return Double.valueOf(value);
        } else if (fieldType == Float.class || fieldType == float.class) {
            return Float.valueOf(value);
        } else if (fieldType == Integer.class || fieldType == int.class) {
            return Integer.valueOf(value);
        } else if (fieldType == Long.class || fieldType == long.class) {
            return Long.valueOf(value);
        } else if (fieldType == Short.class || fieldType == short.class) {
            return Short.valueOf(value);
        } else if (fieldType == Character.class || fieldType == char.class) {
            return value.charAt(0);
        } else if (fieldType == BigDecimal.class) {
            return new BigDecimal(value);
        } else if (fieldType == BigInteger.class) {
            return new BigInteger(value);
        } else if (fieldType == Date.class) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            return sdf.parse(value);
        } else if (fieldType == List.class) {
            return Arrays.asList(value.split(","));
        } else if (fieldType == Set.class) {
            return new HashSet<>(Arrays.asList(value.split(",")));
        } else if (fieldType.isEnum()) { // 枚举类型
            Class<?> cl = Class.forName(fieldType.getName());
            Field field = cl.getDeclaredField(value);
            return field.get(cl);
        } else if (fieldType == Pattern.class) {
            return Pattern.compile(value);
        } else {
            return value;
        }
    }
}