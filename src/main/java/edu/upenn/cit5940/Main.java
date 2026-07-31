package edu.upenn.cit5940;

import edu.upenn.cit5940.common.dto.Article;
import edu.upenn.cit5940.datamanagement.DataParser;
import edu.upenn.cit5940.datamanagement.JsonArticleReader;
import edu.upenn.cit5940.processor.*;
import edu.upenn.cit5940.ui.CommandLineInterface;

import edu.upenn.cit5940.processor.ArticleAnalyticsService;
import edu.upenn.cit5940.processor.ArticleService;
import edu.upenn.cit5940.processor.AutocompleteService;
import edu.upenn.cit5940.processor.CustomTrie;
import edu.upenn.cit5940.processor.IndexBuilder;
import edu.upenn.cit5940.processor.InvertedIndex;
import edu.upenn.cit5940.processor.SearchService;
import edu.upenn.cit5940.processor.TopicTrendService;


import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class Main {
	public static void main(String[] args) {

		String dataFilePath = args.length > 0 ? args[0] : "articles_small.json";

		//testing the JSON reader
		try (var reader = Files.newBufferedReader(Paths.get(dataFilePath))) {
			DataParser jsonParser = new JsonArticleReader(reader);
			Map<String, Article> articles = jsonParser.readAllArticles();

			System.out.println("Successfully loaded " + articles.size() + " JSON articles!");

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
					if (!word.isEmpty()) {
						autocompleteTrie.insertWord(word);
					}
				}
			}

			SearchService searchService = new SearchService(invertedIndex, articlesByDocId);
			AutocompleteService autocompleteService = new AutocompleteService(autocompleteTrie);
			ArticleAnalyticsService analyticsService = new ArticleAnalyticsService(articles, periodWordCounts);
			TopicTrendService trendService = new TopicTrendService(periodWordCounts);
			ArticleService articleService = new ArticleService(articles, dateIndex);

			CommandLineInterface cli = new CommandLineInterface(
					searchService,
					autocompleteService,
					analyticsService,
					trendService,
					articleService
			);

			cli.printStartupMessages(dataFilePath, articles.size());
			cli.run();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}