package edu.upenn.cit5940.ui;

import edu.upenn.cit5940.logging.Logger;
import edu.upenn.cit5940.processor.ArticleAnalyticsService;
import edu.upenn.cit5940.processor.ArticleService;
import edu.upenn.cit5940.processor.AutocompleteService;
import edu.upenn.cit5940.processor.SearchService;
import edu.upenn.cit5940.processor.TopicTrendService;

import java.io.PrintStream;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;

/**
 * Presentation (UI) tier for the Tech News Search Engine.
 *
 * <p>This class owns everything the user sees: startup messages, the Main Menu,
 * Interactive Mode, Command Mode, the Help screen, and all user-facing error
 * messages. It never touches the Data Management tier directly — it only calls
 * methods on the five injected Logic-tier services (dependency injection), per
 * the n-tier design rules. {@code Main} is responsible for constructing each
 * service and passing them in here.
 *
 * <p>Per the "robustness" guiding principle, this class is written so that no
 * single bad input (invalid menu choice, malformed date, unknown command, etc.)
 * ever crashes the app — every input path either succeeds or falls back to a
 * clear error message and a fresh prompt.
 */
public class CommandLineInterface {

    private static final Pattern PERIOD_PATTERN = Pattern.compile("^\\d{4}-(0[1-9]|1[0-2])$");

    private final Scanner in;
    private final PrintStream out;
    private final SearchService searchService;
    private final AutocompleteService autocompleteService;
    private final ArticleAnalyticsService analyticsService;
    private final TopicTrendService trendService;
    private final ArticleService articleService;
    // Logger is a true Singleton (per the spec's suggested design pattern),
    // so it's accessed via Logger.getInstance() rather than constructor
    // injection — that's the whole point of the pattern: any class can reach
    // the one shared instance without it being threaded through every layer.
    // IMPORTANT: Main must call Logger.getInstance(logFilePath) BEFORE
    // constructing this class, so the singleton is created with the correct
    // configured log file. This getInstance() call (no-arg) just returns
    // that already-created instance — it won't re-create it with a
    // different path.
    private final Logger logger;

    public CommandLineInterface(Scanner in, PrintStream out,
                                SearchService searchService,
                                AutocompleteService autocompleteService,
                                ArticleAnalyticsService analyticsService,
                                TopicTrendService trendService,
                                ArticleService articleService) {
        this.in = in;
        this.out = out;
        this.searchService = searchService;
        this.autocompleteService = autocompleteService;
        this.analyticsService = analyticsService;
        this.trendService = trendService;
        this.articleService = articleService;
        this.logger = Logger.getInstance();
    }

    /** Convenience constructor wired to System.in / System.out. */
    public CommandLineInterface(SearchService searchService,
                                AutocompleteService autocompleteService,
                                ArticleAnalyticsService analyticsService,
                                TopicTrendService trendService,
                                ArticleService articleService) {
        this(new Scanner(System.in), System.out, searchService, autocompleteService,
                analyticsService, trendService, articleService);
    }

    // =====================================================================
    // Startup
    // =====================================================================

    /**
     * Prints the required startup sequence. Call this once, after the data
     * file has already been loaded successfully, and before {@link #run()}.
     *
     * @param dataFilePath  the path of the data file that was loaded
     * @param articleCount  number of articles successfully loaded
     */
    public void printStartupMessages(String dataFilePath, int articleCount) {
        out.println("=== Tech News Search Engine ===");
        out.println("Initializing n-tier architecture...");
        out.println("Loading articles from: " + dataFilePath);
        out.println(articleCount + " articles loaded");
        out.println("Architecture initialization complete!");
        out.println();
    }

    // =====================================================================
    // Top-level loop
    // =====================================================================

    /** Runs the application until the user chooses to exit. */
    public void run() {
        boolean running = true;
        // Outer loop: keep showing the Main Menu until the user picks Exit (4).
        while (running) {
            try {
                printMainMenu();
                // readMenuChoice loops internally until it gets a valid 1-4 —
                // by the time it returns, `choice` is guaranteed valid, so the
                // switch below never needs a "handle bad input" branch.
                int choice = readMenuChoice(1, 4, "Please select an option (1-4): ");
                switch (choice) {
                    case 1:
                        runInteractiveMode(); // has its own internal loop; returns here when user picks "back to main menu"
                        break;
                    case 2:
                        runCommandMode(); // has its own internal loop; returns here on the "menu" command
                        break;
                    case 3:
                        printHelpMenu();
                        break;
                    case 4:
                        printExitMessage();
                        running = false; // this is what actually stops the outer while loop
                        break;
                    default:
                        // unreachable: readMenuChoice already enforces the range
                        break;
                }
            } catch (Exception e) {
                // Guiding principle: never crash. Catching Exception here is a
                // last line of defense — if anything unexpected slips past all
                // the specific validation elsewhere, we report it and loop back
                // to the Main Menu instead of the whole program dying.
                logger.error("Unexpected error in main loop: " + e.getMessage());
                out.println("An unexpected error occurred: " + e.getMessage());
            }
        }
    }

