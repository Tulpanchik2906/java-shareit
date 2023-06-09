package ru.practicum.shareit.util;

import java.util.List;
import java.util.stream.Collectors;

public class PageUtil {

    public static int getStartPage(int from, int size) {
        if (from < size) {
            return 0;
        } else {
            return from / size;
        }
    }

    public static int getStartFrom(int from, int size) {
        return from % size;
    }

    public static boolean isTwoSite(int from, int size) {
        if (from < size) {
            if (from == 0) {
                return false;
            } else {
                return true;
            }
        } else {
            if (from % size == 0) {
                return false;
            } else {
                return true;
            }
        }
    }

    /*
        В параметре list передаются элементы 2-х страниц
     */
    public static List getPageListForTwoPage(
            List list, int startFrom, int size) {
        // Удалить все верхние item со страницы
        for (int i = 0; i < startFrom; i++) {
            if (!list.isEmpty()) {
                list.remove(0);
            }else {
                break;
            }
        }

        return (List) list.stream().limit(size).collect(Collectors.toList());
    }
}
