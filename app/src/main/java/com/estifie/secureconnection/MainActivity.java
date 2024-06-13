package com.estifie.secureconnection;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.io.InputStream;
import java.io.IOException;


import androidx.core.app.ActivityCompat;

import java.io.InputStream;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends Activity {

    private FirebaseAuth mAuth;

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothDevice device;
    private BluetoothSocket socket;
    private OutputStream outputStream;
    private InputStream inputStream;
    private Handler handler;

    private EditText entry;
    private TextView data;

    private Spinner devices;
    private String serverIpAddress;
    private TextView title;
    private TextView label;
    private Button openButton;

    private Button action1Button;
    private Button action2Button;
    private Button action3Button;

    private boolean isAction1 = false;
    private boolean isAction2 = false;
    private boolean isAction3 = false;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        label = findViewById(R.id.label);
        title = findViewById(R.id.title);
        entry = findViewById(R.id.entry);
        data = findViewById(R.id.data);
        devices = findViewById(R.id.devicesList);

        openButton = findViewById(R.id.open);
        Button sendButton = findViewById(R.id.send);
        Button findButton = findViewById(R.id.find);

        action1Button = findViewById(R.id.action1);
        action2Button = findViewById(R.id.action2);
        action3Button = findViewById(R.id.action3);
        action1Button.setText("LED ON");
        action2Button.setText("SIREN ON");
        action3Button.setText("TRUNK ON");
        action1Button.setVisibility(View.GONE);
        action2Button.setVisibility(View.GONE);
        action3Button.setVisibility(View.GONE);
        label.setVisibility(View.GONE);
        openButton.setVisibility(View.GONE);
        entry.setVisibility(View.GONE);
        sendButton.setVisibility(View.GONE);
        devices.setVisibility(View.GONE);
        findButton.setVisibility(View.VISIBLE);
        data.setVisibility(View.GONE);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();


        openButton.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("MissingPermission")
            @Override
            public void onClick(View view) {
                UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

                try {

                    socket = device.createRfcommSocketToServiceRecord(uuid);
                    socket.connect();

                    inputStream = socket.getInputStream();
                    listenForData();

                    openButton.setText("Cihaz Bağlı");
                    title.setText("Send Message To The Server Via Bluetooth");
                    label.setVisibility(View.VISIBLE);
                    entry.setVisibility(View.VISIBLE);
                    sendButton.setVisibility(View.VISIBLE);
                    openButton.setVisibility(View.GONE);
                    data.setVisibility(View.VISIBLE);
                    devices.setVisibility(View.GONE);
                    data.setText("Cihaza bağlanıldı!");
                    findButton.setVisibility(View.GONE);
                } catch (Exception e) {

                }
            }
        });


        findButton.setOnClickListener(view -> findPairedDevices());

        sendButton.setOnClickListener(view -> sendData());

        action1Button.setOnClickListener(view -> executeAction(1));
        action2Button.setOnClickListener(view -> executeAction(2));
        action3Button.setOnClickListener(view -> executeAction(3));

        devices.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedDeviceName = (String) parent.getItemAtPosition(position);
                setSelectedDevice(selectedDeviceName);
                openButton.setVisibility(View.VISIBLE);
                openButton.setText("Connect to " + selectedDeviceName);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });

        handler = new Handler(msg -> {
            String receivedData = (String) msg.obj;
            data.setText("Sunucu Adresi" + receivedData);
            action1Button.setVisibility(View.VISIBLE);
            action2Button.setVisibility(View.VISIBLE);
            action3Button.setVisibility(View.VISIBLE);
            return true;
        });
    }



    @SuppressLint("MissingPermission")
    private void findPairedDevices() {
        devices.setVisibility(View.VISIBLE);
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        List<String> deviceNames = new ArrayList<>();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice bt : pairedDevices) {
                deviceNames.add(bt.getName());
                {
                    devices.setVisibility(View.VISIBLE);
                    title.setText("Select A Device To Connect");
                    openButton.setVisibility(View.VISIBLE);
                }
            }
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, deviceNames);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            devices.setAdapter(adapter);
        }
    }

    @SuppressLint({"MissingPermission", "SetTextI18n"})
    private void sendData() {
        try {
            outputStream = socket.getOutputStream();
            String message = entry.getText().toString();
            // integer variable of message length
            int msgLength = message.length();
            message = encryptAES(message) + "," + msgLength;
            outputStream.write(message.getBytes());
            data.setText("Message sent: " + message);
        } catch (IOException e) {
            e.printStackTrace();
            data.setText("An error occurred. Try sending again!");
            Toast.makeText(MainActivity.this, "An error occured. Try sending again!", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void closeBluetooth() throws IOException {
        if (outputStream != null) {
            outputStream.close();
        }
        if (socket != null) {
            socket.close();
        }
        data.setText("Bluetooth connection closed");
    }

    @SuppressLint("MissingPermission")
    private void setSelectedDevice(String deviceName) {
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        for (BluetoothDevice bt : pairedDevices) {
            if (bt.getName().equals(deviceName)) {
                device = bt;
                Toast.makeText(MainActivity.this, "Selected device: " + device.getName(), Toast.LENGTH_SHORT).show();
                break;
            }
        }
    }

    private void listenForData() {
        Thread thread = new Thread(() -> {
            byte[] buffer = new byte[1024];
            int bytes;
            while (true) {
                try {
                    bytes = inputStream.read(buffer);
                    Log.d("Bluetooth", "Bytes: " + bytes);
                    Log.d("Bluetooth", "Buffer: " + buffer);
                    if (bytes > 10) {
                        final String incomingMessage = new String(buffer, 0, bytes);
                        Log.d("Bluetooh", incomingMessage);
                        serverIpAddress = incomingMessage.substring(3);
                        handler.obtainMessage(0, serverIpAddress).sendToTarget();
                    }
                } catch (IOException e) {
                    Log.e("Bluetooth", "Error during data reception", e);
                    break;
                }
            }
        });
        thread.start();
    }

    private void executeAction(int action) {
        String url;
        String endpoint;

        switch (action) {
            case 1:
                if (isAction1) {
                    endpoint = "ledoff";
                    action1Button.setText("LED ON");
                    data.setText("Led is turned off!");
                    isAction1 = false;
                } else {
                    endpoint = "ledon";
                    action1Button.setText("LED OFF");
                    data.setText("Led is turned on!");
                    isAction1 = true;
                }
                break;
            case 2:
                if (isAction2) {
                    endpoint = "sirenoff";
                    action2Button.setText("SIREN ON");
                    data.setText("Siren kapatıldı!");
                    isAction2 = false;
                } else {
                    endpoint = "sirenon";
                    action2Button.setText("SIREN OFF");
                    data.setText("Siren açıldı!");
                    isAction2 = true;
                }
                break;
            case 3:
                if (isAction3) {
                    endpoint = "trunkoff";
                    action3Button.setText("TRUNK ON");
                    data.setText("Bagaj kapatıldı!");
                    isAction3 = false;
                } else {
                    endpoint = "trunkon";
                    action3Button.setText("TRUNK OFF");
                    data.setText("Bagaj açıldı!");
                    isAction3 = true;
                }
                break;
            default:
                endpoint = "ledon";
                break;
        }

        // Send request to the serverIpAddress + /endpoint
        url = "https://" + serverIpAddress + "/" + endpoint;

        // Send request to the server
        sendRequest(url);

    }

    public class CustomX509TrustManager implements X509TrustManager {
        public void checkClientTrusted(X509Certificate[] chain, String authType) {
        }

        public void checkServerTrusted(X509Certificate[] chain, String authType) {
        }

        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }
    }
    private SSLSocketFactory getSSLSocketFactory() throws Exception {
        TrustManager[] trustAllCerts = new TrustManager[] { new CustomX509TrustManager() };

        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, trustAllCerts, new java.security.SecureRandom());

        return sslContext.getSocketFactory();
    }

    private void sendRequest(String url) {
        new Thread(() -> {
            try {
                Log.d("Request", "Sending request to " + url);
                java.net.URL urlObj = new java.net.URL(url);
                java.net.HttpURLConnection connection = (java.net.HttpURLConnection) urlObj.openConnection();

                if (connection instanceof HttpsURLConnection) {
                    HttpsURLConnection httpsConnection = (HttpsURLConnection) connection;
                    httpsConnection.setSSLSocketFactory(getSSLSocketFactory());
                    httpsConnection.setHostnameVerifier((hostname, session) -> true);
                }
                connection.setRequestMethod("GET");
                runOnUiThread(() -> data.setText("Request is sending, please wait!"));
                connection.connect();
                runOnUiThread(() -> data.setText("Request is sent"));
                connection.getResponseCode();
                runOnUiThread(() -> data.setText("Request successful"));
            } catch (Exception e) {
            }
        }).start();
    }

    private String encryptAES(String encryptingText) throws Exception {

        int[] values1 = {23, 45, 56, 67, 67, 87, 98, 12, 32, 34, 45, 56, 67, 87, 65, 5};
        // Define the values for the second array
        int[] values2 = {123, 43, 46, 89, 29, 187, 58, 213, 78, 50, 19, 106, 205, 1, 5, 7};


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