package edu.upenn.cit5940;

import edu.upenn.cit5940.common.dto.Article;
import edu.upenn.cit5940.datamanagement.ArticleCSVParser;
import edu.upenn.cit5940.datamanagement.CharacterReader;
import edu.upenn.cit5940.datamanagement.DataParser;
import edu.upenn.cit5940.datamanagement.JsonArticleReader;
import edu.upenn.cit5940.logging.Logger;
import edu.upenn.cit5940.processor.*;
import edu.upenn.cit5940.ui.CommandLineInterface;

import java.io.BufferedReader;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;


public class Main {

	//initialize default paths for data and log files
    private static final String DEFAULT_DATA_PATH = "articles.csv";
    private static final String DEFAULT_LOG_PATH = "tech_news_search.log";

    public static void main(String[] args) {
        
    	//Parse command line arguments
        String dataFilePath = (args.length > 0 && !args[0].isBlank()) ? args[0] : DEFAULT_DATA_PATH;
        String logFilePath = (args.length > 1 && !args[1].isBlank()) ? args[1] : DEFAULT_LOG_PATH;

        //initialize logger
        Logger logger = Logger.getInstance(logFilePath);
        logger.info("Application starting...");
        System.out.println("=== Tech News Search Engine ===");
        System.out.println("Initializing n-tier architecture...");

        //validate data file existence and readability
        File dataFile = new File(dataFilePath);
        if (!dataFile.exists() || !dataFile.canRead()) {
            System.out.println("Cannot read data file at path: " + dataFilePath);
            logger.error("cannot read data file at " + dataFilePath);
            return;
        }

        System.out.println("Loading articles from: " + dataFilePath);
        
        //load articles based on file extension
        try {
            Map<String, Article> articles;
            String lowerPath = dataFilePath.toLowerCase();

            //instantiate parser based on file extension
            if (lowerPath.endsWith(".json")) {
                try (BufferedReader reader = Files.newBufferedReader(Paths.get(dataFilePath))) {
                    DataParser parser = new JsonArticleReader(reader);
                    articles = parser.readAllArticles();
                }
            } else if (lowerPath.endsWith(".csv")) {
                try (CharacterReader characterReader = new CharacterReader(dataFilePath)) {
                    DataParser parser = new ArticleCSVParser(characterReader);
                    articles = parser.readAllArticles();
                }
            //exit gracefully if unsupported file format is provided
            } else {
                System.out.println("Error: Unsupported data file format. Please provide a .csv or .json file.");
                logger.error("Unsupported data file format: " + dataFilePath);
                return;
            }
            //exit gracefully if no valid articles were loaded
            if (articles == null || articles.isEmpty()) {
                System.out.println("Error: No valid articles loaded from " + dataFilePath);
                logger.error("Fatal error: Zero valid articles parsed from " + dataFilePath);
                return;
            }

            System.out.println(articles.size() + " articles loaded");
            System.out.println("Architecture initialization complete!");
            logger.info("Successfully loaded " + articles.size() + " articles from " + dataFilePath);

            //build Indexing Data Structures
            logger.info("Building inverted index and search structures...");
            
            InvertedIndex invertedIndex = new InvertedIndex();
            Map<Integer, Article> articlesByDocId = IndexBuilder.indexArticles(invertedIndex, articles.values());
            TreeMap<String, Map<String, Integer>> periodWordCounts = IndexBuilder.buildPeriodWordCounts(articles.values());
            TreeMap<String, List<Article>> dateIndex = IndexBuilder.buildDateIndex(articles.values());

            CustomTrie autocompleteTrie = new CustomTrie();
            for (Article article : articles.values()) {
                if (article.getTitle() == null) {
                    continue;
                }

                String cleanedTitle = article.getTitle()
                        .toLowerCase()
                        .replaceAll("[^a-z0-9\\s-]", " ");

                for (String word : cleanedTitle.split("\\s+")) {
                    if (!word.isEmpty() && word.length() > 1) {
                        autocompleteTrie.insertWord(word);
                    }
                }
            }
            
            //initialize services for each layer of the architecture
            SearchService searchService = new SearchService(invertedIndex, articlesByDocId);
            AutocompleteService autocompleteService = new AutocompleteService(autocompleteTrie);
            ArticleAnalyticsService analyticsService = new ArticleAnalyticsService(articles, periodWordCounts);
            TopicTrendService trendService = new TopicTrendService(periodWordCounts);
            ArticleService articleService = new ArticleService(articles, dateIndex);

            //initialize CLI for user interaction
            CommandLineInterface cli = new CommandLineInterface(
                    searchService,
                    autocompleteService,
                    analyticsService,
                    trendService,
                    articleService
            );
            
            logger.info("Entering interactive CLI session...");
            cli.run();
            
         //log application exit
        } catch (Exception e) {
            System.out.println("Error loading application: " + e.getMessage());
            logger.error("Fatal exception during execution: " + e.getMessage());
        }
        finally {
            logger.close();
        }
    }
}