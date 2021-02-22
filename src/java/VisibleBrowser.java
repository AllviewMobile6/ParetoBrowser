package pareto.browser;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.webkit.ConsoleMessage;
import android.webkit.SslErrorHandler;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebStorage;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

public class VisibleBrowser {
    private String TAG = "PARETOLOG";
    private Globals globals;
    public ProgressBar progressBar;
    public WebView webView;
    public WebSettings webSettings;
    public JsInterface js;
    public String lastStartedPage = "";
    public String lastStartedHost = "";
    public String lastFinishedPage = "";
    public String data;
    public String url = "";
    public List<String> consMessages = new ArrayList<String>();

    public void renderData(final String data) {
        this.data = data;
        this.webView.evaluateJavascript("Pareto.renderData(Pareto.getData());", null);
    }

    public void chooseParser(final String url) {
        // Call "choose_parser.js" to decide which parser for this url to use
        Log.d(TAG, "chooseParser: url=" + url);
        String code = globals.storage.read("choose_parser.js");
        if (code == null || code.isEmpty()) {
            code = globals.utils.asset("choose_parser.js");
            Log.w(TAG, "using asset choose_parser");
        }
        Log.d(TAG, "chooseParser: code=" + code.length());
        webView.evaluateJavascript(code, new ValueCallback<String>() {
            @Override
            public void onReceiveValue(String s) {
                String unquoted = s.substring(1, s.length() - 1);
                Log.w(TAG, "chooseParser onReceiveValue: s=" + s + " u=" + unquoted);
                globals.hiddenBrowser.parser(unquoted);
            }
        });
    }

    public class JsInterface {

        @android.webkit.JavascriptInterface
        public void hideProgressBar() {
            globals.mainActivity.runOnUiThread(new Runnable() {
                public void run() {
                    progressBar.setVisibility(View.GONE);
                }
            });
        }

        @android.webkit.JavascriptInterface
        public void hiddenLoadUrl(final String url) {
            globals.mainActivity.runOnUiThread(new Runnable() {
                public void run() {
                    webView.evaluateJavascript("Pareto.pageStarted()", null);
                    globals.hiddenBrowser.webView.loadUrl(url);
                    globals.history.push(url);
                }
            });
        }

        @android.webkit.JavascriptInterface
        public void visibleLoadUrl(final String url) {
            globals.mainActivity.runOnUiThread(new Runnable() {
                public void run() {
                    webView.loadUrl(url);
                    //globals.history.push(url);
                }
            });
        }

        @android.webkit.JavascriptInterface
        public String getData() {
            return data;
        }

        @android.webkit.JavascriptInterface
        public String getUrl() {
            return url;
        }

        @android.webkit.JavascriptInterface
        public void setUrl(final String value) {
            url = value;
        }

        @android.webkit.JavascriptInterface
        public String storageRead(final String path) {
            return globals.storage.read(path);
        }

        @android.webkit.JavascriptInterface
        public boolean storageWrite(final String path, final String data) {
            return globals.storage.write(path, data);
        }

        @android.webkit.JavascriptInterface
        public boolean storageMkDir(final String path) {
            return globals.storage.mkdir(path);
        }

        @android.webkit.JavascriptInterface
        public String consoleData() {
            String s = TextUtils.join("\n", consMessages);
            consMessages.clear();
            return s;
        }

        @android.webkit.JavascriptInterface
        public String consoleDataHidden() {
            String s = TextUtils.join("\n", globals.hiddenBrowser.consMessages);
            globals.hiddenBrowser.consMessages.clear();
            return s;
        }

        private String bin2hex(byte[] data) {
            return String.format("%0" + (data.length*2) + "X", new BigInteger(1, data));
        }

        @android.webkit.JavascriptInterface
        public String sha256(final String text) {
            byte[] h;
            MessageDigest digest = null;
            try {
                digest = MessageDigest.getInstance("SHA-256");
            } catch (NoSuchAlgorithmException e) {
                Log.e(TAG, "sha256: " + e.getMessage());
                return "";
            }
            digest.reset();
            return bin2hex(digest.digest(text.getBytes()));
        }

        @android.webkit.JavascriptInterface
        public String debugHistory() {
            Log.d(TAG, "debugHistory: size=" + globals.history.pages.size());
            String s = "";
            int i;
            for (i = 0; i < globals.history.pages.size(); i++) {
                Log.d(TAG, "debugHistory: #" + i + " = " + globals.history.pages.get(i));
                s += "#" + i + " = " + globals.history.pages.get(i) + "\n";
            }
            return s;
        }
    }

