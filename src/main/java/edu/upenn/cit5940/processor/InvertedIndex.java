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
 * Date: 2026-06-22
 */

package edu.upenn.cit5940.processor;

//import any classes you will need
import java.util.*;

public class InvertedIndex {

    // HashMap to store the inverted index: keyword -> set of document IDs
    private Map<String, Set<Integer>> index;

    // Constructor
    public InvertedIndex() {
        this.index = new HashMap<>();
    }

    static final Set<String> STOP_WORDS = Set.of(
            "a", "an", "and", "are", "as", "at", "be", "been", 
            "by", "for", "from", "has", "have", "he", "in", "is", 
            "it", "its", "of", "on", "that", "the", "their", "there", 
            "they", "this", "to", "was", "will", "with", "you", "your", 
            "i", "me", "my", "we", "our", "us", "him", "his", "her", "she", 
            "them", "these", "those", "but", "if", "or", "because", "so", 
            "than", "too", "very", "when", "where", "why", "how", "all", 
            "any", "both", "each", "few", "more", "most", "other", "some", 
            "such", "no", "nor", "not", "only", "own", "same", "just", "now", 
            "said", "can", "should", "would", "could", "might", "may", "must", 
            "shall", "do", "does", "did", "done", "doing", "get", "got", "getting", 
            "make", "made", "making", "take", "took", "taking", "come", "came", "coming", 
            "go", "went", "going", "see", "saw", "seeing", "know", "knew", "knowing", "think", 
            "thought", "thinking", "say", "saying", "tell", "told", "telling", "give", "gave", 
            "giving", "find", "found", "finding", "use", "used", "using", "work", "worked", "working", 
            "look", "looked", "looking", "want", "wanted", "wanting", "need", "needed", "needing", "feel", 
            "felt", "feeling", "seem", "seemed", "seeming", "try", "tried", "trying", "ask", "asked", "asking", 
            "turn", "turned", "turning", "move", "moved", "moving", "play", "played", "playing", "run", "ran", 
            "running", "live", "lived", "living", "help", "helped", "helping", "show", "showed", "showing", "hear", 
            "heard", "hearing", "let", "letting", "put", "putting", "end", "ended", "ending", "set", "setting", "change", 
            "changed", "changing", "keep", "kept", "keeping", "start", "started", "starting", "stop", "stopped", "stopping", 
            "open", "opened", "opening", "close", "closed", "closing", "read", "reading", "write", "wrote", "writing", "speak", 
            "spoke", "speaking", "call", "called", "calling", "meet", "met", "meeting", "leave", "left", "leaving", "bring", 
            "brought", "bringing", "happen", "happened", "happening", "include", "included", "including", 
            "continue", "continued", "continuing", "follow", "followed", "following", "appear", 
            "appeared", "appearing", "allow", "allowed", "allowing", "provide", "provided", 
            "providing", "serve", "served", "serving", "send", "sent", "sending", "receive", 
            "received", "receiving", "build", "built", "building", "grow", "grew", "growing", 
            "hold", "held", "holding", "create", "created", "creating", "develop", "developed", 
            "developing", "produce", "produced", "producing", "offer", "offered", "offering", 
            "support", "supported", "supporting", "contain", "contained", "containing", "cover", 
            "covered", "covering", "reach", "reached", "reaching", "raise", "raised", "raising", 
            "pass", "passed", "passing", "sell", "sold", "selling", "buy", "bought", "buying", 
            "cost", "costing", "pay", "paid", "paying", "spend", "spent", "spending", "save", 
            "saved", "saving", "lose", "lost", "losing", "win", "won", "winning", "beat", 
            "beating", "hit", "hitting", "cut", "cutting", "break", "broke", "breaking", 
            "kill", "killed", "killing", "die", "died", "dying", "stay", "stayed", "staying", 
            "sit", "sat", "sitting", "stand", "stood", "standing", "lie", "lay", "lying", "walk", 
            "walked", "walking", "drive", "drove", "driving", "fly", "flew", "flying", "ride", "rode", 
            "riding", "carry", "carried", "carrying", "drop", "dropped", "dropping", "pick", "picked", 
            "picking", "catch", "caught", "catching", "throw", "threw", "throwing", "push", "pushed", 
            "pushing", "pull", "pulled", "pulling", "lift", "lifted", "lifting", "press", "pressed", 
            "pressing", "touch", "touched", "touching", "smell", "smelled", "smelling", "taste", "tasted", "tasting", 
            "sound", "sounded", "sounding", "listen", "listened", "listening", "watch", "watched", "watching", "notice", 
            "noticed", "noticing", "realize", "realized", "realizing", "understand", "understood", "understanding", 
            "remember", "remembered", "remembering", "forget", "forgot", "forgetting", "learn", "learned", "learning", 
            "teach", "taught", "teaching", "study", "studied", "studying", "practice", "practiced", "practicing", 
            "train", "trained", "training", "improve", "improved", "improving", "increase", "increased", "increasing", 
            "decrease", "decreased", "decreasing", "rise", "rose", "rising", "fall", "fell", "falling", "jump", 
            "jumped", "jumping", "climb", "climbed", "climbing", "crawl", "crawled", "crawling", "swim", "swam", 
            "swimming", "dive", "dived", "diving", "sink", "sank", "sinking", "float", "floated", "floating", 
            "flow", "flowed", "flowing", "pour", "poured", "pouring", "fill", "filled", "filling", "empty", 
            "emptied", "emptying", "clean", "cleaned", "cleaning", "wash", "washed", "washing", "dry", 
            "dried", "drying", "wet", "wetted", "wetting", "hot", "cold", "warm", "cool", "big", "small", 
            "large", "little", "huge", "tiny", "long", "short", "tall", "high", "low", "deep", "shallow", 
            "wide", "narrow", "thick", "thin", "heavy", "light", "strong", "weak", "fast", "slow", "quick", 
            "quickly", "slowly", "early", "late", "new", "old", "young", "fresh", "stale", "good", "bad", "better", "best", "worse", "worst", 
            "great", "greater", "greatest", "nice", "nicer", "nicest", "beautiful", "less", "least", "much", 
            "many", "several", "every", "either", "neither", "one", "two", "three", "first", "second", "last", 
            "next", "previous", "before", "after", "during", "while", "since", "until", "unless", "although", 
            "though", "however", "therefore", "thus", "hence", "moreover", "furthermore", "additionally", 
            "besides", "also", "never", "always", "often", "sometimes", "rarely", "seldom", "hardly", "barely", 
            "scarcely", "almost", "nearly", "quite", "rather", "pretty", "extremely", "highly", "completely", 
            "totally", "entirely", "partly", "partially", "mostly", "mainly", "primarily", "especially", "particularly", 
            "specifically", "generally", "usually", "normally", "typically", "commonly", "frequently", "occasionally", 
            "up", "over", "users", "about", "out", "like", "into", "which", "what", "who", "time", "were", "re", 
            "around", "able", "another", "available", "according", "already", "git", "across", "being", "back", 
            "between", "based", "behind"
    );

