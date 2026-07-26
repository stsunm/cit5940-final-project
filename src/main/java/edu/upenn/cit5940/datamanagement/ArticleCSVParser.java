/*
 * I attest that the code in this file is entirely my own except for the starter
 * code provided with the assignment and the following exceptions:
 * <Enter all external resources and collaborations here. Note external code may
 * reduce your score but appropriate citation is required to avoid academic
 * integrity violations. Please see the Course Syllabus as well as the
 * university code of academic integrity:
 *  https://catalog.upenn.edu/pennbook/code-of-academic-integrity/ >
 * Signed,
 * Author: Stephanie Sun
 * Penn email: <stsun@seas.upenn.edu>
 * Date: 2026-07-15
 */
package edu.upenn.cit5940.datamanagement;

import java.io.*;
import java.util.*;

import edu.upenn.cit5940.common.dto.Article;

public class ArticleCSVParser {
    private final CharacterReader reader;
    private int iLine = 1;
    private int iRecord = 1;

    public ArticleCSVParser(CharacterReader reader) {
        this.reader = reader;
    }

    private enum STATES {
        START_FIELD,          // Handles comma at the start/middle of a line
        IN_FIELD,             // Inside an unquoted field
        IN_QUOTED_FIELD,      // Inside a quoted field
        QUOTE_IN_QUOTED_FIELD // Hit a quote inside a quoted field
    }

