package index;

public class PerformanceRecorder {

    private int totalNodes;
    private int treeDegree;
    private int totalNodeReads;
    private int totalRangeNodeReads;

    public int getTotalNodes() {
        return totalNodes;
    }
    public void addOneNode() {
        totalNodes++;
    }
    public int getTreeDegree() {
        return treeDegree;
    }
    public void addOneTreeDegree() {
        treeDegree++;
    }
    public void deleteOneTreeDegree() {
        treeDegree--;
    }

    public int getNodeReads() {
        return totalNodeReads;
    }

    public void addOneNodeReads() {
        totalNodeReads++;
    }

    public int getRangeNodeReads() {
        return totalRangeNodeReads;
    }
    public void addOneRangeNodeReads() {
        totalRangeNodeReads++;
        addOneNodeReads();
    }
}
