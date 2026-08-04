package edu.upenn.cit5940.datamanagement;

import edu.upenn.cit5940.common.dto.Article;
import edu.upenn.cit5940.logging.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Map;

/***
 * ArticleRepository is responsible for loading and storing articles from a dataset file (JSON or CSV).
 * It provides methods to access the loaded articles and their count.
 */
public class ArticleRepository {

    private final Map<String, Article> articles;

    public ArticleRepository(String dataFilePath) throws IOException, CSVFormatException {
        Logger logger = Logger.getInstance();
        logger.info("ArticleRepository: Loading dataset from " + dataFilePath);

        Map<String, Article> loadedArticles;

        try (BufferedReader reader = Files.newBufferedReader(Paths.get(dataFilePath))) {
            DataParser parser;

            //pick parser based on file extension
            if (dataFilePath.toLowerCase().endsWith(".json")) {
                parser = new JsonArticleReader(reader);
            } else if (dataFilePath.toLowerCase().endsWith(".csv")) {
                CharacterReader charReader = new CharacterReader(reader);
                parser = new ArticleCSVParser(charReader);
            } else {
                logger.error("Unsupported file extension for path: " + dataFilePath);
                throw new IllegalArgumentException("Unsupported file format. Please provide a .json or .csv file.");
            }

            loadedArticles = parser.readAllArticles();
        }
        
        // Handle the case where the dataset is empty or contains zero valid articles
        if (loadedArticles == null || loadedArticles.isEmpty()) {
            logger.error("Dataset is empty or contains zero valid articles.");
            this.articles = Collections.emptyMap();
        } else {
            this.articles = Collections.unmodifiableMap(loadedArticles);
            logger.info("Successfully initialized with " + this.articles.size() + " articles.");
        }
    }

    /**
     * Returns an unmodifiable map of all loaded articles keyed by URI.
     */
    public Map<String, Article> getAllArticles() {
        return this.articles;
    }

    /**
     * method to get total article count.
     */
    public int getArticleCount() {
        return this.articles.size();
    }
}