    // =====================================================================
    // Main Menu
    // =====================================================================

    private void printMainMenu() {
        out.println("==================================================");
        out.println(" MAIN MENU");
        out.println("==================================================");
        out.println("1. Interactive Mode (Guided Menu)");
        out.println("2. Command Mode (Direct Commands)");
        out.println("3. Help & Documentation");
        out.println("4. Exit");
        out.println("==================================================");
    }

    // =====================================================================
    // Interactive Mode
    // =====================================================================

    private void printInteractiveMenu() {
        out.println("==================================================");
        out.println(" INTERACTIVE MODE");
        out.println("==================================================");
        out.println("This mode will guide you through each operation step by step.");
        out.println("----------------------------------------");
        out.println(" AVAILABLE SERVICES");
        out.println("----------------------------------------");
        out.println("1. Search Articles");
        out.println("2. Get Autocomplete Suggestions");
        out.println("3. View Top Topics");
        out.println("4. Analyze Topic Trends");
        out.println("5. Browse Articles by Date");
        out.println("6. View Specific Article by ID");
        out.println("7. Show Statistics");
        out.println("8. Back to Main Menu");
        out.println("----------------------------------------");
    }

    private void runInteractiveMode() {
        boolean inInteractive = true;
        // Inner loop: after running a service, come back to THIS menu (not the
        // Main Menu) until the user explicitly picks "8. Back to Main Menu".
        while (inInteractive) {
            printInteractiveMenu();
            int choice = readMenuChoice(1, 8, "Select a service (1-8): ");
            switch (choice) {
                case 1:
                    interactiveSearch();
                    waitForEnter(); // pause so the user can read the result before the menu redraws
                    break;
                case 2:
                    interactiveAutocomplete();
                    waitForEnter();
                    break;
                case 3:
                    interactiveTopics();
                    waitForEnter();
                    break;
                case 4:
                    interactiveTrends();
                    waitForEnter();
                    break;
                case 5:
                    interactiveArticlesByDate();
                    waitForEnter();
                    break;
                case 6:
                    interactiveArticleById();
                    waitForEnter();
                    break;
                case 7:
                    doStats();
                    waitForEnter();
                    break;
                case 8:
                    inInteractive = false; // this is what breaks the inner loop and returns to run()'s Main Menu loop
                    break;
                default:
                    break;
            }
        }
    }

    private void waitForEnter() {
        out.println();
        out.print("Press ENTER to return to the Interactive Mode menu...");
        in.nextLine();
        out.println();
    }

    // Each of these prompts the user step-by-step for exactly the arguments
    // that command needs, then hands off to the SAME do*() method Command
    // Mode uses — so Interactive Mode and Command Mode always behave
    // identically once the arguments are collected, no duplicated logic.

    private void interactiveSearch() {
        out.print("Enter search keyword(s): ");
        String line = in.nextLine();
        List<String> keywords = tokenize(line);
        if (keywords.isEmpty()) {
            printSearchUsageError();
            return;
        }
        doSearch(keywords);
    }

    private void interactiveAutocomplete() {
        out.print("Enter prefix: ");
        String prefix = in.nextLine().trim();
        doAutocomplete(prefix);
    }

    private void interactiveTopics() {
        out.print("Enter period (YYYY-MM): ");
        String period = in.nextLine().trim();
        doTopics(period);
    }

    private void interactiveTrends() {
        out.print("Enter topic: ");
        String topic = in.nextLine().trim();
        out.print("Enter start period (YYYY-MM): ");
        String start = in.nextLine().trim();
        out.print("Enter end period (YYYY-MM): ");
        String end = in.nextLine().trim();
        doTrends(topic, start, end);
    }

