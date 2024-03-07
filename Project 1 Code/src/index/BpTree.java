package index;

import java.util.ArrayList;
import java.util.List;
import storage.Address;
import storage.Record;
import storage.Disk;
import util.Parser;

/* BPlus Tree Class */
public class BpTree {

    static final int NODE_SIZE = (Parser.BLOCK_SIZE - Parser.OVERHEAD)/(Parser.POINTER_SIZE+Parser.KEY_SIZE);
    static Node rootNode;
    Node insertNode;
    // Initial Node creation
    public BpTree() {
        rootNode = InitialNode();
    }
    // First Node in the Bp Tree
    public LeafNode InitialNode() {
        LeafNode firstNode = new LeafNode();
        PerformanceRecorder.addOneNode();
        firstNode.setRoot(true);
        firstNode.setLeaf(true);
        setRoot(firstNode);
        return firstNode;
    }

    // Creation of Nodes
    public static Node createNode() {
        Node newNode = new Node();
        PerformanceRecorder.addOneNode();
        return newNode;
    }

    /* Update node to root */
    public static void setRoot(Node root) {
        rootNode = root;
        rootNode.setRoot(true);
    }

    /* Return root node of Bp Tree */ 
    public static Node getRoot() {
        return rootNode;
    }
    /* Function to search for a Node */
    public LeafNode searchNode(int key) {
        ArrayList<Integer> keys;
        if (BpTree.rootNode.isLeaf()) {
            setRoot(rootNode);
            return (LeafNode) rootNode;
        }
        else {
            Node insertNode = (NonLeafNode) getRoot();
            while (!((NonLeafNode) insertNode).getChild(0).isLeaf()) {
                keys = insertNode.getKeys();
                for (int i = keys.size() - 1; i >= 0; i--) {
                    if (insertNode.getKey(i) <= key) {
                        insertNode = ((NonLeafNode) insertNode).getChild(i + 1);
                        break;
                    }
                    else if (i == 0) {
                        insertNode = ((NonLeafNode) insertNode).getChild(0);
                    }
                }

                if (insertNode.isLeaf()) {
                    break;
                }

            }

            keys = insertNode.getKeys();
            // loop to find which index to insert key
            for (int i = keys.size() - 1; i >= 0; i--) {
                if (keys.get(i) <= key) {
                    return (LeafNode) ((NonLeafNode) insertNode).getChild(i + 1);
                }
            }
            return (LeafNode) ((NonLeafNode) insertNode).getChild(0);
        }

    }
    /* Function to insert key */
    public void insertKey(int key, Address add) {
        // have to first search for the LeafNode to insert to, then add a record add
        // that LeafNode
        insertNode = searchNode(key);

        ((LeafNode) insertNode).addRecord(key, add);
    }
    /* Lower Bound check */
    private int findLowerBound(int key) {
        NonLeafNode node = (NonLeafNode) rootNode;
        boolean found = false;
        int lowerbound = 0;

        // loop from back to front to find the first key that is smaller than the key
        for (int i = node.getKeySize() - 1; i >= 0; i--) {
            if (key >= node.getKeyAt(i)) {
                node = (NonLeafNode) node.getChild(i + 1);
                found = true;
                break;
            }
        }
        if (found == false && key < node.getKeyAt(0)) {
            node = (NonLeafNode) node.getChild(0);
        }

        // loop till get leftmost key
        while (!node.getChild(0).isLeaf()) {
            node = (NonLeafNode) node.getChild(0);
        }

        lowerbound = node.getChild(0).getKeyAt(0);
        return (lowerbound);

    }


