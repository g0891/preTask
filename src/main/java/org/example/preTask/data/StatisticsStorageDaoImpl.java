package org.example.preTask.data;

import org.example.preTask.Main;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

public class StatisticsStorageDaoImpl implements StatisticsStorageDao {

    private static final Logger logger = LoggerFactory.getLogger(StatisticsStorageDaoImpl.class);

    String connectionPath;
    private Connection conn;
    private Statement stmt;
    private PreparedStatement getUrlsExistenceStatement;
    private PreparedStatement getUrlsStatisticsStatement;
    private PreparedStatement deleteStatisticsForUrlStatement;
    private PreparedStatement deleteUrlStatement;
    private PreparedStatement insertUrlStatement;
    private PreparedStatement insertWordStatement;
    private PreparedStatement insertWordStatisticsStatement;
    private PreparedStatement clearAll;

    private boolean created = false;

    public StatisticsStorageDaoImpl(){


        try (InputStream input = Main.class.getClassLoader().getResourceAsStream("db.properties")) {
            Properties prop = new Properties();
            prop.load(input);
            connectionPath = prop.getProperty("connection.path");
            logger.info("Connecting to DB " + connectionPath);
            conn = DriverManager.getConnection(connectionPath);
            conn.setAutoCommit(true);
            stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS URLS ("
                    + "ID INT PRIMARY KEY AUTO_INCREMENT,"
                    + "URL VARCHAR UNIQUE NOT NULL"
                    + ")");
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS WORDS ("
                    + "ID INT PRIMARY KEY AUTO_INCREMENT,"
                    + "WORD VARCHAR UNIQUE NOT NULL"
                    + ")");
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS STATISTICS ("
                    + "URL_ID INT NOT NULL,"
                    + "WORD_ID INT NOT NULL,"
                    + "COUNT BIGINT NOT NULL,"
                    + "CONSTRAINT fk_url FOREIGN KEY (URL_ID) REFERENCES URLS(ID),"
                    + "CONSTRAINT fk_word FOREIGN KEY (WORD_ID) REFERENCES WORDS(ID),"
                    + "CONSTRAINT pk_statistics PRIMARY KEY (URL_ID, WORD_ID)"
                    + ")");
            getUrlsExistenceStatement = conn.prepareStatement("SELECT * FROM URLS WHERE URL=? LIMIT 1");
            getUrlsStatisticsStatement = conn.prepareStatement("select words.word as WORD, stat.count as COUNT " +
                    "from (select ID from URLS where URL=? limit 1) url " +
                    "LEFT JOIN Statistics stat ON url.ID=stat.URL_ID " +
                    "LEFT JOIN words on stat.WORD_ID=words.ID");
            deleteStatisticsForUrlStatement = conn.prepareStatement("delete from STATISTICS WHERE URL_ID in (select ID from URLS WHERE URL=?)");
            deleteUrlStatement = conn.prepareStatement("delete from URLS where URL=?");
            insertUrlStatement = conn.prepareStatement("insert into URLS (URL) values(?)");
            insertWordStatement = conn.prepareStatement("insert into WORDS (word) SELECT ? WHERE NOT EXISTS (SELECT word FROM words where word=?)");
            insertWordStatisticsStatement = conn.prepareStatement("insert into statistics values((select ID from URLS where url=?),(select id from words where word=?),?)");

            clearAll = conn.prepareStatement("DELETE FROM STATISTICS; DELETE FROM URLS; DELETE FROM WORDS;");

            created = true;
            logger.info("Connected to DB.");
        } catch (SQLException | IOException ex) {
            logger.error("Failed to connect to DB.", ex);
        }

    }

    @Override
    public boolean isCreated() {
        return created;
    }

    @Override
    public boolean isPresent(String url){
        try {
            getUrlsExistenceStatement.setString(1, url);
            ResultSet rs = getUrlsExistenceStatement.executeQuery();
            return rs.next();
        } catch (SQLException ex) {
            logger.error("Failed to get Statistics from DB.", ex);
            return false;
        }
    }

    @Override
    public Optional<Statistics> get(String url) {
        try {

            getUrlsExistenceStatement.setString(1, url);
            ResultSet rs = getUrlsExistenceStatement.executeQuery();
            if (!rs.next()) return Optional.empty();
            getUrlsStatisticsStatement.setString(1, url);
            rs = getUrlsStatisticsStatement.executeQuery();
            Map<String,Long> wordsMap = new HashMap<>();

            while (rs.next()) {
                wordsMap.put(rs.getString("WORD"),rs.getLong("COUNT"));
            }

            return Optional.of(new Statistics(url, wordsMap));

        } catch (SQLException ex) {
            logger.error("Failed to get Statistics from DB.", ex);
            return Optional.empty();
        }
    }

    @Override
    public void create(Statistics statistics) {
        try {
            insertUrlStatement.setString(1, statistics.getUrl());
            insertUrlStatement.executeUpdate();
            for(String word: statistics.getWordsMap().keySet()) {
                insertWordStatement.setString(1, word);
                insertWordStatement.setString(2, word);
                insertWordStatement.executeUpdate();
            }
            insertWordStatisticsStatement.setString(1, statistics.getUrl());
            for (Map.Entry<String,Long> entry: statistics.getWordsMap().entrySet()) {
                insertWordStatisticsStatement.setString(2, entry.getKey());
                insertWordStatisticsStatement.setLong(3, entry.getValue());
                insertWordStatisticsStatement.executeUpdate();
            }
        } catch (SQLException ex) {
            logger.error("Failed to save statistics in DB.", ex);
        }
    }

    @Override
    public void update(Statistics statistics) {
        try {
            deleteStatisticsForUrlStatement.setString(1, statistics.getUrl());
            deleteStatisticsForUrlStatement.executeUpdate();
            deleteUrlStatement.setString(1, statistics.getUrl());
            deleteUrlStatement.executeUpdate();
            create(statistics);
        } catch (SQLException ex) {
            logger.error("Failed to save statistics in DB.", ex);
        }
    }

    @Override
    public void delete(String url) {
        try {
            deleteStatisticsForUrlStatement.setString(1, url);
            deleteStatisticsForUrlStatement.executeUpdate();
            deleteUrlStatement.setString(1, url);
            deleteUrlStatement.executeUpdate(url);
        } catch (SQLException ex) {
            logger.error("Failed to delete Statistics from DB.", ex);
        }
    }

    public boolean clearAll(){
        try {
            clearAll.execute();
            return true;
        } catch (SQLException ex) {
            logger.error("Failed to clear DB.", ex);
            return false;
        }
    }

    @Override
    public void close() {
        if (created) {
            try {
                stmt.close();
                getUrlsExistenceStatement.close();
                getUrlsStatisticsStatement.close();
                deleteStatisticsForUrlStatement.close();
                deleteUrlStatement.close();
                insertUrlStatement.close();
                insertWordStatement.close();
                insertWordStatisticsStatement.close();
                conn.close();

            } catch (SQLException ex) {
                logger.error("Failed to close DB connection correctly.", ex);
            }
        }
    }
}
