package org.example.preTask.service;

import org.example.preTask.data.Statistics;

import java.util.Optional;

public interface PageService {
    public Optional<Statistics> getWordsStatistics(String url);
}
