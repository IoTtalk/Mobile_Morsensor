package tw.org.cic.tracking_mobile;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.webkit.GeolocationPermissions;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import tw.org.cic.morsensor_mobile.R;

public class WebViewActivity extends Activity {
    private WebView webView;
    private String url;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        runtime_permissions();

        setContentView(R.layout.webview);

        Intent i = getIntent();
        url= i.getStringExtra("url");
        webView = (WebView) findViewById(R.id.webview);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setUseWideViewPort(true);
        webView.setWebViewClient(new WebViewClient());
        //设置脚本是否允许自动打开弹窗
        webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        webView.canGoBack();

        //play video
        webView.getSettings().setPluginState(WebSettings.PluginState.ON);
        webView.getSettings().setUseWideViewPort(true); // 关键点
        webView.getSettings().setAllowFileAccess(true); // 允许访问文件
        webView.getSettings().setSupportZoom(true); // 支持缩放
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE); // 不加载缓存内容

        webView.getSettings().setGeolocationEnabled(true);//允许地理位置可用

        //设置响应js 的Alert()函数
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onJsAlert(WebView view, String url, String message, final JsResult result) {
                AlertDialog.Builder b = new AlertDialog.Builder(WebViewActivity.this);
                b.setTitle("Alert");
                b.setMessage(message);
                b.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        result.confirm();
                    }
                });
                b.setCancelable(false);
                b.create().show();
                return true;
            }
            //设置响应js 的Confirm()函数
            @Override
            public boolean onJsConfirm(WebView view, String url, String message, final JsResult result) {
                AlertDialog.Builder b = new AlertDialog.Builder(WebViewActivity.this);
                b.setTitle("Confirm");
                b.setMessage(message);
                b.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        result.confirm();
                    }
                });
                b.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        result.cancel();
                    }
                });
                b.create().show();
                return true;
            }

            @Override
            public void onGeolocationPermissionsShowPrompt(final String origin, final GeolocationPermissions.Callback callback) {
                Log.i("GeolocationPermissions", "onGeolocationPermissionsShowPrompt()");

                final boolean remember = false;
                AlertDialog.Builder builder = new AlertDialog.Builder(WebViewActivity.this);
                builder.setTitle("Locations");
                builder.setMessage("Would like to use your Current Location ")
                        .setCancelable(true).setPositiveButton("Allow", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // origin, allow, remember
                        callback.invoke(origin, true, true);
                    }
                }).setNegativeButton("Don't Allow", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // origin, allow, remember
                        callback.invoke(origin, false, remember);
                    }
                });
                AlertDialog alert = builder.create();
                alert.show();
            }



        });

//        webView.loadUrl(url);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.v("WebViewActivity", "onDestroy");
        webView.clearCache(true);

        // Loading a blank page is optional, but will ensure that the WebView isn't doing anything when you destroy it.
        webView.loadUrl("about:blank");

        webView.onPause();
        webView.removeAllViews();
        webView.destroyDrawingCache();

        // NOTE: This pauses JavaScript execution for ALL WebViews,
        // do not use if you have other WebViews still alive.
        // If you create another WebView after calling this,
        // make sure to call mWebView.resumeTimers().
        webView.pauseTimers();

        // NOTE: This can occasionally cause a segfault below API 17 (4.2)
        webView.destroy();

        // Null out the reference so that you don't end up re-using it.
        webView = null;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.v("onRequestPermissions", "onRequestPermissionsResult");
        if(requestCode == TrackingConfig.MY_PERMISSIONS_REQUEST_LOCATION){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) { //permission was granted
                Log.v("RequestPermissionResult", "location permission if");
                webView.loadUrl(url);
            } else { //permission denied
                Log.v("RequestPermissionResult", "location permission else");
                runtime_permissions();
            }
        }
    }

    protected void runtime_permissions() {
        requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, TrackingConfig.MY_PERMISSIONS_REQUEST_LOCATION);
    }
}