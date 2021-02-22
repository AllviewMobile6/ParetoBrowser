package pareto.browser;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class History {
    private String TAG = "PARETOLOG";
    private Globals globals;
    public List<String> pages = new ArrayList<String>();

    public History(Globals globals) {
        this.globals = globals;
    }

    public void push(final String url) {
        if (pages.size() == 0) {
            pages.add(0, url);
            return;
        }
        String cur = pages.get(0);
        if (!cur.equals(url)) {
            Log.d(TAG, "History pushed " + url);
            pages.add(0, url);
        }
    }

    public String pop() {
        if (pages.size() == 0) {
            return "";
        }
        String url = pages.remove(0);
        Log.d(TAG, "History popped " + url);
        return url;
    }
}
