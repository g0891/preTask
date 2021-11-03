package org.example.preTask.data;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class StatisticsStorageDaoImplTest {
    static StatisticsStorageDaoImpl statisticsStorageDao;
    String url = "https://simbirsoft.ru";

    @BeforeAll
    static void prepareAll(){
        statisticsStorageDao = new StatisticsStorageDaoImpl();
        if (statisticsStorageDao.isCreated()) statisticsStorageDao.clearAll();
    }

    @AfterAll
    static void close() {
        statisticsStorageDao.close();
    }

    @BeforeEach
    void prepareTest(){
        if (statisticsStorageDao.isCreated()) {
            statisticsStorageDao.clearAll();
            HashMap<String, Long> map = new HashMap<>();
            map.put("good", 3L);
            map.put("bad", 1L);
            Statistics statistics = new Statistics(url, map);
            statisticsStorageDao.create(statistics);
        }
    }

    @Test
    void isPresent() {
        assertTrue(statisticsStorageDao.isCreated());
        assertFalse(statisticsStorageDao.isPresent("http://NotExistingUrl"));
        assertTrue(statisticsStorageDao.isPresent(url));
    }

    @Test
    void create() {
        assertTrue(statisticsStorageDao.isCreated());
        Optional<Statistics> stat = statisticsStorageDao.get(url);
        assertTrue(stat.isPresent());
        if (stat.isPresent()) {
            assertEquals(2, stat.get().getWordsMap().size());
        }
    }

    @Test
    void get() {
        assertTrue(statisticsStorageDao.isCreated());
        Optional<Statistics> stat = statisticsStorageDao.get(url);
        assertTrue(stat.isPresent());
        stat = statisticsStorageDao.get("http://NotExistingUrl");
        assertTrue(stat.isEmpty());
    }



    @Test
    void update() {
        assertTrue(statisticsStorageDao.isCreated());
        HashMap<String, Long> map = new HashMap<>();
        map.put("good", 5L);
        map.put("other", 7L);
        Statistics newStat = new Statistics(url, map);
        statisticsStorageDao.update(newStat);
        Optional<Statistics> getStat = statisticsStorageDao.get(url);
        assertTrue(getStat.isPresent());
        assertEquals(2, getStat.get().getWordsMap().size());
        assertTrue(getStat.get().getWordsMap().containsKey("good"));
        assertTrue(getStat.get().getWordsMap().containsKey("other"));
        assertEquals(5L,getStat.get().getWordsMap().get("good"));
        assertEquals(7L,getStat.get().getWordsMap().get("other"));
    }


}