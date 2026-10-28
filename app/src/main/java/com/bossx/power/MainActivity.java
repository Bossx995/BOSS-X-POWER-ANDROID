package com.bossx.power;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.webkit.PermissionRequest;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebResourceRequest;
import android.graphics.Color;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;
import android.net.Uri;

public class MainActivity extends Activity {
    private WebView web;
    private boolean desktopMode = false;
    private static final int PERMISSION_CODE = 42;
    private PermissionRequest pendingWebPermission;

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(Color.rgb(7,19,33));
        getWindow().setNavigationBarColor(Color.rgb(7,19,33));
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        buildUi();
    }

    private void buildUi() {
        LinearLayout root = new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setBackgroundColor(Color.rgb(7,19,33));
        LinearLayout bar = new LinearLayout(this); bar.setOrientation(LinearLayout.HORIZONTAL); bar.setPadding(10,8,10,8); bar.setBackgroundColor(Color.rgb(10,30,50));
        TextView brand = new TextView(this); brand.setText("BOSS X POWER"); brand.setTextColor(Color.WHITE); brand.setTextSize(16); brand.setGravity( android.view.Gravity.CENTER_VERTICAL ); brand.setPadding(4,0,8,0);
        bar.addView(brand, new LinearLayout.LayoutParams(0, dp(48), 1));
        Button reload = new Button(this); reload.setText("Reload"); bar.addView(reload, new LinearLayout.LayoutParams(-2, dp(48)));
        Button mode = new Button(this); mode.setText("Mobile"); bar.addView(mode, new LinearLayout.LayoutParams(-2, dp(48)));
        Button openApp = new Button(this); openApp.setText("WhatsApp App"); bar.addView(openApp, new LinearLayout.LayoutParams(-2, dp(48)));
        root.addView(bar);
        TextView note = new TextView(this); note.setText("Mobile mode by default • Phone-number verification is handled only by the official WhatsApp app"); note.setTextColor(Color.rgb(180,210,235)); note.setTextSize(11); note.setPadding(12,5,12,5); root.addView(note);
        web = new WebView(this); web.setBackgroundColor(Color.BLACK);
        WebSettings s = web.getSettings(); s.setJavaScriptEnabled(true); s.setDomStorageEnabled(true); s.setDatabaseEnabled(true); s.setMediaPlaybackRequiresUserGesture(false); s.setAllowFileAccess(false); s.setJavaScriptCanOpenWindowsAutomatically(true); s.setSupportMultipleWindows(false);
        applyUserAgent();
        web.setWebViewClient(new WebViewClient() {
            @Override public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                String u = request.getUrl().toString();
                if (u.startsWith("https://web.whatsapp.com/") || u.startsWith("https://whatsapp.com/") || u.startsWith("https://www.whatsapp.com/")) return false;
                if (u.startsWith("https://")) { try { startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(u))); } catch (Exception ignored) {} return true; }
                return true;
            }
        });
        web.setWebChromeClient(new WebChromeClient() {
            @Override public void onPermissionRequest(PermissionRequest request) {
                pendingWebPermission = request;
                boolean mic = checkSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
                boolean cam = checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
                if (!mic || !cam) {
                    requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA}, PERMISSION_CODE);
                } else grantRequestedResources(request);
            }
            @Override public void onPermissionRequestCanceled(PermissionRequest request) { if (pendingWebPermission == request) pendingWebPermission = null; }
        });
        root.addView(web, new LinearLayout.LayoutParams(-1, 0, 1));
        setContentView(root);
        reload.setOnClickListener(v -> web.reload());
        openApp.setOnClickListener(v -> {
            Intent launch = getPackageManager().getLaunchIntentForPackage("com.whatsapp");
            if (launch != null) startActivity(launch);
            else Toast.makeText(this, "Official WhatsApp app is not installed", Toast.LENGTH_LONG).show();
        });
        mode.setOnClickListener(v -> { desktopMode = !desktopMode; applyUserAgent(); mode.setText(desktopMode ? "Desktop" : "Mobile"); web.reload(); });
        web.loadUrl("https://web.whatsapp.com/");
    }

    private int dp(int n) { return (int)(n * getResources().getDisplayMetrics().density + 0.5f); }
    private void applyUserAgent() {
        if (web == null) return;
        WebSettings s = web.getSettings();
        String base = WebSettings.getDefaultUserAgent(this);
        if (desktopMode) {
            base = base.replaceAll("\\bAndroid[^;)]*;?\\s*", "").replace(" Mobile", "");
            if (!base.contains("X11; Linux x86_64")) base += " BOSSXDesktopMode";
        }
        s.setUserAgentString(base);
    }
    private void grantRequestedResources(PermissionRequest request) {
        if (request == null) return;
        java.util.ArrayList<String> allowed = new java.util.ArrayList<>();
        for (String r : request.getResources()) {
            if (PermissionRequest.RESOURCE_AUDIO_CAPTURE.equals(r) && checkSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) allowed.add(r);
            if (PermissionRequest.RESOURCE_VIDEO_CAPTURE.equals(r) && checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) allowed.add(r);
        }
        if (!allowed.isEmpty()) request.grant(allowed.toArray(new String[0])); else request.deny();
    }
    @Override public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] results) {
        super.onRequestPermissionsResult(requestCode, permissions, results);
        if (requestCode == PERMISSION_CODE && pendingWebPermission != null) {
            boolean mic = checkSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
            boolean cam = checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
            if (mic || cam) grantRequestedResources(pendingWebPermission); else pendingWebPermission.deny();
            pendingWebPermission = null;
        }
    }
    @Override public void onBackPressed() { if (web != null && web.canGoBack()) web.goBack(); else super.onBackPressed(); }
    @Override protected void onDestroy() { if (web != null) web.destroy(); super.onDestroy(); }
}
