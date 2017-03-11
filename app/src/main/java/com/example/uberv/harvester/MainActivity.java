package com.example.uberv.harvester;

import android.Manifest;
import android.content.Context;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.webkit.JavascriptInterface;
import android.webkit.JsPromptResult;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import jxl.Workbook;
import jxl.write.WritableWorkbook;

public class MainActivity extends AppCompatActivity {
    public static final String LOG_TAG = MainActivity.class.getSimpleName();

    private WebView mWeb;
    private WritableWorkbook mContactsWorkbook;

    /* An instance of this class will be registered as a JavaScript interface */
    class MyJavaScriptInterface {

        int prevPage=-1;

        @JavascriptInterface
        @SuppressWarnings("unused")
        public void processHTML(String html, String url) {
            // process the html as needed by the app
            // TODO check if has data
            // TODO extract current page from url and max pages from data
            // current page
            int index = url.indexOf("page=");
            final int pageNumber = Integer.parseInt(url.substring(index+5));
            if(prevPage==pageNumber){
                // skip
                // TODO check not to duplicate
                //return;
            }

            // process html data
            Log.d("test", html);
            Document htmlDoc = Jsoup.parse(html);
            htmlDoc.getAllElements();
            Elements contactElements = htmlDoc.getElementsByClass("ese-result-row-item-cont");
            List<Contact> contacts = new ArrayList<>();
            for(Element contactElement: contactElements){
                contacts.add(parseContact(contactElement));
            }

            // save data

            // load next page
            MainActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mWeb.loadUrl("https://search.accenture.com/?aid=ctl&k=clientname|st|google||a||cleangr1|st|&page="+(pageNumber+1));
                }
            });
            Log.d(LOG_TAG, "processHTML");

            prevPage=pageNumber;
        }
    }

    public static Contact parseContact(Element contactElement){
        Contact contact= new Contact();
        contact.setClientName(contactElement.getElementsByClass("clientname").text());
        contact.setCountryForRole(contactElement.getElementsByClass("countryforrole").text());
        contact.setGeoArea(contactElement.getElementsByClass("geoarea").text());
        contact.setGeoUnit(contactElement.getElementsByClass("geounit").text());
        contact.setMcClassification(contactElement.getElementsByClass("mcclassification").text());
        contact.setPerson(contactElement.getElementsByClass("person").text());
        // TODO implement
        // contact.setPersonLink(firstContact.getElementsByClass("clientname").text());
        contact.setPrimaryOg(contactElement.getElementsByClass("primaryog").text());
        contact.setRole(contactElement.getElementsByClass("role").text());
        return contact;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

       // mContactsWorkbook = Workbook.createWorkbook(new File(""));

        mWeb = new WebView(this);
        setContentView(mWeb);
        // must t support javascript
        mWeb.getSettings().setJavaScriptEnabled(true);
        mWeb.getSettings().setDomStorageEnabled(true);
        /* Register a new JavaScript interface called HTMLOUT */
        mWeb.addJavascriptInterface(new MyJavaScriptInterface(), "HTMLOUT");

        HarvesterWebViewClient client = new HarvesterWebViewClient();
        mWeb.setWebViewClient(client);
        mWeb.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
            }
        });

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
        public static enum PageType {
            Authorization, Search
        }

        public PageType currentPageType = PageType.Authorization;

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);


            handleUrl(view, url);
            if (currentPageType == PageType.Search) {
                /* This call inject JavaScript into the page which just finished loading. */
                view.loadUrl("javascript:setTimeout(function(){window.HTMLOUT.processHTML('<head>'+document.getElementsByTagName('html')[0].innerHTML+'</head>','"+url+"');},1)");
            }
        }

        @Override
        public void onLoadResource(WebView view, String url) {
            super.onLoadResource(view, url);
        }

        // handle url
        private boolean handleUrl(WebView view, String url) {
            Uri uri = Uri.parse(url);
//            Log.i(TAG, "Uri =" + uri);
            String host = uri.getHost();
            String scheme = uri.getScheme();
            // Based on some condition you need to determine if you are going to load the url
            // in your web view itself or in a browser.
            // You can use `host` or `scheme` or any part of the `uri` to decide.

            // host: federation-sts.accenture.com
            // search.accenture.com
            switch (host) {
                case "federation-sts.accenture.com":
                    currentPageType = PageType.Authorization;
                    break;
                case "search.accenture.com":
                    currentPageType = PageType.Search;
                    break;
            }
            Log.d(LOG_TAG,"handleUrl");
            return true;
        }
    }
}
