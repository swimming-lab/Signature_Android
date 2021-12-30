package com.swimming.signature;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;
import com.swimming.signature.bridge.AndroidBridge;
import com.swimming.signature.common.CommonConfig;

import java.util.List;

public class LoginActivity extends AppCompatActivity {
    WebView webView;
    WebSettings webSettings;
    Context mContext;
    AndroidBridge androidBridge = new AndroidBridge();
    long lastTimeBackPressed = -1500;

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
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
        webView.addJavascriptInterface(androidBridge, CommonConfig.APP_NAME);
        webView.setWebChromeClient(new WebChromeClient());
        webView.setWebViewClient(new CustomWebViewClient());

        // 앱 에이전트
        String userAgent = webView.getSettings().getUserAgentString();
        webView.getSettings().setUserAgentString(userAgent + CommonConfig.APP_AGENT);
        Log.d(CommonConfig.LOGIN_ACTIVITY + " 에이전트", webView.getSettings().getUserAgentString());

        // 페이지 로드
        webView.loadUrl(CommonConfig.DOMAIN);

        // 주소록 앱 연동
//        runContactApp();
    }

    private class CustomWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            Log.d(CommonConfig.LOGIN_ACTIVITY + " check URL", url);

            // 로그인 완료
            if (url.indexOf(CommonConfig.URL_MAIN_MAIN) > -1) {
                Log.d(CommonConfig.LOGIN_ACTIVITY + " 메인 전환", url);
                Intent intent = new Intent(mContext.getApplicationContext(), MainActivity.class);
                startActivity(intent);
                finish();
            }
            // sub 액티비티 실행
//            else if (url.indexOf(CommonConfig.SUB_GROUPS_DIRECT) > -1
//                    || url.indexOf(CommonConfig.SUB_GROUPS_CONTRACT) > -1
//                    || url.indexOf(CommonConfig.SUB_GROUPS_ARREARS) > -1
//                    || url.indexOf(CommonConfig.SUB_GROUPS_EQUIP) > -1
//                    || url.indexOf(CommonConfig.SUB_GROUPS_ADMIN) > -1) {
//                Log.d(CommonConfig.MAIN_ACTIVITY + " 서브 전환", url);
//
//                Intent intent = new Intent(mContext.getApplicationContext(), SubActivity.class);
//                intent.putExtra("url", url);
//                startActivity(intent);
//                overridePendingTransition(R.anim.anim_slide_in_right, R.anim.not_move_activity);
//
//                // 2초 뒤 로그인 메인으로 뒤로가기
//                new Handler().postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        webView.loadUrl(CommonConfig.DOMAIN);
//                    }
//                }, 2000);
//            }
            // 주소록 앱 호출
            else if (url.indexOf(CommonConfig.SCHEME_GO_CONTACT) > -1) {
                runContactApp();
            }
            // 웹 뒤로가기
            else if (url.indexOf(CommonConfig.SCHEME_GO_BACK) > -1) {
                Log.d(CommonConfig.LOGIN_ACTIVITY + " 뒤로가기 이벤트", url);
                if (webView.canGoBack()) {
                    webView.goBack();
                } else {
                    finish();
                }
            } else {
                view.loadUrl(url);
            }

            return true;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);

            // 로그인 페이지 키패드 노출
            if (url.indexOf(CommonConfig.URL_LOGIN_LOGIN) > -1) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(view, 0);
            }

            // 다이렉트 일 때 로그인/계약서 완료 페이지에서 히스토리 삭제
            if (url.indexOf(CommonConfig.URL_LOGIN_MAIN) > -1
                || url.indexOf(CommonConfig.URL_CONTRACT_SMS) > -1) {
                view.clearHistory();
            }
        }
    }

    @Override
    public void onBackPressed() {
        String url = webView.getUrl();
        Log.d(CommonConfig.LOGIN_ACTIVITY + "뒤로가기 이벤트", url);

        if (androidBridge.isLayer()) {
            webView.loadUrl(CommonConfig.JAVASCRIPT_CLOSE_LAYER);
            androidBridge.setIsLayer(false);
        } else {
            if (webView.canGoBack()) {
                webView.goBack();
            } else {
                if (System.currentTimeMillis() - lastTimeBackPressed <= CommonConfig.FINISH_APP_SECOND) {
                    finish();
                } else {
                    Toast.makeText(LoginActivity.this, CommonConfig.FINISH_APP_MESSAGE, Toast.LENGTH_SHORT).show();
                }
                lastTimeBackPressed = System.currentTimeMillis();
            }
        }
    }

    /**
     * 주소록 앱 연동 시작
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // 주소록 앱에서 데이터 받아오기
        getContactData(requestCode, resultCode, data);
    }

    private void getContactData(int requestCode, int resultCode, Intent data) {
        if (requestCode == CommonConfig.SUCCESS && resultCode == RESULT_OK) {
            String id = Uri.parse(data.getDataString()).getLastPathSegment();

            Cursor cursor = mContext.getContentResolver().query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    new String[] {
                            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                            ContactsContract.CommonDataKinds.Phone.NUMBER
                    },
                    ContactsContract.Data._ID + "=" + id,
                    null,
                    null
            );

            cursor.moveToFirst();
            String name = cursor.getString(0);
            String phone = cursor.getString(1);

            Log.d("id >> ", id);
            Log.d("이름 >> ", name);
            Log.d("전화번호 >> ", phone);

            webView.loadUrl(CommonConfig.JAVASCRIPT_SET_PHONE_NUMBER(phone));
        }
    }

    private void runContactApp() {
        PermissionListener permissionlistener = new PermissionListener() {
            @Override
            public void onPermissionGranted() {
//                Toast.makeText(LoginActivity.this, "권한이 허용되었습니다.", Toast.LENGTH_SHORT).show();
                // 권한 있을 때 주소록 앱 액티비티 실행
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setData(ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
                startActivityForResult(intent, CommonConfig.SUCCESS);
            }

            @Override
            public void onPermissionDenied(List<String> deniedPermissions) {
//                Toast.makeText(LoginActivity.this, "권한이 없습니다.", Toast.LENGTH_SHORT).show();
            }
        };

        TedPermission.with(this)
                .setPermissionListener(permissionlistener)
                .setDeniedMessage("주소록 앱을 실행할 수 있는 권한이 없습니다.\n권한을 설정해주세요.")
                .setPermissions(Manifest.permission.READ_CONTACTS)
                .check();
    }
    /**
     * 주소록 앱 연동 끝
     */
}