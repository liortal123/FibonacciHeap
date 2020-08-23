import java.util.Arrays;

/**
 * FibonacciHeap
 *
 * An implementation of fibonacci heap over integers.
 */

public class FibonacciHeap
{
	
	private static int linkCount;
	private static int cutCount;
	private int size; //number of nodes
	private int treeNum; // number of trees
	private int markCount;
	private HeapNode min;
	private HeapNode first;
	
	
   	
   /**
    * public boolean isEmpty()
    *
    * precondition: none
    * 
    * The method returns true if and only if the heap
    * is empty.
    *   
    */
    public boolean isEmpty()
    {
    	return first==null;
    }
		
   /**
    * public HeapNode insert(int key)
    *
    * Creates a node (of type HeapNode) which contains the given key, and inserts it into the heap. 
    */
    public HeapNode insert(int key)
    {   
    	HeapNode newNode = new HeapNode(key);
    	if (isEmpty()) {
    		size++;      //updating size, first, min,treeNum
    		first = newNode;
    		min = newNode;
    		treeNum = 1;
    		return newNode;
    	}
    	size++; 
    	treeNum++; //adding the new node as a new tree
    	HeapNode tmp = first;
    	first = newNode;
    	first.next = tmp; //updating the new first next to point at the old first
    	first.prev = tmp.prev; //updaing the new first prev to point at the last
    	first.prev.next = first; //updating last. prev to point at first
    	tmp.prev = first; //updating tmp prev to point at first
    	if (key<min.key) {
    		min = newNode; //updating the new min
    	}
    	return newNode;
    }

   /**
    * public void deleteMin()
    *
    * Delete the node containing the minimum key.
    *
    */
    public void deleteMin()
    {
    	if (isEmpty())
    		return;
    	
    	if (size==1) {
    		first = null;
    		min = null;
    		size = 0;
    		treeNum = 0;
    		return;
    	}
    	
    	//each index represent a rank of a tree. if null means the bucket is empty 
    	//ranks from zero to logn (including)
    	HeapNode[] arrBuckets = new HeapNode[Integer.toBinaryString(size).length()+1]; 
    	//starting from min- putting all of min children in the correct bucket 
    	size--;
    	
    	HeapNode node=first;
    	int numberOfTrees = treeNum;
    	for (int i=0;i<numberOfTrees;i++) { //we proceed in the chain of trees from first until completing the loop 
    		if (node==min) {
    			meldMinChildren(arrBuckets); //inserting meld chidren to the buckets
    			node = node.next;
    		}
    		else 
    			node = disconncetAndMeld(arrBuckets,node);	 //the node that returns is the next root in the chain of roots
    	}
    	
    	 bucketsToFibHeap(arrBuckets);
    }
    
    /** private void bucketsToFibHeap(HeapNode[] arrBuckets)
    * connecting the buckets back to a chain of roots and a heap
    */
    
    private void bucketsToFibHeap(HeapNode[] arrBuckets) {
    	HeapNode node = null;
    	treeNum = 0;
    	first = null;
    	for (int i=0;i<arrBuckets.length;i++) { //going through the buckets
    		if (arrBuckets[i]!=null) { //bucket is not empty
    			treeNum++; //there is a tree in the bucket 
    			if (first==null) { //first will point to the tree with the lowest rank 
    				first = arrBuckets[i];
    				node = first;
    				min = first;
    				continue;
    			}
    			node.next = arrBuckets[i]; //connecting the chain of tree roots
    			arrBuckets[i].prev = node;
    			node = node.next;
    			if (node.getKey()<min.getKey()) //updating min if needed
    				min = node;	
    		}		
    	} 
    	//connecting first and last to point at each other
    	node.next = first; //first will never be null because the list will never be empty
    	first.prev = node; //if the size is 0 or 1 before deletemin - dealt separately
    	
    }
    
    /**  private HeapNode discconcetAndMeld(HeapNode[] arrBuckets,HeapNode node)
     * disconnecting the node from the chain of roots
     * and inserting node to the buckets (to be melded if needed)
     */
    
