package sk.stuba.fei.indoorlocator.Helper;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by Martin on 24.10.2016.
 */

public class FileManager {
    public static void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
    }
}
