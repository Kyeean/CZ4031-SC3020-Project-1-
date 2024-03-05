package index;

import java.util.ArrayList;
import java.util.TreeMap;

import storage.Address;

/**
 * Class representing a Leaf Node in a B+ tree
 */
public class LeafNode extends Node {

    protected TreeMap<Integer, ArrayList<Address>> recordMap;
    protected ArrayList<Address> records;
    private LeafNode nextNode;
    private LeafNode prevNode;

    public LeafNode() {
        super();
        setIsLeaf(true);
        setNext(null);
        setPrevious(null);
    }

    /**
     * Find records associated with the key.
     *
     * @param key the key of the records.
     * @return ArrayList<Address> the list of existing records.
     */
    public ArrayList<Address> findRecords(int key) {
        if (recordMap.containsKey(key) || this.keys.contains(key)) {
            return recordMap.get(key);
        }
        return null;
    }

    /**
     * Get Addresses for a given key.
     *
     * @param key the key of the addresses.
     * @return ArrayList<Address> the addresses.
     */
    public ArrayList<Address> getAddressesForKey(int key) {
        return recordMap.get(key);
    }

    /**
     * Add a record with the given key and address object.
     *
     * @param key the key to be added.
     * @param add the address object to be added.
     */
    public void addRecord(int key, Address add) {
        int n = Node_Size;

        if (this.keys == null) {
            initializeNodeWithRecord(key, add);
        } else if (recordMap.containsKey(key) || this.keys.contains(key)) {
            updateExistingRecord(key, add);
        } else if (this.keys.size() < n) {
            addNewRecordToNode(key, add);
        } else {
            splitLeafNode(key, add);
        }
    }

    private void initializeNodeWithRecord(int key, Address add) {
        this.records = new ArrayList<>();
        this.records.add(add);

        this.recordMap = new TreeMap<>();
        this.recordMap.put(key, records);

        this.keys = new ArrayList<>();
        insertInOrder(this.keys, key);
    }

    private void updateExistingRecord(int key, Address add) {
        ArrayList<Address> existingRecords = recordMap.get(key);
        existingRecords.add(add);
        recordMap.put(key, existingRecords);
    }

    private void addNewRecordToNode(int key, Address add) {
        this.records = new ArrayList<>();
        this.records.add(add);

        this.recordMap.put(key, records);

        insertInOrder(this.keys, key);
    }

    /**
     * Find a node with the given key in the B+ tree.
     *
     * @param key      the key to find.
     * @param rootNode the starting node.
     * @return the found node with the key.
     */
    public Node findNodeByKey(int key, Node rootNode) {
        if (rootNode == null) {
            return null;
        }
        for (Node child : ((NonLeafNode) rootNode).getChildren()) {
            Node foundNode = findNodeByKey(key, child);
            if (foundNode != null) {
                return foundNode;
            }
        }
        return null;
    }

    /**
     * Insert the given key into the keys ArrayList in correct ascending order.
     *
     * @param keys the ArrayList<Integer> keys to insert into.
     * @param key  the key to be inserted.
     */
    public static void insertInOrder(ArrayList<Integer> keys, int key) {
        int i = 0;
        while (i < keys.size() && keys.get(i) < key) {
            i++;
        }
        keys.add(i, key);
    }

    /**
     * Set the nextNode with the given sibling.
     *
     * @param sibling the leaf node that is set as the nextNode for the current node.
     */
    public void setNext(LeafNode sibling) {
        nextNode = sibling;
    }

    /**
     * Get the nextNode.
     *
     * @return nextNode, the leaf node that is on the right of the current node.
     */
    public LeafNode getNext() {
        return nextNode;
    }

    /**
     * Set the prevNode with the given prev.
     *
     * @param prev the leaf node that is set as the prevNode for the current Node.
     */
    public void setPrevious(LeafNode prev) {
        prevNode = prev;
    }

    /**
     * Get the prevNode.
     *
     * @return prevNode, the leaf node that is on the left of the current node.
     */
    public LeafNode getPrevious() {
        return prevNode;
    }

    /**
     * Clear the keys ArrayList and records ArrayList of the current leaf node.
     */
    public void clear() {
        keys.clear();
        records.clear();
    }

    /**
     * Insert the given key and addresses into the recordMap.
     *
     * @param key the key to be inserted.
     * @param add the addresses to be inserted into the recordMap.
     */
    public void insertByRedistribution(int key, ArrayList<Address> add) {
        recordMap.put(key, add);
    }

    /**
     * Remove the given key from the recordMap.
     *
     * @param key the key to be removed.
     */
    public void removeKeyInMap(int key) {
        recordMap.remove(key);
    }

    /**
     * Get a formatted string representation of the recordMap, records ArrayList, and the nextNode.
     *
     * @return A formatted string representation.
     */
    @Override
    public String toString() {
        return String.format("\n--------LEAF NODE CONTAINS: map %s records %s, nextNode ------------\n",
                recordMap.toString(), records, nextNode);
    }
}
