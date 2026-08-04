# Tech News Search Engine Project Report

## Part 1: Usage Instructions

### Compilation

This project is a Java command line application built with Maven.

To compile it, open a terminal and go to the project folder:

```bash
cd /path/to/cit5940-final-project
```

For example:

```bash
cd /Users/christynatalisa/Documents/cit5940-final-project
```

Then run:

```bash
mvn compile
```

This compiles the source code and puts the compiled files in `target/classes`.

You can also run:

```bash
mvn package
```

This performs a fuller Maven build and creates the build output in the `target` folder.

---

### Execution

The main class is:

```text
edu.upenn.cit5940.Main
```

Before running with `java`, build the dependency classpath:

```bash
mvn dependency:build-classpath -Dmdep.outputFile=classpath.txt
```

Then run the program:

```bash
java -cp "target/classes:$(cat classpath.txt)" edu.upenn.cit5940.Main
```

The program accepts two optional arguments:

```bash
java -cp "target/classes:$(cat classpath.txt)" edu.upenn.cit5940.Main [dataFilePath] [logFilePath]
```

The first argument is the data file path. The application supports `.csv` and `.json` files.

If no data file is given, it uses:

```text
articles.csv
```

The second argument is the log file path.

If no log file is given, it uses:

```text
tech_news_search.log
```

Example with a custom data file:

```bash
java -cp "target/classes:$(cat classpath.txt)" edu.upenn.cit5940.Main data/articles.csv
```

Example with both a custom data file and custom log file:

```bash
java -cp "target/classes:$(cat classpath.txt)" edu.upenn.cit5940.Main data/articles.csv logs/search.log
```

Example with a JSON file:

```bash
java -cp "target/classes:$(cat classpath.txt)" edu.upenn.cit5940.Main data/articles.json logs/search.log
```

On Windows, the classpath separator should be a semicolon instead of a colon.

```bash
java -cp "target/classes;DEPENDENCY_CLASSPATH_HERE" edu.upenn.cit5940.Main
```

When the application starts, it shows a main menu with four choices:

```text
1. Interactive Mode
2. Command Mode
3. Help & Documentation
4. Exit
```

Interactive Mode walks the user through each feature step by step.

Command Mode lets the user type commands directly, such as:

```text
search <keyword(s)>
autocomplete <prefix>
topics <period>
trends <topic> <start> <end>
articles <start_date> <end_date>
article <id>
stats
help
menu
```

Example commands:

```text
search artificial intelligence
autocomplete tech
topics 2023-12
trends ai 2023-01 2023-12
articles 2023-01-01 2023-12-31
stats
```

The date formats are:

```text
Period: YYYY-MM
Date: YYYY-MM-DD
```

## Part 2: System Design

### System Architecture

I designed the project using an n-tier architecture so that each part of the program has a clear job. This made the code easier to organize and easier to debug.

The presentation tier handles the user interface. This includes the main menu, Interactive Mode, Command Mode, help messages, prompts, and error messages. It also checks user input, such as menu choices and date formats, before passing requests to the logic layer.

The logic tier handles the main application features. This includes searching articles, generating autocomplete suggestions, showing top topics, analyzing topic trends, browsing by date, and retrieving article details. This tier also uses the main data structures, such as the inverted index, trie, `TreeMap`, and heap.

The data management tier handles reading article data from files. It supports CSV and JSON input and turns those records into `Article` objects. This keeps file parsing separate from the rest of the application.

There is also a shared data layer for simple objects like `Article`, which stores information such as the URI, date, title, and body.

Logging is handled separately through a shared logger. The logger records startup events, errors, user commands, and application shutdown.

### Data Structures & Refactoring

The inverted index is the most important data structure for search. I refactored it to use a `HashMap`, where each word maps to the set of document IDs that contain that word.

```text
word -> set of document IDs
```

This makes search much faster because the program does not need to scan every article each time the user searches. Instead, it can look up each word directly.

For example:

```text
ai       -> {1, 2, 5, 8}
ethics   -> {2, 5, 9}
result   -> {2, 5}
```

This means articles 2 and 5 contain both words.

The average lookup time for a `HashMap` is `O(1)`, which is a big improvement for keyword search.

I also used maps to connect document IDs back to articles. This lets the search service quickly turn search results into article titles without scanning the whole article list.

