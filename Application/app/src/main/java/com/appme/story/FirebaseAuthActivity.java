package com.appme.story;

import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.appme.story.databinding.ActivityFirebaseAuthBinding;
import com.google.firebase.auth.UserInfo;

public class FirebaseAuthActivity extends AppCompatActivity {

    private ActivityFirebaseAuthBinding binding;

    //Deklarasi Variable
    private FirebaseAuth.AuthStateListener authListener;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFirebaseAuthBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //Instance Firebasee Auth
        auth = FirebaseAuth.getInstance();

        // Menambahkan Listener untuk mengecek apakah user telah logout / keluar
        authListener = new FirebaseAuth.AuthStateListener() {
                    @Override
                    public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                        // Jika Iya atau Null, maka akan berpindah pada halaman Login
                        FirebaseUser user = firebaseAuth.getCurrentUser();
                        if (user == null) {
                            Toast.makeText(FirebaseAuthActivity.this, "Logout Success",Toast.LENGTH_SHORT).show();                                                                       
                            startActivity(new Intent(FirebaseAuthActivity.this, LoginActivity.class));
                            finish();
                        } else {
                            // User is Login
                            String displayName = user.getDisplayName();
                            String email = user.getEmail();
                            // If the above were null, iterate the provider data
                            // and set with the first non null data
                            for (UserInfo userInfo : user.getProviderData()) {
                                if (displayName == null && userInfo.getDisplayName() != null) {
                                    displayName = userInfo.getDisplayName();
                                }
                                if (email == null && userInfo.getEmail() != null) {
                                    email = userInfo.getEmail();
                                }
                                 
                            }
                            StringBuilder sb = new StringBuilder();
                            sb.append("Welcome ").append(displayName);
                            sb.append("\n");
                            sb.append("Email : ").append(email);
                            binding.userEmail.setText(sb.toString());
                        }
                    }
                };

        binding.logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Fungsi untuk logout
                auth.signOut();
            }
        });
    }

    
    //Menerapkan Listener
    @Override
    protected void onStart() {
        super.onStart();
        auth.addAuthStateListener(authListener);
    }

    //Melepaskan Litener
    @Override
    protected void onStop() {
        super.onStop();
        if(authListener != null){
            auth.removeAuthStateListener(authListener);
        }
    }
}
