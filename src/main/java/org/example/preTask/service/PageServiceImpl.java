package org.example.preTask.service;

import org.example.preTask.data.Statistics;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public class PageServiceImpl implements PageService {
    private static final Logger logger = LoggerFactory.getLogger(PageServiceImpl.class);

    @Override
    public Optional<Statistics> getWordsStatistics(String url){
        Document doc;
        try {
            doc = Jsoup.connect(url).get();
            return Optional.of(new Statistics(url, documentToMap(doc)));
        } catch (Exception ex) {
            logger.error("Failed to retrieve requested page.", ex);
            return Optional.empty();
        }
    }

    public Map<String, Long> documentToMap(Document doc) {
        return Arrays.stream(doc.text().split("[ ,.!?\";:\\[\\]()\n\r\t]+"))
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
    }
}