    private HeapNode disconncetAndMeld(HeapNode[] arrBuckets,HeapNode node) {
    	HeapNode next = node.next;
		disconnect(node); //disconnecting node from the chain of roots
		notLazyMeld(arrBuckets,node);  //adding node to the right bucket
		return next;	
    }
    
    
    
    /** private void meldMinChildren(HeapNode[] arrBuckets)  
     * dealing with min children separately
     * inserting min children to the buckets to be melded
     */  
    
    private void meldMinChildren(HeapNode[] arrBuckets) {
    	int rank = min.rank;  //rank is number of children
    	HeapNode child = min.child;
    	for (int i=0;i<rank;i++) { //for every child root, separating it and melding it to the list 
    		if (child.mark == 1) { //if the child was marked- it will now become a root and therefore unmarked
    			child.mark = 0;
    			markCount--; //decreasing the amount of marked
    		}
    		child = disconncetAndMeld(arrBuckets, child); //adding min children to the buckets
    	}
    	
    }
    
    
    
    /**  private void notLazyMeld(HeapNode[] arrBuckets,HeapNode node) 
     * the methods receives buckets and a node
     * it checks if the bucket is empty (there isn't a tree in the same rank)
     * if the bucket is already occupied we link the tree in the bucket and node
     */  
    private void notLazyMeld(HeapNode[] arrBuckets,HeapNode node) {
    	while (arrBuckets[node.rank]!=null) { 
			node = link(arrBuckets[node.rank],node);// after each link node rank is increased by 1
			arrBuckets[node.rank-1] = null; //updating the bucket to be empty
		}
		arrBuckets[node.rank] = node;
    	
    }
    
    /**  private void disconnect(HeapNode root)
     * disconnecting node from the chain of nodes
     */
    private void disconnect(HeapNode node) {
		node.prev = node;//disconnecting child from next,prev and parent
		node.next = node;
		node.parent = null;
    }
    
    /** private HeapNode link(HeapNode root1, HeapNode root2)
     * assuming root1 and root2 are same rank 
     * they are binomial tree roots  
     * linking both trees- taking the min node and hanging the other node as it's child
     */
    private HeapNode link(HeapNode root1, HeapNode root2) {
    	linkCount++;
    	treeNum--; //two linked- one less tree in the heap
    	if (root2.getKey()<root1.getKey()) {//if root2>root1 switching them
    		HeapNode tmp = root1;
    		root1 = root2;
    		root2 = tmp;
    	}
    	//assuming root1<root2	  
    	if (root1.rank==0) {//also root2.rank==0 we assumed equal rank
    		root1.child = root2;
    		root2.parent = root1;
        	root1.rank++;
    		return root1;
    	}
    	HeapNode childTmp = root1.child;
    	root1.child = root2;
    	root2.next = childTmp;
    	childTmp.prev.next = root2; //changing last next to point to root1
    	root2.prev = childTmp.prev; //changing root 2 prev to point to last
    	childTmp.prev = root2;
    	root2.parent = root1;
    	root1.rank++;
    	return root1;
    }


   /**
    * public HeapNode findMin()
    *
    * Return the node of the heap whose key is minimal. 
    *
    */
    public HeapNode findMin()
    {
    	return min;
    } 
    
   /**
    * public void meld (FibonacciHeap heap2)
    *
    * Meld the heap with heap2
    *
    */
    public void meld (FibonacciHeap heap2)
    {
    	if (this.isEmpty() && heap2.isEmpty())
    		return;  //if both heaps are empty do nothing
    	if (heap2.isEmpty()) 
    		return; //if the second heap is empty do nothing 
    	if (this.isEmpty()) { //if this (heap) empty, turn it to heap2
    		this.first = heap2.first;
    		this.min = heap2.findMin();
    		this.size = heap2.size();
    		this.treeNum = heap2.treeNum;
    		this.markCount = heap2.markCount;
    		return;
    	}
    	
    	// we connect the end of this heap to the first of heap2
    	// updating the pointers to complete a circle
    	
    	HeapNode tmpHeap2Last = heap2.first.prev; //keeping heap2 last as tmp
    	this.first.prev.next = heap2.first;  
    	heap2.first.prev = this.first.prev; 
    	tmpHeap2Last.next = this.first;
    	this.first.prev = tmpHeap2Last;
    	
    	if (heap2.findMin().getKey()<min.getKey()) //updating the new min of the final heap if necessary
    		min = heap2.findMin();
    	
    	size = size + heap2.size(); //updating the necessary fields
    	treeNum = treeNum + heap2.treeNum;
    	markCount = markCount + heap2.markCount;
    }