    /*
     This method adds a document
     @param int docID, String text
     @return no return
     */
    // addDocument
    public void addDocument(int docID, String text) {
        // Ignore null/blank documents; they should not modify the index.
        if (text == null || text.trim().isEmpty()) {
            return;
        }

        //tokenize the text and add the docID to the set of document IDs for each token
        String[] tokenList = tokenize(text);
        for (String token : tokenList) {
            if (token == null || token.isEmpty()) {
                continue;
            }
            // skip stop words
            if (STOP_WORDS.contains(token)) {
                continue;
            }
            
            // Add token to index using HashMap
            // If the token doesn't exist, create a new set for it
            index.computeIfAbsent(token, k -> new HashSet<>()).add(docID);
        }
    }


    /*
    This method returns a set of document IDs based on the query
    Return a Set <Integer> containing all document IDs that contain all the words in the query.
    @param String query
    @return Set<Integer>
    */
    //search
    public Set<Integer> search(String query) {
    	
    	//if the query is empty, return an empty set
    	if (query == null || query.isEmpty()) {
    		return new HashSet<>();
    	}
    	
    	//if index is empty, return an empty set
    	if (index.isEmpty()) {
    		return new HashSet<>();
    	}
    	
        //tokenize the query
		String[] tokenList = tokenize(query);
		Set<Integer> result = null;
		
		//return set of document IDs that contain all the words in the query
		for (String token : tokenList) {
			// skip stop words
			if (STOP_WORDS.contains(token)) {
				continue;
			}
			
			//search for the token in the HashMap
			Set<Integer> docIDs = index.get(token);
			
			//if the token is not found, return an empty set
			if (docIDs == null) {
				return new HashSet<>();
			}
			
			//if this is the first token, initialize result with its document IDs
			if (result == null) {
				result = new HashSet<>(docIDs);
			} else {
				//keep only the document IDs that are in both sets (intersection)
				result.retainAll(docIDs);
			}
			
			//if result is empty, no documents contain all the words
			if (result.isEmpty()) {
				return new HashSet<>();
			}
		}
		
		//if result is still null, all tokens were stop words
		return result == null ? new HashSet<>() : result;
    }


