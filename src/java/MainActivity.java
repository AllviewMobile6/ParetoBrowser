package pareto.browser;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.ProgressBar;

public class MainActivity extends AppCompatActivity {
    private String TAG = "PARETOLOG";
    private Activity thisActivity = this;
    private Globals globals = new Globals();

    private void specialVisiblePage(String title, String url) {
        // open asset page
        globals.uriEditText.setText(title);
        globals.visibleBrowser.webView.clearCache(true);
        globals.visibleBrowser.webView.setVisibility(View.VISIBLE);
        globals.hiddenBrowser.webView.setVisibility(View.INVISIBLE);
        globals.visibleBrowser.webView.loadUrl(url);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // utils class
        globals.mainActivity = this;
        globals.utils = new Utils(this);

        // history
        globals.history = new History(globals);

        // adblock
        globals.adblock = new Adblock(globals);

        // storage
        globals.storage = new Storage(globals);

        // visible browser
        globals.visibleBrowser = new VisibleBrowser(
                globals,
                (WebView)findViewById(R.id.webViewVisible),
                (ProgressBar)findViewById(R.id.progressBar)
        );

        // hidden browser
        globals.hiddenBrowser = new HiddenBrowser(
                globals,
                (WebView)findViewById(R.id.webViewHidden),
                (ProgressBar)findViewById(R.id.progressBar),
                (EditText)findViewById(R.id.uriEditText)
        );

        // url
        EditText uriEditText = (EditText) findViewById(R.id.uriEditText);
        globals.uriEditText = uriEditText;

        // open button
        Button openButton = (Button) findViewById(R.id.openButton);
        openButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String url = String.valueOf(uriEditText.getText()).trim();
                uriEditText.setText(url);
                if (url.equals("Bookmarks")) {
                    globals.visibleBrowser.webView.loadUrl("file:///android_asset/bookmarks.html");
                    return;
                }
                if (url.equals("Console")) {
                    globals.visibleBrowser.webView.loadUrl("file:///android_asset/console.html");
                    return;
                }
                if (url.equals("Updates")) {
                    globals.visibleBrowser.webView.loadUrl("file:///android_asset/check_for_updates.html");
                    return;
                }
                // prepend missing protocol
                if (!url.startsWith("http://") && !url.startsWith("https://") && !url.startsWith("file://")) {
                    url = "http://" + url;
                }
                globals.visibleBrowser.js.setUrl(url);
                // if user is opening same url, clear cache
                if (globals.hiddenBrowser.lastStartedPage.equals(url)) {
                    globals.hiddenBrowser.webView.clearCache(true);
                }
                if (!globals.visibleBrowser.webView.getUrl().equals("file:///android_asset/asset_index.html")) {
                    globals.visibleBrowser.webView.loadUrl("file:///android_asset/asset_index.html");
                }
                globals.visibleBrowser.webView.evaluateJavascript("Pareto.pageStarted()", null);
                globals.hiddenBrowser.webView.loadUrl(url);
                globals.visibleBrowser.webView.requestFocus();
                globals.history.push(url);
            }
        });

        // menu button
        Button menuButton = (Button) findViewById(R.id.menuButton);
        menuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu popup = new PopupMenu(thisActivity, view);
                MenuInflater inflater = popup.getMenuInflater();
                inflater.inflate(R.menu.menu, popup.getMenu());

                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.bookmarkThis:
                                globals.visibleBrowser.webView.evaluateJavascript("Pareto.bookmarkThis()", null);
                                return true;
                            case R.id.bookmarks:
                                specialVisiblePage("Bookmarks", "file:///android_asset/bookmarks.html");
                                return true;
                            case R.id.flip:
                                // Show/Hide hidden browser
                                if (globals.visibleBrowser.webView.getVisibility() == View.VISIBLE) {
                                    globals.visibleBrowser.webView.setVisibility(View.INVISIBLE);
                                    globals.hiddenBrowser.webView.setVisibility(View.VISIBLE);
                                } else {
                                    globals.visibleBrowser.webView.setVisibility(View.VISIBLE);
                                    globals.hiddenBrowser.webView.setVisibility(View.INVISIBLE);
                                }
                                return true;
                            case R.id.console:
                                specialVisiblePage("Console", "file:///android_asset/console.html");
                                return true;
                            case R.id.update:
                                specialVisiblePage("Updates", "file:///android_asset/check_for_updates.html");
                                return true;
                        }
                        return false;
                    };
                });
                popup.show();
            }
        });

        uriEditText.setText("Bookmarks");
        globals.visibleBrowser.webView.loadUrl("file:///android_asset/bookmarks.html");
    }

    @Override
    public void onBackPressed() {
        //Log.d(TAG, "onBackPressed: history=" + globals.visibleBrowser.js.debugHistory());
        String cur = globals.history.pop();

        // if current visible is not index
        if (!globals.visibleBrowser.webView.getUrl().equals("file:///android_asset/asset_index.html")) {
            // show cur
            Log.d(TAG, "onBackPressed v1: url=" + cur);
            globals.visibleBrowser.webView.loadUrl("file:///android_asset/asset_index.html");
            globals.visibleBrowser.js.setUrl(cur);
            globals.hiddenBrowser.webView.loadUrl(cur);
            globals.history.push(cur);
        }

        // we are on index so go to previous page
        String prev = globals.history.pop();
        if (prev != null && !prev.isEmpty()) {
            Log.d(TAG, "onBackPressed v2: url=" + prev);
            globals.visibleBrowser.js.setUrl(prev);
            globals.hiddenBrowser.webView.loadUrl(prev);
            globals.history.push(prev);
        }
    }
}