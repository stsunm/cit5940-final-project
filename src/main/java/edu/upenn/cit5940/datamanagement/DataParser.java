package edu.upenn.cit5940.datamanagement;

import java.io.IOException;
import java.util.Map;

import edu.upenn.cit5940.common.dto.Article;

/***
 * DataParser interface for reading articles from different data sources (e.g., JSON, CSV).
 * Implementations of this interface should provide the logic to read and parse articles into Article objects.
 */
public interface DataParser {
	
	Map<String, Article> readAllArticles() throws IOException, CSVFormatException;

}
