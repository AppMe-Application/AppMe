package com.appme.story;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;
import android.net.Uri;
import android.os.Bundle;
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
import com.google.firebase.auth.UserProfileChangeRequest;

import com.appme.story.databinding.ActivityRegisterBinding;

public class RegisterActivity extends AppCompatActivity {

    //Deklarasi Variable
    
    private FirebaseAuth firebaseAuth;
    private String getUserName, getEmail, getPassword;
    private ActivityRegisterBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //Inisialisasi Widget dan Membuat Objek dari Firebae Authenticaion
        binding.progressBar.setVisibility(View.GONE);
        firebaseAuth = FirebaseAuth.getInstance();

        //Menyembunyikan / Hide Password
        binding.regPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());

        binding.regUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cekDataUser();
            }
        });
    }

    //Method ini digunakan untuk mengecek dan mendapatkan data yang dimasukan user
    private void cekDataUser(){
        //Mendapatkan dat yang diinputkan User
        getEmail = binding.regEmail.getText().toString();
        getPassword = binding.regPassword.getText().toString();
		getUserName = "AsepMo";
        //Mengecek apakah email dan sandi kosong atau tidak
        if(TextUtils.isEmpty(getEmail) || TextUtils.isEmpty(getPassword)){
            Toast.makeText(this, "Email atau Sandi Tidak Boleh Kosong", Toast.LENGTH_SHORT).show();
        }else{
            //Mengecek panjang karakter password baru yang akan didaftarkan
            if(getPassword.length() < 6){
                Toast.makeText(this, "Sandi Terlalu Pendek, Minimal 6 Karakter", Toast.LENGTH_SHORT).show();
            }else {
                binding.progressBar.setVisibility(View.VISIBLE);
                firebaseAuth.createUserWithEmailAndPassword(getEmail, getPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
						@Override
						public void onComplete(@NonNull Task<AuthResult> task) {
							//Mengecek status keberhasilan saat medaftarkan email dan sandi baru
							if(task.isSuccessful()){
								createUserAccount(getUserName, null);
							}else {
								Toast.makeText(RegisterActivity.this, "Terjadi Kesalahan, Silakan Coba Lagi", Toast.LENGTH_SHORT).show();
								binding.progressBar.setVisibility(View.GONE);
							}					
						}
					});
            }
        }
    }

    //Method ini digunakan untuk membuat akun baru user
    private void createUserAccount(String userName, Uri uri){
		UserProfileChangeRequest.Builder profileUpdate = new UserProfileChangeRequest.Builder();
			profileUpdate.setDisplayName(userName);
            if(!TextUtils.isEmpty(uri.getPath())){
                profileUpdate.setPhotoUri(uri);
            }
            
		UserProfileChangeRequest profile = profileUpdate.build();
        
        // Mengecek apakah user yang sudah login / belum logout
        FirebaseUser user = firebaseAuth.getCurrentUser();
		user.updateProfile(profile).addOnCompleteListener(new OnCompleteListener<Void>() {
				@Override
				public void onComplete(@NonNull Task<Void> task) {
					//Mengecek status keberhasilan saat medaftarkan email dan sandi baru
					if(task.isSuccessful()){
						Toast.makeText(RegisterActivity.this, "Sign Up Success", Toast.LENGTH_SHORT).show();
						finish();
					}else {
						Toast.makeText(RegisterActivity.this, "Terjadi Kesalahan, Silakan Coba Lagi", Toast.LENGTH_SHORT).show();
						binding.progressBar.setVisibility(View.GONE);
					}
				}
			});    
    }
}
