package com.diode.lilypadoc.standard.utils;

import com.diode.lilypadoc.standard.exception.ConvertException;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

import java.lang.reflect.Type;

public class JsonTool {

    private final static Gson GSON = new GsonBuilder()
            .disableHtmlEscaping()
            .create();

    /**
     * To json string.
     *
     * @param src the src
     * @return the string
     */
    public static String toJson(Object src) {
        return GSON.toJson(src);
    }

    public static <T> T fromJson(String json, Class<T> classOfT) throws ConvertException {
        try {
            return GSON.fromJson(json, classOfT);
        } catch (JsonSyntaxException e) {
            throw new ConvertException(e);
        }
    }

    /**
     * From json t.
     *
     * @param <T>      the type parameter
     * @param json     the json
     * @param classOfT the class of t
     * @return the t
     * @throws JsonSyntaxException the json syntax exception
     */
    public static <T> T fromJson(String json, Type classOfT) throws ConvertException {
        try {
            return GSON.fromJson(json, classOfT);
        } catch (JsonSyntaxException e) {
            throw new ConvertException(e);
        }
    }

}