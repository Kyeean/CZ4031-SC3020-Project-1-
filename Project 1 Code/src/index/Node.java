package index;

import java.util.ArrayList;
import java.util.SortedMap;
import java.util.TreeMap;
/* 
 * This class represents a node in a B+ Tree 
*/
public class Node {
    private boolean isLeaf; /*Check if node is a leaf node*/
    private boolean isRoot; /*Check if node is a root node*/
    private int nodeSize; /*Node size*/
    private int NonLeafSize; /*minimum size of non leaf node*/
    private int LeafNodeSize; /*minimum size of leaf node*/
    static final int NODE_SIZE = BpTree.NODE_SIZE;
    private NonLeafNode parent;
    protected ArrayList<Integer> keys;
    Node rootNode;


    public Node(){
        this.rootNode = BpTree.getRoot();
        this.isLeaf = false;
        this.isRoot = false;
        this.nodeSize = NODE_SIZE;
        this.LeafNodeSize = (int) (Math.floor((nodeSize + 1) / 2));
        this.NonLeafSize = (int) (Math.floor(nodeSize / 2));
    }

    public int getLeafNodeSize(){
        return this.LeafNodeSize;
    }

    public int getNonLeafSize(){
        return this.NonLeafSize;
    }

    public boolean isLeaf(){
        return isLeaf;
    }

    public boolean isNonLeaf(){
        return !isLeaf;
    }

    public void setLeaf(boolean isALeaf){
        isLeaf = isALeaf;
    }

    public boolean isRoot(){
        return isRoot;
    }
    
    public void setRoot(boolean isARoot){
        isRoot = isARoot;
    }

    public NonLeafNode getParent(){
        return this.parent;
    }

    public void setParent(NonLeafNode setParent){
        if(this.isRoot()){
            this.setRoot(false);
            setParent.setRoot(true);
            setParent.setLeaf(false);
            BpTree.setRoot(setParent);
        }
        else{
            setParent.setLeaf(false);
        }
        this.parent.setParent;
    }

    public void removeKeyAtLast(){
        this.keys.remove(keys.size()-1);
    }
    void replaceKeyAt(int index, int key){
        keys.set(index, key);
    }

    public ArrayList<Integer> getKeys(){
        return this.keys;
    }
    public void printNode(){
        System.out.println(keys);
    }
    public int getKey(int index){
        return this.keys.get(index);
    }
    public int getKeySize(){
        return keys.size();
    }
    public int getLastKey(){
        return this.keys.get(keys.size()-1);
    }
    public int getFirstKey(){
        return this.keys.get(0);
    }
    int removeKeyAt(int index){
        return keys.remove(index);
    }

    int searchKey(int key, boolean upperBound){
        int keyCount = keys.size();
        return searchKey(0, keyCount-1, key, upperBound);
    }

    private int searchKey(int left, int right, int key, boolean upperBound){
        if (left > right)
            return left;
        int middle = (left+right)/2;
        int middleKey = getKeyAt(middle);
        if(middleKey < key){
            return searchKey(middle+1, right, key, upperBound);
        }
        else if(middleKey > key){
            return searchKey(left, middle - 1, key, upperBound);
        }
        else{
            while(middle < keys.size() && keys.get(middle)==key)
                middle++;
            if(!upperBound)
                return middle - 1;
            return middle;
            }
    }
    int getKeyAt(int index) {
        return keys.get(index);
    }

    public int getLastIdx() {
        return keys.size() - 1;
    }

    void insertKeyAt(int index, int key) {
        keys.add(index, key);
    }

    boolean isUnderUtilized(int maxKeyCount) {
        if (isRoot()) {
            return (this.getKeySize() < 1);
        } else if (isLeaf()) {
            return (this.getKeySize() < (maxKeyCount + 1) / 2);
        } else {
            return (this.getKeySize() < maxKeyCount / 2);
        }
    }

    public static void insertInOrder(ArrayList<Integer> keys, int key) {
        int i = 0;

        while (i < keys.size() && keys.get(i) < key) {
            i++;
        }
        keys.add(i, key);
    }

    public void insertChildInOrder(NonLeafNode parent, NonLeafNode child) {
        int i = 0;
        int childToSort = child.getKeyAt(0);
        while (i < parent.getKeySize() && parent.getKeyAt(i) < childToSort) {
            i++;
        }
        parent.children.add(i + 1, child);
    }

    public void updateOneKeyOnly(int keyIndex, int newKey) {
        if (keyIndex >= 0 && keyIndex < keys.size()) {
            keys.set(keyIndex, newKey);
        }
    }

    public void updateKey(int keyIndex, int newKey, boolean leafNotUpdated, int lowerbound) {
        if (keyIndex >= 0 && keyIndex < keys.size() && !leafNotUpdated) {
            keys.set(keyIndex, newKey);
        }
        if (parent != null && parent.isNonLeaf()) {
            int childIndex = parent.getChildren().indexOf(this);

            if (childIndex >= 0) {
                if (childIndex > 0) {
                    parent.replaceKeyAt(childIndex - 1, keys.get(0));

                }
                parent.updateKey(childIndex - 1, newKey, false, lowerbound);
            }
        } else if (parent != null && parent.isLeaf()) {

            parent.updateKey(keyIndex, newKey, false, lowerbound);
        }

    }

    public boolean isAbleToGiveOneKey(int maxKeyCount) {
        if (isNonLeaf())
            return getKeySize() - 1 >= maxKeyCount / 2;
        return getKeySize() - 1 >= (maxKeyCount + 1) / 2;

    }

    public void insertNewNodeToParent(LeafNode newNode) {
        int index = 0;
        boolean insertedNode = false;

        try {
            for (Node currentNode : this.getParent().getChildren()) {

                if (newNode.getKey(newNode.getKeySize() - 1) < currentNode.getKey(0)) {
                    this.getParent().getChildren().add(index, newNode);
                    this.getParent().keys.add(index - 1, newNode.getKey(0));
                    insertedNode = true;
                    break;
                }
                index++;
            }

            if (!insertedNode) {
                this.getParent().getChildren().add(newNode);
                this.getParent().keys.add(newNode.getKey(0));
            }

        } catch (Exception e) {
            this.getParent().getChildren().add(newNode);
            this.getParent().keys.add(newNode.getKey(0));
        }

        newNode.setParent(this.getParent());



}