    /* Deletion of Key & find lower bound using findLowerBound */
    public ArrayList<Address> deleteKey(int key) {
        int lowerbound = 0;
        lowerbound = findLowerBound(key);
        return (deleteNode(rootNode, null, -1, -1, key, lowerbound));
    }

 
    /* Function to delete Node with a key value */
    public ArrayList<Address> deleteNode(Node node, NonLeafNode parent, int parentPointerIndex, int parentKeyIndex,
            int key, int lowerbound) {
        ArrayList<Address> addOfRecToDelete = new ArrayList<>();

        if (node.isLeaf()) {
            // search for the key to delete
            LeafNode leaf = (LeafNode) node;
            int keyIdx = node.searchKey(key, false);
            if ((keyIdx == leaf.getKeySize()) || (key != leaf.getKeyAt(keyIdx))) {
                return null;
            }

            // found keys to delete: 1) remove key in map 2) remove idx in records
            addOfRecToDelete.addAll(leaf.getAddressesForKey(key));
            leaf.removeKeyAt(keyIdx);
            leaf.removeKeyInMap(key);

            // Update Key after deleting
            int ptrIdx = node.searchKey(key, true);
            keyIdx = ptrIdx - 1;

            LeafNode LeafNode = (LeafNode) node;
            int newLowerBound = 0;

            // Get newLowerBound (possible for current key taken to be the lowerbound) if
            // KeyIdx is not KeySize
            if (LeafNode.getKeySize() >= (keyIdx + 1)) {
                newLowerBound = lowerbound;
                List<Integer> keys = LeafNode.getKeys();
                LeafNode.updateKey(ptrIdx - 1, keys.get(0), false, newLowerBound);

            } else {
                newLowerBound = findLowerBound(LeafNode.getKey(keyIdx + 1)); // Get new lowerbound
                List<Integer> keys = LeafNode.getKeys();
                LeafNode.updateKey(ptrIdx - 1, keys.get(0), true, newLowerBound);
            }

        } else {
            // traverse to leaf node to find records to delete
            NonLeafNode nonLeafNode = (NonLeafNode) node;
            int ptrIdx = node.searchKey(key, true);
            int keyIdx = ptrIdx - 1;

            // read the next level node (read action will be recorded in the next level)
            Node next = nonLeafNode.getChild(ptrIdx);
            addOfRecToDelete = deleteNode(next, nonLeafNode, ptrIdx, keyIdx, key, lowerbound);

        }

        // carry out re-balancing tree magic if needed
        if (node.isUnderUtilized(NODE_SIZE)) {
            handleInvalidTree(node, parent, parentPointerIndex, parentKeyIndex);
        }

        return addOfRecToDelete;
    }

    /* Function to rebalance node  */
    private void handleInvalidTree(Node underUtilizedNode, NonLeafNode parent, int parentPointerIndex,
            int parentKeyIndex) throws IllegalStateException {
        if (parent == null) {
            handleInvalidRoot(underUtilizedNode);
        } else if (underUtilizedNode.isLeaf()) {
            handleInvalidLeaf(underUtilizedNode, parent,
                    parentPointerIndex, parentKeyIndex);
        } else if (underUtilizedNode.isNonLeaf()) {
            handleInvalidInternal(underUtilizedNode, parent,
                    parentPointerIndex, parentKeyIndex);
        } else {
            throw new IllegalStateException("state is wrong!");
        }
    }

    /* Function to handle Nodes which are not balanced */
    public void handleInvalidRoot(Node underUtilizedNode) {
        if (underUtilizedNode.isLeaf()) { 
            ((LeafNode) underUtilizedNode).clear();
        } else {
            NonLeafNode nonLeafRoot = (NonLeafNode) underUtilizedNode;
            Node newRoot = nonLeafRoot.getChild(0);
            newRoot.setParent(null);
            rootNode = newRoot;
        }
    }

