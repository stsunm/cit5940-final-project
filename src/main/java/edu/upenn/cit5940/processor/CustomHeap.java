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
 * Date: 2026-06-29
 */

package edu.upenn.cit5940.processor;

import java.util.Random;

public class CustomHeap {

    int[] numArray;

    private int size = 0;

    // TODO constructor
    public CustomHeap(int capacity) {
    	
    	//uses capacity argument to initialize the underlying numArray
    	numArray = new int[capacity];
    }

    // TODO
    //If your heap is full, you should not add any more elements, and instead just return false. 
    //Otherwise, if your heap has space, you should add the given number, updating any relevant array properties, and then return true to indicate a successful add operation.
    public boolean addNum(int number) {
    	
    	//check if the heap is full
    	if(size >= numArray.length) {
    		return false;
    	}
    	else {
    		
			//add the number to the end of the array
			numArray[size] = number;
			size++;
			//bubble up the newly added number to maintain heap property
			bubbleUp(size - 1);		
    	}
        return true;
    }

    // this implementation is given to students in the starter code
    public boolean addList(int[] myList) {
        for (int i = 0; i < myList.length; i++) {
            if (!addNum(myList[i]))
                return false;
        }
        return true;
    }

    // TODO
    //given an index, return the index of that value’s parent. Must remain public for autograder testing.
    public int getParentIndex(int index) {
    	
    	//check if the index is valid (i.e., within the bounds of the array)
    	if(index < 0 || index >= size) {
    		throw new IllegalArgumentException("Index is out of bounds");
    	}
        //if the index is 0, it has no parent, return -1
    	if(index == 0) {
    		return -1;
		}
		else {
			//return the parent index using the formula (index - 1) / 2
			return (index - 1) / 2;
    	}
    }

    // TODO
    //helper method used in bubbleUp(). The parameters to this method are the indices (of the values/nodes) that will be swapped.
    public void swap(int i1, int i2) {
    	//store the value at i1 in a temporary variable
		int temp = numArray[i1];
		//set the value at i1 to the value at i2
		numArray[i1] = numArray[i2];
		//set the value at i2 to the temporary variable
		numArray[i2] = temp;
    }

    // TODO
    //used to maintain a certain property of the min heap; that is, each node has a smaller value than both of its child nodes. 
    public void bubbleUp(int index) {
    	
		//get the parent index of the current index
		int parentIndex = getParentIndex(index);
		
		//while the current index is not the root and the value at the current index is less than the value at the parent index
		while(index > 0 && numArray[index] < numArray[parentIndex]) {
			//swap the values at the current index and the parent index
			swap(index, parentIndex);
			//update the current index to be the parent index
			index = parentIndex;
			//update the parent index to be the new parent of the current index
			parentIndex = getParentIndex(index);
		}
    }

    // this implementation is given to students in the starter code
    public int[] getArray() {
        return this.numArray;
    }

    // main can be used for quick testing
    public static void main(String[] args) {

        int testCapacity = 15;

        Random rnd = new Random();

        // create and populate array w/distinct values
        int[] mainArr = new int[testCapacity];
        mainArr = rnd.ints(1, 20).distinct().limit(testCapacity).toArray();

        // create CustomHeap class, and then add array
        CustomHeap myHeap = new CustomHeap(testCapacity);
        myHeap.addList(mainArr);
    }
}
