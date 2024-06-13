package com.estifie.secureconnection;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import android.util.Base64;


public class TestActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        EditText edittext = findViewById(R.id.entry);
        EditText key = findViewById(R.id.key);
        EditText iv = findViewById(R.id.iv);
        Button decrypt = findViewById(R.id.decrypt);
        TextView data = findViewById(R.id.data);

        iv.setText("172D38434357620C20222D3843574105");
        key.setText("7B2B2E591DBB3AD54E32136ACD010507");

        decrypt.setOnClickListener(v -> {
            try {
                String encrypted = edittext.getText().toString();
                String keyText = key.getText().toString();
                String ivText = iv.getText().toString();

                String decryptedData = encryptAES(encrypted, keyText, ivText);
                data.setText(decryptedData);
            } catch (Exception e) {
                data.setText("Decryption failed: " + e.getMessage());
            }
        });
    }

    private String encryptAES(String encryptingText, String keyText, String ivText) throws Exception {

        int[] values1 = { 23, 45, 56, 67, 67, 87, 98, 12, 32, 34, 45, 56, 67, 87, 65, 5 };
        // Define the values for the second array
        int[] values2 = { 123, 43, 46, 89, 29, 187, 58, 213, 78, 50, 19, 106, 205, 1, 5, 7 };


        // Convert the first array to a byte array
        byte[] keyArray = new byte[values1.length];
        for (int i = 0; i < values1.length; i++) {
            keyArray[i] = (byte) values1[i];
        }

        // Convert the second array to a byte array
        byte[] ivArray = new byte[values2.length];
        for (int i = 0; i < values2.length; i++) {
            ivArray[i] = (byte) values2[i];
        }



        // Encrypt
        SecretKeySpec key = new SecretKeySpec(keyArray, "AES");
        IvParameterSpec iv = new IvParameterSpec(ivArray);

        Log.d("Encrypted", "Key: " + key);
        Log.d("Encrypted", "IV: " + iv);

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, key, iv);
        byte[] encrypted = cipher.doFinal(encryptingText.getBytes());

        String base64 = Base64.encodeToString(encrypted, Base64.DEFAULT);
        Log.d("Encrypted", base64);

        // Convert base64 to hex string
        StringBuilder hexString = new StringBuilder();
        for (byte b : encrypted) {
            hexString.append(String.format("%02X", b));
        }

        Log.d("Encrypted", hexString.toString());

        return hexString.toString();

    }

}