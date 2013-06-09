package com.src.vsocial;


import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class AndroidTwitterConnectActivity extends Activity implements OnClickListener {

	static String TWITTER_CONSUMER_KEY = "TWITTER_CONSUMER_KEY";
	static String TWITTER_CONSUMER_SECRET = "TWITTER_CONSUMER_SECRET";

	// Twitter UI
	Button btnLoginTwitter;
	Button btnUpdateStatus;
	Button btnLogoutTwitter;
	Button btnReadTimeline;
	ImageButton btnspeak;
	EditText txtUpdate;
	TextView lblUpdate;
	TextView lblUserName;
	ProgressDialog pDialog;

	// Twitter4j
	private static Twitter twitter;
	private static RequestToken requestToken;

	static final String TWITTER_CALLBACK_URL = "oauth://vsocial";

	// Shared Preferences
	private static SharedPreferences mSharedPreferences;

	// Preference Constants
	static String PREFERENCE_NAME = "twitter_oauth";
	static final String PREF_KEY_OAUTH_TOKEN = "oauth_token";
	static final String PREF_KEY_OAUTH_SECRET = "oauth_token_secret";
	static final String PREF_KEY_TWITTER_LOGIN = "isTwitterLogedIn";

	// Twitter oauth urls
	static final String URL_TWITTER_AUTH = "auth_url";
	static final String URL_TWITTER_OAUTH_VERIFIER = "oauth_verifier";
	static final String URL_TWITTER_OAUTH_TOKEN = "oauth_token";
	String namelabel;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.twitter_tab);
		init_twitterVars();
		
		mSharedPreferences = getApplicationContext().getSharedPreferences("MyPref", 0);
		
		if(isTwitterLoggedInAlready()){
			showUpdateTwitter();
		}
		
	}

	

	private void init_twitterVars() {

		btnLoginTwitter = (Button) findViewById(R.id.btnLoginTwitter);
		btnUpdateStatus = (Button) findViewById(R.id.btnUpdateStatus);
		btnLogoutTwitter = (Button) findViewById(R.id.btnLogoutTwitter);
		btnReadTimeline  = (Button) findViewById(R.id.btnReadTimeline);
		btnspeak=(ImageButton)findViewById(R.id.img_btn);
		txtUpdate = (EditText) findViewById(R.id.txtUpdateStatus);
		lblUpdate = (TextView) findViewById(R.id.lblUpdate);
		lblUserName = (TextView) findViewById(R.id.lblUserName);

		btnLoginTwitter.setOnClickListener(this);
		btnUpdateStatus.setOnClickListener(this);
		btnReadTimeline.setOnClickListener(this);
		btnLogoutTwitter.setOnClickListener(this);
		btnspeak.setOnClickListener(this);

	}

	private void loginToTwitter() {

		// Check if already logged in
		if (!isTwitterLoggedInAlready()) {
			ConfigurationBuilder builder = new ConfigurationBuilder();
			builder.setOAuthConsumerKey(TWITTER_CONSUMER_KEY);
			builder.setOAuthConsumerSecret(TWITTER_CONSUMER_SECRET);
			Configuration configuration = builder.build();

			TwitterFactory factory = new TwitterFactory(configuration);
			twitter = factory.getInstance();
			try {
				requestToken = twitter.getOAuthRequestToken(TWITTER_CALLBACK_URL);
				Log.d("Tweet login","done");
				Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(requestToken.getAuthenticationURL()));
				this.startActivity(i);
			} catch (TwitterException e) {
				e.printStackTrace();
			}
		} else {
			// user already logged into twitter
			Toast.makeText(getApplicationContext(),
					"Already Logged into twitter", Toast.LENGTH_LONG).show();
			showUpdateTwitter();
		}

	}

	private void showUpdateTwitter() {
		namelabel=mSharedPreferences.getString("username", "");
		// Displaying in xml ui
		lblUserName.setText(Html.fromHtml("<b>Welcome " + namelabel
				+ "</b>"));
		// Hide login button
		btnLoginTwitter.setVisibility(View.GONE);

		// Show Update Twitter
		lblUpdate.setVisibility(View.VISIBLE);
		txtUpdate.setVisibility(View.VISIBLE);
		btnUpdateStatus.setVisibility(View.VISIBLE);
		btnLogoutTwitter.setVisibility(View.VISIBLE);
		btnReadTimeline.setVisibility(View.VISIBLE);
		btnspeak.setVisibility(View.VISIBLE);

	}

	/**
	 * Check user already logged in your application using twitter Login flag is
	 * fetched from Shared Preferences
	 * */

	private boolean isTwitterLoggedInAlready() {
		// return twitter login status from Shared Preferences
		return mSharedPreferences.getBoolean(PREF_KEY_TWITTER_LOGIN, false);
	}

	@Override
	public void onClick(View v) {

		switch (v.getId()) {

		case R.id.btnLoginTwitter:
			loginToTwitter();
			break;
			
		case R.id.btnUpdateStatus:

			// Call update status function
			// Get the status from EditText
			String status = txtUpdate.getText().toString();

			// Check for blank text
			if (status.trim().length() > 0) {
				// update status
				new updateTwitterStatus().execute(status);
			} else {
				// EditText is empty
				Toast.makeText(getApplicationContext(),
						"Please enter status message", Toast.LENGTH_SHORT).show();
			}

			break;
			
		case R.id.btnReadTimeline:
			Bundle b=new Bundle();
			b.putString("user_name",namelabel);
			Intent in =new Intent();
			in.putExtras(b);
			in.setClass(this,TwitterFeedActivity.class);
			startActivity(in);
			
			break;
			
			
		case R.id.btnLogoutTwitter:
			
			logoutFromTwitter();
			
			break;
			
			
		case R.id.img_btn:
			
			Intent inten =new Intent();
			inten.setClass(this,RecoSpeech.class);
			startActivityForResult(inten, 0);
			
			break;

		default:
			break;
		}

	}
	
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		if(resultCode==RESULT_OK){
			Bundle b=data.getExtras();
			String retdata=b.getString("answer");
			txtUpdate.setText(retdata);
		}
	}

	private void getDataFromLogin() {

		if (!isTwitterLoggedInAlready()) {
			Uri uri = getIntent().getData();
			if (uri != null && uri.toString().startsWith(TWITTER_CALLBACK_URL)) {
				// oAuth verifier
				String verifier = uri.getQueryParameter(URL_TWITTER_OAUTH_VERIFIER);

				try {
					// Get the access token
					AccessToken accessToken = twitter.getOAuthAccessToken(requestToken, verifier);

					// Shared Preferences
					Editor e = mSharedPreferences.edit();

					// After getting access token, access token secret
					// store them in application preferences
					e.putString(PREF_KEY_OAUTH_TOKEN, accessToken.getToken());
					e.putString(PREF_KEY_OAUTH_SECRET,
							accessToken.getTokenSecret());
					// Store login status - true
					e.putBoolean(PREF_KEY_TWITTER_LOGIN, true);
					

					Log.e("Twitter OAuth Token", "> " + accessToken.getToken());

					// Getting user details from twitter
					// For now i am getting his name only
					long userID = accessToken.getUserId();
					User user = twitter.showUser(userID);
					String username = user.getScreenName();
					e.putString("username", username);
					e.commit(); // save changes
					showUpdateTwitter();
					
				} catch (Exception e) {
					// Check log for login errors
					Log.e("Twitter Login Error", "> " + e.getMessage());
				}

			}
		}
	}
	
	
	
	class updateTwitterStatus extends AsyncTask<String, String, String> {

		/**
		 * Before starting background thread Show Progress Dialog
		 * */
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pDialog = new ProgressDialog(AndroidTwitterConnectActivity.this);
			pDialog.setMessage("Updating to twitter...");
			pDialog.setIndeterminate(false);
			pDialog.setCancelable(false);
			pDialog.show();
		}
		 
		
		
		

		/**
		 * getting Places JSON
		 * */
		protected String doInBackground(String... args) {
			Log.d("Tweet Text", "> " + args[0]);
			String status = args[0];
			try {
				ConfigurationBuilder builder = new ConfigurationBuilder();
				builder.setOAuthConsumerKey(TWITTER_CONSUMER_KEY);
				builder.setOAuthConsumerSecret(TWITTER_CONSUMER_SECRET);
				
				// Access Token 
				String access_token = mSharedPreferences.getString(PREF_KEY_OAUTH_TOKEN, "");
				// Access Token Secret
				String access_token_secret = mSharedPreferences.getString(PREF_KEY_OAUTH_SECRET, "");
				
				AccessToken accessToken = new AccessToken(access_token, access_token_secret);
				Twitter twitter = new TwitterFactory(builder.build()).getInstance(accessToken);
				
				// Update status
				twitter4j.Status response = twitter.updateStatus(status);
				
				Log.d("Status", "> " + response.getText());
			} catch (TwitterException e) {
				// Error in updating status
				Log.d("Twitter Update Error", e.getMessage());
			}
			return null;
		}

		/**
		 * After completing background task Dismiss the progress dialog and show
		 * the data in UI Always use runOnUiThread(new Runnable()) to update UI
		 * from background thread, otherwise you will get error
		 * **/
		protected void onPostExecute(String file_url) {
			// dismiss the dialog after getting all products
			pDialog.dismiss();
			
			// updating UI from Background Thread
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					Toast.makeText(getApplicationContext(),
							"Status tweeted successfully", Toast.LENGTH_SHORT)
							.show();
					// Clearing EditText field
					txtUpdate.setText("");
				}
			});
		}

	}
	
	private void logoutFromTwitter() {
		// Clear the shared preferences
		Editor e = mSharedPreferences.edit();
		e.remove(PREF_KEY_OAUTH_TOKEN);
		e.remove(PREF_KEY_OAUTH_SECRET);
		e.remove(PREF_KEY_TWITTER_LOGIN);
		e.commit();

		// After this take the appropriate action
		// I am showing the hiding/showing buttons again
		// You might not needed this code
		btnLogoutTwitter.setVisibility(View.GONE);
		btnUpdateStatus.setVisibility(View.GONE);
		btnReadTimeline.setVisibility(View.GONE);
		btnspeak.setVisibility(View.GONE);
		txtUpdate.setVisibility(View.GONE);
		lblUpdate.setVisibility(View.GONE);
		lblUserName.setText("");
		lblUserName.setVisibility(View.GONE);

		btnLoginTwitter.setVisibility(View.VISIBLE);
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		getDataFromLogin();
	}
	
	/*
	@Override
	protected void onRestart() {
		// TODO Auto-generated method stub
			super.onRestart();
			if(namelabel.equals("")){
				loginToTwitter();
			}
			init_twitterVars();
			showUpdateTwitter();
	}*/
	
	
	

}
