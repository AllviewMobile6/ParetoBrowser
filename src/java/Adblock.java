package pareto.browser;

/*
List of blocked domains (in android_asset/blacklist_domains.txt)
*/

import android.util.Log;

import java.util.HashMap;
import java.util.Map;

public class Adblock {
    private String TAG = "PARETOLOG";
    private Globals globals;
    public Map<String, Boolean> ads = new HashMap<String, Boolean>();

    public Adblock(Globals globals) {
        this.globals = globals;
        // load domains blacklist
        String blacklist[] = globals.utils.asset("blacklist_domains.txt").split("\\r?\\n");
        int i;
        for (i = 0; i < blacklist.length; i++) {
            ads.put(blacklist[i], true);
        }
    }

    public boolean allowedDomain(final String domain) {
        boolean allowed = !ads.containsKey(domain);
        if (!allowed) {
            Log.d(TAG, "blocked domain: " + domain);
        } else {
            Log.d(TAG, "allowed domain: " + domain);
        }
        return allowed;
    }
}
