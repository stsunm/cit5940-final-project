package edu.upenn.cit5940.processor;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

//4. Analyze Topic Trends
public class TopicTrendService {

    private final TreeMap<String, Map<String, Integer>> periodWordCounts;

    /**
     * @param periodWordCounts "YYYY-MM" -> (word -> count), from
     *                         {@link IndexBuilder#buildPeriodWordCounts} -- the same
     *                         pre-stored data ArticleAnalyticsService uses for topTopics,
     *                         built once and shared rather than computed twice.
     */
    public TopicTrendService(TreeMap<String, Map<String, Integer>> periodWordCounts) {
        this.periodWordCounts = periodWordCounts;
    }

    /**
     * Returns the monthly frequency of a topic across an inclusive period range,
     * one "YYYY-MM: count" line per month -- including months with a count of 0,
     * since the spec calls for "each month in the range", not just months that
     * happened to have data.
     */
    public List<String> topicTrends(String topic, String startPeriod, String endPeriod) {
        // Run the topic word through the SAME tokenization InvertedIndex uses,
        // so "AI" (user input) matches "ai" (how it's stored as a key).
        String normalizedTopic = normalize(topic);

        List<String> result = new ArrayList<>();

        // YearMonth is a built-in java.time type for exactly a "YYYY-MM" value —
        // it knows how to parse that format and step forward one month at a time,
        // which saves us from hand-rolling month/year rollover math (e.g. Dec -> Jan).
        YearMonth start = YearMonth.parse(startPeriod);
        YearMonth end = YearMonth.parse(endPeriod);

        // Walk every month from start to end, inclusive. We deliberately do NOT
        // just call periodWordCounts.subMap(start, end) here: subMap only
        // returns months that already exist as keys, so a month with zero
        // matching articles would be silently skipped. Explicitly stepping
        // through each month guarantees every month in the range shows up in
        // the output, even as "0".
        for (YearMonth month = start; !month.isAfter(end); month = month.plusMonths(1)) {
            String period = month.toString(); // YearMonth.toString() formats as "YYYY-MM"

            // O(log n) TreeMap lookup for this month's word-count data.
            Map<String, Integer> counts = periodWordCounts.get(period); // TreeMap-backed lookup

            // If this month has no data at all, or the topic just never came
            // up that month, the count is 0.
            int count = (counts == null) ? 0 : counts.getOrDefault(normalizedTopic, 0);
            result.add(period + ": " + count);
        }
        return result;
    }

    /**
     * Runs the raw topic string through InvertedIndex's tokenizer (lowercases,
     * strips punctuation) and takes the first resulting token, so lookups
     * against periodWordCounts use the exact same word format as the keys
     * IndexBuilder stored them under.
     */
    private String normalize(String topic) {
        String[] tokens = InvertedIndex.tokenize(topic == null ? "" : topic);
        return tokens.length > 0 ? tokens[0] : "";
    }
}