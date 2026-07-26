/*
 * I attest that the code in this file is entirely my own except for the starter
 * code provided with the assignment and the following exceptions:
 * <
 * Enter all external resources and collaborations here. Note external code may
 * reduce your score but appropriate citation is required to avoid academic
 * integrity violations. Please see the Course Syllabus as well as the
 * university code of academic integrity:
 *  https://catalog.upenn.edu/pennbook/code-of-academic-integrity/
 * >
 * Signed,
 * Author: Stephanie Sun
 * Penn email: <stsun@seas.upenn.edu>
 * Date: 2026-06-29
 */

package edu.upenn.cit5940.processor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CustomTrie {

    // inner class
    private class Node {
        private HashMap<Character, Node> children = new HashMap<>();

        // TODO (provide starting value)
        private boolean endOfWord = false;

    }

    // root node (has no value)
    private Node root = new Node();

    // TODO
    public void insertWord(String word) {
    	// handle null or empty string - don't insert them
    	if(word == null || word.isEmpty()) {
    		return;
    	}
    	
    	//start at the root node
    	Node currentNode = root;
    	//iterate through each character in the word
    	for(int i = 0; i < word.length(); i++) {
    		char currentChar = word.charAt(i);
			//check if the current character is already a child of the current node
			if(!currentNode.children.containsKey(currentChar)) {
				//if not, create a new node for the character and add it to the children of the current node
				currentNode.children.put(currentChar, new Node());
			}
			//move to the child node corresponding to the current character
			currentNode = currentNode.children.get(currentChar);
    	}
    	// mark the final node as end of word
    	currentNode.endOfWord = true;
    }

    // this implementation is given to students in the starter code
    public void insertList(String[] wordList) {
        for (String string : wordList) {
            insertWord(string);
        }
    }

    // TODO
    public boolean findWord(String word) {
    	// handle null or empty string - they are not valid words
    	if(word == null || word.isEmpty()) {
    		return false;
    	}
    	
    	Node currentNode = root;
    	//iterate through each character in the word
    	for(int i = 0; i < word.length(); i++) {
    		char currentChar = word.charAt(i);
    		//check if the current character is a child of the current node
    		if(!currentNode.children.containsKey(currentChar)) {
				//if not, the word is not in the trie
				return false;
			}
    		//advance to the child node corresponding to the current character
    		currentNode = currentNode.children.get(currentChar);
    	}
    	//only return true if this node is marked as the end of a word
        return currentNode.endOfWord;
    } 

    // TODO
    public void deleteWord(String word) {
    	// handle null or empty string - ignore deletion attempts for invalid input
    	if(word == null || word.isEmpty()) {
    		return;
    	}
    	//call the helper method to delete the word starting from the root node and index 0
    	deleteWordHelper(word, 0, root);
    }

    // TODO
    private boolean deleteWordHelper(String word, int index, Node curNode) {
    	//base case: if we have reached the end of the word
    	if(index == word.length()) {
    		// if it is not the end of word, then word doesn't exist in the trie — nothing to delete
    		if(!curNode.endOfWord) {
    			return false;
    		}
    		// unmark end of word so the word is no longer recognized
    		curNode.endOfWord = false;
    		// only signal the parent to delete this node if it has no children
    		return curNode.children.isEmpty();
    	}
    	
    	//get the current character in the word
    	char currentChar = word.charAt(index);
		//check if the current character is a child of the current node
    	if(!curNode.children.containsKey(currentChar)) {
    		//if not, the word is not in the trie
    		return false;
    	}
    	else {
			//if the current character is a child of the current node, recursively call the helper method on the child node
			Node childNode = curNode.children.get(currentChar);
			boolean shouldDeleteChild = deleteWordHelper(word, index + 1, childNode);
			
			//if the child node should be deleted, remove it from the children of the current node
			if(shouldDeleteChild) {
				curNode.children.remove(currentChar);
				//return true if the current node has no other children and is not the end of another word
				return curNode.children.isEmpty() && !curNode.endOfWord;
			}
			else {
				return false;
			}
    	}
        
    }

    // TODO
    //returns a list of all String (words) currently in the trie.
    public List<String> allWords() {
    	List<String> myList = new ArrayList<>();
		//call the helper method to find all words starting from the root node and an empty accumulated string
		allWordsHelper(root, new StringBuilder(), myList);
		return myList;
    }

    // TODO
    public void allWordsHelper(Node node, StringBuilder accumulated, List<String> myList) {
    	//base case: if the current node is null, return
    	if(node == null) {
    		return;
    	}
    	//if the current node is the end of a word, add the accumulated string to the list
    	if(node.endOfWord) {
    		//add the accumulated string to the list
    		myList.add(accumulated.toString());	
    	}
    	//iterate through the children of the current node
    	for(Character c : node.children.keySet()) {
    		accumulated.append(c);
    		//recursively call the helper method on the child node
    		allWordsHelper(node.children.get(c), accumulated, myList);
    		//backtrack by removing the last character from the accumulated string
    		accumulated.deleteCharAt(accumulated.length() - 1);
    	}
    }

}