    /* Function to handle unbalanced Leaf Nodes  */
    private void handleInvalidLeaf(Node underUtilizedNode,
            NonLeafNode parent,
            int parentPointerIndex,
            int parentKeyIndex) throws IllegalStateException {
        int numChildrenOfNextParent = 0;
        int numChildrenOfNodeParent = 0;
        LeafNode nextNode;
        nextNode = null;
        // load the neighbors
        LeafNode underUtilizedLeaf = (LeafNode) underUtilizedNode;
        if (underUtilizedLeaf.getNext() != null) {
            nextNode = (LeafNode) underUtilizedLeaf.getNext();
            if (nextNode.getParent() != null) {
                numChildrenOfNextParent = nextNode.getParent().getChildren().size();
            }
        }

        LeafNode prevNode = (LeafNode) underUtilizedLeaf.getPrevious();

        if (underUtilizedNode.getParent() != null) {
            numChildrenOfNodeParent = underUtilizedNode.getParent().getChildren().size();
        }
        if (nextNode != null && nextNode.isAbleToGiveOneKey(NODE_SIZE)) {
            moveOneKey(nextNode, underUtilizedLeaf, false, parent, parentKeyIndex + 1);
        } else if (prevNode != null && prevNode.isAbleToGiveOneKey(NODE_SIZE)) {
            moveOneKey(prevNode, underUtilizedLeaf, true, parent, parentKeyIndex);
        }
        else if ((prevNode != null && (prevNode.getKeySize() + underUtilizedLeaf.getKeySize()) <= NODE_SIZE
                && (numChildrenOfNodeParent >= underUtilizedNode.getParent().getLeafNodeSize()))) {
            mergeLeafNodes(prevNode, underUtilizedLeaf, parent, parentPointerIndex, parentKeyIndex, false);
        } else if (nextNode != null && (nextNode.getKeySize() + underUtilizedLeaf.getKeySize()) <= NODE_SIZE
                && (numChildrenOfNextParent >= underUtilizedNode.getParent().getLeafNodeSize())) {
            mergeLeafNodes(underUtilizedLeaf, nextNode, parent, parentPointerIndex + 1, parentKeyIndex + 1, true);
        } else {
            throw new IllegalStateException("Can't have both leaf " +
                    "pointers null and not be root or no " +
                    "common parent");
        }
    }
    /* Handle internal node redistribution or merging */
    private void handleInvalidInternal(Node underUtilizedNode,
            NonLeafNode parent,
            int parentPointerIndex,
            int parentKeyIndex) throws IllegalStateException {

        Node underUtilizedInternal = underUtilizedNode;
        NonLeafNode prevInternal = null;
        NonLeafNode nextInternal = null;
        try {
            nextInternal = (NonLeafNode) parent.getChild(parentPointerIndex + 1);
        } catch (Exception e) {
            System.out.print(e);
        }

        try {
            prevInternal = (NonLeafNode) parent.getChild(parentPointerIndex - 1);
        } catch (Exception e) {
            System.out.print(e);
        }

        if (nextInternal == null && prevInternal == null)
            throw new IllegalStateException("Both prevInternal and nextInternal is null for " + underUtilizedNode);

        if (prevInternal != null && prevInternal.isAbleToGiveOneKey(NODE_SIZE)) {
            moveOneKeyNonLeafNode(prevInternal, (NonLeafNode) underUtilizedInternal, true, parent, parentKeyIndex);

        } else if (nextInternal != null && nextInternal.isAbleToGiveOneKey(NODE_SIZE)) {
            moveOneKeyNonLeafNode(nextInternal, (NonLeafNode) underUtilizedInternal, false, parent, parentKeyIndex + 1);

        }
        /* Left node merge */
        else if (prevInternal != null &&
                (underUtilizedInternal.getKeySize() + prevInternal.getKeySize()) <= NODE_SIZE) {
            mergeNonLeafNodes(prevInternal, (NonLeafNode) underUtilizedInternal, parent,
                    parentPointerIndex, parentKeyIndex, true);
        }
        /* Right node merge */
        else if (nextInternal != null &&
                (underUtilizedInternal.getKeySize() + nextInternal.getKeySize()) <= NODE_SIZE) {
            mergeNonLeafNodes((NonLeafNode) underUtilizedInternal, nextInternal, parent,
                    parentPointerIndex + 1, parentKeyIndex + 1, false);
        } else {
            throw new IllegalStateException("Can't merge or redistribute internal node " + underUtilizedInternal);
        }
    }
    
    /* Function to handle rebalancing a non-leaf node */
    private void moveOneKeyNonLeafNode(NonLeafNode giver, NonLeafNode receiver, boolean giverOnLeft, NonLeafNode parent, int inBetweenKeyIdx) {
            int key;
            if (giverOnLeft) {
            giver.removeKeyAt(giver.getKeySize() - 1);
            Node nodeToMove = giver.getChild(giver.getKeySize()); // get last child of giver
            giver.removeChild(nodeToMove);
            receiver.addChild(nodeToMove);
            receiver.getKeys().add(receiver.getKeySize(), receiver.getChild(1).getFirstKey());

            key = receiver.getKeyAt(0);
        } else {

            giver.removeKeyAt(0);

            Node nodeToMove = giver.getChild(0); // get first child of giver
            giver.removeChild(nodeToMove);
            receiver.addChild(nodeToMove);

            receiver.getKeys().add(receiver.getKeySize(), receiver.getChild(1).getFirstKey());

            key = receiver.getKeyAt(0);
        }
    
        int ptrIdx = receiver.searchKey(key, true);
        int keyIdx = ptrIdx - 1;

        NonLeafNode LeafNode = (NonLeafNode) receiver;
        int lowerbound = findLowerBound(key);

        int newLowerBound = 0;

        if (LeafNode.getKeySize() >= (keyIdx + 1)) {
            newLowerBound = lowerbound;
        } else {
            newLowerBound = findLowerBound(LeafNode.getKey(keyIdx + 1));
            parent.updateKey(inBetweenKeyIdx - 1, key, false, findLowerBound(key));

        }
        parent.replaceKeyAt(inBetweenKeyIdx, newLowerBound);

    }
    /* Merge non leaf node with adjacent nodes */
    
