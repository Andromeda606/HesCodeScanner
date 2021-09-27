package com.androsoft.heschecker.Utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.androsoft.heschecker.BuildConfig;

import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Response;

public class HesCodeScan {

    private String cookie = "";

    void setCookie(String cookie) {
        this.cookie = cookie;
    }

    String getCookie() {
        return cookie;
    }

    public void login(String tc, String pass, LoginListener loginListener) {
        RequestNetwork req = new RequestNetwork(null);
        req.setHeaders(new HashMap<String, Object>() {{
            put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/93.0.4577.82 Safari/537.36");
        }});
        req.startRequestNetworkOkHTTP(RequestNetworkController.GET, "https://giris.turkiye.gov.tr/Giris/gir", new RequestNetwork.RequestListenerDetail() {
            @Override
            public void onResponse(String response, Response responseOkHttp) {
                String regex = "currentPageToken\" value=\"(.*?)\"";

                Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE);
                Matcher matcher = pattern.matcher(response);
                String currentPageToken = "";
                while (matcher.find()) {
                    currentPageToken = matcher.group(1);
                    Log.wtf("currentPageToken", currentPageToken);
                }

                List<String> cookies = responseOkHttp.headers().toMultimap().get("Set-Cookie");
                StringBuilder fullCookie = new StringBuilder();
                for (String cookie : cookies) {
                    String[] data = cookie.split("=");
                    fullCookie.append(data[0]).append("=").append(data[1].split(";")[0]).append(";");
                }

                RequestNetwork requestNetwork = new RequestNetwork(null);
                String finalCurrentPageToken = currentPageToken;
                requestNetwork.setParams(new HashMap<String, Object>() {{
                    put("tridField", tc);
                    put("encTridField", "");
                    put("encEgpField", "");
                    put("jsonData", "");
                    put("submitButton", "Giri%C5%9F+Yap");
                    put("egpField", pass);
                    put("currentPageToken", finalCurrentPageToken);
                    put("actionName", "giris");
                }}, RequestNetworkController.REQUEST_PARAM);
                requestNetwork.setHeaders(new HashMap<String, Object>() {{
                    put("Cookie", fullCookie.toString());
                    put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/93.0.4577.82 Safari/537.36");
                }});
                requestNetwork.startRequestNetworkOkHTTP(RequestNetworkController.POST, "https://giris.turkiye.gov.tr/Giris/gir", new RequestNetwork.RequestListenerDetail() {
                    @Override
                    public void onResponse(String response, Response responseOkHttp) {
                        Log.wtf("STATUS", response);
                        Log.wtf("STATUSCODE", String.valueOf(responseOkHttp.code()));
                        if (responseOkHttp.code() != 302) {
                            Log.wtf("STATUSCODE", "GİRİŞ YAPILAMADI");
                            loginListener.onError("Başarısız! Şifrenizi Kontrol Edin");
                            return;
                        }
                        List<String> cookies = responseOkHttp.headers().toMultimap().get("SET-COOKIE");
                        StringBuilder fullCookie = new StringBuilder();
                        for (String cookie : cookies) {
                            if (cookie.contains("w3a")) {
                                String[] data = cookie.split("=");
                                fullCookie.append(data[0]).append("=").append(data[1].split(";")[0]).append(";");
                            }
                        }

                        setCookie(fullCookie.toString());
                        loginListener.onSuccess();

                    }

                    @Override
                    public void onErrorResponse(String tag, String message) {
                        loginListener.onError("Başarısız! İnternetinizi Kontrol Edin!");
                    }
                });

            }

            @Override
            public void onErrorResponse(String tag, String message) {
                loginListener.onError("Başarısız! İnternetinizi Kontrol Edin!");
            }
        });
    }


    public void scan(String code, ScanListener scanListener) {
        final String[] sessId = {""};
        RequestNetwork requestNetwork2 = new RequestNetwork(null);
        requestNetwork2.setHeaders(new HashMap<String, Object>() {{
            put("Cookie", getCookie());
            put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/93.0.4577.82 Safari/537.36");

        }});
        requestNetwork2.startRequestNetworkOkHTTP(RequestNetworkController.GET, "https://www.turkiye.gov.tr/", new RequestNetwork.RequestListenerDetail() {
            @Override
            public void onResponse(String response, Response responseOkHttp) {
                List<String> cookies = responseOkHttp.headers().toMultimap().get("Set-Cookie");
                StringBuilder fullCookie = new StringBuilder();
                for (String cookie : cookies) {
                    String[] data = cookie.split("=");
                    fullCookie.append(data[0]).append("=").append(data[1].split(";")[0]).append(";");
                }

                for (String cookie : cookies) {
                    String[] data = cookie.split("=");
                    if (cookie.contains("TURKIYESESSIONID")) {
                        sessId[0] = ";" + data[0] + "=" + data[1].split(";")[0];
                    }
                }


                final String[] w3p = {""};
                RequestNetwork requestNetwork = new RequestNetwork(null);
                requestNetwork.setHeaders(new HashMap<String, Object>() {{
                    put("Cookie", getCookie() + fullCookie.toString() + sessId[0]);
                    put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/93.0.4577.82 Safari/537.36");

                }});
                requestNetwork.startRequestNetworkOkHTTP(RequestNetworkController.GET, "https://www.turkiye.gov.tr/saglik-bakanligi-hes-kodu-sorgulama", new RequestNetwork.RequestListenerDetail() {
                    @Override
                    public void onResponse(String response, Response responseOkHttp) {
                        String regex = "body data-token=\"(.*?)\"";

                        Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE);
                        Matcher matcher = pattern.matcher(response);
                        String token = "";
                        while (matcher.find()) {
                            token = matcher.group(1);
                            Log.wtf("token", token);
                        }

                        for (String cookie : cookies) {
                            String[] data = cookie.split("=");
                            if (cookie.contains("w3p")) {
                                w3p[0] = data[0] + "=" + data[1].split(";")[0];
                            }
                        }

                        RequestNetwork requestNetwork = new RequestNetwork(null);
                        HashMap<String, Object> body = new HashMap<>();
                        body.put("hes_kodu", code.toUpperCase());
                        body.put("token", token);
                        HashMap<String, Object> header = new HashMap<>();
                        header.put("Cookie", getCookie() + fullCookie.toString() + w3p[0] + sessId[0]);
                        header.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/93.0.4577.82 Safari/537.36");
                        requestNetwork.setHeaders(header);
                        requestNetwork.setParams(body, RequestNetworkController.REQUEST_PARAM);
                        String finalToken = token;
                        requestNetwork.startRequestNetworkOkHTTP(RequestNetworkController.POST, "https://www.turkiye.gov.tr/saglik-bakanligi-hes-kodu-sorgulama?submit", new RequestNetwork.RequestListenerDetail() {
                            @Override
                            public void onResponse(String response, Response responseOkHttp) {
                                if (responseOkHttp.code() == 200){
                                    scanListener.onError("GİRİŞ BAŞARISIZ");
                                    return;
                                }
                                List<String> cookies = responseOkHttp.headers().toMultimap().get("Set-Cookie");
                                StringBuilder fullCookie = new StringBuilder();
                                for (String cookie : cookies) {
                                    String[] data = cookie.split("=");
                                    fullCookie.append(data[0]).append("=").append(data[1].split(";")[0]).append(";");
                                }

                                loopShow(getCookie() + fullCookie.toString() + w3p[0] + sessId[0], scanListener);


                            }

                            @Override
                            public void onErrorResponse(String tag, String message) {
                                scanListener.onError("Başarısız! İnternetinizi Kontrol Edin!");
                            }
                        });

                    }

                    @Override
                    public void onErrorResponse(String tag, String message) {
                        scanListener.onError("Başarısız! İnternetinizi Kontrol Edin!");
                    }
                });

            }

            @Override
            public void onErrorResponse(String tag, String message) {
                scanListener.onError("Başarısız! İnternetinizi Kontrol Edin!");
            }
        });
        Log.wtf("CODE", code);

    }


    void loopShow(String cookie, ScanListener scanListener) {
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                RequestNetwork req = new RequestNetwork(null);
                req.setHeaders(new HashMap<String, Object>() {{
                    put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/93.0.4577.82 Safari/537.36");
                    put("Cookie", cookie);
                }});
                req.startRequestNetworkOkHTTP(RequestNetworkController.GET, "https://www.turkiye.gov.tr/saglik-bakanligi-hes-kodu-sorgulama?sonuc=Goster", new RequestNetwork.RequestListenerDetail() {
                    @Override
                    public void onResponse(String response, Response responseOkHttp) {
                        if (response.contains("Kuyru")) {
                            loopShow(cookie, scanListener);
                        } else {
                            final String regex = "<dl class=\"compact\">\\s.*<dt>Adı Soyadı<\\/dt>\\s.*<dd>(.*?)<\\/dd>\\s.*<dt>T\\.C\\. Kimlik Numarası<\\/dt>\\s.*<dd>(.*?)<\\/dd>\\s.*<dt>HES Kodu<\\/dt>\\s.*<dd>(.*?)<\\/dd>\n"
                                    + "\\s.*<dt>Risk Durumu<\\/dt>\n"
                                    + "\\s.*<dd>(.*?)<\\/dd>\n"
                                    + "\\s.*<dt>Geçerlilik Zamanı<\\/dt>\n"
                                    + "\\s.*<dd>(.*?)<\\/dd>";

                            final Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE);
                            final Matcher matcher = pattern.matcher(response);
                            String tc = "", status = "", nameSurname = "";
                            while (matcher.find()) {

                                nameSurname = matcher.group(1);
                                tc = matcher.group(2);
                                status = matcher.group(4);
                            }
                            scanListener.onSuccess(tc, status, nameSurname);

                        }
                    }

                    @Override
                    public void onErrorResponse(String tag, String message) {
                        scanListener.onError(message);
                    }
                });
            }
        }, 500);
    }

    public interface LoginListener {
        public void onSuccess();

        public void onError(String errorName);
    }

    public interface ScanListener {
        public void onSuccess(String tc, String status, String nameAndSurname);

        public void onError(String errorName);
    }
}


