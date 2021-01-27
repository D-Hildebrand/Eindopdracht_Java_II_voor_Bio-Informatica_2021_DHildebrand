// Made by Dominic Hildebrand
// Java end project 2021
// 27-1-2021

package parentsDiseaseFinderApp;

import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import java.awt.*;
import java.io.*;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.zip.GZIPInputStream;


/**
 * Application to compare two 23andMe parent files and find possible diseases if they get children
 */
public class parentsDiseaseFinder {

    public static void main(String[] args) throws IOException {
        referenceChecker();
//        parentComparer();
    }

    /**
     * Function to download 'variant_summary.txt.gz.md5' reference MD5 file from NCBI.
     * Makes system prints to tell user what the program is doing.
     *
     * @throws IOException Ambiguous read/write error
     */
    public static void referenceMD5Downloader() throws IOException {

        // Download with system prints for user to know what the program is downloading...
        System.out.println("Downloading 'variant_summary.txt.gz.md5'...");
        URL website = new URL("ftp://ftp.ncbi.nlm.nih.gov/pub/clinvar/tab_delimited/variant_summary.txt.gz.md5");
        ReadableByteChannel rbc = Channels.newChannel(website.openStream());
        FileOutputStream fos = new FileOutputStream("variant_summary.txt.gz.md5");
        fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);

