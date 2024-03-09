package storage;

import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;

public class Disk {
    private Block[] blocks;

    private int recordCount = 0;
    private static int blockAccesses = 0;
    private Set<Integer> occupiedBlocks;
    private Set<Integer> freeBlocks;

    public Disk(int diskSize, int blockSize) {
        this.blocks = new Block[diskSize / blockSize];
        this.freeBlocks = new HashSet<>();
        this.occupiedBlocks = new HashSet<>();

        for (int i = 0; i < blocks.length; i++) {
            blocks[i] = new Block(blockSize);
            freeBlocks.add(i);
        }  
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
        if (freeBlocks.isEmpty()) {
            return -1;
        }
        
        return freeBlocks.iterator().next();
    }

    private Address insertRecordIntoBlock(int blockPtr, Record r){
        if(blockPtr == -1){
            return null;
        }

        int addr = blocks[blockPtr].insertRecord(r);
        occupiedBlocks.add(blockPtr);

        if(!blocks[blockPtr].isBlockAvailable()){

        	freeBlocks.remove(blockPtr);
        }

        return new Address(blockPtr, addr);
    }
    
    public int getNoOfOccupiedBlocks() {
        return occupiedBlocks.size();
    }

    private Block getBlock(int blockNumber) {
        blockAccesses++;
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
        ArrayList<Record> finalRes = new ArrayList<>();

        for (int blockPtr : occupiedBlocks) {
            countBlockAccess++;
            Block block = blocks[blockPtr];
            int numberOfRecordsInBlock = block.getCurSize();
            for (int i = 0; i < numberOfRecordsInBlock; i++) {
                // retrieve the record
                r = block.getRecord(i);
                curNumVotes = r.getNumVotes();
                if (curNumVotes <= numVotesValueUpperRange && numVotesValue <= curNumVotes ) {
                    finalRes.add(r);
                }
            }
        }

        if (finalRes.size() == 0) {
            System.out.printf("Value in range [%d, %d] not found int database!\n",numVotesValue, numVotesValueUpperRange);
        }

        for (Record record : finalRes)
            System.out.printf("Found Records (Brute Force) %s\n", record);

        return countBlockAccess;
    }
    

    public void deleteRecord(ArrayList<Address> addList) {
        for (Address add : addList) {
            int blockId = add.getBlockId();
            int position = add.getPosition();
            Block block = getBlock(blockId);
            block.deleteRecord(position);

            if (occupiedBlocks.contains(blockId)) {
            	occupiedBlocks.remove(blockId);
            }
            freeBlocks.add(blockId);
        }
    }

    public int getBlockAccesses() {
        return blockAccesses;
    }
    
    public void experimentOne() {
        System.out.println("\n----------------------EXPERIMENT 1-----------------------");
        System.out.printf("Total Number of Records Stored: %d\n", this.getNumberOfRecords());
        System.out.println(String.format("Size of Each Record: %d Bytes", Record.size()));
        System.out.printf("Number of Records Stored in a Block: %d\n", Block.getTotalRecords());
        System.out.println(String.format("Number of Blocks Allocated: %d\n", this.getNoOfOccupiedBlocks()));
    }
}
