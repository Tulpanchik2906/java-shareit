package ru.practicum.shareit;

public class TestUtil {
    private static volatile long currentCountEmail = 0;

    public static String getRandomPartForEmail() {
        currentCountEmail++;
        return String.valueOf(currentCountEmail);
    }
}
