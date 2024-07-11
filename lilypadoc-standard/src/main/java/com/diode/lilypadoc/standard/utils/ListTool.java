package com.diode.lilypadoc.standard.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class ListTool {
    public static <T> List<T> safeArrayList(List<T> unSafeList){
        return Optional.ofNullable(unSafeList).map(e -> e.stream().filter(Objects::nonNull).collect(Collectors.toList())).orElse(new ArrayList<>());
    }
}
