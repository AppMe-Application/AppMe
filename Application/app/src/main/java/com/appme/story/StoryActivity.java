package com.appme.story;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.Menu;

import android.widget.Toast;
import androidx.annotation.CallSuper;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.navigation.NavigationView;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;

import com.appme.story.databinding.ActivityMainBinding;
import com.google.firebase.auth.FirebaseAuth;

public class StoryActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.appBarMain.toolbar);
        binding.appBarMain.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                  Intent intent = new Intent(StoryActivity.this, FirebaseAuthActivity.class);
                  startActivity(intent); 
            }
        });
        
        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;
        
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow)
                .setOpenableLayout(drawer)
                .build();
        
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
        handleIntent(getIntent());
    }

    public void logout() {
        // Digunakan Untuk Logout
        FirebaseAuth.getInstance().signOut();
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // TODO: Implement this method
        if(item.getItemId() == R.id.action_settings){
            Intent intent = new Intent(this, QuickStartActivity.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }
    
    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration) || super.onSupportNavigateUp();
    }
    
    @Override
    @CallSuper
    protected void onNewIntent(Intent arg0) {
        super.onNewIntent(arg0);
        // TODO: Implement this method
        handleIntent(arg0);
    }

    private void handleIntent(Intent intent) {
        String appLinkAction = intent.getAction();
        Uri appLinkData = intent.getData();
        if (Intent.ACTION_VIEW.equals(appLinkAction) && appLinkData != null) {
            String recipeId = appLinkData.getLastPathSegment();
            Uri appData = Uri.parse("content://appme-booster.web.app/.well-known/assetlink.json")
                            .buildUpon()
                            .appendPath(recipeId)
                            .build();
            showRecipe(appData);
        }
    }
    
    private void showRecipe(Uri uri){
        Toast.makeText(this, uri.getPath(), Toast.LENGTH_SHORT).show();
    }
    
    private void snackBar(View view, String message){
       Snackbar.make(view, message, Snackbar.LENGTH_LONG).setAction("Action", new View.OnClickListener(){
                            @Override
                            public void onClick(View v) {
                    
                            }
                        }).show();
    }
}
