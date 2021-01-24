package parentsDiseaseFinderApp;

import java.io.*;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.zip.GZIPInputStream;


public class parentsDiseaseFinder {

    public static void main(String[] args) throws IOException {
        referenceChecker();
    }

    /**
     * Function to download reference MD5 file from NCBI.
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

            // If the downloaded hash isn't the same as the local hash, overwrite old reference with new reference
            if (!MD5local.equals(MD5download)){
                System.out.println("Local reference file is not the latest variant. " +
                        "Commencing download of most recent variant.\n");
                referenceDownloader();
            } else {
                System.out.println("File 'variant_summary.txt.gz.md5' matches, files up to date.\n");
                variant_summaryToObject();
            }


        } catch (FileNotFoundException exception) {
            System.out.println("One or both reference files not found, commencing download of reference files " +
                    "'variant_summary.txt.gz.md5' and 'variant_summary.txt.gz'" +
                    "Downloading 'variant_summary.txt.gz.md5'...\n");
            referenceMD5Downloader();
            System.out.println("Downloaded 'variant_summary.txt.gz.md5', downloading 'variant_summary.txt.gz'...\n");
            referenceDownloader();
            System.out.println("Downloaded 'variant_summary.txt.gz'");
            variant_summaryToObject();
        }



    }
    public static void variant_summaryToObject() throws IOException {
        try {
            InputStream fileStream = new FileInputStream("variant_summary.txt.gz");
            InputStream gzStream = new GZIPInputStream(fileStream);
            Reader decoder = new InputStreamReader(gzStream, StandardCharsets.UTF_16LE);
            BufferedReader buffered = new BufferedReader(decoder);

            Scanner reader = new Scanner(buffered);

            while (reader.hasNextLine()) {
                String line = reader.nextLine();
                System.out.println(line);
            }




        } catch (FileNotFoundException exception){
            System.out.println("Something went wrong finding file 'variant_summary.txt.gz', " +
                    "restarting checking phase now.");
            referenceChecker();
        }
    }

}
