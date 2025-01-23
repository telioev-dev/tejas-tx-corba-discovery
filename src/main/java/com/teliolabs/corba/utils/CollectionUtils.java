package com.teliolabs.corba.utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class CollectionUtils {

    private CollectionUtils() {
        // Private constructor to prevent instantiation
    }

    public static <K, V> Map<K, V> convertListToMap(List<V> list, Function<V, K> keyMapper) {
        return list.stream()
                .collect(Collectors.toMap(
                        keyMapper,
                        Function.identity(),
                        (existing, replacement) -> existing,
                        HashMap::new
                ));
    }
}
