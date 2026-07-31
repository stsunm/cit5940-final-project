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

    // Root of the BST
    private BSTNode root;

    // define a private static inner class that represents a node in the BST
    private static class BSTNode{
        // keyWord that is indexed
        String keyWord;
        // set of IDs where the keyWord appears
        Set<Integer> documentIDs;
        // the left node stores keywords less than this node's keyword
        // the right node stores keywords greater than this node's keyword
        BSTNode left, right;

        // constructor to initialize each node
        BSTNode(String keyWord, int docID){
            this.keyWord = keyWord;
            this.documentIDs = new HashSet<>();
            this.documentIDs.add(docID);
        }
    }

    // DO NOT CHANGE THE FOLLOWING SET OF STOP_WORDS
    static final Set<String> STOP_WORDS = Set.of(
            "i", "me", "my" , "myself" , "we" , "our" , "ours" , "ourselves" , "you" , "your" ,
            "yours" , "yourself" , "yourselves" , "he" , "him" , "his" , "himself" , "she" ,
            "her" , "hers" , "herself" , "it" , "its" , "itself" , "they" , "them" , "their" ,
            "theirs" , "themselves" , "what" , "which" , "who" , "whom" , "this" , "that" ,
            "these" , "those" , "am" , "is" , "are" , "was" , "were" , "be" , "been" , "being" ,
            "have" , "has" , "had" , "having" , "do" , "does" , "did" , "doing" , "a" , "an" ,
            "the" , "and" , "but" , "if" , "or" , "because" , "as" , "until" , "while" ,
            "of" , "at" , "by" , "for" , "with" , "about" , "against" , "between" , "into" , "through" ,
            "during" , "before" , "after" , "above" , "below" , "to" , "from", "up", "down", "in", "out", "on", "off", "over", "under", "again", "further", "then", "once", "here", "there", "when",
            "where", "why", "how", "all", "any", "both", "each", "few", "more", "most", "other", "some", "such",
            "no", "nor", "not", "only", "own", "same", "so", "than", "too", "very", "s", "t", "can", "will", "just",
            "don", "should", "now", "said", "announced", "company", "industry", "technology", "system", "application",
            "software", "update", "service"
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

        //tokenize the text and add the docID to the set of document IDs for each token in the BST
        String[] tokenList = tokenize(text);
        for (String token : tokenList) {
            if (token == null || token.isEmpty()) {
                continue;
            }
            // skip stop words
			if (STOP_WORDS.contains(token)) {
				continue;
			}
			
			//check if the token is already in the BST, if it is add the docID to the set of document IDs for that token
			BSTNode current = root; // start at the root and traverse the tree to find the token
			BSTNode parent = null; // keep track of the parent node to insert a new node if the token is not found
			
			while (current != null) {
				parent = current;
				if (token.compareTo(current.keyWord) < 0) {
					current = current.left;
				} else if (token.compareTo(current.keyWord) > 0) {
					current = current.right;
				} else {
					current.documentIDs.add(docID);
					break;
				}
			}
			//if we reach a null node, it means the token is not in the BST, so we insert a new node with the token and docID
			if (current == null) {
				BSTNode newNode = new BSTNode(token, docID);
				if (parent == null) {
					root = newNode; // if the tree is empty, set the new node as the root
				} else if (token.compareTo(parent.keyWord) < 0) {
					parent.left = newNode;
				} else {
					parent.right = newNode;
				}
			}
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
    	if (root == null) {
    		return new HashSet<>();
    	}
    	
        //tokenize the query
		String[] tokenList = tokenize(query);
		Set<Integer> result = new HashSet<>();
		
		//return set of document IDs that contain all the words in the query
		for (String token : tokenList) {
			// skip stop words
			if (STOP_WORDS.contains(token)) {
				continue;
			}
			
			//search for the token in the BST
			BSTNode current = root;
			while (current != null) {
				if (token.compareTo(current.keyWord) < 0) {
					current = current.left;
				} else if (token.compareTo(current.keyWord) > 0) {
					current = current.right;
				} else {
					//if the token is found, add the document IDs to the result set
					if (result.isEmpty()) {
						result.addAll(current.documentIDs); //if the result set is empty, add all the document IDs for the first token
					} else {
						result.retainAll(current.documentIDs); //keep only the document IDs that are in both sets
					}
					break;
				}
			}
			
			//if the token is not found, return an empty set
			if (current == null) {
				return new HashSet<>();
			}
		}
		
		return result;
    }


    /*
     This method removes a document based on the docID
     @param int docID
     @return void
     */
    // to remove a document traverse the entire tree and remove the given docID from the node's set
    // remove the document ID
    public void removeDocument(int docID){
    	
    	//if the index is empty, return 
    	if (root == null) {
    		return;
    	}
    	
    	//traverse the entire tree and remove the given docID from the node's set
    	BSTNode current = root;
    	Stack<BSTNode> stack = new Stack<>();
    	while (current != null || !stack.isEmpty()) {
    		//traverse the left subtree
			while (current != null) {
				stack.push(current); //push the current node to the stack
				current = current.left; //move to the left child
			}
			current = stack.pop(); //pop the node from the stack
			//remove the docID from the current node's set of document IDs if it exists
			if (current.documentIDs.contains(docID)) {
				current.documentIDs.remove(docID);
			}
			current = current.right; //move to the right child
    	}
    	  	
        return;
    }

    /*
     This method get the map of inverted index
     can be used for testing purposes
     key is keyword and value is the set of document IDs
     It MUST perform an in-order traversal to ensure that the map is sorted alphabetically by keyword
     @param none
     @return Map<String, Set<Integer>>
     */
    // returns the map of the inverted index
    public Map<String, Set<Integer>> getIndex() {
    	//if the index is empty, return an empty map
    	if (root == null) {
    		return new HashMap<>();
    	}
    	//Option 1: Perform an in-order traversal of the BST and construct the returned map in the same order as the traversal
    	Map<String, Set<Integer>> indexMap = new LinkedHashMap<>();
    	Stack<BSTNode> stack = new Stack<>();
    	BSTNode current = root;
    	while (current != null || !stack.isEmpty()) {
    		//traverse the left subtree
    		while (current != null) {
    			stack.push(current); //push the current node to the stack
    			current = current.left; //move to the left child
    										
    		}
    		current = stack.pop(); //pop the node from the stack
    		indexMap.put(current.keyWord, current.documentIDs); //add the current node's keyword and document IDs to the map
    		current = current.right; //move to the right child
    	}
    	
       return indexMap;
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

		return tokens;

    }


}