    private void mergeNonLeafNodes(NonLeafNode nodeToMergeTo, NonLeafNode current, NonLeafNode parent,
            int rightPointerIdx,
            int inBetweenKeyIdx, boolean mergeWithLeft) {
        int keyToRemove;

        if (mergeWithLeft) {

            int moveKeyCount = current.getKeySize();
            keyToRemove = nodeToMergeTo.getChild(nodeToMergeTo.getKeySize()).getLastKey();

            for (int i = 0; i < moveKeyCount; i++) {
                nodeToMergeTo.getKeys().add(nodeToMergeTo.getKeySize(), current.getKeyAt(i));
            }

            for (int i = 0; i < current.getChildren().size(); i++) {
                nodeToMergeTo.getChildren().add(current.getChild(i));
            }

            nodeToMergeTo.getKeys().add(nodeToMergeTo.getKeySize(),
                    nodeToMergeTo.getChild(nodeToMergeTo.getKeySize() + 1).getFirstKey());
            current.getParent().removeChild(current);

        }

        else {
            int moveKeyCount = current.getKeySize();

            keyToRemove = current.getFirstKey();

            for (int i = 0; i < moveKeyCount; i++) {
                nodeToMergeTo.getKeys().add(0, current.getKeyAt(i));
            }
            for (int i = 0; i < current.getChildren().size(); i++) {
                nodeToMergeTo.getChildren().add(current.getChild(i));
            }
            nodeToMergeTo.getKeys().add(0, nodeToMergeTo.getChild(1).getFirstKey());
            current.getParent().removeChild(current);

        }

        int ptrIdx = nodeToMergeTo.searchKey(keyToRemove, true);
        int keyIdx = ptrIdx - 1;

        NonLeafNode LeafNode = (NonLeafNode) nodeToMergeTo;
        int lowerbound = findLowerBound(keyToRemove);
        int newLowerBound = 0;


        if (LeafNode.getKeySize() >= (keyIdx + 1)) {
            newLowerBound = lowerbound;
        } else {
            newLowerBound = findLowerBound(LeafNode.getKey(keyIdx + 1)); // Get new lowerbound
            parent.updateKey(inBetweenKeyIdx - 1, keyToRemove, false, findLowerBound(keyToRemove));

        }
    }


