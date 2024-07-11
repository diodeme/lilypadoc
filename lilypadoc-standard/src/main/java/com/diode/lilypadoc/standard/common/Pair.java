package com.diode.lilypadoc.standard.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class Pair<K,V> {

    @Getter
    private K key;

    @Getter
    private V value;
}
