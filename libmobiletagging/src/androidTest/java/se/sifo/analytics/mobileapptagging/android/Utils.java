package se.sifo.analytics.mobileapptagging.android;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class Utils {

    static void convertStreamToFile(InputStream is, File file) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        BufferedWriter fileWriter = new BufferedWriter(new FileWriter(file));
        String line;
        while ((line = reader.readLine()) != null) {
            fileWriter.write(line + "\n");
        }
        fileWriter.flush();
        fileWriter.close();
        is.close();
    }
}
