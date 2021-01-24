package parentsDiseaseFinderApp;

import java.io.*;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;
import java.util.zip.GZIPInputStream;


public class parentsDiseaseFinder {

    public static void main(String[] args) throws IOException {
        referenceChecker();
    }

    /**
     * Function to download reference MD5 file from NCBI.
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
     * Function to download reference file from NCBI.
     *
     * @throws IOException
     */
    public static void referenceDownloader() throws IOException {

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

        System.out.println("Checking local 'variant_summary.txt.gz.md5' vs online version...\n");
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
                // Deletes old reference file before downloading new one
                Files.deleteIfExists(Path.of("variant_summary.txt.gz"));
                referenceDownloader();
            } else {
                System.out.println("File 'variant_summary.txt.gz.md5' matches, files up to date.\n");
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

    public static void variant_summaryToObject() throws IOException {
        try {

            //
            System.out.println("Reading creating reference variable...");
            BufferedReader in = new BufferedReader
                    (new InputStreamReader
                            (new GZIPInputStream
                                    (new FileInputStream("variant_summary.txt.gz"))));

            // Creating ArrayList for variant data
            ArrayList<diseaseVariant> variantRefArray = new ArrayList<>();
            String line;

            // To parse through the entire variant_summary file
            while ((line = in.readLine()) != null) {
                String[] temp = line.split("\t");

                variantRefArray.add(new diseaseVariant(Integer.parseInt(temp[0]), temp[1], Integer.parseInt(temp[31]),
                        Integer.parseInt(temp[7]), Integer.parseInt(temp[3]), temp[32], temp[33], temp[13], temp[18]));
            }
            System.out.println("Finished creating reference variable.\n");

            // For sorting
            System.out.println("Sorting reference data by chromosome...");
            Collections.sort(variantRefArray);
            System.out.println("Finished sorting reference data.\n");


        } catch (FileNotFoundException exception) {
            System.out.println("Something went wrong finding file 'variant_summary.txt.gz', " +
                    "restarting checking phase now.");
            referenceChecker();
        }
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