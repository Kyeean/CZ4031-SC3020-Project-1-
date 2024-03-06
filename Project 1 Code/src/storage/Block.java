package storage;

public class Block {
    private int currentRecords;     // Number of reccords in the block
    private static int totalRecords;       // Total number of records in a block
    private Record[] records;       // The records in a block

    public Block(int BLOCK_SIZE) {
        this.currentRecords = 0;
        this.totalRecords = BLOCK_SIZE / Record.size();
        this.records = new Record[this.totalRecords];
    }

    public boolean isBlockAvailable() {
        return currentRecords < totalRecords;
    }

    public Record getRecord(int pos) {
        return records[pos];
    }

    public int getCurSize() {
        return currentRecords;
    }

    public static int getTotalRecords() {
        return totalRecords;
    }

    public int insertRecord(Record r) {
        // Insert into first available space
        for(int i = 0; i < records.length; i++) {
            if(records[i] == null) {
                records[i] = r;
                this.currentRecords++;
                return i;
            }
        }
        // No available space
        return -1;
    }

    public void deleteRecord(int pos) {
        // Clear the entry
        records[pos] = null;
        currentRecords--;
    }
}
