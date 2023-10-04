package com.appme.story;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.text.TextUtils;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import com.appme.story.databinding.ActivityLoginBinding;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener{

    //Deklarasi Variable
    private FirebaseAuth auth;
    private FirebaseAuth.AuthStateListener listener;
    private String getEmail, getPassword;
    private ActivityLoginBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        binding.register.setOnClickListener(this);
        binding.login.setOnClickListener(this);
        binding.progressBar.setVisibility(View.GONE);

        //Menyembunyikan / Hide Password
        binding.getPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());

        //Instance / Membuat Objek Firebase Authentication
        auth = FirebaseAuth.getInstance();

        // Mengecek Keberadaan User
        listener = new FirebaseAuth.AuthStateListener() {
                    @Override
                    public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                        // Mengecek apakah ada user yang sudah login / belum logout
                        FirebaseUser user = firebaseAuth.getCurrentUser();
                        if (user != null) {
							Log.d("Testing", "User Profile Updated");
							runOnUiThread(new Runnable() {
									@Override
									public void run() {
										// Jika ada, maka halaman
										// akan langsung
										// berpidah pada
										// MainActivity
										startActivity(new Intent(LoginActivity.this, FirebaseAuthActivity.class));                                                                                             
										finish();
									}
								});
                        }
                    }
                };
    }

    //Menerapkan Listener
    @Override
    protected void onStart() {
        super.onStart();
        auth.addAuthStateListener(listener);
    }

    //Melepaskan Litener
    @Override
    protected void onStop() {
        super.onStop();
        if(listener != null){
            auth.removeAuthStateListener(listener);
        }
    }

    //Method ini digunakan untuk proses autentikasi user menggunakan email dan kata sandi
    private void loginUserAccount(){
        auth.signInWithEmailAndPassword(getEmail, getPassword)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        //Mengecek status keberhasilan saat login
                        if(task.isSuccessful()){
                            Toast.makeText(LoginActivity.this, "Login Success", Toast.LENGTH_SHORT).show();
                        }else {
                            Toast.makeText(LoginActivity.this, "Terjadi Kesalahan, Silakan Coba Lagi", Toast.LENGTH_SHORT).show();
                            binding.progressBar.setVisibility(View.GONE);
                        }
                    }
                });
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.register){
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        }else if(v.getId() == R.id.login){
                //Mendapatkan dat yang diinputkan User
                getEmail = binding.getEmail.getText().toString();
                getPassword = binding.getPassword.getText().toString();

                //Mengecek apakah email dan sandi kosong atau tidak
                if(TextUtils.isEmpty(getEmail) || TextUtils.isEmpty(getPassword)){
                    Toast.makeText(this, "Email atau Sandi Tidak Boleh Kosong", Toast.LENGTH_SHORT).show();
                }else{
                    loginUserAccount();
                    binding.progressBar.setVisibility(View.VISIBLE);
                }
        }
    }
}
