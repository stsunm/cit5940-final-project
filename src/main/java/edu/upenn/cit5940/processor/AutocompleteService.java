package edu.upenn.cit5940.processor;

import java.util.ArrayList;
import java.util.List;

// 2. Get Autocomplete Suggestions
public class AutocompleteService {

    private static final int MAX_SUGGESTIONS = 10;

    private final CustomTrie titleWordTrie;

    /**
     * @param titleWordTrie a CustomTrie already populated (via insertWord/insertList)
     *                      with every distinct word that appears in an article title
     */
    public AutocompleteService(CustomTrie titleWordTrie) {
        this.titleWordTrie = titleWordTrie;
    }

    /**
     * Returns up to 10 words from article titles that start with the given prefix
     * (case-insensitive).
     *
     * <p>NOTE: CustomTrie currently only exposes insertWord/findWord/deleteWord/
     * allWords() — there's no method that walks directly to a prefix's node and
     * collects completions from there, so this filters the full word list instead.
     * That's correct but not the O(prefix length + matches) a real trie prefix-walk
     * would give you; if performance matters, CustomTrie would need a
     * {@code wordsWithPrefix(String prefix)} method added to it.
     */
    public List<String> autocomplete(String prefix) {
        List<String> matches = new ArrayList<>();

        // Guard clause: an empty prefix would match every word, which isn't
        // a useful autocomplete suggestion — return nothing instead.
        if (prefix == null || prefix.isEmpty()) {
            return matches;
        }

        // Case-insensitive matching: lowercase the prefix once up front so we
        // don't have to lowercase every trie word inside the loop.
        String lowerPrefix = prefix.toLowerCase();

        // allWords() walks the ENTIRE trie and returns every word stored in
        // it. We then filter down to just the ones starting with our prefix.
        // (See the class-level note above: a real prefix-walk would skip
        // straight to the prefix's node instead of scanning every word.)
        for (String word : titleWordTrie.allWords()) {
            if (word.startsWith(lowerPrefix)) {
                matches.add(word);
            }
        }

        // allWords() has no guaranteed order (it depends on HashMap iteration
        // order inside the trie's nodes), so sort alphabetically for
        // deterministic, predictable output.
        java.util.Collections.sort(matches);

        // Spec: show at most 10 suggestions.
        if (matches.size() > MAX_SUGGESTIONS) {
            matches = matches.subList(0, MAX_SUGGESTIONS);
        }
        return matches;
    }
}
