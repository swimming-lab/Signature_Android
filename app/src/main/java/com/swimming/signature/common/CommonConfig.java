package com.swimming.signature.common;

public final class CommonConfig {
    /**
     * URL
     */
    // 도메인
    public static final String DOMAIN = "http://signaturewas.cafe24.com";
//    public static final String DOMAIN = "http://172.20.10.4:8080/";
//    public static final String DOMAIN = "http://10.184.170.68:8080/";


    // 로그인
    public static final String URL_INDEX = "/";
    public static final String URL_LOGIN_MAIN = "/login/main.view";
    public static final String URL_LOGIN_SEARCH= "/login/search.view";
    public static final String URL_LOGIN_JOIN = "/login/join.view";
    public static final String URL_LOGIN_LOGIN = "/login/login.view";
    public static final String URL_LOGIN_LOGOUT = "/login/logout.view";
    // 메인
    public static final String URL_MAIN_MAIN = "/main/main.view";
    // 멤버
    public static final String URL_MEMBER_PASSWORD_CHECK = "/member/passwordCheck.view";
    public static final String URL_MEMBER_MYPAGE = "/member/mypage.view";
    // 계약서
    public static final String URL_CONTRACT_INSERT_FORM = "/contract/insertForm.view";
    public static final String URL_CONTRACT_UPDATE_FORM = "/contract/updateForm.view";
    public static final String URL_CONTRACT_READ_FORM = "/contract/readForm.view";
    public static final String URL_CONTRACT_INFO = "/contract/info.view";
    public static final String URL_CONTRACT_SMS = "/contract/sms.view";
    public static final String URL_CONTRACT_LOAD = "/contract/load.view";
    public static final String URL_CONTRACT_LIST = "/contract/list.view";
    // 장비
    public static final String URL_EQUIP_LIST = "/equip/list.view";
    // 체불신고
    public static final String URL_ARREARS_FORM = "/arrears/form.view";
    public static final String URL_ARREARS_LIST = "/arrears/list.view";

    /**
     * SUB 액티비티 그룹
     */
    public static final String SUB_GROUPS_ARREARS = "/arrears/";
    public static final String SUB_GROUPS_CONTRACT = "/contract/";
    public static final String SUB_GROUPS_MEMBER = "/member/";
    public static final String SUB_GROUPS_EQUIP = "/equip/";
    public static final String SUB_GROUPS_ADMIN = "/admin/";
    public static final String SUB_GROUPS_DIRECT = "/direct.view";

    /**
     * Custom Scheme
     */
    // 뒤로가기
    public static final String SCHEME_GO_BACK = "signatureapp://goBack";
    // 주소록 앱
    public static final String SCHEME_GO_CONTACT = "signatureapp://goContact";
    public static final String JAVASCRIPT_CLOSE_LAYER = "javascript:closeLayer()";
    public static final String JAVASCRIPT_SET_PHONE_NUMBER = "javascript:setPhoneNumber";
    public static String JAVASCRIPT_SET_PHONE_NUMBER(String param) {
        return JAVASCRIPT_SET_PHONE_NUMBER + "('" + param + "')";
    }

    /**
     * Common
     */
    public static final String APP_AGENT = " signatureapp_Android";
    public static final String APP_NAME = "SignatureApp";
    public static final int SUCCESS = 200;
    public static final int FAIL = 500;
    public static final int FINISH_APP_SECOND = 1500;
    public static final String FINISH_APP_MESSAGE = "종료하려면 한번 더 누르세요.";
    public static final String LOGIN_ACTIVITY = "LoginActivity";
    public static final String MAIN_ACTIVITY = "MainActivity";
    public static final String SUB_ACTIVITY = "SubActivity";
}
