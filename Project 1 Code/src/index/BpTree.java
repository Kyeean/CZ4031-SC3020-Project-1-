package index;

import java.util.ArrayList;
import java.util.List;

import storage.Address;
import storage.Disk;
import storage.Record;
//import utils.Parser;

public class BpTree {

    static final int NODE_SIZE = (Parser.BLOCK_SIZE - Parser.OVERHEAD) / (Parser.POINTER_SIZE + Parser.KEY_SIZE);
    private static Node rootNode;
    private Node nodeToInsertTo;

    public BpTree() {
        rootNode = createFirstNode();
    }

    public LeafNode createFirstNode() {
        LeafNode newNode = new LeafNode();
        PerformanceRecorder.addOneNode();
        newNode.setRoot(true);
        newNode.setLeaf(true);
        setRoot(newNode);
        return newNode;
    }

    public static Node createNode() {
        Node newNode = new Node();
        PerformanceRecorder.addOneNode();
        return newNode;
    }

    public static void setRoot(Node root) {
        rootNode = root;
        rootNode.setRoot(true);
    }

    public static Node getRoot() {
        return rootNode;
    }

    public void insertKey(int key, Address add) {
        nodeToInsertTo = searchNode(key);
        ((LeafNode) nodeToInsertTo).addRecord(key, add);
    }

    public LeafNode searchNode(int key) {
        if (rootNode.isLeaf()) {
            setRoot(rootNode);
            return (LeafNode) rootNode;
        } else {
            Node nodeToInsertTo = (NonLeafNode) getRoot();
            while (!((NonLeafNode) nodeToInsertTo).getChild(0).isLeaf()) {
                ArrayList<Integer> keys = nodeToInsertTo.getKeys();
                for (int i = keys.size() - 1; i >= 0; i--) {
                    if (nodeToInsertTo.getKey(i) <= key) {
                        nodeToInsertTo = ((NonLeafNode) nodeToInsertTo).getChild(i + 1);
                        break;
                    } else if (i == 0) {
                        nodeToInsertTo = ((NonLeafNode) nodeToInsertTo).getChild(0);
                    }
                }
                if (nodeToInsertTo.isLeaf()) {
                    break;
                }
            }
            ArrayList<Integer> keys = nodeToInsertTo.getKeys();
            for (int i = keys.size() - 1; i >= 0; i--) {
                if (keys.get(i) <= key) {
                    return (LeafNode) ((NonLeafNode) nodeToInsertTo).getChild(i + 1);
                }
            }
            return (LeafNode) ((NonLeafNode) nodeToInsertTo).getChild(0);
        }
    }

