package com.androsoft.heschecker;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.androsoft.heschecker.Utils.HesCodeScan;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.ResultPoint;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;
import com.journeyapps.barcodescanner.DefaultDecoderFactory;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private DecoratedBarcodeView barcodeView;
    String lastText;
    HesCodeScan hesCodeScan;
    SharedPreferences sharedPreferences;
    AlertDialog ad;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        barcodeView = findViewById(R.id.barcode_scanner);
        hesCodeScan = new HesCodeScan();
        sharedPreferences = getSharedPreferences(BuildConfig.APPLICATION_ID, MODE_PRIVATE);
        loginRun();

    }

    void loginRun(){
        if (getTc() == null || getPass() == null){
            View view = getLayoutInflater().inflate(R.layout.get_info_layout, null);
            AlertDialog.Builder ab = new AlertDialog.Builder(this).setView(view).setCancelable(false);
            view.findViewById(R.id.login).setOnClickListener(view1 -> {
                TextInputLayout tcLayout = view.findViewById(R.id.tcLayout), passLayout = view.findViewById(R.id.passLayout);
                TextInputEditText tc = view.findViewById(R.id.tc), pass = view.findViewById(R.id.pass);
                String tcStr = tc.getText().toString().trim(), passStr = pass.getText().toString().trim();
                if (tcStr.isEmpty()){
                    tcLayout.setError("Lütfen Bu Alanı Doldurunuz");
                    return;
                }

                if (passStr.isEmpty()){
                    passLayout.setError("Lütfen Bu Alanı Doldurunuz");
                    return;
                }
                setPass(passStr);
                setTc(tcStr);

                login(tcStr, passStr);
            });
            ad = ab.create();
            ad.show();
        }else{
            login(getTc(), getPass());
        }
    }

    void login(String tc, String pass){
        hesCodeScan.login(tc, pass, new HesCodeScan.LoginListener() {
            @Override
            public void onSuccess() {
                if (ad != null) ad.dismiss();
                runOnUiThread(() -> init());
            }

            @Override
            public void onError(String errorName) {
                setTc(null);
                setPass(null);

                Log.wtf("sadas", errorName);
                runOnUiThread(() -> Toast.makeText(getApplicationContext(), errorName, Toast.LENGTH_LONG).show());
            }
        });
    }

    String getTc() {
        return sharedPreferences.getString("EdevletTc", null);
    }

    String getPass() {
        return sharedPreferences.getString("EdevletPass", null);
    }

    void setPass(String pass) {
        sharedPreferences.edit().putString("EdevletPass", pass).apply();
    }

    void setTc(String tc) {
        sharedPreferences.edit().putString("EdevletTc", tc).apply();
    }

    void init() {

        Collection<BarcodeFormat> formats = Arrays.asList(BarcodeFormat.QR_CODE, BarcodeFormat.CODE_39);
        barcodeView.getBarcodeView().setDecoderFactory(new DefaultDecoderFactory(formats));
        barcodeView.initializeFromIntent(getIntent());
        barcodeView.decodeContinuous(new BarcodeCallback() {
            @Override
            public void barcodeResult(BarcodeResult result) {
                if (result.getText() == null) {
                    return;
                }else{
                    if (lastText != null){
                        if (lastText.equals(result.getText())){
                            return;
                        }
                    }
                }
                Log.wtf("sadas", result.getText());

                lastText = result.getText();
                String[] data = lastText.split("\\|");
                if (data.length == 2) {
                    String code = data[1];
                    hesCodeScan.scan(code, new HesCodeScan.ScanListener() {
                        @Override
                        public void onSuccess(String tc, String status, String nameAndSurname, String vaccination, String isHasta, String negatifStatus) {
                            runOnUiThread(() -> {
                                TextView dataText = findViewById(R.id.data);
                                LinearLayout rootLayout = findViewById(R.id.rootLayout);
                                if (status.equals("Riskli Değil")){
                                    rootLayout.setBackgroundColor(Color.GREEN);
                                }else{
                                    rootLayout.setBackgroundColor(Color.RED);
                                }

                                dataText.setText(String.format("%s\n%s\n%s", tc, status, nameAndSurname));
                            });
                        }

                        @Override
                        public void onError(String errorName) {
                            runOnUiThread(() -> {
                                if (errorName.equals("GİRİŞ BAŞARISIZ")){
                                    barcodeView.pause();
                                    Toast.makeText(getApplicationContext(), "Tekrar Giriş Yapılıyor, Lütfen Bekleyiniz", Toast.LENGTH_LONG).show();
                                    login(getTc(), getPass());
                                }else{
                                    Toast.makeText(getApplicationContext(), errorName, Toast.LENGTH_LONG).show();
                                }

                            });
                        }
                    });
                }

            }

            @Override
            public void possibleResultPoints(List<ResultPoint> resultPoints) {
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();

        if (barcodeView != null) barcodeView.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (barcodeView != null) barcodeView.pause();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return barcodeView.onKeyDown(keyCode, event) || super.onKeyDown(keyCode, event);
    }

}
