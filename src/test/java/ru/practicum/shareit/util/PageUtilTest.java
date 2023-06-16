package ru.practicum.shareit.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

public class PageUtilTest {

    @Test
    public void testGetStartPageFromLessSize() {
        Assertions.assertEquals(0, PageUtil.getStartPage(9, 10));
    }

    @Test
    public void testGetStartPageFromMoreSize() {
        Assertions.assertEquals(1, PageUtil.getStartPage(19, 10));
    }

    @Test
    public void testGetStartFromFromLessSize() {
        Assertions.assertEquals(2, PageUtil.getStartFrom(2, 5));
    }

    @Test
    public void testGetStartFromFromMoreSize() {
        Assertions.assertEquals(1, PageUtil.getStartFrom(5, 2));
    }

    @Test
    public void testIsTwoSite1PageFromLessSize() {
        Assertions.assertEquals(false, PageUtil.isTwoSite(0, 10));
    }

    @Test
    public void testIsTwoSite1PageFromMoreSize() {
        Assertions.assertEquals(false, PageUtil.isTwoSite(6, 3));
    }

    @Test
    public void testIsTwoSite2PageFromLessSize() {
        Assertions.assertEquals(true, PageUtil.isTwoSite(7, 10));
    }

    @Test
    public void testIsTwoSite2PageFromMoreSize() {
        Assertions.assertEquals(true, PageUtil.isTwoSite(7, 3));
    }

    @Test
    public void testGetPageListForTwoPageBetween() {
        List<String> list = new ArrayList<>(List.of("1", "2", "3", "4", "5", "6"));
        Assertions.assertEquals(3, PageUtil.getPageListForTwoPage(list, 2, 3).size());
    }

    @Test
    public void testGetPageListForTwoPageEnd() {
        List<String> list = new ArrayList<>(List.of("1", "2", "3", "4", "5", "6"));
        Assertions.assertEquals(4, PageUtil.getPageListForTwoPage(list, 2, 10).size());
    }

    @Test
    public void testGetPageListForTwoPageLast() {
        List<String> list = new ArrayList<>(List.of("1", "2", "3", "4", "5", "6"));
        Assertions.assertEquals(1, PageUtil.getPageListForTwoPage(list, 5, 10).size());
    }

    @Test
    public void testGetPageListForTwoPageStart() {
        List<String> list = new ArrayList<>(List.of("1", "2", "3", "4", "5", "6"));
        Assertions.assertEquals(2, PageUtil.getPageListForTwoPage(list, 0, 2).size());
    }
}
