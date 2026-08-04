package edu.upenn.cit5940.datamanagement;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

import edu.upenn.cit5940.common.dto.Article;
import edu.upenn.cit5940.logging.Logger;

//for reading JSON files and parsing them into Article objects (using Gson or Jackson)
public class JsonArticleReader implements DataParser {
	private final Reader reader;

    // Accepts a Reader (or CharacterReader) directly
    public JsonArticleReader(Reader reader) {
        this.reader = reader;
    }

    @Override
    public Map<String, Article> readAllArticles() throws IOException {
        Map<String, Article> articles = new HashMap<>();
        ObjectMapper mapper = new ObjectMapper();
        Logger logger = Logger.getInstance();
        JsonNode rootNode = mapper.readTree(this.reader);

        if (rootNode != null && rootNode.isArray()) {
        	int entryCount = 0;
        	
            for (JsonNode node : rootNode) {
            	entryCount++;
            	//skip empty nodes
            	if (node == null || node.isEmpty()) {
                    continue;
                }

                //ensure exactly 16 fields exist
                if (node.size() != 16) {
                    logger.error("Skipping malformed JSON entry " + entryCount
                            + ": invalid field count " + node.size() + " (Expected 16)");
                    continue;
                }

                //extract URI
                String uri = node.has("uri") && !node.get("uri").isNull() ? node.get("uri").asText().trim() : "";

                //skip header row if it exists
                if (uri.equalsIgnoreCase("uri")) {
                    continue;
                }

                //ensure URI is not empty
                if (uri.isEmpty()) {
                    logger.error("Skipping malformed JSON entry " + entryCount + ": empty URI.");
                    continue;
                }

                //extract other fields
                String date = node.has("date") ? node.get("date").asText() : "";
                String title = node.has("title") ? node.get("title").asText() : "";
                String body = node.has("body") ? node.get("body").asText() : "";

                if (title == null || title.isBlank()) {
                    logger.error("Skipping malformed JSON entry " + entryCount + ": missing or empty title.");
                    continue;
                }

                // Check for empty date field
                if (date == null || date.isBlank()) {
                    logger.error("Skipping malformed JSON entry " + entryCount + ": missing or empty date.");
                    continue;
                }

                // Check for empty body field
                if (body == null || body.isBlank()) {
                    logger.error("Skipping malformed JSON entry " + entryCount + ": missing or empty body.");
                    continue;
                }

                Article article = new Article(uri, date, title, body);
                articles.put(uri, article);
            }
        }
        logger.info("Successfully parsed " + articles.size() + " JSON articles.");
        
        return articles;
    }	

}