    /**
     * Reads the entire CSV stream and parses it into a map of Articles.
     */
    public Map<String, Article> readAllArticles() throws IOException, CSVFormatException {
        Map<String, Article> articles = new HashMap<>(); //Initialize the map to hold the parsed articles
        List<String> currentRecord = new ArrayList<>(); //Initialize the list to hold the current ariticle being parsed
        StringBuilder currentField = new StringBuilder(); //Initialize the StringBuilder to hold the current field being parsed
        STATES state = STATES.START_FIELD; //Initialize the state machine to the START_FIELD state
        
        int c; //Variable to hold the current character being read
        boolean hasDataOnLine = false; //Flag to track if any data has been encountered on the current line

        while ((c = reader.read()) != -1) { //Read the next character from the input stream until EOF
            char ch = (char) c; //Convert the integer to a character for processing

            switch (state) {
                case START_FIELD:
                    if (ch == '\n') { // Handle newline character
                        if (hasDataOnLine || !currentRecord.isEmpty()) { //Check if it's a blank line or if there is data on the line
                            currentRecord.add("");  // Add an empty field to the current record
                            processRecord(currentRecord, articles); // Process the current record and add it to the articles map
                            currentRecord.clear(); // Clear the current record for the next line
                        }
                        currentField.setLength(0); // reset the current field just to be safe
                        iLine++;
                        iRecord++;
                        hasDataOnLine = false;
                    } else if (ch == '\r') { // Handle carriage return character
                        int next = reader.read();
                        if (next == '\n') { //check for CRLF line ending
                            if (hasDataOnLine || !currentRecord.isEmpty()) { //Check if it's a blank line or if there is data on the line
                                currentRecord.add(""); 
                                processRecord(currentRecord, articles);
                                currentRecord.clear();
                            }
                            currentField.setLength(0);
                            iLine++;
                            iRecord++;
                            hasDataOnLine = false;
                        } else if (next == -1) {
                            //CR at EOF is a formatting violation, throw an exception
                            throw new CSVFormatException("No CR at EOF", iLine, -1, iRecord, -1);
                        } else { //CR should be followed by LF
                            throw new CSVFormatException("Invalid character following a CR", iLine, -1, iRecord, -1);
                        }
                    } else if (ch == ',') { //Handle comma character
                        currentRecord.add(""); 
                        hasDataOnLine = true;
                    } else if (ch == '"') {
                        state = STATES.IN_QUOTED_FIELD;
                        hasDataOnLine = true;
                    } else {
                        currentField.append(ch);
                        state = STATES.IN_FIELD;
                        hasDataOnLine = true;
                    }
                    break;

                case IN_FIELD:
                    if (ch == '\n') {
                        currentRecord.add(currentField.toString());
                        processRecord(currentRecord, articles);
                        currentRecord.clear();
                        currentField.setLength(0);
                        iLine++;
                        iRecord++;
                        hasDataOnLine = false;
                        state = STATES.START_FIELD;
                    } else if (ch == '\r') {
                        int next = reader.read();
                        if (next == '\n') {
                            currentRecord.add(currentField.toString());
                            processRecord(currentRecord, articles);
                            currentRecord.clear();
                            currentField.setLength(0);
                            iLine++;
                            iRecord++;
                            hasDataOnLine = false;
                            state = STATES.START_FIELD;
                        } else if (next == -1) {
                            throw new CSVFormatException("No CR at EOF", iLine, -1, iRecord, -1);
                        } else {
                            throw new CSVFormatException("Invalid character following a CR", iLine, -1, iRecord, -1);
                        }
                    } else if (ch == ',') {
                        currentRecord.add(currentField.toString());
                        currentField.setLength(0);
                        state = STATES.START_FIELD;
                    } else if (ch == '"') {
                        throw new CSVFormatException("Unexpected quote inside unquoted field", iLine, -1, iRecord, -1);
                    } else {
                        currentField.append(ch);
                    }
                    break;

                case IN_QUOTED_FIELD:
                    if (ch == '"') {
                        state = STATES.QUOTE_IN_QUOTED_FIELD;
                    } else {
                        currentField.append(ch);
                    }
                    break;

                case QUOTE_IN_QUOTED_FIELD:
                    if (ch == '"') {
                        currentField.append('"'); // Escaped quote inside string
                        state = STATES.IN_QUOTED_FIELD;
                    } else if (ch == ',') {
                        currentRecord.add(currentField.toString());
                        currentField.setLength(0);
                        state = STATES.START_FIELD;
                    } else if (ch == '\n') {
                        currentRecord.add(currentField.toString());
                        processRecord(currentRecord, articles);
                        currentRecord.clear();
                        currentField.setLength(0);
                        iLine++;
                        iRecord++;
                        hasDataOnLine = false;
                        state = STATES.START_FIELD;
                    } else if (ch == '\r') {
                        int next = reader.read();
                        if (next == '\n') {
                            currentRecord.add(currentField.toString());
                            processRecord(currentRecord, articles);
                            currentRecord.clear();
                            currentField.setLength(0);
                            iLine++;
                            iRecord++;
                            hasDataOnLine = false;
                            state = STATES.START_FIELD;
                        } else if (next == -1) {
                            throw new CSVFormatException("No CR at EOF", iLine, -1, iRecord, -1);
                        } else {
                            throw new CSVFormatException("Invalid character following a CR", iLine, -1, iRecord, -1);
                        }
                    } else {
                        throw new CSVFormatException("Unexpected character after closing quote", iLine, -1, iRecord, -1);
                    }
                    break;
            }
        }

        if (state == STATES.IN_QUOTED_FIELD) {
            throw new CSVFormatException("Unclosed quoted field at end of file", iLine, -1, iRecord, -1);
        }
        
       //check for files with missing trailing line breaks
        if (hasDataOnLine || !currentRecord.isEmpty() || currentField.length() > 0) {
            if (state == STATES.START_FIELD) {
                currentRecord.add(""); 
            } else {
                currentRecord.add(currentField.toString());
            }
            processRecord(currentRecord, articles);
        }
        
        return articles;
    }

    /**
     * Helper method to convert a parsed record into an Article.
     */
    private void processRecord(List<String> rec, Map<String, Article> articles) throws CSVFormatException {
        if (rec.isEmpty()) {
            return; 
        }
        
        if (rec.size() == 1 && rec.get(0).isEmpty()) {
            return;
        }

        if (rec.get(0).equalsIgnoreCase("uri")) {
            return; // Skip header
        }
        
        if (rec.size() != 16) {
            throw new IllegalArgumentException("Incorrect number of fields. Expected exactly 16, found: " + rec.size());
        }
        
        if (rec.get(0) == null || rec.get(0).trim().isEmpty()) {
            throw new IllegalArgumentException("The first field (uri) cannot be empty.");
        }
        
        Article article = new Article(rec);
        articles.put(article.getUri(), article);
    }
}