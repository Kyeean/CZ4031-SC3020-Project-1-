package index;

import java.util.ArrayList;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import storage.Address;
/* 
 * This class represents a node class in a Bp Tree 
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

    //Initial parameters of a Node
    public Node(){
        this.rootNode = BpTree.getRoot();
        this.isLeaf = false;
        this.isRoot = false;
        this.nodeSize = NODE_SIZE;
        this.LeafNodeSize = (int) (Math.floor((nodeSize + 1) / 2));
        this.NonLeafSize = (int) (Math.floor(nodeSize / 2));
    }
    public void removeKeyAtLast(){
        this.keys.remove(keys.size()-1);
    }
    public void replaceKeyAt(int index, int key){
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
    public int removeKeyAt(int index){
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
    public int getKeyAt(int index) {
        return keys.get(index);
    }
    public int getLastIdx() {
        return keys.size() - 1;
    }

    void insertKeyAt(int index, int key) {
        keys.add(index, key);
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
        this.parent = setParent;
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
    public void createFirstParentNode(LeafNode newNode) {
    NonLeafNode newParent = new NonLeafNode();
    PerformanceRecorder.addOneNode();
    newParent.keys = new ArrayList<Integer>();
    newParent.addChild(this);
    newParent.addChild(newNode);
    newParent.keys.add(newNode.getKey(0));
    this.setParent(newParent);
    newNode.setParent(newParent);
}

public void createRootNode(NonLeafNode newNode) {
    NonLeafNode newParent = new NonLeafNode();
    PerformanceRecorder.addOneNode();
    newParent.keys = new ArrayList<Integer>();
    newParent.addChild(this);
    newParent.addChild(newNode);
    newParent.keys.add(newNode.getKey(0));
    this.setParent(newParent);
    newNode.setParent(newParent);
}

public LeafNode leafSplitAndDistribute(int key, Address addr) {
    LeafNode newNode = new LeafNode();
    PerformanceRecorder.addOneNode();
    ((LeafNode) this).records = new ArrayList<Address>();
    ((LeafNode) this).records.add(addr);
    ((LeafNode) this).map.put(key, ((LeafNode) this).records);

    int n = NODE_SIZE - NonLeafSize + 1;
    int i = 0;
    int fromKey = 0;

    for (Map.Entry<Integer, ArrayList<Address>> entry : ((LeafNode) this).map.entrySet()) {
        if (i == n) {
            fromKey = entry.getKey();
            break;
        }
        i++;
    }

    SortedMap<Integer, ArrayList<Address>> lastnKeys = ((LeafNode) this).map.subMap(fromKey, true,
            ((LeafNode) this).map.lastKey(), true);

    newNode.map = new TreeMap<Integer, ArrayList<Address>>(lastnKeys);

    lastnKeys.clear();

    insertInOrder(this.keys, key);

    newNode.keys = new ArrayList<Integer>(this.keys.subList(n, this.keys.size()));
    this.keys.subList(n, this.keys.size()).clear();

    if (((LeafNode) this).getNext() != null) {
        newNode.setNext(((LeafNode) this).getNext());
        ((LeafNode) this).getNext().setPrevious(newNode);
    }
    ((LeafNode) this).setNext(newNode);
    newNode.setPrevious(((LeafNode) this));
    return newNode;
}

public NonLeafNode nonLeafSplitAndDistribute() {
    NonLeafNode currentParent = (NonLeafNode) (this);
    NonLeafNode newParent = new NonLeafNode();
    PerformanceRecorder.addOneNode();
    newParent.keys = new ArrayList<Integer>();

    int keyToSplitAt = currentParent.getKeyAt(NonLeafSize);
    for (int k = currentParent.getKeySize(); k > 0; k--) {
        if (currentParent.getKeyAt(k - 1) < keyToSplitAt) {
            break;
        }
        int currentKey = currentParent.getKeyAt(k - 1);
        Node currentChild = currentParent.getChild(k);

        newParent.children.add(0, currentChild);
        newParent.keys.add(0, currentKey);
        currentChild.setParent(newParent);

        currentParent.removeChild(currentParent.getChild(k));
        currentParent.keys.remove(k - 1);
    }

    return newParent;
}

public void splitLeafNode(int key, Address addr) {
    LeafNode newNode = this.leafSplitAndDistribute(key, addr);

    if (this.getParent() != null) {
        this.insertNewNodeToParent(newNode);

        if (this.getParent().getKeySize() > NODE_SIZE) {
            this.getParent().splitNonLeafNode();
        }
    } else {
        this.createFirstParentNode(newNode);
    }
}

public void splitNonLeafNode() {
    NonLeafNode newParent = this.nonLeafSplitAndDistribute();

    if (this.getParent() != null) {
        insertChildInOrder(this.getParent(), newParent);
        newParent.setParent(this.getParent());

        insertInOrder(this.getParent().keys, newParent.getKeyAt(0));
        newParent.keys.remove(0);

        if (this.getParent().getKeySize() > NODE_SIZE) {
            this.getParent().splitNonLeafNode();
        }
    } else {
        NonLeafNode newRoot = new NonLeafNode();
        PerformanceRecorder.addOneNode();
        newRoot.keys = new ArrayList<Integer>();
        newRoot.keys.add(newParent.getKeyAt(0));

        newParent.keys.remove(0);

        newRoot.addChild(this);
        newRoot.addChild(newParent);

        this.setParent(newRoot);
        newParent.setParent(newRoot);

        BpTree.setRoot(newRoot);
    }
}




}