    private int checkForLowerbound(int key) {
        NonLeafNode node = (NonLeafNode) rootNode;
        boolean found = false;
        int lowerbound = 0;
        for (int i = node.getKeySize() - 1; i >= 0; i--) {
            if (key >= node.getKeyAt(i)) {
                node = (NonLeafNode) node.getChild(i + 1);
                found = true;
                break;
            }
        }
        if (!found && key < node.getKeyAt(0)) {
            node = (NonLeafNode) node.getChild(0);
        }
        while (!node.getChild(0).isLeaf()) {
            node = (NonLeafNode) node.getChild(0);
        }
        lowerbound = node.getChild(0).getKeyAt(0);
        return lowerbound;
    }


public ArrayList<Address> deleteKey(int key) {
    int lowerbound = 0;
    lowerbound = checkForLowerbound(key);
    return deleteNode(rootNode, null, -1, -1, key, lowerbound);
}


public ArrayList<Address> deleteNode(Node node, NonLeafNode parent, int parentPointerIndex, int parentKeyIndex,
                                      int key, int lowerbound) {
    ArrayList<Address> addOfRecToDelete = new ArrayList<>();

    if (node.isLeaf()) {
        LeafNode leaf = (LeafNode) node;
        int keyIdx = node.searchKey(key, false);
        if ((keyIdx == leaf.getKeySize()) || (key != leaf.getKeyAt(keyIdx))) {
            return addOfRecToDelete; // Key not found
        }

        addOfRecToDelete.addAll(leaf.getAddressesForKey(key));
        leaf.removeKeyAt(keyIdx);
        leaf.removeKeyInMap(key);
        int ptrIdx = node.searchKey(key, true);
        keyIdx = ptrIdx - 1;

        LeafNode leafNode = (LeafNode) node;
        int newLowerBound = 0;

        if (leafNode.getKeySize() >= (keyIdx + 1)) {
            newLowerBound = lowerbound;
            List<Integer> keys = leafNode.getKeys();
            leafNode.updateKey(ptrIdx - 1, keys.get(0), false, newLowerBound);
        } else {
            newLowerBound = checkForLowerbound(leafNode.getKey(keyIdx + 1)); // Get new lowerbound
            List<Integer> keys = leafNode.getKeys();
            leafNode.updateKey(ptrIdx - 1, keys.get(0), true, newLowerBound);
        }
    } else {
        NonLeafNode nonLeafNode = (NonLeafNode) node;
        int ptrIdx = node.searchKey(key, true);
        int keyIdx = ptrIdx - 1;
        Node next = nonLeafNode.getChild(ptrIdx);
        addOfRecToDelete = deleteNode(next, nonLeafNode, ptrIdx, keyIdx, key, lowerbound);
    }

    if (node.isUnderUtilized(NODE_SIZE)) {
        handleInvalidTree(node, parent, parentPointerIndex, parentKeyIndex);
    }

    return addOfRecToDelete;
}

private void handleInvalidTree(Node underUtilizedNode, NonLeafNode parent, int parentPointerIndex,
                                int parentKeyIndex) throws IllegalStateException {
    if (parent == null) {
        handleInvalidRoot(underUtilizedNode);
    } else if (underUtilizedNode.isLeaf()) {
        // Rebalancing of Leaf node
        handleInvalidLeaf(underUtilizedNode, parent, parentPointerIndex, parentKeyIndex);
    } else if (underUtilizedNode.isNonLeaf()) {
        // Rebalancing of Non-leaf node
        handleInvalidInternal(underUtilizedNode, parent, parentPointerIndex, parentKeyIndex);
    } else {
        throw new IllegalStateException("State is wrong!");
    }
}

public void handleInvalidRoot(Node underUtilizedNode) {
    if (underUtilizedNode.isLeaf()) {
        // Only node in B+ Tree - Root
        ((LeafNode) underUtilizedNode).clear();
    } else {
        NonLeafNode nonLeafRoot = (NonLeafNode) underUtilizedNode;
        Node newRoot = nonLeafRoot.getChild(0);
        newRoot.setParent(null);
        rootNode = newRoot;
    }
}
private void handleInvalidLeaf(Node underUtilizedNode, NonLeafNode parent, int parentPointerIndex,
                                int parentKeyIndex) throws IllegalStateException {
    int numChildrenOfNextParent = 0;
    int numChildrenOfNodeParent = 0;
    LeafNode nextNode;
    nextNode = null;

    // Load the neighbors
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
            && (numChildrenOfNodeParent >= underUtilizedNode.getParent().getMinNonLeafNodeSize()))) {
        mergeLeafNodes(prevNode, underUtilizedLeaf, parent, parentPointerIndex, parentKeyIndex, false);
    } else if (nextNode != null && (nextNode.getKeySize() + underUtilizedLeaf.getKeySize()) <= NODE_SIZE
            && (numChildrenOfNextParent >= underUtilizedNode.getParent().getMinNonLeafNodeSize())) {
        mergeLeafNodes(underUtilizedLeaf, nextNode, parent, parentPointerIndex + 1, parentKeyIndex + 1, true);
    } else {
        throw new IllegalStateException("Can't have both leaf " +
                "pointers null and not be root or no " +
                "common parent");
    }
}

