package com.example.shoppinglist.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.example.shoppinglist.R;

public class CreditsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_credits);
    }

    public void goBackMainActivity(View view){
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    // metodo per aprire il profilo Instagram
    public void goToInstagramProfile(View view) {
        String instagramProfile = "https://www.instagram.com/nicomezzaa/";
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(instagramProfile));
        startActivity(intent);
    }

    // metodo per aprire il profilo GitHub
    public void goToGitHubProfile(View view) {
        String githubProfile = "https://github.com/NicoMezzaa";
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(githubProfile));
        startActivity(intent);
    }
}