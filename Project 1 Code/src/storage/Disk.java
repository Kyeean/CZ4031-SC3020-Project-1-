package storage;

import java.util.ArrayList;
import java.util.HashSet;

public class Disk {
    private Block[] blocks;

    private int recordCount = 0;
    
    private boolean[] availableBlocks;
    private boolean[] filledBlocks;

    public Disk(int diskSize, int blockSize) {
        
        this.blocks = new Block[diskSize / blockSize];
        this.availableBlocks = new boolean[diskSize / blockSize];
        this.filledBlocks = new boolean[diskSize / blockSize];
  
        for (int i = 0; i < blocks.length; i++) {
            blocks[i] = new Block(blockSize);
            availableBlocks[i] = true;
        }
        //int lruCacheSize = (int) (256.0 / 500000.0 * diskSize / blockSize);
        //this.lruCache = new LRUCache(lruCacheSize);
    }

    public Address writeRecordToStorage(Record r){
        recordCount++;
        int blockPtr = getFirstAvailableBlockId();
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
        int addr = blocks[blockPtr].insertRecord(r);
        filledBlocks[addr] = true;
        if(!blocks[blockPtr].isBlockAvailable()){
            availableBlocks[blockPtr] = false;
        }
        return new Address(blockPtr, addr);
    }
    
    public int getNoOfFilledBlocks() {
        int count = 0;

        for(int i = 0; i < filledBlocks.length; i++){
            if(filledBlocks[i] = true){
                count++;
            }
        }
        return count;
    }

    private Block getBlock(int blockNumber) {
        return blocks[blockNumber];
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
                Block block = blocks[i];
                int numberOfRecordsInBlock = block.getCurSize();
                for (int j = 0; j < numberOfRecordsInBlock; j++) {
                    // retrieve the record
                    r = block.getRecord(j);
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

    public void deleteRecord(ArrayList<Address> addList) {
        for (Address add : addList) {
            int blockId = add.getBlockId();
            int position = add.getPosition();
            Block block = getBlock(blockId);
            block.deleteRecord(position);
            if (filledBlocks[blockId] == true) {
                filledBlocks[blockId] = false;
            }
            availableBlocks[blockId] = true;
        }
    }


    public void experimentOne() {
        System.out.println("\n----------------------EXPERIMENT 1-----------------------");
        System.out.printf("Total Number of Records Stored: %d\n", this.getNumberOfRecords());
        System.out.println(String.format("Size of Each Record: %d Bytes", Record.size()));
        System.out.printf("Number of Records Stored in a Block: %d\n", Block.getTotalRecords());
        System.out.println(String.format("Number of Blocks Allocated: %d\n", this.getNoOfFilledBlocks()));
    }
}