private void handleInvalidInternal(Node underUtilizedNode,
                                    NonLeafNode parent,
                                    int parentPointerIndex,
                                    int parentKeyIndex) throws IllegalStateException {
    Node underUtilizedInternal = underUtilizedNode;

    // Load the adjacent nodes
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
        throw new IllegalStateException("Both prevInternal and nextInternal are null for " + underUtilizedNode);

    if (prevInternal != null && prevInternal.isAbleToGiveOneKey(NODE_SIZE)) {
        // Move one key from the left non-leaf node
        moveOneKeyNonLeafNode(prevInternal, (NonLeafNode) underUtilizedInternal, true, parent, parentKeyIndex);
    } else if (nextInternal != null && nextInternal.isAbleToGiveOneKey(NODE_SIZE)) {
        // Move one key from the right non-leaf node
        moveOneKeyNonLeafNode(nextInternal, (NonLeafNode) underUtilizedInternal, false, parent, parentKeyIndex + 1);
    }
    // Now, check if we can merge with the left node
    else if (prevInternal != null &&
            (underUtilizedInternal.getKeySize() + prevInternal.getKeySize()) <= NODE_SIZE) {
        // Merge with the left Non-Leaf node
        mergeNonLeafNodes(prevInternal, (NonLeafNode) underUtilizedInternal, parent,
                parentPointerIndex, parentKeyIndex, true);
    }
    // Check if we can merge with the right node
    else if (nextInternal != null &&
            (underUtilizedInternal.getKeySize() + nextInternal.getKeySize()) <= NODE_SIZE) {
        // Merge with the right Non-Leaf Node
        mergeNonLeafNodes((NonLeafNode) underUtilizedInternal, nextInternal, parent,
                parentPointerIndex + 1, parentKeyIndex + 1, false);
    } else {
        throw new IllegalStateException("Can't merge or redistribute internal node " + underUtilizedInternal);
    }
}
private void moveKeyInNonLeafNode(NonLeafNode giver, NonLeafNode receiver,
                                   boolean giverOnLeft, NonLeafNode parent,
                                   int inBetweenKeyIdx) {
    int key;

    if (giverOnLeft) {
        giver.removeLastKey();
        Node nodeToMove = giver.removeChildAt(giver.getKeySize());
        receiver.addChildAt(receiver.getKeySize(), nodeToMove);
        receiver.addKeyAt(receiver.getKeySize(), receiver.getChild(1).getFirstKey());
        key = receiver.getKeyAt(0);
    } else {
        giver.removeFirstKey();
        Node nodeToMove = giver.removeChildAt(0);
        receiver.addChildAt(0, nodeToMove);
        receiver.addKeyAt(receiver.getKeySize(), receiver.getChild(1).getFirstKey());
        key = receiver.getKeyAt(0);
    }

    int ptrIdx = receiver.searchKey(key, true);
    int keyIdx = ptrIdx - 1;

    NonLeafNode leafNode = (NonLeafNode) receiver;
    int lowerBound = checkForLowerbound(key);
    int newLowerBound;

    if (leafNode.getKeySize() >= (keyIdx + 1)) {
        newLowerBound = lowerBound;
    } else {
        newLowerBound = checkForLowerbound(leafNode.getKey(keyIdx + 1));
        parent.updateKey(inBetweenKeyIdx - 1, key, false, checkForLowerbound(key));
    }

    parent.replaceKeyAt(inBetweenKeyIdx, newLowerBound);
}