   /**
    * public int size()
    *
    * Return the number of elements in the heap
    *   
    */
    public int size()
    {
    	return size; 
    }
    	
    /**
    * public int[] countersRep()
    *
    * Return a counters array, where the value of the i-th entry is the number of trees of order i in the heap. 
    * 
    */
    public int[] countersRep()
    {
    	if (isEmpty()) { 
    		return new int[0];
    	}
    	int[] arr = new int[Integer.toBinaryString(size).length()+1];
    	HeapNode node = first;
    	int maxRank = 0; //keeping the value of the max rank
    	int i=0;
    	while (i<treeNum) { //going through all the trees in the heap 
    		arr[node.rank]++; //increasing the number of trees in this rank
    		node = node.next;
    		if (node.rank>maxRank)  //updating the value of max rank
    			maxRank = node.rank; 
    		i++;
    	}
    	return Arrays.copyOfRange(arr,0,maxRank+1); //returning the array in length of the maximal rank
    }
	
   /**
    * public void delete(HeapNode x)
    *
    * Deletes the node x from the heap. 
    *
    */
    public void delete(HeapNode x) 
    {
    	x.key = 0; //in order to delete x we increase it's value to the min value possible in Integer
    	this.decreaseKey(x, Integer.MIN_VALUE);
    	this.deleteMin(); //it will be the minimum and we will delete it
    }

   /**
    * public void decreaseKey(HeapNode x, int delta)
    *
    * The function decreases the key of the node x by delta. The structure of the heap should be updated
    * to reflect this chage (for example, the cascading cuts procedure should be applied if needed).
    */
    public void decreaseKey(HeapNode x, int delta)
    {
    	x.key = x.getKey()-delta; //decreasing delta from x

    	if (x.getKey()<=min.getKey()) 
			min = x; 
    	
    	if (x.parent == null)   //x is a root of a tree, so increasing him doesn't change the structure
    		return;
    	
    	if (x.parent.getKey()<x.getKey())  //the heap is still legal
    		return;
    	
    	cascadingCuts(x,x.parent);
    	
    }
    
    /**
     *private void cascadingCuts(HeapNode x,HeapNode parent)
     *The function assumes x needs to be cut from the tree, 
     *parent is the parent of x.
     *the function cuts x from the tree, and check to see if the parent need to be cut
     *as well, if so called recursively with parent and parent's parent.
     */
    
    private void cascadingCuts(HeapNode x,HeapNode parent) {
    	cut(x,parent); //cutting x from tree and inserting to the start of the heap 
    	if (parent.parent!=null) { //if parent.parent = null parent is a root and it doesn't need to be marked
    		if (parent.mark==0) {
    			parent.mark = 1; //marking parent because his child was cut
    			markCount++;
    		}
    		else cascadingCuts(parent,parent.parent); //if the parent was already marked we need to cut him too 
    	}
    }
    
    /**
     *private void cut(HeapNode x,HeapNode parent) 
     *cutting x from the tree and updating the necessary fields
     */
    
    private void cut(HeapNode x,HeapNode parent) {
    	cutCount++;
    	x.parent = null;
    	markCount = markCount - x.mark; //if x.mark==0 no change, else x is now unmarked markCount--;
    	x.mark = 0;
    	parent.rank--; //parent lost a child and that is why rank--
    	if (x.next==x)
    		parent.child = null;
    	else {
    		if (parent.child==x) { //if x is the left child, we link parent and x.next
    			parent.child=x.next;
    		}
    		x.prev.next = x.next;
    		x.next.prev = x.prev;
    	}
    	
    	//inserting x back to the start of the heap
    	HeapNode tmpLast = first.prev;
    	first.prev = x;
    	x.next = first;
    	tmpLast.next = x;
    	x.prev = tmpLast;
    	first = x; //x will be the new first
    	treeNum++; //one more tree in the heap 
    }

