package storage;

import java.util.ArrayList;
import java.util.HashSet;

public class Disk {
    private Block[] blocks;

    private int recordCount = 0;
    private int diskSize;
    private int blockSize;
    
    private boolean[] availableBlocks;
    private boolean[] filledBlocks;

    public Disk(int diskSize, int blockSize) {
        this.diskSize = diskSize;
        this.blockSize = blockSize;
        this.blocks = new Block[diskSize / blockSize];
        this.availableBlocks = new boolean[diskSize / blockSize];
        this.filledBlocks = new boolean[diskSize / blockSize];
        // initialise all available blocks in hashMap
        for (int i = 0; i < blocks.length; i++) {
            blocks[i] = new Block(blockSize);
            availableBlocks[i] = true;
        }
        //int lruCacheSize = (int) (256.0 / 500000.0 * diskSize / blockSize);
        //this.lruCache = new LRUCache(lruCacheSize);
    }

    public Address writeRecordToStorage(Record r){
        recordCount++;
        int blockPtr = getFirstAvailableBlockID();
        Address addressofRecordStored = this.insertRecordIntoBlock(blockPtr, r);
        return addressofRecordStored;
    }

    public int getNumberOfRecords() {
        return recordCount;
    }

    private int getFirstAvailableBlockId() {
        for(int i = 0 ; i < availableBlocks.length; i++){
            if (availableBlocks[i] = true) {
                return i;
            }
        }
        return -1;
    }

    private Address insertRecordIntoBlock(int blockPtr, Record r){
        if(blockPtr == -1){
            return null;
        }
        int addr = blocks[blockPtr].insertRecordIntoBlocks(r);
        filledBlocks[addr] = true;
        if(!blocks[blockPtr.isBlockAvailable()]){
            availableBlocks[blockPtr] = false;
        }
        return new Address(blockPtr, addr);
    }
    
    public int getNumberBlockUsed() {
        int usedBlocks;
        for(int i = 0; i < filledBlocks.length; i++){
            if(filledBlocks[i] = true)
                usedBlocks++;
        }
        return usedBlocks;
    }

    private Block getBlock(int blockNumber) {
        Block block = lruCache.get(blockNumber);
        if (block != null) {
            //blockAccessReduced++;
        }
        if (block == null && blockNumber >= 0) {
            // 1 I/O
            block = blocks[blockNumber];
            //blockAccesses++;
            lruCache.put(blockNumber, block);
        }
        return block;
    }

    public Record getRecord(Address addr) {
        Block block = getBlock(addr.getBlockId());
        return block.getRecord(addr.getPosition());
    }

    public int getBlocksAccessedByForce(int numVotesValue, int numVotesValueUpperRange) {
        return runBruteForceSearch(numVotesValue, numVotesValueUpperRange);
    }

    public int runBruteForceSearch(int numVotesValue, int numVotesValueUpperRange) {
        Record r;
        int curNumVotes;
        int countBlockAccess = 0;
        ArrayList<Record> foundRecords = new ArrayList<>();
        for (int i = 0; i < filledBlocks.length; i++) {
            if(filledBlocks[i] = true){
                countBlockAccess++;
                Block block = blocks[blockPtr];
                int numberOfRecordsInBlock = block.getCurSize();
                for (int j = 0; j < numberOfRecordsInBlock; j++) {
                    // retrieve the record
                    r = block.getRecordFromBlock(j);
                    curNumVotes = r.getNumVotes();
                    if (numVotesValue <= curNumVotes && curNumVotes <= numVotesValueUpperRange) {
                        foundRecords.add(r);
                    }
                }
            }
        }
        if (foundRecords.size() == 0) {
            System.out.printf("Value in range [%d, %d] not found int database!\n",numVotesValue, numVotesValueUpperRange);
        }
        for (Record record : foundRecords)
            System.out.printf("Found Records (Brute Force) %s\n", record);
        return countBlockAccess;
    }

}