    /*
     This method removes a document based on the docID
     @param int docID
     @return void
     */
    // remove the document ID from all keywords in the index
    public void removeDocument(int docID){
    	
    	//if the index is empty, return 
    	if (index.isEmpty()) {
    		return;
    	}
    	
    	//iterate through all keywords and remove the docID from their sets
    	for (Set<Integer> docIDs : index.values()) {
    		docIDs.remove(docID);
    	}
    	  	
        return;
    }

    /*
     This method get the map of inverted index
     can be used for testing purposes
     key is keyword and value is the set of document IDs
     It MUST return a map sorted alphabetically by keyword
     @param none
     @return Map<String, Set<Integer>>
     */
    // returns the map of the inverted index sorted alphabetically by keyword
    public Map<String, Set<Integer>> getIndex() {
    	//if the index is empty, return an empty map
    	if (index.isEmpty()) {
    		return new HashMap<>();
    	}
    	
    	// Create a TreeMap which automatically sorts keys alphabetically
    	return new TreeMap<>(index);
    }

    /*
     * TODO: Implement helper methods below
     */
    
    /***
     * Helper method to to tokenize the text. 
     * The tokenization MUST: convert text to lowercase, preserve hyphens (-) when they appear between letters or digits, so “in-order” remains “in-order”, remove all other punctuation or symbols, replacing them with spaces. 
     * Split on one or more whitespace characters to produce tokens. 
	 * @param text the text to tokenize
	 * @return a list of tokens
     */
    static String[] tokenize(String text) {
    	
    	// tokenize ensures the text is lowercase all non-alphanumeric (except spaces and hyphens)
    	// characters are replaced with a space and then split on whitespace
    	text = text.toLowerCase().replaceAll("[^a-z0-9\\s-]", " ");
    	
    	// remove leading hyphens
    	text = text.replaceAll("^-+", "");
    	//remove trailing hyphens
    	text = text.replaceAll("-+$", "");

    	// remove hyphens after spaces
    	text = text.replaceAll("\\s-+", " ");
    	// remove hyphens before spaces
    	text = text.replaceAll("-+\\s", " ");
    	
    	//split on one or more whitespace characters
    	String[] tokens = text.split("\\s+");
    	
    	//skip any token with length of 1
    	tokens = Arrays.stream(tokens).filter(token -> token.length() > 1).toArray(String[]::new);

		return tokens;

    }


}
