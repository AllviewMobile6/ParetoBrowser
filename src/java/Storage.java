package pareto.browser;

import android.util.Log;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.ProgressBar;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class Storage {
    private String TAG = "PARETOLOG";
    private Globals globals;
    private String base_dir;

    public Storage(Globals globals) {
        this.globals = globals;
        base_dir = globals.mainActivity.getFilesDir().getAbsolutePath();
    }

    private String sanitizeFilename(final String filename) {
        // Return true if filename is valid (has only allowed characters)
        String s = filename.replaceAll("[^a-zA-Z0-9_\\.\\-]", "_");
        s = s.replaceAll("^[\\.]+", "_");
        s = s.replaceAll("[\\.]{2}", "__");
        return s;
    }

    private boolean validFilename(final String filename) {
        // Return true if filename is valid (has only allowed characters)
        return sanitizeFilename(filename).equals(filename);
    }

    private String sanitizePath(final String path) {
        // Return true if pathname is valid (has only allowed characters)
        String s = path.replaceAll("[^a-zA-Z0-9_\\/\\.\\-]", "_");
        s = s.replaceAll("^[\\.]+", "_");
        s = s.replaceAll("[\\.]{2}", "__");
        return s;
    }

    private boolean validPath(final String path) {
        // Return true if pathname is valid (has only allowed characters)
        return sanitizePath(path).equals(path);
    }

    public boolean mkdir(final String path) {
        // Create directory
        if (!validPath(path)) {
            Log.e(TAG, "mkdir: invalid path " + path);
            return false;
        }
        File f = new File(base_dir, sanitizePath(path));
        return f.mkdirs();
    }

    public boolean write(final String path, final String data) {
        // Write data to file
        if (!validPath(path)) {
            Log.e(TAG, "write: invalid path " + path);
            return false;
        }
        File f = new File(base_dir, sanitizePath(path));
        try {
            FileWriter writer = new FileWriter(f);
            writer.append(data);
            writer.flush();
            writer.close();
            return true;
        } catch (FileNotFoundException e) {
            Log.e(TAG, "write error #1: " + path + " - " + e.getMessage());
            return false;
        } catch (IOException e) {
            Log.e(TAG, "write error #2: " + path + " - " + e.getMessage());
            return false;
        }
    }

    public String read(final String path) {
        // Read data from file
        String s = null;
        if (!validPath(path)) {
            Log.e(TAG, "internalRead: invalid path " + path);
            return null; // undefined in js
        }
        try {
            File file = new File(base_dir, sanitizePath(path));
            if (!file.exists()) {
                return null; // undefined in js
            }
            long length = file.length();
            if (length == 0) {
                return "";
            }
            try (FileReader in = new FileReader(file)) {
                char[] content = new char[(int) length];
                int numRead = in.read(content);
                s = new String(content, 0, numRead);
            } catch (Exception ex) {
                Log.e(TAG, "internalRead: read error " + path + " " + ex.getMessage());
                return null; // undefined in js
            }
        } catch (Exception e) {
            Log.e(TAG, "internalRead: read error " + path + " " + e.getMessage());
            return null; // undefined in js
        }
        return s;
    }
}
