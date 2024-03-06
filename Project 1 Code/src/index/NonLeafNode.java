package index;

import java.util.ArrayList;

public class NonLeafNode extends Node {
    ArrayList<Node> children;

    // Construct a NonLeaf Node object with an ArrayList of Node type.
    public NonLeafNode() {
        super();
        this.children = new ArrayList<>();
        setLeaf(false);
    }
    
    // Get all children of current parent node
    public ArrayList<Node> getChildren() {
        return children;
    }

    // Get child node from index of parent node
    public Node getChild(int index) {
        return children.get(index);
    }

    // Add child node to parent node
    public void addChild(Node childNode) {
        this.children.add(childNode);
    }

    // Remove child node from parent node
    public void removeChild(Node childNode) {
        this.children.remove(childNode);
    }
}
