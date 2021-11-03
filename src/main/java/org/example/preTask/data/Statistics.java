package org.example.preTask.data;

import java.util.Map;

public class Statistics {
    private final String url;
    private final Map<String,Long> wordsMap;

    public Statistics(String url, Map<String,Long> map) {
        this.url = url;
        this.wordsMap = map;
    }

    public void print(){
        this.print(false);
    }

    public void print(boolean reverseOrder) {
        int order = reverseOrder ? -1 : 1;
        wordsMap.entrySet()
                .stream()
                .sorted((e1, e2) -> (int) (order * (e1.getValue() - e2.getValue())))
                .forEach(entry -> System.out.printf("%d - %s\n", entry.getValue(), entry.getKey()));
    }

    public String getUrl() {
        return url;
    }

    public Map<String, Long> getWordsMap() {
        return wordsMap;
    }
}
