package com.diode.lilypadoc.standard.common.enums;

public interface BaseEnum {

    /**
     * 获取枚举标识
     *
     * @return
     */
    Object getCode();

    /**
     * 获取枚举描述
     *
     * @return
     */
    Object getValue();

    /**
     * 通过枚举类型和code值获取对应的枚举类型
     *
     * @param enumType
     * @param code
     * @param <T>
     * @return
     */
    @SuppressWarnings("unchecked")
    static <T extends BaseEnum> T valueOf(Class<? extends BaseEnum> enumType, Object code) {
        if (enumType == null || code == null) {
            return null;
        }
        T[] enumConstants = (T[]) enumType.getEnumConstants();
        if (enumConstants == null) {
            return null;
        }
        for (T enumConstant : enumConstants) {
            Object enumCode = enumConstant.getCode();
            if (code.equals(enumCode)) {
                return enumConstant;
            }
        }
        return null;
    }
}