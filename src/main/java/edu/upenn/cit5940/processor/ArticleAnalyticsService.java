package edu.upenn.cit5940.processor;

import edu.upenn.cit5940.common.dto.Article;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.TreeMap;

//3. View Top Topics
//7. Show Statistics
public class ArticleAnalyticsService {

    private static final int TOP_N = 10;

    private final Map<String, Article> articlesByUri;
    private final TreeMap<String, Map<String, Integer>> periodWordCounts;

    /**
     * @param articlesByUri     URI-keyed article map, used for getStats()
     * @param periodWordCounts  "YYYY-MM" \u2192 (word \u2192 count), from
     *                          {@link IndexBuilder#buildPeriodWordCounts}, used for topTopics()
     */
    public ArticleAnalyticsService(Map<String, Article> articlesByUri,
                                   TreeMap<String, Map<String, Integer>> periodWordCounts) {
        this.articlesByUri = articlesByUri;
        this.periodWordCounts = periodWordCounts;
    }

    /** Returns a human-readable summary of data statistics. */
    public String getStats() {
        return "Total articles: " + articlesByUri.size();
    }

    /**
     * Returns the top 10 trending words (ignoring stop words, counted across
     * title + body) for the given month, formatted as "word: count" lines in
     * descending order of frequency.
     *
     * <p>Uses a bounded min-heap of size 10 (java.util.PriorityQueue) rather than
     * sorting the whole word list \u2014 O(n log 10) instead of O(n log n), the same
     * "keep only the top k" technique CustomHeap demonstrates, just able to
     * carry (word, count) pairs, which CustomHeap's raw-int API can't.
     */
    public List<String> topTopics(String period) {
        Map<String, Integer> counts = periodWordCounts.get(period);
        if (counts == null || counts.isEmpty()) {
            return Collections.emptyList();
        }

        PriorityQueue<Map.Entry<String, Integer>> minHeap =
                new PriorityQueue<>(TOP_N + 1, Comparator.comparingInt(Map.Entry::getValue));

        for (Map.Entry<String, Integer> entry : counts.entrySet()) {
            minHeap.offer(entry);
            if (minHeap.size() > TOP_N) {
                minHeap.poll(); // evict the current smallest, keeping only the top TOP_N so far
            }
        }

        List<Map.Entry<String, Integer>> top = new ArrayList<>(minHeap);
        top.sort((a, b) -> b.getValue() - a.getValue()); // descending by count for display

        List<String> result = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : top) {
            result.add(entry.getKey() + ": " + entry.getValue());
        }
        return result;
    }
}