private void mergeNodesInNonLeaf(NonLeafNode nodeToMergeTo, NonLeafNode current, NonLeafNode parent,
                                  int rightPointerIdx, int inBetweenKeyIdx, boolean mergeWithLeft) {
    int keyToRemove;

    if (mergeWithLeft) {
        int moveKeyCount = current.getKeySize();
        keyToRemove = nodeToMergeTo.getChild(nodeToMergeTo.getKeySize()).getLastKey();

        for (int i = 0; i < moveKeyCount; i++) {
            nodeToMergeTo.addKeyAt(nodeToMergeTo.getKeySize(), current.getKeyAt(i));
        }

        for (int i = 0; i < current.getChildren().size(); i++) {
            nodeToMergeTo.addChild(current.removeChildAt(i));
        }

        nodeToMergeTo.addKeyAt(nodeToMergeTo.getKeySize(), nodeToMergeTo.getChild(nodeToMergeTo.getKeySize() + 1).getFirstKey());
        current.getParent().removeChild(current);
    } else {
        int moveKeyCount = current.getKeySize();
        keyToRemove = current.getFirstKey();

        for (int i = 0; i < moveKeyCount; i++) {
            nodeToMergeTo.addKeyAt(0, current.getKeyAt(i));
        }

        for (int i = 0; i < current.getChildren().size(); i++) {
            nodeToMergeTo.addChildAt(0, current.removeChildAt(i));
        }

        nodeToMergeTo.addKeyAt(0, nodeToMergeTo.getChild(1).getFirstKey());
        current.getParent().removeChild(current);
    }

    int ptrIdx = nodeToMergeTo.searchKey(keyToRemove, true);
    int keyIdx = ptrIdx - 1;

    NonLeafNode leafNode = (NonLeafNode) nodeToMergeTo;
    int lowerBound = checkForLowerbound(keyToRemove);
    int newLowerBound;

    if (leafNode.getKeySize() >= (keyIdx + 1)) {
        newLowerBound = lowerBound;
    } else {
        newLowerBound = checkForLowerbound(leafNode.getKey(keyIdx + 1));
        parent.updateKey(inBetweenKeyIdx - 1, keyToRemove, false, checkForLowerbound(keyToRemove));
    }
}

private void mergeLeafNodes(LeafNode nodeToMergeTo, LeafNode current, NonLeafNode parent,
                            int rightPointerIdx, int inBetweenKeyIdx, boolean mergeToRight) {
    int removedKey = 0;
    int moveKeyCount = current.getKeySize();
    int numberOfChildren = current.getParent().getChildren().size();

    for (int i = 0; i < moveKeyCount; i++) {
        removedKey = current.removeKeyAt(0);
        int leftLastIdx = nodeToMergeTo.getLastIdx();
        nodeToMergeTo.insertKeyAt(leftLastIdx + 1, removedKey);
        nodeToMergeTo.insertByRedistribution(removedKey, current.getAddressesForKey(removedKey));
        current.removeKeyInMap(removedKey);
    }

    parent.removeChild(current);

    if (parent.getChildren().size() == parent.getKeySize()) {
        // No need to update parent
    } else {
        parent.removeKeyAt(inBetweenKeyIdx);
    }

    if (mergeToRight) {
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
            if (currentPrev != null && currentPrev.getPrevious() != null) {
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

            if (currParent.getKeySize() > currParent.getNonLeafSize()
                    && currParent.getChildren().size() > current.getNonLeafSize()) {
                currParent.removeKeyAt(0);
            }
        }
    }

    int lowerBound = checkForLowerbound(removedKey);
    int newLowerBound;

    if (current.getParent().getKeySize() >= numberOfChildren) {
        newLowerBound = lowerBound;
    } else {
        newLowerBound = current.getParent().getChild(0).getFirstKey();

        if (inBetweenKeyIdx == 0) {
            // inBetweenKeyIdx is 0
        } else {
            current.getParent().updateKey(inBetweenKeyIdx - 1, newLowerBound, true, newLowerBound);
        }
    }
}

