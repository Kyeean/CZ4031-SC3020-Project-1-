package storage;

/**
 * Represents the logical address of a record
 * It contains the ID of the block where the record is stored,
 * and the position of the record inside that block.
 */
public class Address{
    private int blockID;
    private int position;

    public Address(int blockID, int position){
        this.blockID = blockID;
        this.position = position;
    }

    public int getBlockId() {
        return blockID;
    }

    public int getPosition() {
        return position;
    }

    @Override
    public String toString() {
        return String.format("blk %d position %d", blockID, position);
    }

}

