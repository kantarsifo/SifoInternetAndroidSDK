package se.kantarsifo.mobileanalytics.sampleapp;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ListView;

import se.kantarsifo.mobileanalytics.framework.TSMobileAnalytics;


/**
 * Created by ahmetcengiz on 26/09/2017.
 */
public class WebFragment extends Fragment {

    private WebView mWebView;

    public static WebFragment newInstance() {

        return new WebFragment();
    }

    public WebFragment(){

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.web_fragment, container, false);
        WebViewClient webViewClient = new WebViewClient();
        mWebView = (WebView) v.findViewById(R.id.webview);
        mWebView.setWebViewClient(webViewClient);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);

        ListView listView = (ListView) v.findViewById(R.id.list);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                loadUrl((String) parent.getItemAtPosition(position));
                if (TSMobileAnalytics.getInstance() != null) {
                    TSMobileAnalytics.getInstance().activateCookies();
                }
            }
        });

        return v;
    }


    @Override
    public void onResume() {
        super.onResume();
    }

    private void loadUrl(String url) {
        mWebView.loadUrl(url);
    }
}
