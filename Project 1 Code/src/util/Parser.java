package util;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Scanner;
import storage.Address;
import storage.Record;
import storage.Disk;
import index.BpTree;

/* Parser Class to interpret and process data from TSV File */
public class Parser {
    public static final int BLOCK_SIZE = 200;
    public static final int OVERHEAD = 8;
    public static final int POINTER_SIZE = 8; // for 64-bit systems
    public static final int KEY_SIZE = 4; // Integer datatype
    private static int counter = 0;

    public static void parseAndLoadData(String filePath, int diskCapacity) {
        try {
            String line;
            // Initialize the database
            Disk database = new Disk(diskCapacity, BLOCK_SIZE);

            // Start loading data
            BufferedReader reader = new BufferedReader(new FileReader(filePath));
            reader.readLine(); // Skip the first line (the column line)

            // Initialize a new B+ tree
            BpTree bplusTree = new BpTree();

            while ((line = reader.readLine()) != null) {
                counter++;
                if (counter % 200000 == 0)
                    System.out.printf("Reading Data... (%d rows)\n", counter);
                
                // Split the line into fields
                String[] fields = line.split("\t");
                String tconst = fields[0];
                float averageRating = Float.parseFloat(fields[1]);
                int numVotes = Integer.parseInt(fields[2]);
                
                // Create a record from the data
                Record record = createRecord(tconst, averageRating, numVotes);
                
                // Write the record to storage and obtain its address
                Address address = database.writeRecordToStorage(record);
                
                // Use the number of votes as the key for the B+ tree
                int key = record.getNumVotes();
                bplusTree.insertKey(key, address);
            }
            reader.close();

            // Choose an experiment to run
            try {
                int index = 0;
                while (true) {
                    try {
                        System.out.println("\nSelect Experiment (1-5):");
                        Scanner sc = new Scanner(System.in);
                        index = sc.nextInt();

                        if (index > 0 && index < 6) {
                            break;
                        } else {
                            System.out.println("\nInvalid input. (Only input 1-5)");
                        }
                    } catch (Exception e) {
                        System.out.println("\nPlease only input 1-5!");
                    }
                }

                // Run the selected experiment
                switch (index) {
                    case 1:
                        database.experimentOne();
                        break;
                    case 2:
                        BpTree.experimentTwo(bplusTree);
                        break;
                    case 3:
                        BpTree.experimentThree(database, bplusTree);
                        break;
                    case 4:
                        BpTree.experimentFour(database, bplusTree);
                        break;
                    case 5:
                        BpTree.experimentFive(database, bplusTree);
                        break;
                }

            } catch (Exception e) {
                // Handle exceptions
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Create a record object from the provided data.
     *
     * @param tconst        Alphanumeric unique identifier of the title
     * @param averageRating Weighted average of all the individual user ratings
     * @param numVotes      Number of votes the title has received
     * @return              A Record object created from the input data
     */
    public static Record createRecord(String tconst, float averageRating, int numVotes) {
        return new Record(tconst, averageRating, numVotes);
    }
    
}
