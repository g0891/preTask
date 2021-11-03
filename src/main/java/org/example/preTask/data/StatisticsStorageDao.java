package org.example.preTask.data;

import java.util.Optional;

public interface StatisticsStorageDao {
    boolean isCreated();
    boolean isPresent(String url);
    Optional<Statistics> get(String url);
    void create(Statistics statistics);
    void update(Statistics statistics);
    void delete(String url);
    void close();

}
