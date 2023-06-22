package ru.practicum.shareit;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.Collections;

public class TestUtil {
    private static volatile long currentCountEmail = 0;

    public static String getRandomPartForEmail() {
        currentCountEmail++;
        return String.valueOf(currentCountEmail);
    }

    public static MultiValueMap getPageParams(Integer from, Integer size) {
        MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
        if (from != null) {
            parameters.put("from", Collections.singletonList(String.valueOf(from)));
        }
        if (size != null) {
            parameters.put("size", Collections.singletonList(String.valueOf(size)));
        }
        return parameters;
    }

}
