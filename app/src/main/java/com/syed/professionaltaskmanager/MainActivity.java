package com.syed.professionaltaskmanager;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
    private static final String PREFS = "ptm_prefs";
    private static final String KEY_URL = "server_url";

    private WebView webView;
    private ProgressBar progressBar;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        String savedUrl = prefs.getString(KEY_URL, "");
        if (savedUrl == null || savedUrl.trim().isEmpty()) {
            showSetupScreen("");
        } else {
            showWebView(savedUrl);
        }
    }

    private void showSetupScreen(String message) {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER);
        root.setPadding(dp(24), dp(24), dp(24), dp(24));
        root.setBackgroundColor(Color.rgb(15, 23, 42));

        TextView title = new TextView(this);
        title.setText("Connect Task Manager");
        title.setTextColor(Color.WHITE);
        title.setTextSize(26);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, 0, 0, dp(10));
        root.addView(title, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        TextView info = new TextView(this);
        info.setText("Enter your live PHP task manager URL. The app will use the same login, tasks, comments, files, QA status, reports, and database.");
        info.setTextColor(Color.rgb(203, 213, 225));
        info.setTextSize(15);
        info.setGravity(Gravity.CENTER);
        info.setLineSpacing(4, 1);
        root.addView(info, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        if (message != null && !message.isEmpty()) {
            TextView error = new TextView(this);
            error.setText(message);
            error.setTextColor(Color.rgb(254, 202, 202));
            error.setTextSize(14);
            error.setGravity(Gravity.CENTER);
            error.setPadding(0, dp(18), 0, 0);
            root.addView(error, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        }

        EditText input = new EditText(this);
        input.setSingleLine(true);
        input.setHint("https://yourdomain.com/task-manager/");
        input.setTextColor(Color.WHITE);
        input.setHintTextColor(Color.rgb(148, 163, 184));
        input.setText(prefs.getString(KEY_URL, ""));
        input.setSelectAllOnFocus(false);
        input.setPadding(dp(14), 0, dp(14), 0);
        LinearLayout.LayoutParams inputParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(54));
        inputParams.setMargins(0, dp(24), 0, dp(12));
        root.addView(input, inputParams);

        Button save = new Button(this);
        save.setText("Save & Open App");
        root.addView(save, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(52)));

        Button clear = new Button(this);
        clear.setText("Clear Saved URL");
        LinearLayout.LayoutParams clearParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(52));
        clearParams.setMargins(0, dp(10), 0, 0);
        root.addView(clear, clearParams);

        setContentView(root);

        save.setOnClickListener(v -> {
            hideKeyboard(input);
            String url = cleanUrl(input.getText().toString());
            if (!isValidUrl(url)) {
                Toast.makeText(this, "Please enter a valid URL", Toast.LENGTH_SHORT).show();
                return;
            }
            prefs.edit().putString(KEY_URL, url).apply();
            showWebView(url);
        });

        clear.setOnClickListener(v -> {
            prefs.edit().remove(KEY_URL).apply();
            input.setText("");
            Toast.makeText(this, "Saved URL cleared", Toast.LENGTH_SHORT).show();
        });
    }

    private void showWebView(String url) {
        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setBackgroundColor(Color.WHITE);

        progressBar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        progressBar.setMax(100);
        progressBar.setVisibility(View.GONE);
        container.addView(progressBar, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(3)));

        webView = new WebView(this);
        container.addView(webView, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1));
        setContentView(container);

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);
        settings.setBuiltInZoomControls(false);
        settings.setDisplayZoomControls(false);
        settings.setAllowFileAccess(false);
        settings.setAllowContentAccess(true);
        settings.setMediaPlaybackRequiresUserGesture(false);
        if (android.os.Build.VERSION.SDK_INT >= 21) {
            settings.setMixedContentMode(WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE);
            CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true);
        }
        CookieManager.getInstance().setAcceptCookie(true);

        webView.setWebChromeClient(new WebChromeClient() {
            @Override public void onProgressChanged(WebView view, int newProgress) {
                progressBar.setVisibility(newProgress < 100 ? View.VISIBLE : View.GONE);
                progressBar.setProgress(newProgress);
            }
        });

        webView.setWebViewClient(new WebViewClient() {
            @Override public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return handleUrl(request.getUrl().toString());
            }
            @Override public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return handleUrl(url);
            }
            @Override public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                if (request == null || request.isForMainFrame()) {
                    showErrorDialog("Could not load the task manager. Check the URL, hosting, SSL, or internet connection.");
                }
            }
        });

        if (!isNetworkAvailable()) {
            Toast.makeText(this, "No internet connection", Toast.LENGTH_LONG).show();
        }
        webView.loadUrl(cleanUrl(url));
    }

    private boolean handleUrl(String url) {
        if (url == null) return false;
        if (url.startsWith("tel:") || url.startsWith("mailto:") || url.startsWith("whatsapp:")) {
            try { startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url))); } catch (Exception ignored) {}
            return true;
        }
        return false;
    }

    private void showErrorDialog(String message) {
        new AlertDialog.Builder(this)
                .setTitle("Connection problem")
                .setMessage(message)
                .setPositiveButton("Retry", (d, w) -> webView.reload())
                .setNegativeButton("Change URL", (d, w) -> showSetupScreen("Enter the correct task manager URL."))
                .show();
    }

    private String cleanUrl(String value) {
        if (value == null) return "";
        value = value.trim();
        if (value.isEmpty()) return "";
        if (!value.startsWith("http://") && !value.startsWith("https://")) {
            value = "https://" + value;
        }
        return value;
    }

    private boolean isValidUrl(String value) {
        return value != null && (value.startsWith("http://") || value.startsWith("https://")) && value.length() > 10;
    }

    private boolean isNetworkAvailable() {
        try {
            ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo active = cm == null ? null : cm.getActiveNetworkInfo();
            return active != null && active.isConnected();
        } catch (Exception e) {
            return true;
        }
    }

    private void hideKeyboard(View view) {
        try {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            if (imm != null) imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        } catch (Exception ignored) {}
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }

    @Override
    public void onBackPressed() {
        if (webView != null && webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }
}
