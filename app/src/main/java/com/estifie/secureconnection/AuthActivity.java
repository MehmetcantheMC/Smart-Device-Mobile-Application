package com.estifie.secureconnection;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;


import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class AuthActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;

    private EditText eposta;
    private EditText sifre;
    private Button girisYap;
    private TextView kayitOl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mAuth = FirebaseAuth.getInstance();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        eposta = findViewById(R.id.eposta);
        sifre = findViewById(R.id.sifre);
        girisYap = findViewById(R.id.girisYap);
        kayitOl = findViewById(R.id.kayitOl);

        girisYap.setOnClickListener(v -> signIn(eposta.getText().toString(), sifre.getText().toString()));
        kayitOl.setOnClickListener(v -> {
            Intent intent = new Intent(AuthActivity.this, KayitActivity.class);
            startActivity(intent);
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            reload();
        }
    }

    private void signIn(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Intent intent = new Intent(AuthActivity.this, MainActivity.class);
                        startActivity(intent);
                    } else {
                        Toast.makeText(AuthActivity.this, "There was an error signing in.",
                                Toast.LENGTH_LONG).show();
                    }
                });
    }


    private void reload() { }

}