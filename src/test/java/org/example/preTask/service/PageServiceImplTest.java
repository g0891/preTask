package org.example.preTask.service;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PageServiceImplTest {

    @Test
    void documentToMap() {
        PageServiceImpl pageService = new PageServiceImpl();
        Document doc = Jsoup.parse("<html><body><p>cat dog\nrain sun,rain!dog.dog");
        var map = pageService.documentToMap(doc);
        assertEquals(4, map.size());
        assertTrue(map.containsKey("sun"));
        assertEquals(1, map.get("sun"));
        assertTrue(map.containsKey("dog"));
        assertEquals(3, map.get("dog"));
        assertFalse(map.containsKey("snow"));
    }
}