    public VisibleBrowser(Globals globals, WebView webView, ProgressBar progressBar) {
        this.globals = globals;
        this.progressBar = progressBar;
        this.webView = webView;
        webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        try {
            webSettings.setSupportZoom(true);
            webSettings.setDisplayZoomControls(false);
            webSettings.setBuiltInZoomControls(true);
        } catch (Exception e) {
            Log.e(TAG, "VisibleBrowser: zoom error - " + e.getMessage());
        }
        webSettings.setMediaPlaybackRequiresUserGesture(false);

        js = new JsInterface();
        webView.addJavascriptInterface(js, "Pareto");

        // set webview client

        webView.setWebViewClient(new WebViewClient() {

            public WebResourceResponse shouldInterceptRequest(final WebView view, WebResourceRequest request) {
                // adblocker
                try {
                    Uri uri = request.getUrl();
                    String url = uri.toString();
                    String host = uri.getHost();

                    Log.d(TAG, "shouldInterceptRequest: ifmf=" + request.isForMainFrame());

                    // Important: block opening non-asset (and non-local net) pages in visible browser because it has access
                    // to internal storage and other stuff. non-asset pages can only be opened in hidden browser
                    if (request.isForMainFrame() && !host.startsWith("192.168.0.")) {
                        Log.d(TAG, "shouldInterceptRequest: blocked url=" + url);
                        return new WebResourceResponse("text/html", "UTF-8", 403, "Forbidden", null, null);
                    }
                    if (!lastStartedHost.isEmpty() && !lastStartedHost.startsWith("192.168.0.")) {
                        Log.d(TAG, "shouldInterceptRequest: blocked2 host=" + lastStartedHost + " url=" + url);
                        return new WebResourceResponse("text/html", "UTF-8", 403, "Forbidden", null, null);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "shouldInterceptRequest: " + e.getMessage());
                }
                // allowed
                return super.shouldInterceptRequest(view, request);
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                Log.d(TAG, "Visible onPageStarted: " + url);
                lastStartedPage = url;
                lastStartedHost = "";
                // only asset pages are allowed
                try {
                    URL u = new URL(url);
                    lastStartedHost = u.getHost();
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                    return;
                }

                if (url.startsWith("tel:")) {
                    return;
                }
                if (url.startsWith("mailto:")) {
                    return;
                }

                super.onPageStarted(view, url, favicon);
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                Log.d(TAG, "onReceivedError " + errorCode + " description=" + description + " url=" + failingUrl);
                if (failingUrl.startsWith("intent:")) {
                    return;
                }
                if (failingUrl.startsWith("market:")) {
                    return;
                }
                if (failingUrl.startsWith("tel:") || failingUrl.startsWith("mailto:")) {
                    return;
                }
                if (description.equals("net::ERR_FAILED")) {
                    return;
                }
                Log.e(TAG, "onReceivedError: " + description);
            }

            @Override
            public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
                try {
                    Log.e(TAG, "onReceivedHttpError " + request.getUrl().toString());
                } catch (Exception e) {
                }
                super.onReceivedHttpError(view, request, errorResponse);
            }

            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                super.onReceivedSslError(view, handler, error);
            }

            @Override
            public void onLoadResource(WebView view, String url) {
                Log.d(TAG, "onLoadResource " + url);
                super.onLoadResource(view, url);
            }

            @Override
            public void onPageFinished(WebView view, final String url) {
                lastFinishedPage = url;
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                Log.w(TAG, "shouldOverrideUrlLoading " + request.getUrl());
                return super.shouldOverrideUrlLoading(view, request);
            }
        });

        // set web chrome client
        webView.setWebChromeClient(new WebChromeClient() {

            @Override
            public void onShowCustomView(View view, CustomViewCallback callback) {
                super.onShowCustomView(view, callback);
            }

            @Override
            public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                try {
                    Log.d(TAG, consoleMessage.messageLevel().toString() + ": " + consoleMessage.sourceId() + " - " + consoleMessage.lineNumber() + ": " + consoleMessage.message());
                    consMessages.add(consoleMessage.messageLevel().toString() + ": " + consoleMessage.sourceId() + " - " + consoleMessage.lineNumber() + ": " + consoleMessage.message());
                    if (consMessages.size() > 1000) {
                        consMessages.remove(0);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "onConsoleMessage: " + e.getMessage());
                }
                return super.onConsoleMessage(consoleMessage);
            }

            @Override
            public void onProgressChanged(WebView view, int progress) {
                // update progress bar
                if (progress >= 100) {
                    progressBar.setVisibility(View.GONE);
                } else {
                    progressBar.setVisibility(View.VISIBLE);
                    progressBar.setProgress(progress);
                }
            }

        });
    }
}
