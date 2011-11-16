package com.twiturr;

import android.widget.Toast;
import android.widget.Button;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.content.Intent;

public class PrefsActivity extends PreferenceActivity {
    Button authButton;

    @Override protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	addPreferencesFromResource(R.xml.prefs);
	setContentView(R.layout.prefs);

	authButton = (Button) findViewById(R.id.buttonAuth);
	authButton.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
		 startActivity(new Intent(PrefsActivity.this, AuthActivity.class));
		 Toast.makeText(PrefsActivity.this, "Loading Twitter", Toast.LENGTH_SHORT).show();
             }
         });

    }
}