package com.swimming.signature;

import android.Manifest;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.ContactsContract;
import android.util.Base64;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;
import android.webkit.DownloadListener;
import android.webkit.WebBackForwardList;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;
import com.swimming.signature.bridge.AndroidBridge;
import com.swimming.signature.common.CommonConfig;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.util.List;

public class SubActivity extends Activity {
    WebView webView;
    WebSettings webSettings;
    Context mContext;
    AndroidBridge androidBridge = new AndroidBridge();

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sub);
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
        webView.addJavascriptInterface(androidBridge, CommonConfig.APP_NAME);

        // 앱 에이전트
        String userAgent = webView.getSettings().getUserAgentString();
        webView.getSettings().setUserAgentString(userAgent + CommonConfig.APP_AGENT);
        Log.d(CommonConfig.SUB_ACTIVITY + " 에이전트", webView.getSettings().getUserAgentString());

        // 데이터 수신
        Intent intent = getIntent();
        String url = intent.getExtras().getString("url");

        webView.loadUrl(url);

        // 파일 다운로드
        webView.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimeType, long contentLength) {
                try {
                    if (url.startsWith("blob:")) {
                        url = url.replace("blob:", "");
                    } else if (url.startsWith("data:")) {
                        /**
                         * 계약서 이미지 저장
                         */
                        final String finalUrl = url;
                        PermissionListener permissionlistener = new PermissionListener() {
                            @Override
                            public void onPermissionGranted() {
                                // 권한 있을 때 이미지 저장 실행
                                Log.d(CommonConfig.SUB_ACTIVITY + " 미디어 권한 있음", "저장 실행");
                                String path = createAndSaveFileFromBase64Url(finalUrl);
                            }

                            @Override
                            public void onPermissionDenied(List<String> deniedPermissions) {
                                Log.d(CommonConfig.SUB_ACTIVITY + " 미디어 권한 없음", deniedPermissions.toString());
                            }
                        };

                        TedPermission.with(mContext.getApplicationContext())
                                .setPermissionListener(permissionlistener)
                                .setDeniedMessage("이미지를 저장할 수 있는 권한이 없습니다.\n권한을 설정해주세요.")
                                .setPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)
                                .check();

                        return;
                    }

                    // 없어도 되는 로직이지만 혹시몰라서 남겨둠
                    url = URLDecoder.decode(url, "UTF-8");
                    DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
                    request.setMimeType(mimeType);
                    request.addRequestHeader("User-Agent", userAgent);
                    request.setDescription("Downloading file");
                    String fileName = contentDisposition.replace("inline; filename=", "");
//                    fileName = fileName.replaceAll("\"", "");
                    fileName = System.currentTimeMillis() + ".png";
                    request.setTitle(fileName);
                    request.allowScanningByMediaScanner();
                    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                    request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);
                    DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                    dm.enqueue(request);
                    Toast.makeText(getApplicationContext(), "Downloading File", Toast.LENGTH_LONG).show();
                } catch (Exception e) {
                    if (ContextCompat.checkSelfPermission(mContext, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        // Should we show an explanation?
                        if (ActivityCompat.shouldShowRequestPermissionRationale(SubActivity.this,
                                android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                            Toast.makeText(getBaseContext(), "파일 다운로드를 위해\n동의가 필요합니다.", Toast.LENGTH_LONG).show();
                            ActivityCompat.requestPermissions(SubActivity.this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                    110);
                        } else {
                            Toast.makeText(getBaseContext(), "파일 다운로드를 위해\n동의가 필요합니다.", Toast.LENGTH_LONG).show();
                            ActivityCompat.requestPermissions(SubActivity.this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                    110);
                        }
                    }
                }
            }
        });
    }

    private String createAndSaveFileFromBase64Url(String url) {
        File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
//        File path = mContext.getApplicationContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        String filetype = url.substring(url.indexOf("/") + 1, url.indexOf(";"));
        String filename = System.currentTimeMillis() + "." + filetype;
        File file = new File(path, filename);

        try {
            if (!path.exists()) {
                path.mkdirs();
            }
            if (!file.exists()) {
                file.createNewFile();
            }

            String base64EncodedString = url.substring(url.indexOf(",") + 1);
            byte[] decodedBytes = Base64.decode(base64EncodedString, Base64.DEFAULT);
            OutputStream os = new FileOutputStream(file);
            os.write(decodedBytes);
            os.close();

            mContext.sendBroadcast(new Intent( Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)) );
            Toast.makeText(getApplicationContext(), "이미지 저장 완료", Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            Log.w("ExternalStorage", "Error writing " + file, e);
            Toast.makeText(getApplicationContext(), "이미지 저장 실패", Toast.LENGTH_LONG).show();
        }

        return file.toString();
    }

    @Override
    public void onBackPressed() {
        String url = webView.getUrl();
        Log.d(CommonConfig.SUB_ACTIVITY + " 뒤로가기 이벤트", url);

        if (androidBridge.isLayer()) {
            webView.loadUrl(CommonConfig.JAVASCRIPT_CLOSE_LAYER);
            androidBridge.setIsLayer(false);
        } else {
            if (webView.canGoBack()) {
                subActivityGoBack(url);
            } else {
                finish();
            }
        }
    }

    private class CustomWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            Log.d(CommonConfig.SUB_ACTIVITY + "check URL", url);

            // 웹 뒤로가기
            if (url.indexOf(CommonConfig.SCHEME_GO_BACK) > -1) {
                Log.d(CommonConfig.SUB_ACTIVITY + "뒤로가기 이벤트", url);
                if (webView.canGoBack()) {
                    subActivityGoBack(url);
                } else {
                    finish();
                }
            }
            // 주소록 앱 호출
            else if (url.indexOf(CommonConfig.SCHEME_GO_CONTACT) > -1) {
                runContactApp();
            }
            // 메인
            else if (url.indexOf(CommonConfig.URL_MAIN_MAIN) > -1) {
                finish();
            } else {
                view.loadUrl(url);
            }

            return true;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            // Display the keyboard automatically when relevant
            if (url.indexOf(CommonConfig.URL_MEMBER_PASSWORD_CHECK) > -1) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(view, 0);
            }
        }
    }

    @Override
    public void finish() {
        super.finish();
//        overridePendingTransition(R.anim.anim_slide_in_left, R.anim.anim_slide_out_right);
        overridePendingTransition(R.anim.not_move_activity, R.anim.anim_slide_out_right);
    }

    private void subActivityGoBack(String url) {
        WebBackForwardList mWebBackForwardList = webView.copyBackForwardList();
        String historyUrl = mWebBackForwardList.getItemAtIndex(mWebBackForwardList.getCurrentIndex()-1).getUrl();

        if (historyUrl.indexOf(CommonConfig.URL_MEMBER_PASSWORD_CHECK) > -1
                || url.indexOf(CommonConfig.URL_CONTRACT_SMS) > -1) {
            finish();
        } else {
            webView.goBack();
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