    private void interactiveArticlesByDate() {
        out.print("Enter start date (YYYY-MM-DD): ");
        String start = in.nextLine().trim();
        out.print("Enter end date (YYYY-MM-DD): ");
        String end = in.nextLine().trim();
        doArticlesByDateRange(start, end);
    }

    private void interactiveArticleById() {
        out.print("Enter article ID: ");
        String id = in.nextLine().trim();
        doArticleById(id);
    }

    // =====================================================================
    // Command Mode
    // =====================================================================

    private void printCommandModeHeader() {
        out.println("==================================================");
        out.println(" COMMAND MODE");
        out.println("==================================================");
        out.println("Enter commands directly. Type 'help' for available commands.");
        out.println("Type 'menu' to return to the main menu.");
    }

    private void runCommandMode() {
        printCommandModeHeader();
        boolean inCommandMode = true;
        while (inCommandMode) {
            out.print("> ");
            String line = in.nextLine();
            if (line == null) {
                continue;
            }
            String trimmed = line.trim();
            if (trimmed.isEmpty()) {
                // Blank line: just re-prompt, nothing to dispatch.
                continue;
            }

            try {
                // dispatchCommand returns false only for "menu" — that's the
                // signal to stop looping and fall back out to the Main Menu.
                inCommandMode = dispatchCommand(trimmed);
            } catch (Exception e) {
                // Same "never crash" principle as run(): one bad command
                // shouldn't kill Command Mode, just report and keep prompting.
                logger.error("Unexpected error processing command '" + trimmed + "': " + e.getMessage());
                out.println("An error occurred while processing that command: " + e.getMessage());
            }
        }
    }

    /**
     * Parses and executes one command-mode line.
     *
     * @return {@code true} to stay in Command Mode, {@code false} to return to the Main Menu
     */
    private boolean dispatchCommand(String line) {
        // Spec requirement: log every user command. Logging the raw line
        // (before parsing) captures exactly what the user typed, which is
        // more useful for debugging than a reconstructed/normalized version.
        logger.info("User command: " + line);

        // split("\\s+", 2) means: split on whitespace, but stop after the
        // FIRST split. So "search ai ethics" -> ["search", "ai ethics"] —
        // the command name is isolated, but multi-word arguments stay intact
        // as one string for each command to parse further as it needs to.
        String[] parts = line.split("\\s+", 2);
        String command = parts[0].toLowerCase(); // case-insensitive command matching
        String rest = parts.length > 1 ? parts[1].trim() : ""; // everything after the command name, or "" if none

        switch (command) {
            case "search":
                // "search" takes one-or-more space-separated keywords —
                // tokenize() splits `rest` into a List<String> for us.
                List<String> keywords = tokenize(rest);
                if (keywords.isEmpty()) {
                    printSearchUsageError();
                } else {
                    doSearch(keywords);
                }
                return true;

            case "autocomplete":
                if (rest.isEmpty()) {
                    out.println("Usage: autocomplete <prefix>");
                } else {
                    doAutocomplete(rest);
                }
                return true;

            case "topics":
                if (rest.isEmpty()) {
                    out.println("Usage: topics <period>");
                } else {
                    doTopics(rest);
                }
                return true;

            case "trends": {
                // "trends" needs exactly 3 arguments (topic, start, end), so
                // split the remainder on whitespace and check the count —
                // this assumes the topic itself is a single word, which
                // matches how topics are stored (one word per key).
                String[] args = rest.isEmpty() ? new String[0] : rest.split("\\s+");
                if (args.length != 3) {
                    out.println("Usage: trends <topic> <start> <end>");
                } else {
                    doTrends(args[0], args[1], args[2]);
                }
                return true;
            }

            case "articles": {
                // "articles" needs exactly 2 arguments (start date, end date).
                String[] args = rest.isEmpty() ? new String[0] : rest.split("\\s+");
                if (args.length != 2) {
                    out.println("Usage: articles <start_date> <end_date>");
                } else {
                    doArticlesByDateRange(args[0], args[1]);
                }
                return true;
            }

            case "article":
                if (rest.isEmpty()) {
                    out.println("Usage: article <id>");
                } else {
                    doArticleById(rest);
                }
                return true;

            case "stats":
                doStats();
                return true;

            case "help":
                printCommandModeHelp();
                return true;

            case "menu":
                return false; // exit command mode, back to Main Menu

            default:
                // Anything that isn't one of the known command words above.
                printUnknownCommandError();
                return true;
        }
    }

