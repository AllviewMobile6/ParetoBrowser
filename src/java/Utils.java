package pareto.browser;

import android.app.Activity;
import android.util.Log;
import android.webkit.ValueCallback;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.ProgressBar;

import java.io.IOException;
import java.io.InputStream;

public class Utils {
    private static String TAG = "PARETOLOG";
    public Activity activity;

    public Utils(Activity activity) {
        this.activity = activity;
    }

    public String asset(String filename) {
        // read content of a file into string
        String s = "";
        try {
            InputStream stream = activity.getAssets().open(filename);
            int size = stream.available();
            byte[] buffer = new byte[size];
            stream.read(buffer);
            stream.close();
            s = new String(buffer);
        } catch (IOException e) {
            Log.e(TAG, "asset error: " + filename + " - " + e.getMessage());
        }
        return s;
    }
}
