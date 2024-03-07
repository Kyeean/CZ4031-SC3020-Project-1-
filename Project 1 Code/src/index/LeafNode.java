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
    private LeafNode rightNode;
    private LeafNode leftNode;

    public LeafNode() {
        super();
        setLeaf(true);
        setRight(null);
        setLeft(null);
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

    /* set variable rightNode as node on the right */
    public void setRight(LeafNode siblingRight) {
        rightNode = siblingRight;
    }
    /* get next node */
    public LeafNode getNext() {
        return rightNode;
    }

    /* set variable leftNode with node on the left  */
    public void setLeft(LeafNode siblingLeft) {
        leftNode = siblingLeft;
    }

    /* Get the prevNode. */
    public LeafNode getLeft() {
        return leftNode;
    }

    /* Empty keys and records in current leaf node */
    public void clear() {
        keys.clear();
        records.clear();
    }

    public void insertByRedistribution(int key, ArrayList<Address> add) {
        map.put(key, add);
    }
    /* Remove a specificed key */
    public void removeKeyInMap(int key) {
        map.remove(key);
    }
}