        System.out.println("Downloaded 'variant_summary.txt.gz.md5'\n");
    }

    /**
     * Function to download 'variant_summary.txt.gz' reference file from NCBI.
     * Makes system prints to tell user what the program is doing.
     *
     * @throws IOException Ambiguous read/write error
     */
    public static void referenceDownloader() throws IOException {

        // Deletes old reference file before downloading new one, if there is one
        Files.deleteIfExists(Path.of("variant_summary.txt.gz"));
        // Download with system prints for the user to know what the program is downloading...
        System.out.println("Downloading 'variant_summary.txt.gz'...");
        URL website2 = new URL("ftp://ftp.ncbi.nlm.nih.gov/pub/clinvar/tab_delimited/variant_summary.txt.gz");
        ReadableByteChannel rbc2 = Channels.newChannel(website2.openStream());
        FileOutputStream fos2 = new FileOutputStream("variant_summary.txt.gz");
        fos2.getChannel().transferFrom(rbc2, 0, Long.MAX_VALUE);

        System.out.println("Downloaded 'variant_summary.txt.gz'\n");
    }


    /**
     * Function to check if the reference md5 is still up to date.
     * Downloads 'variant_summary.txt.gz.md5' using referenceMD5Downloader and
     * downloads 'variant_summary.txt.gz' using referenceDownloader if it needs to.
     *
     * @throws IOException Ambiguous read/write error
     */
    public static void referenceChecker() throws IOException {

        System.out.println("Comparing local 'variant_summary.txt.gz.md5' vs online version...\n");
        try {
            Scanner filereader = new Scanner(new File("variant_summary.txt.gz.md5"));

            String line = filereader.nextLine();
            // regex has two spaces, since 'variant_summary.txt.gz.md5' has two spacebars between the sets of text
            String MD5local = line.split("  ")[0];

            referenceMD5Downloader();
            filereader = new Scanner(new File("variant_summary.txt.gz.md5"));

            line = filereader.nextLine();
            // regex has two spaces, since 'variant_summary.txt.gz.md5' has two spacebars between the sets of text
            String MD5download = line.split("  ")[0];

            // Just to throw a fileNotFound exception in case the file is not there
            filereader = new Scanner(new File("variant_summary.txt.gz"));
            filereader.close();

            // If the downloaded hash isn't the same as the local hash, delete old reference, download new reference
            if (!MD5local.equals(MD5download)) {
                System.out.println("Local reference file is not the latest variant. " +
                        "Deleting old file and commencing download of most recent variant.\n");
                referenceDownloader();
            } else {
                System.out.println("File 'variant_summary.txt.gz.md5' matches; files up to date.\n");
                variant_summaryToObject();
            }


        } catch (FileNotFoundException exception) {
            // Notifies user to not finding one or both files and starts download of MD5 and reference files.
            System.out.println("One or both reference files not found, commencing download of reference files " +
                    "'variant_summary.txt.gz.md5' and 'variant_summary.txt.gz'.\n");
            referenceMD5Downloader();
            referenceDownloader();
            System.out.println("Finished downloads.\n");
            variant_summaryToObject();
        }
    }

    /**
     * Function to place variant_summary data into a HashMap.
     * Creates HashMap variantRefHashMap key: chromosome+position+refAlell+altAlell, value: Object diseaseVariant
     * Calls parentComparer(variantRefHashMap)
     * bigO: N
     *
     * @throws IOException Ambiguous read/write error
     */
    public static void variant_summaryToObject() throws IOException {

        try {
            // Using system prints to give user input of what the program is doing
            System.out.println("Parsing reference data...");
            // BufferedReader to read the .gz file
            BufferedReader in = new BufferedReader
                    (new InputStreamReader
                            (new GZIPInputStream
                                    (new FileInputStream("variant_summary.txt.gz"))));

            // Creating HashMap for variant data, stored as
            // chromosome+position+referenceAlelle+altAlelle as key, with an object as value
            // This way a maximum searching bigO of N can be achieved instead of N^N
            HashMap<String, diseaseVariant> variantRefHashMap = new HashMap();
            String line;

            // Dirty but effective way of removing first line
            in.readLine();

            // To parse through the entire variant_summary file
            while ((line = in.readLine()) != null) {
                String[] temp = line.split("\t");

                // To add required data from variant_summary.txt.gz into objects
                // temp[0]=AllelID, temp[1]=Type, temp[31]=Position, temp[7]=Pathogenicity, temp[3]=GeneID,
                // temp[32]=referenceAlelle, temp[33]=alternateAlelle, temp[13]=disease, temp[18]=Chromosome.

                // variantRefHashMap has the chromosome+position+refAlell+altAlell as key with an object as value
                variantRefHashMap.put((temp[18] + temp[31] + temp[32] + temp[33]),
                        new diseaseVariant(Integer.parseInt(temp[0]), temp[1], Integer.parseInt(temp[31]),
                                Integer.parseInt(temp[7]), Integer.parseInt(temp[3]), temp[32], temp[33], temp[13],
                                temp[18]));
            }
//            variantRefHashMap used to be an array, which could be sorted, but now it's a hashmap, which is unsortable.

            parentComparer(variantRefHashMap);

        } catch (FileNotFoundException exception) {
            System.out.println("Something went wrong finding file 'variant_summary.txt.gz', " +
                    "restarting checking phase now.");
            referenceChecker();

        } catch (NumberFormatException exception) {
            System.out.println("Fatal error: 'variant_summary.txt.gz' corrupt, " +
                    "attempting re-download of 'variant_summary.txt.gz' before re-attempting parsing...\n");
            referenceDownloader();
            variant_summaryToObject();
        }
    }


    /**
     * Method receives variantRefHashMap to pass over to next function. Method receives filelocation from fileSelecter,
     * passes it on to fileToHashMap to receive HashMaps. Creates two overlapping parent files, passes them to
     * method diseaseSeeker. Errors restart this block of code.
     * parentComparer calls function fileSelecter to receive location of file to use.
     * parentComparer calls function fileToHashMap to receive filled HashMaps with variables.
     *
     * @param variantRefHashMap dirty way of giving passing over variable to another function
     */
    public static void parentComparer(HashMap<String, diseaseVariant> variantRefHashMap) {

        System.out.println("\nThis app will ONLY work with standard 23andMe files!!!");

        try {

            System.out.println("Please select the 23andMe file for parent 1:");
            File parent1 = new File(Objects.requireNonNull(fileSelecter()));
            String fileNameParent1 = parent1.getName();
            System.out.println("Selected file: " + fileNameParent1 + "\n");
            // Asking for an integer early to throw error in case of improper file
            String parent1ID = fileNameParent1.split("\\.")[0];


            System.out.println("Please select the 23andMe file for parent 2:");
            File parent2 = new File(Objects.requireNonNull(fileSelecter()));
            String fileNameParent2 = parent2.getName();
            System.out.println("Selected file: " + fileNameParent2 + "\n");
            // Asking for an integer early to throw error in case of improper file
            String parent2ID = fileNameParent2.split("\\.")[0];


            if (parent1.equals(parent2)) {
                System.out.println("You have selected the same file twice, please select two different files.");
                parentComparer(variantRefHashMap);
            } else {
                // Calls function fileToHashMap to get HashMaps with the filepath
                System.out.println("Analysing both files to find overlapping mutations...");
                HashMap<String, String[]> parent1HashMap =
                        Objects.requireNonNull(fileToHashMap(parent1.getAbsolutePath()));
                HashMap<String, String[]> parent2HashMap =
                        Objects.requireNonNull(fileToHashMap(parent2.getAbsolutePath()));

                // Making sure both parents have the same keySet
                parent1HashMap.keySet().retainAll(parent2HashMap.keySet());
                parent2HashMap.keySet().retainAll(parent1HashMap.keySet());


                if (parent1HashMap.isEmpty() && parent2HashMap.isEmpty()) {
                    System.out.println("No overlapping mutations have been found between both parents. " +
                            "\nHaving children is totally safe.");
                    System.exit(0);
                } else {
                    System.out.println("Ovelapping mutations have been found between both parents. " +
                            "\n" + parent1HashMap.size()
                            + " overlapping identifiers found between both parent files. " +
                            "\nStarting final phase of analysis.\n");
                    diseaseSeeker(parent1ID, parent2ID, variantRefHashMap, parent1HashMap, parent2HashMap);
                }
            }
        } catch (Exception e) {
            System.out.println("\n!!!ERROR!!!");
            System.out.println(e);
            System.out.println("Please close all files related to this app. If that's the case, " +
                    "please select the correct 23andMe files again." +
                    "\nIf this problem persists, please try a different 23andMe file, notify the developer" +
                    " and give him a good scare.\n");
            parentComparer(variantRefHashMap);
        }
    }

    /**
     * Function to place a 23andMe file into a HashMap. Returns HashMap with String identifier as key,
     * and String[Identifier, Chromosome, Position, Genotype]
     * BigO = N
     *
     * @param filepath; String with the absolute path of a file
     * @throws FileNotFoundException restarts file selection, as a file might have been moved or corrupted.
     * @returns HashMap<String, String [ ]> key: identifier, value: String [identifier, chromosome, position, genotype]
     */
    public static HashMap<String, String[]> fileToHashMap(String filepath) throws IOException {

        HashMap<String, String[]> parent = new HashMap<>();
        String line;
        String[] splitLine;

        try {
            Scanner filereader = new Scanner(new File(filepath));

            // While loop to loop through file
            while (filereader.hasNextLine()) {
                line = filereader.nextLine();

                // If the line doesn't start with #
                if (!line.startsWith("#")) {
                    splitLine = line.split("\t");

                    // RSID / IntID as key, while line as value
                    // splitLine[0]=Identifier; splitLine[1]=chromosome; splitLine[2]=position; splitLine[3]=genotype
                    parent.put(splitLine[0], splitLine);
                }
            }
            return parent;

        } catch (FileNotFoundException e) {
            System.out.println("Unexpected error: The selected file has not been found. " +
                    "The file might have been moved, re-named or been corrupted.\n" +
                    "Re-running program to give it another try.");
            referenceChecker();
        }
        return null;
    }

    /**
     * Function for the user to select a file, returns String with file location.
     * Stops program if filechooser is closed. Re-runs method fileSelecter if option is invalid.
     *
     * @return String fileloc (absolute file location of selected file)
     */
    public static String fileSelecter() {
        // File chooser for the user to select inputfiles
        JFileChooser fc = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());

        int returnValue = fc.showOpenDialog(null);

        // If selected file is approved, return the location
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fc.getSelectedFile();
            return selectedFile.getAbsolutePath();

            // If the filechooser is closed, stops the program.
        } else if (returnValue == JFileChooser.CANCEL_OPTION || returnValue == JFileChooser.ABORT) {
            System.out.println("File chooser closed, stopping program.");
            System.exit(0);

            // If the file in any other way is not accepted, restarts the filechooser to try again.
        } else {
            System.out.println("File not accepted, please try again.");
            fileSelecter();
        }
        return null;
    }

    /**
     * diseaseSeeker uses parentID's to check if a file exists, if it does, deletes it and creates a new file.
     * Takes parent1hashmap, iterates through each of the values, takes the chrnr, pos and genotype
     * to assemble the key for variantRefHashMap and checks if the assembled key is in variantRefHashMap.
     * If it exists, retrieves data from match and writes said data in the created file.
     * When finished, closes opens created file.
     * bigO = N
     *
     * @param parent1ID         String containing the ID of parent 1
     * @param parent2ID         String containing the ID of parent 2
     * @param variantRefHashMap HashMap containing all diseaseVariant objects as values
     *                          and chr+pos+refAlell+altAlell as key
     * @param parent1HashMap    HashMap containing RSID as key and String[RSID, chromosomenr, position, genotype]
     * @param parent2HashMap    HashMap containing RSID as key and String[RSID, chromosomenr, position, genotype]
     * @throws IOException      Read/Write error
     * @generates file parent1ID+"compared_with"+parent2ID+".txt"
     */
    public static void diseaseSeeker(String parent1ID, String parent2ID, HashMap variantRefHashMap,
                                     HashMap parent1HashMap, HashMap parent2HashMap) throws IOException {

        // If old file exists, removing said file before writing a new one.
        if (Files.exists(Path.of(parent1ID + "compared_with" + parent2ID + ".txt"))) {
            System.out.println("Other parent comparison file detected; \n" +
                    "Deleting old file...");
            Files.deleteIfExists(Path.of(parent1ID + "compared_with" + parent2ID + ".txt"));
        }

        System.out.println("Starting to write new file: '" + parent1ID + "compared_with" + parent2ID + ".txt" + "'...\n");
        FileWriter fileWriter = new FileWriter(parent1ID + "compared_with" + parent2ID + ".txt");
        fileWriter.write("#RSID\tNT Combination\tChromosome number\tNT parent1\tNT parent2\t"
                + "parent1ID" + "\t" + "parent2ID" + "\n");

        // parentData[0]=Identifier; parentData[1]=chromosome; parentData[2]=position; parentData[3]=genotype
        System.out.println("Searching for diseases...\n");

        // To loop though the parent1HashMap values
        for (Object o : parent1HashMap.entrySet()) {
            // To get rid of cast errors and nullpointerexceptions
            Map.Entry element = (Map.Entry) o;
            String[] parentData = (String[]) element.getValue();

            // To create a possible key and check if it's in variantRefHashMap
            String key = parentData[1] + parentData[2] + parentData[3];
            if (variantRefHashMap.containsKey((key))) {
                diseaseVariant match = (diseaseVariant) variantRefHashMap.get(key);

                // To retrieve parent2's data
                String[] parent2Data = (String[]) parent2HashMap.get(parentData[0]);

                // To write RSID + NT combo + chromosome nr. + parent1 NT + parent 2 NT + parent1ID + parent2ID
                fileWriter.write(parentData[0] + "\t" + (match.getRefAlelle() + match.getAltAlelle()) + "\t" +
                        match.getChromosome() + "\t" + parentData[3] + "\t" + parent2Data[3] + "\t" +
                        parent1ID + "\t" + parent2ID + "\n");
            }
        }

        fileWriter.close();
        System.out.println("File has been written. \nWould you like to sort the file by chromosome number?\n" +
                "Type 'Y' or 'Yes' and press 'Enter' to sort, otherwise, type 'N' or 'No' and press 'Enter' : ");

        //Creating scanner to read user reply to program.
        Scanner scan = new Scanner(System.in);
        String input = scan.next();
        scan.close();

        String filename = parent1ID + "compared_with" + parent2ID + ".txt";

        if (input.toUpperCase().equals("Y") || input.toUpperCase().equals("YES")) {
            fileSorter(filename);
        } else {
            System.out.println("No sorting selected, opening created file.");
            Desktop.getDesktop().open(new File(filename));
        }
    }


    /**
     * Function to sort file generated by parentDiseaseFinder. Needs the name of the file to read and sort.
     * Reads the file, places contents in an arraylist, sorts said arraylist, deletes old file and places arraylist
     * contents into a new file.
     *
     * bigO = N
     * @Throws IOException Ambiguous Read/Write error
     * @param filename name of the file which needs to be sorted
     */
    public static void fileSorter(String filename) throws IOException {

        System.out.println("Starting file conversion...");
        ArrayList<String[]> SortArray = new ArrayList<>();

        try {
            Scanner filereader = new Scanner(new File(filename));

            // Saving first line for writing later
            String firstline = filereader.nextLine();

            // While loop to loop through file
            while (filereader.hasNextLine()) {
                String line = filereader.nextLine();
                SortArray.add(line.split("\t"));
            }
            filereader.close();

            // Sorting happens here, all the numbers get put together
            SortArray.sort(Comparator.comparing(o -> o[2]));

            System.out.println("Deleting unsorted file and re-writing sorted one...");
            Files.deleteIfExists(Path.of(filename));
            FileWriter FileWriter = new FileWriter(filename);
            FileWriter.write(firstline + "\n");

            // For loop to iteratively write the file again
            for (String[] data : SortArray) {
                FileWriter.write(data[0] + "\t" + data[1] + "\t" + data[2] + "\t" + data[3] +
                        "\t" + data[4] + "\t" + data[5] + "\t" + data[6] + "\n");
            }
            FileWriter.close();

            System.out.println("File has been sorted. Opening file now...");
            // To open the file after it has been written and ending the program.
            Desktop.getDesktop().open(new File(filename));
            System.exit(0);

        } catch (FileNotFoundException exception) {
            System.out.println("File has possibly been moved, restarting programme to ensure nothing is corrupt.\n");
            referenceChecker();
        }
    }
}


