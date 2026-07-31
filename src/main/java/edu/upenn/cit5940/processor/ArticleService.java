package edu.upenn.cit5940.processor;

import edu.upenn.cit5940.common.dto.Article;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

//5. Browse Articles by Date
//6. View Specific Article by ID
public class ArticleService {

    private final Map<String, Article> articlesByUri;
    private final TreeMap<String, List<Article>> articlesByDate;

    /**
     * @param articlesByUri  the Map<String, Article> already produced by
     *                       ArticleCSVParser.readAllArticles() / the JSON reader,
     *                       keyed by article URI, used for getArticleById()
     * @param articlesByDate "YYYY-MM-DD" -> articles published that day, from
     *                       {@link IndexBuilder#buildDateIndex}, used for
     *                       articlesByDateRange()
     */
    public ArticleService(Map<String, Article> articlesByUri, TreeMap<String, List<Article>> articlesByDate) {
        this.articlesByUri = articlesByUri;
        this.articlesByDate = articlesByDate;
    }

    /**
     * Returns a human-readable detail block for a single article, or {@code null}
     * if no article has that ID.
     */
    public String getArticleById(String id) {
        // Simple O(1) HashMap-style lookup by URI (the "id" the CLI refers to
        // is really the article's URI).
        Article article = articlesByUri.get(id);
        if (article == null) {
            return null; // caller (the CLI) turns null into a "not found" message
        }
        // Build a human-readable multi-line summary of the article's fields.
        return "Title: " + article.getTitle()
                + "\nDate: " + article.getDate()
                + "\nURI: " + article.getUri()
                + "\nBody: " + article.getBody();
    }

    /**
     * Returns titles of articles published within an inclusive date range,
     * sorted chronologically -- since YYYY-MM-DD keys sort lexicographically in
     * the same order as chronologically, TreeMap.subMap() gives both the range
     * filter and the sort order for free.
     */
    public List<String> articlesByDateRange(String startDate, String endDate) {
        List<String> titles = new ArrayList<>();

        // subMap(startDate, true, endDate, true) returns a *view* of just the
        // keys between startDate and endDate, INCLUSIVE on both ends (the two
        // `true` flags). Because TreeMap keeps keys sorted, this view is
        // already in chronological order — no separate sort step needed.
        SortedMap<String, List<Article>> range = articlesByDate.subMap(startDate, true, endDate, true);

        // Each date key can map to MULTIPLE articles (several published the
        // same day), so we need a nested loop: outer loop walks the dates in
        // range, inner loop walks that day's articles.
        for (List<Article> articlesOnDate : range.values()) {
            for (Article article : articlesOnDate) {
                titles.add(article.getTitle());
            }
        }
        return titles;
    }
}