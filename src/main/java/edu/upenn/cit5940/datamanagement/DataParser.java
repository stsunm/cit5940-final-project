package edu.upenn.cit5940.datamanagement;

import java.io.IOException;
import java.util.Map;

import edu.upenn.cit5940.common.dto.Article;

public interface DataParser {
	
	Map<String, Article> readAllArticles() throws IOException, CSVFormatException;

}