For autocomplete, I used a trie. A trie is a good fit because autocomplete is based on prefixes. The trie stores words from article titles, and the autocomplete feature returns matching suggestions for a prefix. The output is limited to 10 suggestions so the results stay readable.

For date and time based features, I used `TreeMap`. This works well because dates like `YYYY-MM` and `YYYY-MM-DD` sort naturally as strings. That makes it easier to handle topic trends and article browsing by date range.

For top topics, I used a heap with Java’s `PriorityQueue`. Instead of sorting every word count, the program keeps only the top 10 candidates while scanning the data. This is more efficient than sorting the entire list of words.

I also used sets for search results. Sets prevent duplicate document IDs and make it easy to intersect results for multi-word searches.

### Design Patterns

I used several design patterns in the project.

#### Singleton Pattern

The logger uses the Singleton pattern. This means the application uses one shared logger instance instead of creating separate loggers in different classes.

This was useful because many parts of the program need to write to the same log file. A single logger keeps the logging consistent.

Simplified example:

```java
public class Logger {
    private static Logger instance;

    private Logger(String filePath) {
        // Open log file
    }

    public static synchronized Logger getInstance(String filePath) {
        if (instance == null) {
            instance = new Logger(filePath);
        }
        return instance;
    }

    public static synchronized Logger getInstance() {
        if (instance == null) {
            instance = new Logger("tech_news_search.log");
        }
        return instance;
    }
}
```

#### Strategy Pattern

I used the Strategy pattern for file parsing. CSV and JSON files need different parsing code, but the rest of the program should not care which type of file was used.

To handle this, both parsers follow the same general interface.

```java
public interface DataParser {
    Map<String, Article> readAllArticles() throws Exception;
}
```

Then the program can choose the correct parser based on the file extension.

```java
DataParser parser;

if (dataFilePath.endsWith(".json")) {
    parser = new JsonArticleReader(reader);
} else if (dataFilePath.endsWith(".csv")) {
    parser = new ArticleCSVParser(characterReader);
}

Map<String, Article> articles = parser.readAllArticles();
```

This made the design easier to extend. If another file type is added later, I could add a new parser without rewriting the rest of the application.

#### Dependency Injection

The command line interface receives its service objects through its constructor. This is a form of dependency injection.

```java
public CommandLineInterface(
        SearchService searchService,
        AutocompleteService autocompleteService,
        ArticleAnalyticsService analyticsService,
        TopicTrendService trendService,
        ArticleService articleService) {

    this.searchService = searchService;
    this.autocompleteService = autocompleteService;
    this.analyticsService = analyticsService;
    this.trendService = trendService;
    this.articleService = articleService;
}
```

This keeps the UI layer focused on user interaction. It does not need to know how to build the search index or load articles. It just calls the services it receives.

#### Command Dispatch

Command Mode uses command dispatch. The program reads the first word of the user’s input and sends the command to the correct method.

```java
switch (command) {
    case "search":
        doSearch(tokenize(rest));
        break;
    case "autocomplete":
        doAutocomplete(rest);
        break;
    case "topics":
        doTopics(rest);
        break;
    case "stats":
        doStats();
        break;
    default:
        printUnknownCommandError();
        break;
}
```

This made Command Mode easier to organize. It also let Interactive Mode and Command Mode share the same helper methods, so the behavior stays consistent.

### Challenges Faced

One challenge was supporting both CSV and JSON files without mixing file parsing logic into the rest of the program. I solved this by using a shared parser interface and separate parser classes.

Another challenge was making the user interface handle bad input without crashing. I added validation for menu choices, dates, periods, unknown commands, and missing arguments.

Search performance was also important. A simple approach would scan every article for each query, but that would become slow. The inverted index made searching much faster by mapping words directly to document IDs.

For top topics, I wanted to avoid sorting every word. Using a bounded heap made it possible to keep only the top 10 results while processing the word counts.

## Summary

This project is a Java command line search engine for tech news articles. It uses n-tier architecture to keep the user interface, logic, and data parsing separate.

The main data structures are:

```text
HashMap        fast search lookup
Set            unique document IDs and search intersections
Trie           autocomplete support
TreeMap        sorted date and period data
PriorityQueue  top 10 topic selection
```

The main design patterns are:

```text
Singleton             shared logger
Strategy              CSV and JSON parser selection
Dependency Injection  service objects passed into the UI
Command Dispatch      command mode routing
```

Overall, these choices helped make the application faster, cleaner, and easier to extend.