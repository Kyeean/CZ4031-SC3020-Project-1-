package index;

import java.util.ArrayList;
import java.util.TreeMap;
import storage.Address;

/**
 * Class representing a Leaf Node in a B+ tree
 */
public class LeafNode extends Node {

    protected TreeMap<Integer, ArrayList<Address>> map;
    protected ArrayList<Address> records;
    private LeafNode nextNode;
    private LeafNode prevNode;

    public LeafNode() {
        super();
        setLeaf(true);
        setNext(null);
        setPrevious(null);
    }

    /* Find records associated with the key. */
    public ArrayList<Address> findRecords(int key) {
        if (map.containsKey(key) || this.keys.contains(key)) {
            return map.get(key);
        }
        return null;
    }

    /* Get Addresses for a given key. */
    public ArrayList<Address> getAddressesForKey(int key) {
        return map.get(key);
    }

    /* Add a record with the given key and address object. */
    public void addRecord(int key, Address add) {
        int n = NODE_SIZE;

        if (this.keys == null) {
            initializeNodeWithRecord(key, add);
        } else if (map.containsKey(key) || this.keys.contains(key)) {
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

        this.map = new TreeMap<>();
        this.map.put(key, records);

        this.keys = new ArrayList<>();
        insertInOrder(this.keys, key);
    }

    private void updateExistingRecord(int key, Address add) {
        ArrayList<Address> existingRecords = map.get(key);
        existingRecords.add(add);
        map.put(key, existingRecords);
    }

    private void addNewRecordToNode(int key, Address add) {
        this.records = new ArrayList<>();
        this.records.add(add);

        this.map.put(key, records);

        insertInOrder(this.keys, key);
    }

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

    /* Insert key into arraylist in ascending order */
    public static void insertInOrder(ArrayList<Integer> keys, int key) {
        int i = 0;
        while (i < keys.size() && keys.get(i) < key) {
            i++;
        }
        keys.add(i, key);
    }

    /* set next node as sibling node */
    public void setNext(LeafNode sibling) {
        nextNode = sibling;
    }
    /* get next node */
    public LeafNode getNext() {
        return nextNode;
    }

    /* set prevNode with previous node */
    public void setPrevious(LeafNode prev) {
        prevNode = prev;
    }

    /* Get the prevNode. */
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
        map.put(key, add);
    }

    /**
     * Remove the given key from the recordMap.
     *
     * @param key the key to be removed.
     */
    public void removeKeyInMap(int key) {
        map.remove(key);
    }

    /**
     * Get a formatted string representation of the recordMap, records ArrayList, and the nextNode.
     *
     * @return A formatted string representation.
     */
    @Override
    public String toString() {
        return String.format("\n--------LEAF NODE CONTAINS: map %s records %s, nextNode ------------\n",
                map.toString(), records, nextNode);
    }
}
