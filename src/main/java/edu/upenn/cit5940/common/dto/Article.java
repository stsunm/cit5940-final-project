package edu.upenn.cit5940.common.dto;
import java.util.*;

/**
 * @author nanzheng
 */

public class Article {

    private String uri;
    private String date;
    private String title;
    private String body;

    // The number of fields required to construct an Article.
    public static final int EXPECTED_FIELD_COUNT = 16;

    public Article(List<String> csvRow) {
        if (csvRow == null || csvRow.size() != EXPECTED_FIELD_COUNT) {
            throw new IllegalArgumentException("CSV row must contain exactly " + EXPECTED_FIELD_COUNT + " fields for an Article.");
        }

        // Mapping: 0=uri, 1=date, 4=title, 5=body
        this.uri = csvRow.get(0);
        this.date = csvRow.get(1);
        this.title = csvRow.get(4);
        this.body = csvRow.get(5);

        if (uri == null || uri.isBlank()) {
            throw new IllegalArgumentException("URI cannot be null or empty.");
        }
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Title cannot be null or empty.");
        }
    }
    
    public Article(String uri, String date, String title, String body) {
        this.uri = uri;
        this.date = date;
        this.title = title;
        this.body = body;
        
        if (uri == null || uri.isBlank()) {
            throw new IllegalArgumentException("URI cannot be null or empty.");
        }
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Title cannot be null or empty.");
        }
    }

    /*
    Getters and setters
     */
    public String getUri() {
        return uri;
    }
    public String getDate() {
        return date;
    }
    public String getTitle() {
        return title;
    }
    public String getBody() {
        return body;
    }

    public void setUri(String uri) {
        if (uri == null || uri.isBlank()) {
            throw new IllegalArgumentException("URI cannot be null or empty.");
        }
        this.uri = uri;
    }

    public void setDate(String date) {
        this.date = date;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public void setBody(String body) {
        this.body = body;
    }
}
