package pareto.browser;

/*
HiddenBrowser class implements hidden webview browser that is navigating pages in the background
and then runs javascript code in those pages that extract data needed to render in actual pareto
browser (visible browser).
*/

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.util.Log;
import android.view.View;
import android.webkit.ConsoleMessage;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.SslErrorHandler;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebStorage;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class HiddenBrowser {
    private String TAG = "PARETOLOG";
    private Globals globals;
    public ProgressBar progressBar;
    public WebView webView;
    public WebSettings webSettings;
    public EditText uriEditText;
    public String lastStartedPage = "";
    public String lastFinishedPage = "";
    public List<String> consMessages = new ArrayList<String>();

    public void parser(String parser) {
        // Run parser and send data back to visible browser
        String code = globals.storage.read("parser/" + parser + ".js");
        if ((code == null) || code.isEmpty()) {
            code = globals.utils.asset("parser/" + parser + ".js");
        }
        webView.evaluateJavascript(code, new ValueCallback<String>() {
            @Override
            public void onReceiveValue(String s) {
                Log.w(TAG, "HiddenBrowser.parser.onReceiveValue: s=" + s);
                globals.visibleBrowser.renderData(s);
            }
        });
    }

    public HiddenBrowser(Globals globals, WebView webView, ProgressBar progressBar, EditText uriEditText) {
        this.globals = globals;
        this.progressBar = progressBar;
        this.webView = webView;
        this.uriEditText = uriEditText;

        webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        try {
            webSettings.setSupportZoom(true);
            webSettings.setDisplayZoomControls(false);
            webSettings.setBuiltInZoomControls(true);
        } catch (Exception e) {
            Log.e(TAG, "HiddenBrowser zoom error: " + e.getMessage());
        }

        webView.getSettings().setLoadsImagesAutomatically(false);
        //webView.getSettings().setBlockNetworkImage(true);

        // set webview client
        webView.setWebViewClient(new WebViewClient() {

            public WebResourceResponse shouldInterceptRequest(final WebView view, WebResourceRequest request) {
                // adblocker
                try {
                    Uri uri = request.getUrl();
                    String url = uri.toString();
                    String host = uri.getHost();
                    WebResourceResponse response;
                    // simple domain blacklist
                    if (!globals.adblock.allowedDomain(host)) {
                        return new WebResourceResponse("text/html", "UTF-8", 403, "Forbidden", null, null);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "HiddenBrowser.adBlock: " + e.getMessage());
                }

                // allowed
                return super.shouldInterceptRequest(view, request);
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                lastStartedPage = url;
                uriEditText.setText(url);
                super.onPageStarted(view, url, favicon);
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                Log.d(TAG, "onReceivedError " + errorCode + " desc=" + description + " url=" + failingUrl);
                if (failingUrl.startsWith("intent:")) {
                    return;
                }
                if (failingUrl.startsWith("market:")) {
                    return;
                }
                if (failingUrl.startsWith("tel:") || failingUrl.startsWith("mailto:")) {
                    return;
                }
                Log.e(TAG, "onReceivedError: " + description);
            }

            @Override
            public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
                try {
                    Log.e(TAG, "onReceivedHttpError u=" + request.getUrl().toString());
                } catch (Exception e) {
                }
                super.onReceivedHttpError(view, request, errorResponse);
            }

            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                // show ssl errors
                try {
                    Log.e(TAG, "onReceivedSslError: " + error.toString());
                } catch (Exception e) {
                }
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
                globals.visibleBrowser.chooseParser(url);
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
            public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
                // Bad pages could use alert to show popup ads
                Log.w(TAG, "onJsAlert: suppressed alert(" + message + ") at " + url);
                result.cancel();
                return true;
            }

            @Override
            public boolean onJsConfirm(WebView view, String url, String message, JsResult result) {
                // Bad pages could use alert to show popup ads
                Log.w(TAG, "onJsConfirm: suppressed confirm(" + message + ") at " + url);
                result.cancel();
                return true;
            }

            @Override
            public boolean onJsPrompt(WebView view, String url, String message, String defaultValue, JsPromptResult result) {
                // Bad pages could use alert to show popup ads
                Log.w(TAG, "onJsPrompt: suppressed prompt(" + message + ", " + defaultValue + ") at " + url);
                result.cancel();
                return true;
            }

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
                progressBar.setVisibility(View.VISIBLE);
                progressBar.setProgress((int) Math.round(0.9 * progress));
            }
        });
    }
}
