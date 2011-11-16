package com.twiturr;

import oauth.signpost.OAuth;
import oauth.signpost.OAuthConsumer;
import oauth.signpost.OAuthProvider;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import oauth.signpost.commonshttp.CommonsHttpOAuthProvider;
import oauth.signpost.signature.HmacSha1MessageSigner;

import android.net.Uri;

import android.app.Activity;
import android.util.Log;
import android.os.Bundle;
import android.content.Intent;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;

/**
 * This activity runs OAuth protocol for getting secret keys.
 * Once user allowed permission to use this app,
 * issued secret keys do not expire and are reused 
 * for all subsequent application use.
 * Or, until user decided to reauthenticate.
 */
public class AuthActivity extends Activity {

    private static final String TAG = "AuthActivity";

    public static final Uri CALLBACK_URI = Uri.parse("twiturr://auth");
    public static final String REQUEST_TOKEN_URL = "http://api.twitter.com/oauth/request_token";
    public static final String ACCESS_TOKEN_URL = "http://api.twitter.com/oauth/access_token";
    public static final String AUTHORIZE_URL = "http://api.twitter.com/oauth/authorize";

    public static final String CONSUMER_KEY = "YOUR_CONSUMER_KEY_GOES_HERE";
    public static final String CONSUMER_SECRET = "YOUR_CONSUMER_SECRET_GOES_HERE";

    public static final String USER_TOKEN = "user_token";
    public static final String USER_SECRET = "user_secret";
    public static final String ACCESS_TOKEN = "request_token";
    public static final String ACCESS_SECRET = "request_secret";

    public OAuthConsumer consumer;
    public OAuthProvider provider;

    private AuthWebView authWebView;
    private SharedPreferences prefs;


    public void onCreate(Bundle elephant_nostrils) {
	super.onCreate(elephant_nostrils);

	prefs = PreferenceManager.getDefaultSharedPreferences(this);
	Editor editPrefs = prefs.edit();

	/* Setup OAuth URLs */
	consumer = new CommonsHttpOAuthConsumer(CONSUMER_KEY, 
						CONSUMER_SECRET);
	provider = new CommonsHttpOAuthProvider(REQUEST_TOKEN_URL, 
						ACCESS_TOKEN_URL, 
						AUTHORIZE_URL);
	provider.setOAuth10a(true);

	/* WebView for viewing Twitter's login page */
	authWebView = new AuthWebView(this);
	authWebView.clearCache(true);
	authWebView.clearFormData();
	authWebView.clearHistory();

	WebSettings ws = authWebView.getSettings();
	ws.setSavePassword(false);
	ws.setSaveFormData(false);
	ws.setCacheMode(WebSettings.LOAD_NO_CACHE);

	/* Don't save cookies */
	CookieSyncManager cs;
	cs = CookieSyncManager.createInstance(authWebView.getContext());
	CookieManager cookieManager = CookieManager.getInstance();
	cookieManager.setAcceptCookie(false);
	//cookieManager.removeSessionCookie();

	setContentView(authWebView);

	/* Start OAuth with retrival of request tokens */
	Intent i = this.getIntent();
	if (i.getData() == null) {
	    try {
		String authUrl = provider.retrieveRequestToken(consumer, 
							       CALLBACK_URI.toString());
		
		editPrefs.putString(USER_TOKEN, consumer.getToken());
		editPrefs.putString(USER_SECRET, consumer.getTokenSecret());
		editPrefs.commit();

		authWebView.loadUrl(authUrl);	
	    } catch (Exception e) {
		Log.e(TAG, "Exception in provider.retrieveRequestToken()");
	    }
	}
    }


    @Override protected void onResume() {
	super.onResume();
	
	/* Check for callback url from authWebView */
	Uri uri = getIntent().getData();
	if (uri != null && CALLBACK_URI.getScheme().equals(uri.getScheme())) {
	    try {
		String token = prefs.getString(USER_TOKEN, "");
		String secret = prefs.getString(USER_SECRET, "");
		consumer.setTokenWithSecret(token, secret);

		/* r_token == token at this point, if not, then something went wrong */
		//String r_token = uri.getQueryParameter(OAuth.OAUTH_TOKEN);
		String r_verifier = uri.getQueryParameter(OAuth.OAUTH_VERIFIER);

		provider.retrieveAccessToken(consumer, r_verifier);
	        
		/* Saving access tokens to prefs */
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		Editor editPrefs = prefs.edit();
		editPrefs.putString(ACCESS_TOKEN, consumer.getToken());
		editPrefs.putString(ACCESS_SECRET, consumer.getTokenSecret());
		editPrefs.commit();

		Log.i(TAG, "Access Tokens recieved.["+consumer.getToken()+"],["+consumer.getTokenSecret()+"]");

		finish();
	    } catch (Exception e) {
		Log.e(TAG, "Exception in retrieving Access Tokens");
		e.printStackTrace();
	    }
	}
    }


    /**
     * Custom WebView class
     */
    public class AuthWebView extends WebView {
	public AuthWebView(Context c) {
	    super(c);
	}
     

	/**
	 * Anytime AuthWebView looses focus, kill current activity
	 */
	public void onWindowFocusChanged(boolean hasWindowFocus) {
	    if (hasWindowFocus == false) {
		Log.i(AuthActivity.TAG, "Lost focus, killing AuthWebView");
		finish();
	    }
	}
    }

}