    /* Merge leaf node with adjacent leaf node */
    private void mergeLeafNodes(LeafNode nodeToMergeTo, LeafNode current, NonLeafNode parent,
            int rightPointerIdx, int inBetweenKeyIdx, boolean mergetoright) {
        int removedKey = 0;
        int moveKeyCount = current.getKeySize();
        int NoOfChildren = current.getParent().getChildren().size();
        for (int i = 0; i < moveKeyCount; i++) {
            removedKey = current.removeKeyAt(0);
            int leftLastIdx = nodeToMergeTo.getLastIdx();
            nodeToMergeTo.insertKeyAt(leftLastIdx + 1, removedKey);
            // 2. Move over the records
            nodeToMergeTo.insertByRedistribution(removedKey, current.getAddressesForKey(removedKey));
            current.removeKeyInMap(removedKey);

        }

        parent.removeChild(current); 
        if ((parent.getChildren().size()) == (parent.getKeySize())) {
        } else {
            parent.removeKeyAt(inBetweenKeyIdx);
        }

        if (mergetoright == true) {
            if (current.getNext() != null) {
                LeafNode currentNext = current.getNext();
                currentNext.setPrevious(current.getPrevious());
            }

            nodeToMergeTo.setNext(current.getNext());
            if (current.getKeySize() == 0) {

                NonLeafNode currParent = current.getParent();
                currParent.removeChild(current);
                currParent.removeKeyAt(0);
            }
        } else {

            if (current.getPrevious() != null) {
                LeafNode currentPrev = current.getPrevious();
                if (currentPrev != null && (currentPrev.getPrevious() != null)) {
                    currentPrev.getPrevious().setPrevious(current.getPrevious());
                }

            }

            if (current.getNext() != null) {
                nodeToMergeTo.setNext(current.getNext());
                current.getNext().setPrevious(nodeToMergeTo);
            }
            if (current.getKeySize() == 0) {

                NonLeafNode currParent = current.getParent();
                currParent.removeChild(current);
                // if (currParent.getKeySize() > 0){
                if (inBetweenKeyIdx < 0) {
                    currParent.removeKeyAt(inBetweenKeyIdx + 1);

                } else if (currParent.getKeySize() > 0) {

                    currParent.removeKeyAt(inBetweenKeyIdx);
                } else {
                    currParent.removeKeyAt(0);
                }

            } else {

                NonLeafNode currParent = current.getNext().getParent();
                currParent.removeChild(current);
                // Check if parent key satisfy min node size
                if ((currParent.getKeySize() > currParent.getLeafNodeSize())
                        && (currParent.getChildren().size() > current.getLeafNodeSize())) {
                    currParent.removeKeyAt(0);

                }
            }
        }

        int lowerbound = findLowerBound(removedKey);
        int newLowerBound = 0;
        
        if (current.getParent().getKeySize() >= NoOfChildren) { // check if number of children == original # of children
            newLowerBound = lowerbound;
        } else {
            newLowerBound = current.getParent().getChild(0).getFirstKey();

            if (inBetweenKeyIdx == 0) {
                // inBetweenKeyIdx is 0
            } else {
                current.getParent().updateKey(inBetweenKeyIdx - 1, newLowerBound, true, newLowerBound);
            }
        }

    }


    private void moveOneKey(LeafNode giver, LeafNode receiver,
            boolean giverOnLeft, NonLeafNode parent,
            int inBetweenKeyIdx) {
        int key;
        if (giverOnLeft) {
        
            int giverKey = giver.getLastKey();
            receiver.insertByRedistribution(giverKey, giver.getAddressesForKey(giverKey));
            giver.removeKeyInMap(giverKey);

            receiver.insertKeyAt(0, giverKey);
            giver.removeKeyAtLast();
            key = receiver.getKeyAt(0);
        } else {
        
            int giverKey = giver.getFirstKey();
            receiver.insertByRedistribution(giverKey, giver.getAddressesForKey(giverKey));
            giver.removeKeyInMap(giverKey);

            giver.removeKeyAt(0);
            receiver.insertKeyAt(receiver.getKeySize(), giverKey);
            key = giver.getKeyAt(0);

        }

        if (inBetweenKeyIdx == -1) {
        } else if (inBetweenKeyIdx >= 0) {
            if (parent.getKeySize() == inBetweenKeyIdx) {
                parent.replaceKeyAt(inBetweenKeyIdx - 1, key);

                int lastParentChild = receiver.getParent().getKeys().size() - 1;// point to last child
                int lastParentChildKey = receiver.getParent().getChild(receiver.getParent().getKeys().size())
                        .getFirstKey();
                if (giver.getParent().getChild(giver.getParent().getChildren().size() - 1).getFirstKey() != key) {
                    receiver.getParent().replaceKeyAt(lastParentChild, lastParentChildKey);
                }
            } else {
                parent.replaceKeyAt(inBetweenKeyIdx, key);

                if (giver.getParent().getChild(inBetweenKeyIdx + 1).getFirstKey() != key) {
                    giver.getParent().replaceKeyAt(inBetweenKeyIdx,
                            giver.getParent().getChild(inBetweenKeyIdx + 1).getFirstKey());
                }
            }

        } else {
            parent.replaceKeyAt(inBetweenKeyIdx - 1, key);
        }

        int ptrIdx = receiver.searchKey(key, true);
        int keyIdx = ptrIdx - 1;

        LeafNode LeafNode = (LeafNode) receiver;
        int lowerbound = findLowerBound(key);
        int newLowerBound = 0;

        
        if (LeafNode.getKeySize() >= (keyIdx + 1)) {
            newLowerBound = lowerbound;
        } else {
            newLowerBound = findLowerBound(LeafNode.getKey(keyIdx + 1)); // Get new lowerbound
            parent.updateKey(inBetweenKeyIdx - 1, parent.getChild(inBetweenKeyIdx).getFirstKey(), false,
            findLowerBound(key));

        }

    }

