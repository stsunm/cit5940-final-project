package edu.upenn.cit5940.processor;

import edu.upenn.cit5940.common.dto.Article;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

// 1. Search Articles
public class SearchService {

    private final InvertedIndex invertedIndex;
    private final Map<Integer, Article> articlesByDocId;

    /**
     * @param invertedIndex    the (eventually HashMap-refactored, per HW6) inverted index,
     *                         already populated via addDocument(docId, title + " " + body)
     *                         for every loaded article
     * @param articlesByDocId  maps the same docId used when indexing back to its Article,
     *                         so search results (which come back as docIds) can be turned
     *                         into titles. This mapping doesn't exist anywhere yet — it will
     *                         probably live on ArticleRepository once that's built out, but
     *                         a plain Map works fine here in the meantime.
     */
    public SearchService(InvertedIndex invertedIndex, Map<Integer, Article> articlesByDocId) {
        this.invertedIndex = invertedIndex;
        this.articlesByDocId = articlesByDocId;
    }

    /**
     * Returns titles of articles whose indexed keywords contain ALL of the
     * given search terms (InvertedIndex.search() already does the AND logic
     * and skips stop words internally).
     */
    public List<String> search(List<String> keywords) {
        // Guard clause: no keywords means nothing to search for — return early
        // instead of letting an empty query fall through to InvertedIndex.
        if (keywords == null || keywords.isEmpty()) {
            return Collections.emptyList();
        }

        // InvertedIndex.search() takes ONE query string and tokenizes it
        // internally, so join all keywords with spaces into a single query.
        // e.g. ["ai", "ethics"] -> "ai ethics"
        String query = String.join(" ", keywords);

        // This does the actual AND search: InvertedIndex.search() tokenizes
        // the query, skips stop words, and returns only the docIds whose
        // articles contain EVERY remaining token.
        Set<Integer> docIds = invertedIndex.search(query);

        // InvertedIndex only knows about int docIds, not Article objects —
        // resolve each docId back to its Article so we can grab the title.
        List<String> titles = new ArrayList<>();
        for (Integer docId : docIds) {
            Article article = articlesByDocId.get(docId);
            if (article != null) { // defensive: skip if the docId->Article mapping is ever incomplete
                titles.add(article.getTitle());
            }
        }

        // Set iteration order is unspecified, so sort for a stable, predictable
        // order rather than depending on HashSet's internal ordering.
        Collections.sort(titles);
        return titles;
    }
}