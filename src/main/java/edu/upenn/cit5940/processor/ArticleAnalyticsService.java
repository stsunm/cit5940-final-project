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
     * @param periodWordCounts  "YYYY-MM" -> (word -> count), from
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
     * sorting the whole word list -- O(n log 10) instead of O(n log n), the same
     * "keep only the top k" technique CustomHeap demonstrates, just able to
     * carry (word, count) pairs, which CustomHeap's raw-int API can't.
     */
    public List<String> topTopics(String period) {
        // Look up this month's word-count data. If we never saw this period
        // at all (no articles that month), there's nothing to rank.
        Map<String, Integer> counts = periodWordCounts.get(period);
        if (counts == null || counts.isEmpty()) {
            return Collections.emptyList();
        }

        // A MIN-heap ordered by count (smallest count at the top/head).
        // Comparator.comparingInt(Map.Entry::getValue) tells the queue to
        // compare entries by their count value, not by word or object identity.
        PriorityQueue<Map.Entry<String, Integer>> minHeap =
                new PriorityQueue<>(TOP_N + 1, Comparator.comparingInt(Map.Entry::getValue));

        // Walk every (word, count) pair once. This is the "bounded heap"
        // trick for finding the top-K of a large collection without sorting
        // everything: the heap never holds more than TOP_N+1 items at once.
        for (Map.Entry<String, Integer> entry : counts.entrySet()) {
            minHeap.offer(entry); // add this word/count pair to the heap
            if (minHeap.size() > TOP_N) {
                // We now have 11 items, one too many. Since this is a MIN-heap,
                // poll() removes the smallest count — i.e. the current weakest
                // "top 10" contender. Net effect: after every offer+poll pair,
                // the heap holds exactly the TOP_N largest counts seen so far.
                minHeap.poll(); // evict the current smallest, keeping only the top TOP_N so far
            }
        }

        // At this point the heap contains the top 10 (word, count) pairs, but
        // in MIN-heap order (smallest first) — not the order we want to show
        // the user. Dump it into a List and re-sort descending for display.
        List<Map.Entry<String, Integer>> top = new ArrayList<>(minHeap);
        top.sort((a, b) -> b.getValue() - a.getValue()); // descending by count for display

        // Format each pair as "word: count" for the CLI to print one per line.
        List<String> result = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : top) {
            result.add(entry.getKey() + ": " + entry.getValue());
        }
        return result;
    }
}