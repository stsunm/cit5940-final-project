package edu.upenn.cit5940;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

import edu.upenn.cit5940.common.dto.Article;
import edu.upenn.cit5940.datamanagement.DataParser;
import edu.upenn.cit5940.datamanagement.JsonArticleReader;

public class Main {
    public static void main(String[] args) {
    	
    	//testing the JSON reader
    	try (var reader = Files.newBufferedReader(Paths.get("articles_small.json"))) {
    	    DataParser jsonParser = new JsonArticleReader(reader); 
    	    Map<String, Article> articles = jsonParser.readAllArticles();

    	    System.out.println("Successfully loaded " + articles.size() + " JSON articles!");
    	} catch (Exception e) {
    	    e.printStackTrace();
    	}
    	
    }
}
