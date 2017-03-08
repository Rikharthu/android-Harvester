package com.example.uberv.harvester;

import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.webkit.JavascriptInterface;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class MainActivity extends AppCompatActivity {

    private WebView mWeb;

    /* An instance of this class will be registered as a JavaScript interface */
    class MyJavaScriptInterface
    {
        @JavascriptInterface
        @SuppressWarnings("unused")
        public void processHTML(String html)
        {
            // process the html as needed by the app
            // TODO need some delay for javascript to parse encoded data
            Log.d("test",html);
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mWeb = new WebView(this);
        setContentView(mWeb);
        // must t support javascript
        mWeb.getSettings().setJavaScriptEnabled(true);
        /* Register a new JavaScript interface called HTMLOUT */
        mWeb.addJavascriptInterface(new MyJavaScriptInterface(), "HTMLOUT");

        HarvesterWebViewClient client = new HarvesterWebViewClient();
        mWeb.setWebViewClient(client);

        mWeb.loadUrl("https://search.accenture.com/?aid=ctl&k=clientname|st|volkswagen||a||cleangr1|st|&page=1");
    }

    // To handle "Back" key press event for WebView to go back to previous screen.
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK) && mWeb.canGoBack()) {
            mWeb.goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    public static class HarvesterWebViewClient extends WebViewClient {

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);

            /* This call inject JavaScript into the page which just finished loading. */
            view.loadUrl("javascript:setTimeout(function(){window.HTMLOUT.processHTML('<head>'+document.getElementsByTagName('html')[0].innerHTML+'</head>');},10000)");
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            return handleUrl(view,url);
        }

        // handle url
        private boolean handleUrl(WebView view, String url) {
            final Uri uri = Uri.parse(url);
//            Log.i(TAG, "Uri =" + uri);
            final String host = uri.getHost();
            final String scheme = uri.getScheme();
            // Based on some condition you need to determine if you are going to load the url
            // in your web view itself or in a browser.
            // You can use `host` or `scheme` or any part of the `uri` to decide.

            // host: federation-sts.accenture.com
            view.loadUrl(url);
            return true;
        }
    }
}
