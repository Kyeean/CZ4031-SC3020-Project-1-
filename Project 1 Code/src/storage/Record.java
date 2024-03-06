package storage;

public class Record {
    public String tConst;   // Unique ID
    public float avgRate;   // Average Rating
    public int numVote;     // Number of Votes

    public Record(String tConst, float avgRate, int numVote) {
        this.tConst = tConst;
        this.avgRate = avgRate;
        this.numVote = numVote;
    }

    // Return Tconst of the record
    public String getTconst() {
        return tConst;
    }

    // Return average rating of the record
    public float getAvgRating() {
        return avgRate;
    }

    // Return number of votes of the record
    public int getNumVotes() {
        return numVote;
    }

    /* Return the size of record (It is fixed),
    tConst = 2 Bytes * 10 chars = 20 Bytes
    avgRate = 4 Bytes
    numVote = 4 Bytes
    Total = 28 Bytes 
    */
    public static int size() {
        return 28;
    }

    public String toString() {
        return String.format("Record Info: Tconst: %s, avgRating: %f, numVotes: %d ", this.getTconst(), this.getAvgRating(), this.getNumVotes());
    }
}