    public ArrayList<Address> searchKey(int key) {
        return (searchValue(this.rootNode, key));
    }


    /* Function to search a value with a given key */
    public ArrayList<Address> searchValue(Node node, int key) {
        PerformanceRecorder.addOneNodeReads();

        if (node.isLeaf()) {
            int ptrIdx = node.searchKey(key, false);
            if (ptrIdx >= 0 && ptrIdx < node.getKeySize() && key == node.getKeyAt(ptrIdx)) {
                return ((LeafNode) node).getAddressesForKey(key); // returns an ArrayList of addresses
            }
            return null;
        }
        else {
            int ptrIdx = node.searchKey(key, false); // looks for the upper bound of the key in the node
            Node childNode = ((NonLeafNode) node).getChild(ptrIdx);
            return (searchValue(childNode, key));
        }
    }

    /* Function to Search in a range */
    public ArrayList<Address> rangeSearch(int minKey, int maxKey) {
        return searchValuesInRange(minKey, maxKey, this.rootNode);
    }

    /* Search for values in a given range */
    public static ArrayList<Address> searchValuesInRange(int minKey, int maxKey, Node node) {
        int ptrIdx;
        ArrayList<Address> resultList = new ArrayList<>();
        PerformanceRecorder.addOneRangeNodeReads();
        if (node.isLeaf()) {
            ptrIdx = node.searchKey(minKey, false); // if minKey is in key array, get key index
            LeafNode leaf = (LeafNode) node;
            while (true) {
                if (ptrIdx == leaf.getKeySize()) {
                    if (leaf.getNext() == null)
                        break; 
                    leaf = (LeafNode) (leaf.getNext());
                    PerformanceRecorder.addOneRangeNodeReads();

                    ptrIdx = 0;
                    if (ptrIdx >= leaf.getKeySize())
                        throw new IllegalStateException("Range search found a node with 0 keys");
                }
                if (leaf.getKey(ptrIdx) > maxKey)
                    break;
                int key = leaf.getKey(ptrIdx);
                resultList.addAll(leaf.getAddressesForKey(key));
                ptrIdx++;
            }
            return (resultList.size() > 0 ? resultList : null);
        } else {
            ptrIdx = node.searchKey(minKey, true);
            // Descend into leaf node
            Node childNode = ((NonLeafNode) node).getChild(ptrIdx);
            return (searchValuesInRange(minKey, maxKey, childNode));
        }
    }

    /*
     * Count the number of levels in the Bp Tree
     */
    private void countLevel(Node node) {
        while (!node.isLeaf()) {
            NonLeafNode nonLeaf = (NonLeafNode) node;
            node = nonLeaf.getChild(0);
            PerformanceRecorder.addOneTreeDegree();
        }
        PerformanceRecorder.addOneTreeDegree();
    }

    // Experiment 2
    public static void experimentTwo(BpTree tree) {
        System.out.println("\nEXPERIMENT 2: ");
        PerformanceRecorder performance = new PerformanceRecorder();
        System.out.println("Parameter n of B+ Tree: " + NODE_SIZE);
        System.out.printf("Number of nodes in B+ tree: %d\n", performance.getTotalNodes());
        tree.countLevel(tree.getRoot());
        System.out.printf("Number of levels in B+ tree: %d\n", performance.getTreeDegree());
        System.out.println("Content of the root node: " + BpTree.getRoot().keys);
    }
    // Experiment 3
    public static void experimentThree(Disk db, BpTree tree) {
        System.out.println("\nEXPERIMENT 3: ");
        PerformanceRecorder performance = new PerformanceRecorder();
        System.out.println("Number of records with 'numVotes' = 500: ");

        long startTime = System.nanoTime();
        ArrayList<Address> resultAdd = tree.searchKey(500);
        long endTime = System.nanoTime();
        double totalAverageRating = 0;
        int totalCount = 0;
        ArrayList<Record> results = new ArrayList<>();
        if (resultAdd != null) {
            for (Address add : resultAdd) {
                Record record = db.getRecord(add);
                System.out.print("\n" + record);
                results.add(record);
                totalAverageRating += record.getAvgRating();
                totalCount++;
            }
        }
        System.out.printf("\n\nNumber of Index Nodes accessed: %d\n", performance.getNodeReads());
        System.out.printf("Number of Data Blocks accessed: %d\n", db.getBlockAccesses());
        System.out.printf("Average of 'averageRating's' of the records accessed: %.2f\n",
                (double) totalAverageRating / totalCount);
        long duration = (endTime - startTime); // divide by 1000000 to get milliseconds.
        System.out.printf("Time taken searching using B+ Tree: %d nanoseconds\n", duration);
        startTime = System.nanoTime();
        int bruteForceAccessCount = db.getBlocksAccessedByForce(500, 500);
        endTime = System.nanoTime();
        System.out.printf("Number of Data Blocks Accessed by Brute Force (numVotes = 500): %d", bruteForceAccessCount);
        System.out.printf("\nLinear Time Accessed by Brute Force (numVotes = 500): %d", endTime - startTime);
        /*System.out.printf("\nNo. of Data Blocks accessed reduced in total: %d\n ", db.getBlockAccessReduced()); */
    }

