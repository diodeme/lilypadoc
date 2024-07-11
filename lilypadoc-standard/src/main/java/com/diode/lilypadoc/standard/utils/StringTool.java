package com.diode.lilypadoc.standard.utils;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

@Slf4j
@UtilityClass
public class StringTool {

    public static String capitalizeFirstLetterIfAlphabetic(String str) {
        if (str == null || str.isEmpty()) {
            return str; // 返回原字符串如果它是空的
        }

        // 检查第一个字符是否为字母
        char firstChar = str.charAt(0);
        if (Character.isLetter(firstChar)) {
            // 如果是字母，则将首字母转换为大写
            return Character.toUpperCase(firstChar) + str.substring(1);
        }

        // 如果首字符不是字母，返回原字符串
        return str;
    }

    public static int count(String src, String target) {
        if (Objects.isNull(src) || Objects.isNull(target)) {
            return 0;
        }
        return (src.length() - src.replace(target, "").length()) / target.length();
    }
}