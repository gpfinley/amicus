package edu.umn.amicus.internal.coreference;

import edu.umn.amicus.AmicusException;

import java.io.*;
import java.nio.file.Paths;
import java.util.Scanner;
import java.util.logging.Logger;

/**
 * Utility script to convert plaintext extracted annotations by this program to the .con format used in the 2011
 *      i2b2 challenge.
 * Takes three args:
 *      path to original text data (files ending in .txt);
 *      path to data with extracted annotations (files will end in _annotations.txt);
 *      output path for processed files (will end in .txt.con).
 *
 * Created by gpfinley on 12/12/16.
 */
public class ExtractedToCorefConcepts {

    private static final Logger LOGGER = Logger.getLogger(ExtractedToCorefConcepts.class.getName());

    public static void main(String[] args) throws IOException {
        String origDataPath = args[0];
        String extractedTextPath = args[1];
        String outPath = args[2];

        // allow annotations that don't line up exactly with word boundaries? Probably should (there are lots)
        boolean allowPartialWords = false;

        final int midword = -1;

        File originalDataDir = Paths.get(origDataPath).toFile();
        if (!originalDataDir.exists() || !originalDataDir.isDirectory()) {
            throw new AmicusException("Bad data directory at %s", originalDataDir.getAbsolutePath());
        }

        for (File origFile : Paths.get(origDataPath).toFile().listFiles()) {
            if (origFile.getName().startsWith(".")) continue;
            String origName = origFile.getName();
            String origText = new Scanner(origFile).useDelimiter("\\Z").next();
            char[] chars = origText.toCharArray();
            int[] lineThisChar = new int[chars.length];
            int[] wordThisChar = new int[chars.length];
            lineThisChar[0] = 1;
            int curline = 1;
            int curword = 0;
            for (int i=1; i<chars.length; i++) {
                if (chars[i-1] == '\n') {
                    curline++;
                    curword = 0;
                }
                if (chars[i-1] == ' ') {
                    curword++;
                }
                lineThisChar[i] = curline;
                wordThisChar[i] = (allowPartialWords || chars[i] == ' ' || chars[i-1] == ' ' || chars[i-1] == '\n' || chars[i] == '\n') ? curword : midword;
            }

            String extractedName = origName.substring(0, origName.length() - 4) + "_annotations.txt";
            File extractedFile = Paths.get(extractedTextPath).resolve(extractedName).toFile();
            File outDir = Paths.get(outPath).toFile();
            if (!outDir.exists() && !outDir.mkdirs()) {
                throw new AmicusException("Could not create output directory at " + outPath);
            }
            File conFile = outDir.toPath().resolve(origName + ".con").toFile();
            BufferedReader reader = new BufferedReader(new FileReader(extractedFile));
            BufferedWriter writer = new BufferedWriter(new FileWriter(conFile));
            String line;
            while ((line = reader.readLine()) != null) {
                String[] fields = line.split("\\t");
                String type = fields[0];
                int begin = Integer.parseInt(fields[1]);
                int end = Integer.parseInt(fields[2]);
                if (wordThisChar[begin] != midword && wordThisChar[end] != midword) {
                    writer.write(String.format("c=\"%s\" %d:%d %d:%d||t=\"%s\"\n", origText.substring(begin, end),
                            lineThisChar[begin], wordThisChar[begin], lineThisChar[end], wordThisChar[end], type));
                }
            }
            reader.close();
            writer.close();
        }
    }
}