    private void printCommandModeHelp() {
        out.println("Available commands:");
        out.println("  search <keyword(s)>            - find articles containing all keywords");
        out.println("  autocomplete <prefix>          - suggest up to 10 title words starting with prefix");
        out.println("  topics <period>                - top 10 trending words for a YYYY-MM period");
        out.println("  trends <topic> <start> <end>   - monthly frequency of a topic across a period range");
        out.println("  articles <start> <end>         - titles published within a YYYY-MM-DD date range");
        out.println("  article <id>                   - show details for a specific article");
        out.println("  stats                           - show data statistics");
        out.println("  help                            - show this list of available commands");
        out.println("  menu                            - return to the main menu");
    }

    // =====================================================================
    // Shared command implementations (used by both Interactive and Command Mode)
    // =====================================================================

    private void doSearch(List<String> keywords) {
        // Spec calls out "search queries" as their own logged category,
        // separate from general user commands — log it here so both
        // Interactive Mode and Command Mode searches get captured, since
        // they both funnel through this one shared method.
        logger.info("Search query: " + String.join(" ", keywords));
        List<String> titles = searchService.search(keywords);
        printTitlesOrNone(titles);
    }

    private void doAutocomplete(String prefix) {
        List<String> suggestions = autocompleteService.autocomplete(prefix);
        if (suggestions.isEmpty()) {
            out.println("No suggestions found.");
            return;
        }
        for (String s : suggestions) {
            out.println(s);
        }
    }

    private void doTopics(String period) {
        // Validate the FORMAT here in the UI tier (per the n-tier design:
        // format validation is the CLI's job so the service layer can trust
        // its inputs). The service only needs to worry about whether there's
        // DATA for that period, not whether the string is well-formed.
        if (!isValidPeriod(period)) {
            printInvalidDateError("Please use the YYYY-MM format with a valid month (01-12).");
            return;
        }
        List<String> topics = analyticsService.topTopics(period);
        if (topics.isEmpty()) {
            out.println("No articles found.");
            return;
        }
        for (String t : topics) {
            out.println(t);
        }
    }

    private void doTrends(String topic, String start, String end) {
        // Both periods must individually be well-formed YYYY-MM...
        if (!isValidPeriod(start) || !isValidPeriod(end)) {
            printInvalidDateError("Please use the YYYY-MM format with a valid month (01-12).");
            return;
        }
        // ...AND start must not come after end. String comparison works here
        // because YYYY-MM is a fixed-width, zero-padded format, so
        // lexicographic order == chronological order.
        if (start.compareTo(end) > 0) {
            out.println("Error: Invalid date range. The start period cannot be after the end period.");
            return;
        }
        List<String> trend = trendService.topicTrends(topic, start, end);
        if (trend.isEmpty()) {
            out.println("No articles found.");
            return;
        }
        for (String line : trend) {
            out.println(line);
        }
    }

    private void doArticlesByDateRange(String start, String end) {
        // LocalDate.parse (inside isValidDate) does real calendar validation —
        // rejects things like 2024-04-31 (April only has 30 days), not just
        // format-shape checking.
        if (!isValidDate(start) || !isValidDate(end)) {
            printInvalidDateError("Please use the YYYY-MM-DD format with valid values.");
            return;
        }
        // Same lexicographic-order trick as doTrends above, but for full dates.
        if (start.compareTo(end) > 0) {
            out.println("Error: Invalid date range. The start date cannot be after the end date.");
            return;
        }
        List<String> titles = articleService.articlesByDateRange(start, end);
        printTitlesOrNone(titles);
    }

    private void doArticleById(String id) {
        String details = articleService.getArticleById(id);
        if (details == null) {
            out.println("No article found with ID: " + id);
        } else {
            out.println(details);
        }
    }

    private void doStats() {
        out.println(analyticsService.getStats());
    }

    private void printTitlesOrNone(List<String> titles) {
        if (titles == null || titles.isEmpty()) {
            out.println("No articles found.");
            return;
        }
        for (String title : titles) {
            out.println(title);
        }
    }

    // =====================================================================
    // Help Menu
    // =====================================================================

