package edu.upenn.cit5940.processor;

import edu.upenn.cit5940.common.dto.Article;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static edu.upenn.cit5940.processor.InvertedIndex.*;

/**
 * Builds the derived, in-memory indexes that the Logic-tier services need but
 * that don't come directly out of the parsed article map. This is meant to be
 * called once from {@code Main}, right after articles are loaded, per the
 * design rule that dependency construction belongs in {@code Main} rather than
 * inside the services themselves.
 */
public final class IndexBuilder {

    private IndexBuilder() {
        // static utility class, not instantiable
    }

    /**
     * Assigns each article a sequential integer docId, feeds (docId, title + " " + body)
     * into the given InvertedIndex, and returns the docId -> Article mapping so search
     * results (which come back as docIds) can be resolved to titles.
     */
    public static Map<Integer, Article> indexArticles(InvertedIndex invertedIndex, Collection<Article> articles) {
        Map<Integer, Article> docIdToArticle = new HashMap<>();
        int docId = 0; // simple counter — first article gets id 0, next gets 1, etc.

        for (Article article : articles) {
            // Remember which Article this docId belongs to, so SearchService
            // can translate InvertedIndex's search results (docIds) back into
            // real articles/titles later.
            docIdToArticle.put(docId, article);

            // InvertedIndex indexes ONE blob of text per document — combine
            // title and body so keywords from either count as a match.
            // safe() guards against a null title/body throwing an NPE on concat.
            String text = safe(article.getTitle()) + " " + safe(article.getBody());
            invertedIndex.addDocument(docId, text);

            docId++; // move to the next id for the next article
        }
        return docIdToArticle;
    }

    /**
     * Builds a TreeMap of "YYYY-MM" period -> (word -> frequency), counted across
     * both the title and body of every article in that period, using the exact
     * same tokenization and stop-word rules as {@link InvertedIndex} so results
     * stay consistent with search. Articles with a missing/malformed date are
     * skipped (their word counts just don't contribute to any period).
     *
     * <p>Powers both {@code topics <period>} (ArticleAnalyticsService) and
     * {@code trends <topic> <start> <end>} (TopicTrendService) -- built once
     * and shared between them rather than computed twice.
     */
    public static TreeMap<String, Map<String, Integer>> buildPeriodWordCounts(Collection<Article> articles) {
        // TreeMap (not HashMap) because periods need to come out in sorted
        // order for trends' range queries later.
        TreeMap<String, Map<String, Integer>> periodWordCounts = new TreeMap<>();

        for (Article article : articles) {
            // Turn "2024-01-15" into "2024-01"; skip articles with a
            // missing/malformed date since we can't bucket them into a period.
            String period = extractPeriod(article.getDate());
            if (period == null) {
                continue;
            }

            // computeIfAbsent: if this is the first article we've seen for this
            // period, create a fresh empty word-count map for it; otherwise
            // reuse the one already there. Either way we get back the map to
            // add this article's words into.
            Map<String, Integer> counts = periodWordCounts.computeIfAbsent(period, k -> new HashMap<>());

            String text = safe(article.getTitle()) + " " + safe(article.getBody());

            // Reuse InvertedIndex's exact tokenizer/stop-word list (now
            // package-private) so a word counts as "the same word" here as it
            // does during search — no risk of the two drifting out of sync.
            for (String word : InvertedIndex.tokenize(text)) {
                if (word == null || word.isEmpty() || InvertedIndex.STOP_WORDS.contains(word)) {
                    continue; // skip blanks and stop words — they don't count as "topics"
                }
                // merge: if "word" isn't in the map yet, insert it with count 1;
                // if it's already there, add 1 to its existing count.
                counts.merge(word, 1, Integer::sum);
            }
        }
        return periodWordCounts;
    }

    /**
     * Builds a TreeMap of "YYYY-MM-DD" date -> articles published that day.
     * Because YYYY-MM-DD strings sort lexicographically in the same order as
     * chronologically, TreeMap's natural ordering (and subMap) gives sorted,
     * inclusive-range date queries for free -- this powers
     * {@code articles <start_date> <end_date>} (ArticleService).
     */
    public static TreeMap<String, List<Article>> buildDateIndex(Collection<Article> articles) {
        TreeMap<String, List<Article>> dateIndex = new TreeMap<>();
        for (Article article : articles) {
            String date = article.getDate();
            if (date == null || date.isEmpty()) {
                continue; // can't bucket an article with no date
            }
            // A single date can have multiple articles, so each key maps to a
            // LIST, not a single Article. computeIfAbsent creates that list
            // the first time we see a given date, then we just add() to it.
            dateIndex.computeIfAbsent(date, k -> new ArrayList<>()).add(article);
        }
        return dateIndex;
    }

    /** Extracts the "YYYY-MM" prefix from a "YYYY-MM-DD" date, or null if the date looks invalid. */
    private static String extractPeriod(String date) {
        if (date == null || date.length() < 7) {
            return null;
        }
        String period = date.substring(0, 7); // "2024-01-15" -> "2024-01"
        // Sanity-check the shape (4 digits, dash, 2 digits) before trusting it —
        // guards against garbage/malformed date strings sneaking through.
        return period.matches("\\d{4}-\\d{2}") ? period : null;
    }

    /** Returns the empty string instead of null, so string concatenation never throws an NPE. */
    private static String safe(String s) {
        return s == null ? "" : s;
    }
}