   /**
    * public int potential() 
    *
    * This function returns the current potential of the heap, which is:
    * Potential = #trees + 2*#marked
    * The potential equals to the number of trees in the heap plus twice the number of marked nodes in the heap. 
    */
    public int potential() 
    {    
    	return treeNum+2*markCount; 
    }

   /**
    * public static int totalLinks() 
    *
    * This static function returns the total number of link operations made during the run-time of the program.
    * A link operation is the operation which gets as input two trees of the same rank, and generates a tree of 
    * rank bigger by one, by hanging the tree which has larger value in its root on the tree which has smaller value 
    * in its root.
    */
    public static int totalLinks()
    {    
    	return linkCount; 
    }

   /**
    * public static int totalCuts() 
    *
    * This static function returns the total number of cut operations made during the run-time of the program.
    * A cut operation is the operation which diconnects a subtree from its parent (during decreaseKey/delete methods). 
    */
    public static int totalCuts()
    {    
    	return cutCount; 
    }

     /**
    * public static int[] kMin(FibonacciHeap H, int k) 
    *
    * This static function returns the k minimal elements in a binomial tree H.
    * The function should run in O(k(logk + deg(H)). 
    */
    public static int[] kMin(FibonacciHeap H, int k)
    {    
    	if (k<=0 || H==null || H.isEmpty()) 
    		return new int[0];
        int[] arr = new int[k]; 
        arr[0] = H.findMin().getKey(); //inserting the min of the H to be the first in arr
        FibonacciHeap fh = new FibonacciHeap(); 
        HeapNode node = H.findMin(); //starting from the min
        for (int i=1;i<k;i++) { //we look for the k smallest numbers
        	int deg = node.rank;  
        	HeapNode child = node.child; //adding node's children to fh, because they
        	//are the next candidates to be the smallest numbers
        	for (int j=0;j<deg;j++) { //adding all of node's sibilings to fh
        		HeapNode fhNode = fh.insert(child.getKey());
        		fhNode.pointerForKMin = child; //keeping a pointer to the location of child in H
        		child = child.next;
        	}
        	HeapNode minNode = fh.findMin(); //finding the next smallest number in fh
        	arr[i] = minNode.getKey(); //adding it to the arr
        	fh.deleteMin(); //deleting it from fh 
        	node = minNode.pointerForKMin; //node's children are now the next candidates to be 
        	//the smallest in the Heap. adding them in the proceeding iteration
        }
        return arr;
    }
    
   /**
    * public class HeapNode
    * 
    * If you wish to implement classes other than FibonacciHeap
    * (for example HeapNode), do it in this file, not in 
    * another file 
    *  
    */
    public class HeapNode{
    	
		public int key;
    	private int rank = 0;
    	private int mark = 0;
    	private HeapNode child;
    	private HeapNode next = this;
    	private HeapNode prev = this;
       	private HeapNode parent;
       	private HeapNode pointerForKMin = null;

       	//getter and setters for the different fields
       	
    	public HeapNode(int key) {
    		this.key = key;
    	}

    	public int getKey() {
    		return this.key;
    	}
    	
    	public void setKey(int key) {
			this.key = key;
		}

		public int getRank() {
			return rank;
		}

		public void setRank(int rank) {
			this.rank = rank;
		}

		public int getMark() {
			return mark;
		}

		public void setMark(int mark) {
			this.mark = mark;
		}

		public HeapNode getChild() {
			return child;
		}

		public void setChild(HeapNode child) {
			this.child = child;
		}

		public HeapNode getNext() {
			return next;
		}

		public void setNext(HeapNode next) {
			this.next = next;
		}

		public HeapNode getPrev() {
			return prev;
		}

		public void setPrev(HeapNode prev) {
			this.prev = prev;
		}

		public HeapNode getParent() {
			return parent;
		}

		public void setParent(HeapNode parent) {
			this.parent = parent;
		}

    	

    }
}
