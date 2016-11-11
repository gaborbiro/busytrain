package com.gaborbiro.busytrain.util;

import java.lang.reflect.Array;
import java.util.Comparator;

public class ArrayUtils {

    public static <T> T[] insert(T[] array, T item, Class<T> clazz, Comparator<T> comparator) {
        T[] result = (T[]) Array.newInstance(clazz, array.length + 1);

        if (array.length == 0) {
            result[0] = item;
        } else {
            for (int i = 0; i < result.length; i++) {
                if (i >= array.length || comparator.compare(item, array[i]) > 0) {
                    result[i] = item;
                    for (int j = i + 1; j < result.length; j++) {
                        result[j] = array[i];
                        i++;
                    }
                    return result;
                } else {
                    result[i] = array[i];
                }
            }
        }
        return result;
    }

    public static <T> T[] remove(T[] array, T item, Class<T> clazz) {
        if (item == null) {
            return array;
        }
        T[] result = (T[]) Array.newInstance(clazz, array.length - 1);
        boolean found = false;

        for (int i = 0; i < result.length; i++) {
            if (array[i].equals(item)) {
                found = true;
            }
            if (found) {
                result[i] = array[i + 1];
            } else {
                result[i] = array[i];
            }
        }
        return result;
    }
}