private void moveOneKey(LeafNode giver, LeafNode receiver, boolean giverOnLeft, NonLeafNode parent,
                        int inBetweenKeyIdx) {
    int key;

    if (giverOnLeft) {
        int giverKey = giver.getLastKey();
        receiver.insertByRedistribution(giverKey, giver.getAddressesForKey(giverKey));
        giver.removeKeyInMap(giverKey);

        receiver.insertKeyAt(0, giverKey);
        giver.removeLastKey();
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
        // Do not update parent
    } else if (inBetweenKeyIdx >= 0) {
        if (parent.getKeySize() == inBetweenKeyIdx) {
            parent.replaceKeyAt(inBetweenKeyIdx - 1, key);

            int lastParentChild = receiver.getParent().getKeys().size() - 1;
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

    LeafNode leafNode = (LeafNode) receiver;
    int lowerBound = checkForLowerbound(key);
    int newLowerBound;

    if (leafNode.getKeySize() >= (keyIdx + 1)) {
        newLowerBound = lowerBound;
    } else {
        newLowerBound = checkForLowerbound(leafNode.getKey(keyIdx + 1));
        parent.updateKey(inBetweenKeyIdx - 1, parent.getChild(inBetweenKeyIdx).getFirstKey(), false,
                checkForLowerbound(key));
    }
}

private ArrayList<Address> searchKey(int key) {
    return searchValue(this.rootNode, key);
}

public ArrayList<Address> searchValue(Node node, int key) {
    PerformanceRecorder.addOneNodeReads();

    if (node.isLeaf()) {
        int ptrIdx = node.searchKey(key, false);

        if (ptrIdx >= 0 && ptrIdx < node.getKeySize() && key == node.getKeyAt(ptrIdx)) {
            return ((LeafNode) node).getAddressesForKey(key);
        }

        return null;
    } else {
        int ptrIdx = node.searchKey(key, false);
        Node childNode = ((NonLeafNode) node).getChild(ptrIdx);
        return searchValue(childNode, key);
    }
}

public ArrayList<Address> rangeSearch(int minKey, int maxKey) {
    return searchValuesInRange(minKey, maxKey, this.rootNode);
}

public static ArrayList<Address> searchValuesInRange(int minKey, int maxKey, Node node) {
    int ptrIdx;
    ArrayList<Address> resultList = new ArrayList<>();
    PerformanceRecorder.addOneRangeNodeReads();

    if (node.isLeaf()) {
        ptrIdx = node.searchKey(minKey, false);
        LeafNode leaf = (LeafNode) node;

        while (true) {
            if (ptrIdx == leaf.getKeySize()) {
                if (leaf.getNext() == null) {
                    break;
                }

                leaf = (LeafNode) (leaf.getNext());
                PerformanceRecorder.addOneRangeNodeReads();
                ptrIdx = 0;

                if (ptrIdx >= leaf.getKeySize()) {
                    throw new IllegalStateException("Range search found a node with 0 keys");
                }
            }

            if (leaf.getKey(ptrIdx) > maxKey) {
                break;
            }

            int key = leaf.getKey(ptrIdx);
            resultList.addAll(leaf.getAddressesForKey(key));
            ptrIdx++;
        }

        return (resultList.size() > 0 ? resultList : null);
    } else {
        ptrIdx = node.searchKey(minKey, true);
        Node childNode = ((NonLeafNode) node).getChild(ptrIdx);
        return searchValuesInRange(minKey, maxKey, childNode);
    }
}

public void printBPlusTree(Node root) {
    printBPlusTreeHelper(root, "");
}

private void printBPlusTreeHelper(Node node, String indent) {
    if (node == null) {
        return;
    }

    if (node.isLeaf()) {
        LeafNode leaf = (LeafNode) node;
        System.out.print(indent + "LeafNode: ");
        leaf.getKeys().forEach(key -> System.out.print(key + " "));
        System.out.println();
    } else {
        NonLeafNode nonLeaf = (NonLeafNode) node;
        System.out.print(indent + "NonLeafNode: ");
        nonLeaf.getKeys().forEach(key -> System.out.print(key + " "));
        System.out.println();

        for (Node child : nonLeaf.getChildren()) {
            printBPlusTreeHelper(child, indent + "  ");
        }
    }
}

private void countLevel(Node node) {
    while (!node.isLeaf()) {
        NonLeafNode nonLeaf = (NonLeafNode) node;
        node = nonLeaf.getChild(0);
        PerformanceRecorder.addOneTreeDegree();
    }
    PerformanceRecorder.addOneTreeDegree();
}
}