    // Experiment 4
    public static void experimentFour(Disk db, BpTree tree) {
        System.out.println("\n\nEXPERIMENT 4: ");
        PerformanceRecorder performance = new PerformanceRecorder();
        System.out.println("Movies with the 'numVotes' from 30,000 to 40,000: ");
        long startTime = System.nanoTime();
        ArrayList<Address> resultAdd = tree.rangeSearch(30000, 40000);
        long endTime = System.nanoTime();
        double totalAverageRating = 0;
        int totalCount = 0;
        ArrayList<Record> results = new ArrayList<>();
        if (resultAdd != null) {
            for (Address add : resultAdd) {
                Record record = db.getRecord(add);
                System.out.print("\n From Indexing" + record);
                results.add(record);
                totalAverageRating += record.getAvgRating();
                totalCount++;
            }
        }
        System.out.printf("\n\nNumber of Index Nodes accessed: %d\n", performance.getRangeNodeReads());
        System.out.printf("Number of Data Blocks the process accesses: %d\n", db.getBlockAccesses());
        System.out.printf("Average of 'averageRating's' of the records accessed: %.2f",
                (double) totalAverageRating / totalCount);
        long duration = (endTime - startTime); // divide by 1000000 to get milliseconds.
        System.out.printf("\nTime taken searching using B+ Tree: %d nanoseconds\n", duration);
        startTime = System.nanoTime();
        int bruteForceAccessCount = db.getBlocksAccessedByForce(30000, 40000);
        endTime = System.nanoTime();
        System.out.printf("Number of Data Blocks Accessed by Brute Force (30000<=numVotes<=40000): %d",
                bruteForceAccessCount);
        System.out.printf("\nLinear Time Accessed by Brute Force (30000<=numVotes<=40000): %d", endTime - startTime);
        /*System.out.printf("\nNo. of Data Blocks accessed reduced in total: %d\n ", db.getBlockAccessReduced()); */
    }

    // Experiment 5
    public static void experimentFive(Disk db, BpTree tree) {
        System.out.println("\n\nEXPERIMENT 5: ");
        PerformanceRecorder performance = new PerformanceRecorder();
        System.out.println("-- Deleting all records with 'numVotes' of 1000 -- ");
        long startTime = System.nanoTime();
        ArrayList<Address> deletedAdd = tree.deleteKey(1000);

        db.deleteRecord(deletedAdd);
        long endTime = System.nanoTime();
        System.out.printf("Number of Nodes in updated B+ tree: %d\n", performance.getTotalNodes());
        tree.countLevel(tree.getRoot());
        System.out.printf("Number of Levels in updated B+ tree: %d\n", performance.getTreeDegree());
        System.out.printf("\nContent of the root node in updated B+ tree: %s\n", BpTree.getRoot().keys);
        long duration = (endTime - startTime); // divide by 1000000 to get milliseconds.
        System.out.printf("Running time of retrieval process: %d nanoseconds\n", duration);
        System.out.println("Number of Data Blocks Accessed by Brute Force (numVotes=1000):");
        startTime = System.nanoTime();
        int bruteForceAccessCount = db.getBlocksAccessedByForce(1000, 1000);
        endTime = System.nanoTime();
        System.out.printf("Number of Data Blocks Accessed by Brute Force (numVotes = 1000): %d", bruteForceAccessCount);
        System.out.printf("\nLinear Time Accessed by Brute Force (numVotes = 1000): %d", endTime - startTime);
       /*  System.out.printf("\nNo. of Data Blocks accessed reduced in total: %d\n ", db.getBlockAccessReduced());*/
    }

}