/**
 * Class diseaseVariant to for variant_summary data input, so each line inserted is an object.
 * Implemented compareTo method to order chromosomes.
 * Implemented toString method to receive string when called.
 */
class diseaseVariant implements Comparable<diseaseVariant> {

    private int alelleID;
    private String type;
    private int position;
    private int pathogenicity;
    private int geneID;
    private String refAlelle;
    private String altAlelle;
    private String disease;
    private String chromosome;

    // Getters & Setters for all the variables
    public int getAlelleID() {
        return alelleID;
    }

    public void setAlelleID(int alelleID) {
        this.alelleID = alelleID;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public int getPathogenicity() {
        return pathogenicity;
    }

    public void setPathogenicity(int pathogenicity) {
        this.pathogenicity = pathogenicity;
    }

    public int getGeneID() {
        return geneID;
    }

    public void setGeneID(int geneID) {
        this.geneID = geneID;
    }

    public String getRefAlelle() {
        return refAlelle;
    }

    public void setRefAlelle(String refAlelle) {
        this.refAlelle = refAlelle;
    }

    public String getAltAlelle() {
        return altAlelle;
    }

    public void setAltAlelle(String altAlelle) {
        this.altAlelle = altAlelle;
    }

    public String getDisease() {
        return disease;
    }

    public void setDisease(String disease) {
        this.disease = disease;
    }

    public String getChromosome() {
        return chromosome;
    }

    public void setChromosome(String chromosome) {
        this.chromosome = chromosome;
    }

    // compareTo method to order chromosomes
    public int compareTo(diseaseVariant d) {
        return this.getChromosome().compareTo(((diseaseVariant) d).getChromosome());
    }

    public String toString() {
        return type + " mutation on alelle " + alelleID + ", gene " + geneID + ", chromosome " +
                chromosome + " position " + position + " where nucleobase " + refAlelle + " is replaced with " +
                altAlelle + ", which may cause disease : " + disease + " with a pathogenicity score of " +
                pathogenicity + ", where 0 means benign and 1 means pathogenic.";
    }

    public diseaseVariant(int alelleID, String type, int position, int pathogenicity, int geneID,
                          String refAlelle, String altAlelle, String disease, String chromosome) {

        this.alelleID = alelleID;
        this.type = type;
        this.position = position;
        this.pathogenicity = pathogenicity;
        this.geneID = geneID;
        this.refAlelle = refAlelle;
        this.altAlelle = altAlelle;
        this.disease = disease;
        this.chromosome = chromosome;
    }
}
