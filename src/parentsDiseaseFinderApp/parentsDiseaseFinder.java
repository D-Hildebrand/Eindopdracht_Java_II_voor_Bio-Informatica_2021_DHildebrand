package parentsDiseaseFinderApp;

import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import java.io.*;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.zip.GZIPInputStream;


public class parentsDiseaseFinder {

    public static void main(String[] args) throws IOException {
//        referenceChecker();
        parentSelecter();
    }

    /**
     * Function to download 'variant_summary.txt.gz.md5' reference MD5 file from NCBI.
     * Makes system prints to tell user what the program is doing.
     *
     * @throws IOException
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
     * @throws IOException
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
     * @throws IOException
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
            // Prompts user to not finding one or both files and starts download of MD5 and reference files.
            System.out.println("One or both reference files not found, commencing download of reference files " +
                    "'variant_summary.txt.gz.md5' and 'variant_summary.txt.gz'.\n");
            referenceMD5Downloader();
            referenceDownloader();
            System.out.println("Finished downloads.\n");
            variant_summaryToObject();
        }
    }

    /**
     * Function to place variant_summary data into a list.
     * bigO: N
     *
     * @throws IOException
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

            // Creating ArrayList for variant data
            ArrayList<diseaseVariant> variantRefArray = new ArrayList<>();
            String line;

            // Dirty but effective way of removing first line
            String firstline = in.readLine();
            firstline = null;

            // To parse through the entire variant_summary file
            while ((line = in.readLine()) != null) {
                String[] temp = line.split("\t");

                // To add required data from variant_summary.txt.gz into objects
                variantRefArray.add(new diseaseVariant(Integer.parseInt(temp[0]), temp[1], Integer.parseInt(temp[31]),
                        Integer.parseInt(temp[7]), Integer.parseInt(temp[3]), temp[32], temp[33], temp[13], temp[18]));
            }
            System.out.println("Finished creating reference data.\n");

            // For sorting
            System.out.println("Sorting reference data by chromosome...");
            Collections.sort(variantRefArray);
            System.out.println("Finished sorting reference data.\n");

            // To test & see how Collections.sort works
//            for (diseaseVariant var : variantRefArray){
//                System.out.println(var.getChromosome());
//            }


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


    public static void parentSelecter() {

        System.out.println("\nThis app will ONLY work with 23andMe files!!!");

        try {

            System.out.println("Please select the 23andMe file for parent 1:");
            File parent1 = new File(Objects.requireNonNull(fileSelecter()));
            String idParent1 = parent1.getName();
            String parent1Path = parent1.getAbsolutePath();
            System.out.println("Selected file: " + idParent1 + "\n");
            int iden1 = Integer.parseInt(idParent1.split("\\.")[0]);


            System.out.println("Please select the 23andMe file for parent 2:");
            File parent2 = new File(Objects.requireNonNull(fileSelecter()));
            String idParent2 = parent2.getName();
            String parent2Path = parent2.getAbsolutePath();
            System.out.println("Selected file: " + idParent2 + "\n");
            int iden2 = Integer.parseInt(idParent2.split("\\.")[0]);

        } catch (Exception exception) {
            System.out.println("\n!!!ERROR!!!");
            System.out.println(exception);
            System.out.println("Selected file is likely not an 23andMe file. Please select the files again." +
                    "\nIf this problem persists, please select a different 23andMe file and notify the developer.\n");
            parentSelecter();
        }
    }

    public static HashMap<String, String[]> fileToHashMap(String filepath) throws FileNotFoundException {

        HashMap<String, String[]> parent = new HashMap<>();
//        File inputfile = new File(filepath);
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
                    "The file might have been moved.\n" +
                    "Please try selecting files again.");
            parentSelecter();
        }
        return null;
    }

    /**
     * Function for the user to select a file, returns String with file location.
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
            String fileloc = selectedFile.getAbsolutePath();
            return fileloc;

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


}


/**
 * Class diseaseVariant to for variant_summary data input, so each line inserted is an object.
 * Implemented compareTo method to order chromosomes.
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