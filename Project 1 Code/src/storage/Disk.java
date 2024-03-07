package storage;

import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;

public class Disk {
    private Block[] blocks;

    private int recordCount = 0;
    private int blockAccessReduced = 0;
    private static int blockAccesses = 0;
    private int blockSize, diskSize;
//    private boolean[] availableBlocks;
//    private boolean[] filledBlocks;
    private Set<Integer> occupiedBlocks;
    private Set<Integer> freeBlocks;

    public Disk(int diskSize, int blockSize) {
        this.blockSize = blockSize;
        this.diskSize = diskSize;
        this.blocks = new Block[diskSize / blockSize];
//        this.availableBlocks = new boolean[diskSize / blockSize];
        this.freeBlocks = new HashSet<>();
//        this.filledBlocks = new boolean[diskSize / blockSize];
        this.occupiedBlocks = new HashSet<>();
  
        for (int i = 0; i < blocks.length; i++) {
            blocks[i] = new Block(blockSize);
//            availableBlocks[i] = true;
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

//    private int getFirstAvailableBlockId() {
//        for(int i = 0 ; i < availableBlocks.length; i++){
//            if (availableBlocks[i] = true) {
//                return i;
//            }
//        }
//        return -1;
//    }
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
//        filledBlocks[addr] = true;
        occupiedBlocks.add(blockPtr);
        if(!blocks[blockPtr].isBlockAvailable()){
//            availableBlocks[blockPtr] = false;
        	freeBlocks.remove(blockPtr);
        }
        return new Address(blockPtr, addr);
    }
    
//    public int getNoOfFilledBlocks() {
//        int count = 0;
//
//        for(int i = 0; i < filledBlocks.length; i++){
//            if(filledBlocks[i] = true){
//                count++;
//            }
//        }
//        return count;
//    }
    public int getNoOfOccupiedBlocks() {
        return occupiedBlocks.size();
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

//    public int runBruteForceSearch(int numVotesValue, int numVotesValueUpperRange) {
//        Record r;
//        int curNumVotes;
//        int countBlockAccess = 0;
//        ArrayList<Record> foundRecords = new ArrayList<>();
//        for (int i = 0; i < filledBlocks.length; i++) {
//            if(filledBlocks[i] = true){
//                countBlockAccess++;
//                Block block = blocks[i];
//                int numberOfRecordsInBlock = block.getCurSize();
//                for (int j = 0; j < numberOfRecordsInBlock; j++) {
//                    // retrieve the record
//                    r = block.getRecord(j);
//                    curNumVotes = r.getNumVotes();
//                    if (numVotesValue <= curNumVotes && curNumVotes <= numVotesValueUpperRange) {
//                        foundRecords.add(r);
//                    }
//                }
//            }
//        }
//        if (foundRecords.size() == 0) {
//            System.out.printf("Value in range [%d, %d] not found int database!\n",numVotesValue, numVotesValueUpperRange);
//        }
//        for (Record record : foundRecords)
//            System.out.printf("Found Records (Brute Force) %s\n", record);
//        return countBlockAccess;
//    }
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
//            if (filledBlocks[blockId] == true) {
//                filledBlocks[blockId] = false;
//            }
//            availableBlocks[blockId] = true;
//        }
            if (occupiedBlocks.contains(blockId)) {
            	occupiedBlocks.remove(blockId);
            }
            freeBlocks.add(blockId);
        }
    }

    public int getBlockAccesses() {
        return blockAccesses;
    }
    public int getBlockAccessReduced() {
        return blockAccessReduced;
    }
    
    public void printRecordsinBlock(){

        for(int k = 0; k < 10; k++){
            Block b = getBlock(k);
            System.out.println(diskSize/blockSize + "\n");
            for(int i = 0; i < 7 ; i++){
                Record r = b.getRecord(i);
                System.out.printf(r.toString() + "\n");
            }
        }       
    }

    public void experimentOne() {
        System.out.println("\n----------------------EXPERIMENT 1-----------------------");
        System.out.printf("Total Number of Records Stored: %d\n", this.getNumberOfRecords());
        System.out.println(String.format("Size of Each Record: %d Bytes", Record.size()));
        System.out.printf("Number of Records Stored in a Block: %d\n", Block.getTotalRecords());
        System.out.println(String.format("Number of Blocks Allocated: %d\n", this.getNoOfOccupiedBlocks()));
    }
}