    private void printHelpMenu() {
        out.println("============================================================");
        out.println(" HELP & DOCUMENTATION");
        out.println("============================================================");
        out.println("INTERACTIVE MODE:");
        out.println(" - Guided step-by-step interface");
        out.println(" - Prompts for all required inputs");
        out.println(" - Perfect for beginners");
        out.println();
        out.println("COMMAND MODE:");
        out.println(" - Direct command entry");
        out.println(" - Faster for experienced users");
        out.println(" - Type 'help' for command list");
        out.println();
        out.println("AVAILABLE SERVICES:");
        out.println(" 1. Search Articles - Find articles by keywords");
        out.println(" 2. Autocomplete - Get search suggestions");
        out.println(" 3. Top Topics - View trending topics by period");
        out.println(" 4. Topic Trends - Analyze topic popularity over time");
        out.println(" 5. Browse Articles - Filter articles by date range");
        out.println(" 6. View Article - Get detailed article information");
        out.println(" 7. Statistics - View database statistics");
        out.println();
        out.println("DATE FORMATS:");
        out.println(" - Period: YYYY-MM (e.g., 2023-12)");
        out.println(" - Date: YYYY-MM-DD (e.g., 2023-12-01)");
        out.println();
        out.print("Press Enter to return to the main menu...");
        in.nextLine();
        out.println();
    }

    // =====================================================================
    // Exit
    // =====================================================================

    private void printExitMessage() {
        out.println("Thank you for using the Tech News Search Engine!");
        out.println("Goodbye!");
        logger.info("Application exiting");
    }

    // =====================================================================
    // Menu input handling (shared error messages)
    // =====================================================================

    /**
     * Repeatedly prompts until the user enters an integer within [min, max].
     * Handles the three required error cases: empty input, non-numeric
     * input, and out-of-range numeric input.
     */
    private int readMenuChoice(int min, int max, String prompt) {
        // Infinite loop with `continue` on bad input and `return` on success —
        // this is what makes the method "repeatedly prompt until valid":
        // every bad-input branch loops back to re-print the prompt instead of
        // propagating an error up and crashing.
        while (true) {
            out.print(prompt);
            String line = in.nextLine();

            // Case 1: empty input (just hit Enter).
            if (line == null || line.trim().isEmpty()) {
                out.println("Please enter a choice.");
                continue;
            }

            // Case 2: non-numeric input (e.g. "abc"). Integer.parseInt throws
            // NumberFormatException on anything that isn't a valid integer.
            int value;
            try {
                value = Integer.parseInt(line.trim());
            } catch (NumberFormatException e) {
                out.println("Please enter a valid number (" + min + "-" + max + ").");
                continue;
            }

            // Case 3: a valid number, but outside the allowed menu range.
            if (value < min || value > max) {
                out.println("Invalid choice. Please enter " + min + "-" + max + ".");
                continue;
            }

            // Only reachable once we have a genuinely valid choice.
            return value;
        }
    }

    // =====================================================================
    // Required error message helpers
    // =====================================================================

    private void printUnknownCommandError() {
        out.println("Unknown command. Type 'help' for available commands.");
    }

    private void printSearchUsageError() {
        out.println("Usage: search <keyword>");
    }

    private void printInvalidDateError(String detail) {
        out.println("Error: Invalid date provided. " + detail);
    }

    // =====================================================================
    // Validation helpers
    // =====================================================================

    private boolean isValidPeriod(String period) {
        // PERIOD_PATTERN = ^\d{4}-(0[1-9]|1[0-2])$ : 4 digits, a dash, then a
        // month that's 01-09 or 10-12 — rejects things like "2024-13" or "24-01".
        return period != null && PERIOD_PATTERN.matcher(period).matches();
    }

    private boolean isValidDate(String date) {
        if (date == null) {
            return false;
        }
        try {
            // LocalDate.parse does more than check the SHAPE of the string —
            // it validates the date is real on the calendar (e.g. rejects
            // 2024-04-31, since April has only 30 days). A regex alone
            // couldn't catch that.
            LocalDate.parse(date); // ISO_LOCAL_DATE: YYYY-MM-DD, rejects e.g. 2024-04-31
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    /** Splits a line of user input into non-empty, whitespace-separated tokens. */
    private List<String> tokenize(String line) {
        List<String> tokens = new ArrayList<>();
        if (line == null) {
            return tokens;
        }
        // split("\\s+") on a trimmed line still can produce an empty first
        // token in some edge cases, so filter out any empty strings defensively.
        for (String token : line.trim().split("\\s+")) {
            if (!token.isEmpty()) {
                tokens.add(token);
            }
        }
        return tokens;
    }
}