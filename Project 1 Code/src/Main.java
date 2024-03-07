import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import util.Parser;

/**
 * Main program
 */
public class Main {
    private static final int DEFAULT_MAX_DISK_CAPACITY = 500 * (int) (Math.pow(10, 6));

    public static void main(String[] args) throws Exception {

        
        String filePath = new File("").getAbsolutePath();
        String fileSeparator = System.getProperty("file.separator");
        // filePath = filePath.concat(separator + "data.tsv");
        filePath = filePath.concat(fileSeparator + "src" + fileSeparator + "data.tsv");
        System.out.print(filePath + "\n");
        File datafile = new File(String.valueOf(filePath));
        if (datafile.exists()) {
            System.out.print("File Exists\nReading data...\n");
            int diskSize = getDiskInput();
            Parser.parseAndLoadData(String.valueOf(filePath), diskSize);
        } else if (!datafile.exists()) {
            Scanner sc = new Scanner(System.in);
            System.out.print("Default file path failed! Please input the absolute file path of data.tsv: ");
            filePath = sc.nextLine();
            File newFileCheck = new File(String.valueOf(filePath));
            if (newFileCheck.exists()) {
                System.out.print("File Exists\nReading data...\n");
                int diskSize = getDiskInput();
                Parser.parseAndLoadData(String.valueOf(filePath), diskSize);
            }
        } else {
            throw new FileNotFoundException("File does not exist!");
        }
    }

    /**
     * The getDiskInput method prompts the user to input a disk size between 200 and 500 MB, and returns the disk size in bytes.
     * If the user does not enter a valid disk size within 5 attempts, the method returns the default disk size of 500.
     *
     * @return the disk size
     */
    private static int getDiskInput() {
        
        Scanner sc = new Scanner(System.in);
        int i = 0;
        while (i < 5) {
            try {
                System.out.print("Enter Disk Size between 200MB-500MB: ");
                int diskSize = sc.nextInt();
                
                if (diskSize <= 199 || diskSize >= 501) {
                    i++;
                   
                } else {
                	System.out.printf("Disk size of %d:\n",diskSize);
                    return diskSize * (int) (Math.pow(10, 6));
                }
            } catch (IndexOutOfBoundsException e) {
                System.out.printf("No value input detected, default maximum disk size: %d\n", DEFAULT_MAX_DISK_CAPACITY);
                break;
            } catch (NumberFormatException e) {
                System.out.printf("Invalid disk size input detected, default maximum disk size: %d\n", DEFAULT_MAX_DISK_CAPACITY);
                break;
            } catch (Exception e) {
                System.out.printf("Error detected, default maximum disk size: %d\n", DEFAULT_MAX_DISK_CAPACITY);
                break;
            }
        }
        System.out.printf("Number of tries exceeded, commencing with default maximum disk size of %d\n", 500);
        return DEFAULT_MAX_DISK_CAPACITY;
    }
}