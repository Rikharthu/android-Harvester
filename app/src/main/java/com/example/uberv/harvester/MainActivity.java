package com.example.uberv.harvester;

import android.Manifest;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.JsPromptResult;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import com.android.internal.util.Predicate;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jxl.Workbook;
import jxl.write.WritableWorkbook;

import static com.example.uberv.harvester.AccSearchHelper.getSearchUrl;

public class MainActivity extends AppCompatActivity {
    public static final String LOG_TAG = MainActivity.class.getSimpleName();

    private int mCompanyIndex = 0;

    private WebView mWeb;
    private WritableWorkbook mContactsWorkbook;
    private String[] mCompanyNames;
    private List<Contact> mContacts = new ArrayList<>();
    private TextView mStatusTv;

    /* An instance of this class will be registered as a JavaScript interface */
    class MyJavaScriptInterface {

        public class BossFilter implements Predicate<Contact> {

            private boolean isNullOrEmpty(String str) {
                return str == null || str.isEmpty() || str.equals("-");
            }

            @Override
            public boolean apply(Contact contact) {
                return isNullOrEmpty(contact.getGeoArea()) || isNullOrEmpty(contact.getGeoUnit());
            }
        }

        int prevPage = -1;

        @JavascriptInterface
        @SuppressWarnings("unused")
        public void processHTML(String html, String url) {
            Log.d(LOG_TAG, "processHTML");
            // process the html as needed by the app
            // TODO check if has data
            // TODO extract current company
            // TODO extract current page from url and max pages from data
            // current page
            int index = url.indexOf("page=");
            final int pageNumber = Integer.parseInt(url.substring(index + 5));

            // process html data
            Document htmlDoc = Jsoup.parse(html);
            int resultsCount = -1;


            Elements contactElements = htmlDoc.getElementsByClass("ese-result-row-item-cont");
            List<Contact> contacts = new ArrayList<>();
            for (Element contactElement : contactElements) {
                contacts.add(parseContact(contactElement));
            }
            // save data
            mContacts.addAll(contacts);

            // check if it was the last page
            Element searchMetrics = htmlDoc.getElementsByClass("ese-tabular-searchmetrics-cont").first();
            if (searchMetrics != null) {
                // c - a of b results (0.xy seconds)
                String searchMetricsText = searchMetrics.text();
                Pattern pattern = Pattern.compile("\\d+ of \\d+");
                Matcher matcher = pattern.matcher(searchMetricsText);
                if (matcher.find()) {
                    Log.d(LOG_TAG, "searchmetrics: " + matcher.group(0));
                    String numbers[] = matcher.group(0).split("of");
                    int a = Integer.parseInt(numbers[0].trim());
                    int b = Integer.parseInt(numbers[1].trim());
                    if (a == b) {
                        Log.w(LOG_TAG, "LAST PAGE!");
                        // TODO filter save the data
                        BossFilter bossFilter = new BossFilter();
                        Iterator<Contact> contactsIterator = mContacts.iterator();
                        while (contactsIterator.hasNext()) {
                            if (bossFilter.apply(contactsIterator.next())) {
                                contactsIterator.remove();
                            }
                        }
                        Log.w(LOG_TAG, "DATA READY:");
                        for (Contact c : mContacts) {
                            Log.d(LOG_TAG, c.toExcelString());
                        }
                        // TODO load next company
                        //mWeb.loadUrl("https://search.accenture.com/?aid=ctl&k=clientname|st|"+mCompany+"||a||cleangr1|st|&page=1");
                        MainActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mCompanyIndex++;
                                mWeb.loadUrl("https://search.accenture.com/?aid=ctl&k=clientname|st|" + mCompanyNames[mCompanyIndex] + "||a||cleangr1|st|&page=1");
                            }
                        });
                        return;
                    }
                }
            }

            // check if mContacts size equals to total contacts for that company
            // load next page
            if (contacts.size() != 0) {
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mStatusTv.setText("" + mContacts.size());
                        mWeb.loadUrl("https://search.accenture.com/?aid=ctl&k=clientname|st|" + mCompanyNames[mCompanyIndex] + "||a||cleangr1|st|&page=" + (pageNumber + 1));
                    }
                });
            }

            prevPage = pageNumber;
        }
    }

    public static Contact parseContact(Element contactElement) {
        Contact contact = new Contact();
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
        setContentView(R.layout.activity_main);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mStatusTv = (TextView) findViewById(R.id.status_textview);

        // initialize output excel workbook
        // mContactsWorkbook = Workbook.createWorkbook(new File(""));

        // read source companies
        mCompanyNames = getResources().getStringArray(R.array.array_companies);

        // FIXME DEBUG

        mWeb = (WebView) findViewById(R.id.webview);
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

        mWeb.loadUrl("https://search.accenture.com/?aid=ctl&k=clientname|st|" + mCompanyNames[mCompanyIndex] + "||a||cleangr1|st|&page=1");
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
        int counter = 0;
        public AccSearchHelper.PageType currentPageType = AccSearchHelper.PageType.Authorization;
        private String prevUrl = null;

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);

            handleUrl(view, url);
            //if (currentPageType == AccSearchHelper.PageType.Search && counter == 2) {
                Log.d(LOG_TAG, "injecting javascript");
                counter = 0;
                /* This call inject JavaScript into the page which just finished loading. */
                view.loadUrl("javascript:setTimeout(function(){window.HTMLOUT.processHTML('<head>'+document.getElementsByTagName('html')[0].innerHTML+'</head>','" + url + "');},1)");
            //}
        }


        private boolean handleUrl(WebView view, String url) {
            if (prevUrl == null) prevUrl = url;
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
                    currentPageType = AccSearchHelper.PageType.Authorization;
                    break;
                case "search.accenture.com":
                    if (!prevUrl.equals(url)) counter = 0;
                    // first time blank page is loaded
                    // second time results are loaded
                    counter++;
                    currentPageType = AccSearchHelper.PageType.Search;
                    break;
            }
            Log.d(LOG_TAG, "handleUrl: " + url + "\ncounter=" + counter);
            prevUrl = url;
            return true;
        }
    }
}
