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

    public String getTconst() {
        return tConst;
    }

    public float getAvgRating() {
        return avgRate;
    }

    public int getNumVotes() {
        return numVote;
    }

    public static int size() {
        return 17;
    }
}
