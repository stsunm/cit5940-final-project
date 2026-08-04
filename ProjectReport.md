# Tech News Search Engine — Project Report

## Part 1: Usage Instructions

### Build & Run

```bash
mvn compile
mvn dependency:build-classpath -Dmdep.outputFile=classpath.txt
java -cp "target/classes:$(cat classpath.txt)" edu.upenn.cit5940.Main
```

On Windows, use `;` instead of `:` in the classpath.

### Arguments (both optional)

```bash
java -cp "target/classes:$(cat classpath.txt)" edu.upenn.cit5940.Main [dataFilePath] [logFilePath]
```

- `dataFilePath` — `.csv` or `.json`. Defaults to `articles.csv`.
- `logFilePath` — defaults to `tech_news_search.log`.

### Using it

On startup you get a menu:
```text
1. Interactive Mode
2. Command Mode
3. Help & Documentation
4. Exit
```

Interactive Mode prompts you step by step. Command Mode takes direct
commands:

| Command | Example |
|---|---|
| `search <keywords>` | `search artificial intelligence` |
| `autocomplete <prefix>` | `autocomplete tech` |
| `topics <period>` | `topics 2023-12` |
| `trends <topic> <start> <end>` | `trends ai 2023-01 2023-12` |
| `articles <start> <end>` | `articles 2023-01-01 2023-12-31` |
| `article <id>` | `article 7122001823` |
| `stats` / `help` / `menu` | |

Dates are `YYYY-MM-DD`, periods are `YYYY-MM`.

---

## Part 2: System Design

### Architecture

The app is split into four tiers so each piece has one job:

- **Presentation** (`ui.CommandLineInterface`) — menu, prompts, input
  validation.
- **Logic** (`processor.*`) — search, autocomplete, topics, trends, date
  browsing. Owns the core data structures.
- **Data management** (`datamanagement.*`) — reads CSV/JSON into
  `Article` objects.
- **Shared model** — `Article` (uri, date, title, body).

A single `Logger` handles logging across all tiers.

### Data Structures

- **Inverted index** — `HashMap<String, Set<Integer>>` mapping each word
  to the document IDs containing it. Multi-word search intersects these
  sets. Turns search into an `O(1)`-per-word lookup instead of scanning
  every article. Stop words are filtered out during indexing.
- **Trie** — stores words from article titles for autocomplete
  (max 10 suggestions).
- **TreeMap** — used for `"YYYY-MM"` and `"YYYY-MM-DD"` keyed data
  (topic trends, date-range browsing), since those formats sort correctly
  as plain strings.
- **Bounded min-heap** (`PriorityQueue`, size 10) — for Top Topics, keeps
  only the top 10 word counts while scanning instead of sorting
  everything: `O(n log 10)` instead of `O(n log n)`.
- **Sets** — de-duplicate document IDs and power the AND-search
  intersection.

### Design Patterns

**Singleton** — `Logger.getInstance()` ensures one shared log file.

**Strategy** — CSV and JSON parsers both implement `DataParser`, so
`Main` just picks one by file extension:
```java
DataParser parser = dataFilePath.endsWith(".json")
        ? new JsonArticleReader(reader)
        : new ArticleCSVParser(characterReader);
```

**Dependency Injection** — `CommandLineInterface` receives its services
through its constructor instead of building them itself, keeping the UI
layer decoupled from how search/indexing actually works.

**Command Dispatch** — Command Mode routes on the first input token:
```java
switch (command) {
    case "search": doSearch(tokenize(rest)); break;
    case "autocomplete": doAutocomplete(rest); break;
    ...
}
```

### Known Limitations

- **Autocomplete isn't a true prefix-walk.** It currently calls
  `CustomTrie.allWords()` (full trie traversal) and filters with
  `startsWith()`, instead of walking directly to the prefix node.
- **`CustomHeap` is unused.** It's an `int[]`-based heap from an earlier
  assignment that can't hold `(word, count)` pairs, so Top Topics uses
  `java.util.PriorityQueue` instead.


None of these affect correctness — they're just cleanup opportunities.

### Challenges

- Supporting CSV *and* JSON without leaking format-specific logic →
  solved with the `DataParser` interface.
- Keeping the CLI resilient to bad input → validation lives in the
  presentation tier before requests reach the logic layer.
- Avoiding a full scan per search → inverted index.
- Avoiding a full sort for Top Topics → bounded heap.

## Summary

An n-tier CLI search engine for tech news articles.

```text
HashMap        word -> document IDs (search)
Set            dedup + AND-search intersection
Trie           autocomplete
TreeMap        sorted date/period lookups
PriorityQueue  top-10 topic selection

Singleton, Strategy, Dependency Injection, Command Dispatch
```