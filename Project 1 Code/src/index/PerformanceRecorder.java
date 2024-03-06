package index;

public class PerformanceRecorder {

    private static int totalNodes;
    private static int treeDegree;
    private static int totalNodeReads;
    private static int totalRangeNodeReads;

    public int getTotalNodes() {
        return totalNodes;
    }
    public static void addOneNode() {
        totalNodes++;
    }
    public int getTreeDegree() {
        return treeDegree;
    }
    public static void addOneTreeDegree() {
        treeDegree++;
    }
    public static void deleteOneTreeDegree() {
        treeDegree--;
    }

    public int getNodeReads() {
        return totalNodeReads;
    }

    public static void addOneNodeReads() {
        totalNodeReads++;
    }

    public int getRangeNodeReads() {
        return totalRangeNodeReads;
    }
    public static void addOneRangeNodeReads() {
        totalRangeNodeReads++;
        addOneNodeReads();
    }
}
