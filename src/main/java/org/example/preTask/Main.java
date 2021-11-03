package org.example.preTask;

import org.example.preTask.data.Statistics;
import org.example.preTask.data.StatisticsStorageDao;
import org.example.preTask.data.StatisticsStorageDaoImpl;
import org.example.preTask.service.PageService;
import org.example.preTask.service.PageServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Scanner;

public class Main {

    private static final Scanner scanner = new Scanner(System.in);
    private static final PageService pageService = new PageServiceImpl();
    private static final StatisticsStorageDao statisticsStorageDao = new StatisticsStorageDaoImpl();
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        if (!statisticsStorageDao.isCreated()) {
            logger.info("Application stopped.");
            return;
        }

        logger.info("Application started.");

        while (true) {
            System.out.println("Please choose an option:\n[1] Get statistics\n[2] Exit");
            switch (scanner.nextLine()) {
                case "1":
                    System.out.print("Please enter URL: ");
                    String url = scanner.nextLine();

                    if (statisticsStorageDao.isPresent(url)) {
                        System.out.println("Do you want to get previously calculated statistics for this URL or calculate a new one?");
                        System.out.println("[1] Get previously calculated\n[2] Calculate a new one");
                        switch (scanner.nextLine()) {
                            case "1":
                                statisticsStorageDao.get(url).ifPresent(Statistics::print);
                                break;
                            case "2":
                                pageService.getWordsStatistics(url).ifPresent(stat -> {
                                    stat.print();
                                    statisticsStorageDao.update(stat);
                                });
                                break;
                            default:
                                System.out.println("Incorrect input. 1 or 2 expected.");
                        }
                    } else {
                        pageService.getWordsStatistics(url).ifPresent(stat -> {
                            stat.print();
                            statisticsStorageDao.update(stat);
                        });
                    }
                    break;
                case "2":
                    logger.info("Application stopped.");
                    return;
                default:
                    System.out.println("Incorrect input. 1 or 2 expected.");
            }
        }
    }


}
