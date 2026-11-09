package com.nguyenlan.lankkth;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient.FileChooserParams;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;
import java.io.OutputStream;

public class MainActivity extends Activity {
    private static final int CREATE_FILE_REQUEST = 4102;
    private static final int PICK_FILE_REQUEST = 4103;
    private ValueCallback<Uri[]> fileChooserCallback;
    private WebView webView;
    private String pendingName = "export";
    private String pendingMime = "application/octet-stream";
    private byte[] pendingBytes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(0xFF1B5E20);
        getWindow().setNavigationBarColor(0xFF111111);
        webView = new WebView(this);
        setContentView(webView);
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(true);
        settings.setJavaScriptCanOpenWindowsAutomatically(false);
        settings.setSupportMultipleWindows(false);
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onShowFileChooser(WebView view, ValueCallback<Uri[]> callback, FileChooserParams params) {
                if (fileChooserCallback != null) fileChooserCallback.onReceiveValue(null);
                fileChooserCallback = callback;
                Intent intent = params.createIntent();
                try {
                    startActivityForResult(intent, PICK_FILE_REQUEST);
                } catch (Exception e) {
                    fileChooserCallback = null;
                    callback.onReceiveValue(null);
                    Toast.makeText(MainActivity.this, "Không mở được trình chọn tệp.", Toast.LENGTH_SHORT).show();
                }
                return true;
            }
        });
        webView.setWebViewClient(new WebViewClient());
        webView.addJavascriptInterface(new AndroidBridge(), "AndroidBridge");
        webView.loadUrl("file:///android_asset/index.html");
    }

    private class AndroidBridge {
        @JavascriptInterface
        public void saveFile(String name, String mime, String base64) {
            runOnUiThread(() -> {
                try {
                    pendingName = (name == null || name.trim().isEmpty()) ? "export" : name;
                    pendingMime = (mime == null || mime.trim().isEmpty()) ? "application/octet-stream" : mime;
                    pendingBytes = Base64.decode(base64, Base64.DEFAULT);
                    Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    intent.setType(pendingMime);
                    intent.putExtra(Intent.EXTRA_TITLE, pendingName);
                    startActivityForResult(intent, CREATE_FILE_REQUEST);
                } catch (Exception e) {
                    Toast.makeText(MainActivity.this, "Không thể tạo tệp: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_FILE_REQUEST) {
            if (fileChooserCallback != null) {
                Uri[] results = (resultCode == RESULT_OK && data != null && data.getData() != null)
                        ? new Uri[]{data.getData()} : null;
                fileChooserCallback.onReceiveValue(results);
                fileChooserCallback = null;
            }
            return;
        }
        if (requestCode == CREATE_FILE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null && pendingBytes != null) {
            try (OutputStream out = getContentResolver().openOutputStream(data.getData())) {
                if (out != null) {
                    out.write(pendingBytes);
                    out.flush();
                    Toast.makeText(this, "Đã lưu tệp thành công.", Toast.LENGTH_LONG).show();
                }
            } catch (Exception e) {
                Toast.makeText(this, "Lỗi lưu tệp: " + e.getMessage(), Toast.LENGTH_LONG).show();
            } finally {
                pendingBytes = null;
            }
        } else if (requestCode == CREATE_FILE_REQUEST) {
            pendingBytes = null;
        }
    }

    @Override
    public void onBackPressed() {
        if (webView != null && webView.canGoBack()) webView.goBack();
        else super.onBackPressed();
    }

    @Override
    protected void onPause() {
        if (webView != null) webView.evaluateJavascript("try{if(typeof saveDB==='function')saveDB();}catch(e){}", null);
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (fileChooserCallback != null) {
            fileChooserCallback.onReceiveValue(null);
            fileChooserCallback = null;
        }
        if (webView != null) {
            webView.stopLoading();
            webView.removeJavascriptInterface("AndroidBridge");
            webView.destroy();
            webView = null;
        }
        super.onDestroy();
    }
}
