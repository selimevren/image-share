package com.stjerneklart.imgshare;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Firebase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class Signup extends AppCompatActivity {

    private EditText nameText,surnameText,emailText,passwordText;
    private FirebaseAuth mAuth;
    private Button signup;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        mAuth = FirebaseAuth.getInstance();
        nameText = findViewById(R.id.name);
        surnameText = findViewById(R.id.surname);
        emailText = findViewById(R.id.mail);
        passwordText = findViewById(R.id.sifre);

        signup = findViewById(R.id.GirisEkran);

        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = nameText.getText().toString().trim();
                String email = emailText.getText().toString().trim();
                String surname = surnameText.getText().toString().trim();
                String password = passwordText.getText().toString().trim();

                if(name.isEmpty()){
                        nameText.setError("Lütfen bir isim giriniz");
                        return;
                 }
                 if (email.isEmpty()){
                emailText.setError("lütfen bir email giriniz");
                return;
                 }
                if(surname.isEmpty()){
                    surnameText.setError("lütfen bir soyad giriniz");
                    return;
                }
                if (password.isEmpty()) {
                    passwordText.setError("lütfen bir şifre giriniz");
                    return;
                }
                    mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(Signup.this, task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(Signup.this, "üyelik başarı ile oluşturuldu", Toast.LENGTH_SHORT).show();

                            FirebaseFirestore db = FirebaseFirestore.getInstance();
                            Map<String, Object> user = new HashMap<>();
                            user.put("name",name);
                            user.put("surname", surname);
                            db.collection("users").document(mAuth.getUid()).set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    Log.d(TAG,"Başarılı bir şekilde oluşturuldu");
                                }
                            })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.w(TAG,"Hata oluştu");
                                        }
                                    });
                        }
                        else{
                            Exception e = task.getException();
                            if (e != null) {
                                Toast.makeText(Signup.this, "hata", Toast.LENGTH_SHORT).show();

                            }
                        }
                    });
                Button login = (Button) findViewById(R.id.kayitOl);
                login.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(Signup.this, Login.class);
                                startActivity(intent);
                    }
                });
                }
        });
        Button login = (Button) findViewById(R.id.kayitOl);
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Signup.this, Login.class);
                startActivity(intent);
            }
        });
    }
}