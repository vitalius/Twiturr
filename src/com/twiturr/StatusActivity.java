package com.twiturr;

import winterwell.jtwitter.Twitter;

import android.graphics.Color;
import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class StatusActivity extends BaseActivity implements OnClickListener, TextWatcher {
	
    private static final String TAG = "StatusActivity";
    private static final Integer TWEET_CHAR_LIMIT = 140;

    EditText editText;
    Button updateButton;
    TextView textCount;

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
	setContentView(R.layout.status);

	editText = (EditText) findViewById(R.id.editText);
	editText.addTextChangedListener(this);

	updateButton = (Button) findViewById(R.id.buttonUpdate);
	updateButton.setOnClickListener(this);

	textCount = (TextView) findViewById(R.id.textCount);
	textCount.setText(Integer.toString(TWEET_CHAR_LIMIT));
	textCount.setTextColor(Color.GREEN);
    }

    /*
     * Captures button click and tries to post status
     */
    public void onClick(View v) {
	try {
	    String status = editText.getText().toString();
	    new PostToTwitter().execute(status);
	} catch (Exception e) {
	    Log.d(TAG, "Twitter setStatus failed: " + e);
	}
    }


    /*
     * Displays the number of character in the status text
     */
    public void afterTextChanged(Editable statusText) {
	int count = TWEET_CHAR_LIMIT - statusText.length();
	textCount.setText(Integer.toString(count));
	textCount.setTextColor(Color.GREEN);
	if (count < 20)
	    textCount.setTextColor(Color.YELLOW);
	if (count < 0)
	    textCount.setTextColor(Color.RED);
    }
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
    public void onTextChanged(CharSequence s, int start, int before, int count) {}




    /**
     * This task runs in background and attempts to make a Twitter post
     */
    class PostToTwitter extends AsyncTask<String, Integer, String> {
	@Override protected String doInBackground(String... statuses) {
	    try {
		Twitter twitter = ((Twiturr) getApplication()).getTwitter();
		Twitter.Status status = twitter.updateStatus(statuses[0]);
		return status.text;
	    } catch (Exception e) {
		Log.e(TAG, e.toString());
		return "Failed to post";
	    }
	}

	@Override protected void onProgressUpdate(Integer... values) {
	    super.onProgressUpdate(values);
	}

	@Override protected void onPostExecute(String result) {
	    Toast.makeText(StatusActivity.this, result, Toast.LENGTH_LONG).show();
	}
    }
}
