package com.estifie.secureconnection;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class KayitActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    private EditText eposta;
    private EditText sifre;
    private EditText sifreTekrar;

    private Button kayitOl;
    private TextView girisYap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kayit);

        mAuth = FirebaseAuth.getInstance();

        eposta = findViewById(R.id.eposta);
        sifre = findViewById(R.id.sifre);
        sifreTekrar = findViewById(R.id.sifreTekrar);
        kayitOl = findViewById(R.id.kayitOl);
        girisYap = findViewById(R.id.girisYap);

        kayitOl.setOnClickListener(v -> {
            if (sifre.getText().toString().equals(sifreTekrar.getText().toString())) {
                if (sifre.getText().toString().length() < 6) {
                    Toast.makeText(KayitActivity.this, "Password should be at least 6 characters.", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (eposta.getText().toString().isEmpty()) {
                    Toast.makeText(KayitActivity.this, "Email cannot be empty.", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (sifre.getText().toString().isEmpty()) {
                    Toast.makeText(KayitActivity.this, "Password cannot be empty.", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!eposta.getText().toString().contains("@")) {
                    Toast.makeText(KayitActivity.this, "Please enter a valid email", Toast.LENGTH_SHORT).show();
                    return;
                }

                kayitOl(eposta.getText().toString(), sifre.getText().toString());
            } else {
                Toast.makeText(KayitActivity.this, "Passwords doesnt match", Toast.LENGTH_SHORT).show();
            }
        });

        girisYap.setOnClickListener(v -> {
            Intent intent = new Intent(KayitActivity.this, AuthActivity.class);
            startActivity(intent);
        });
    }

    private void kayitOl(String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(KayitActivity.this, "Register success.",
                                Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(KayitActivity.this, MainActivity.class);
                        startActivity(intent);
                    } else {

                        Toast.makeText(KayitActivity.this, "Register failed.",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }


}