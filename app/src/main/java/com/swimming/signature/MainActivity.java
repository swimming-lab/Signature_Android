package com.swimming.signature;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.swimming.signature.common.CommonConfig;

public class MainActivity extends AppCompatActivity {
    WebView webView;
    WebSettings webSettings;
    Context mContext;
    long lastTimeBackPressed = -1500;

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this.getApplicationContext();

        // 웹뷰 선언
        webView = (WebView) findViewById(R.id.webview);

        // 웹뷰 설정
        webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);                     // javascript 사용
        webSettings.setSupportMultipleWindows(true);                // 윈도우 여러개
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true); // window.open() 사용
        webSettings.setDatabaseEnabled(false);
        webSettings.setDomStorageEnabled(false);
        webSettings.setAppCacheEnabled(false);
        webSettings.setTextZoom(100);
        webView.setWebContentsDebuggingEnabled(true);
        webView.setWebChromeClient(new WebChromeClient());
        webView.setWebViewClient(new CustomWebViewClient());

        // 앱 에이전트
        String userAgent = webView.getSettings().getUserAgentString();
        webView.getSettings().setUserAgentString(userAgent + CommonConfig.APP_AGENT);
        Log.d(CommonConfig.MAIN_ACTIVITY + " 에이전트", webView.getSettings().getUserAgentString());

        // 페이지 로드
        webView.loadUrl(CommonConfig.DOMAIN + CommonConfig.URL_MAIN_MAIN);
    }

    @Override
    public void onBackPressed() {
        String url = webView.getUrl();
        Log.d(CommonConfig.MAIN_ACTIVITY + " 뒤로가기 이벤트", url);

        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            if (System.currentTimeMillis() - lastTimeBackPressed <= CommonConfig.FINISH_APP_SECOND) {
                finish();
            } else {
                Toast.makeText(MainActivity.this, CommonConfig.FINISH_APP_MESSAGE, Toast.LENGTH_SHORT).show();
            }
            lastTimeBackPressed = System.currentTimeMillis();
        }
    }

    private class CustomWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            Log.d(CommonConfig.MAIN_ACTIVITY + " check URL", url);

            // sub 액티비티 실행
            if (url.indexOf(CommonConfig.SUB_GROUPS_CONTRACT) > -1
                    || url.indexOf(CommonConfig.SUB_GROUPS_MEMBER) > -1
                    || url.indexOf(CommonConfig.SUB_GROUPS_ARREARS) > -1
                    || url.indexOf(CommonConfig.SUB_GROUPS_EQUIP) > -1
                    || url.indexOf(CommonConfig.SUB_GROUPS_ADMIN) > -1) {
                Log.d(CommonConfig.MAIN_ACTIVITY + " 서브 전환", url);

                Intent intent = new Intent(mContext.getApplicationContext(), SubActivity.class);
                intent.putExtra("url", url);
                startActivity(intent);
//                overridePendingTransition(R.anim.anim_slide_in_right, R.anim.anim_slide_out_left);
                overridePendingTransition(R.anim.anim_slide_in_right, R.anim.not_move_activity);
            }
            // 로그아웃
            else if (url.indexOf(CommonConfig.URL_LOGIN_LOGOUT) > -1) {
                Log.d(CommonConfig.MAIN_ACTIVITY + " 로그아웃", url);

                Intent intent = new Intent(mContext.getApplicationContext(), LoginActivity.class);
                startActivity(intent);
//                overridePendingTransition(R.anim.rightin_activity, R.anim.not_move_activity);
//                overridePendingTransition(R.anim.anim_slide_in_right, R.anim.anim_slide_out_left);
                finish();
            }
            // 웹 뒤로가기
            else if (url.indexOf(CommonConfig.SCHEME_GO_BACK) > -1) {
                Log.d(CommonConfig.MAIN_ACTIVITY + " 뒤로가기 이벤트", url);
                if (webView.canGoBack()) {
                    webView.goBack();
                } else {
                    finish();
                }
            }
            // 전화걸기
            else if (url.startsWith("tel:")) {
                Intent dial = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(dial);
            } else {
                view.loadUrl(url);
            }

            return true;
        }
    }
}
