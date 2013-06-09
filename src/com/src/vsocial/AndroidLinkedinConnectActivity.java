package com.src.vsocial;

import java.util.EnumSet;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.code.linkedinapi.client.LinkedInApiClient;
import com.google.code.linkedinapi.client.LinkedInApiClientException;
import com.google.code.linkedinapi.client.LinkedInApiClientFactory;
import com.google.code.linkedinapi.client.enumeration.ProfileField;
import com.google.code.linkedinapi.client.oauth.LinkedInAccessToken;
import com.google.code.linkedinapi.client.oauth.LinkedInOAuthService;
import com.google.code.linkedinapi.client.oauth.LinkedInOAuthServiceFactory;
import com.google.code.linkedinapi.client.oauth.LinkedInRequestToken;
import com.google.code.linkedinapi.schema.DateOfBirth;
import com.google.code.linkedinapi.schema.Location;
import com.google.code.linkedinapi.schema.Person;



public class AndroidLinkedinConnectActivity extends Activity  implements OnClickListener{
    public static final String CONSUMER_KEY             = "CONSUMER_KEY";
    public static final String CONSUMER_SECRET          = "CONSUMER_SECRET";    
    public static final String APP_NAME                 = "Vsocial";
    public static final String OAUTH_CALLBACK_SCHEME    = "x-oauthflow-linkedin";
    public static final String OAUTH_CALLBACK_HOST      = "litestcalback";
    public static final String OAUTH_CALLBACK_URL       = OAUTH_CALLBACK_SCHEME + "://" + OAUTH_CALLBACK_HOST;
    static final String OAUTH_QUERY_TOKEN               = "oauth_token";
    static final String OAUTH_QUERY_VERIFIER            = "oauth_verifier";
    static final String OAUTH_QUERY_PROBLEM             = "oauth_problem";
    static final String OAUTH_PREF                      = "AppPreferences";
    static final String PREF_TOKEN                      = "linkedin_token";
    static final String PREF_TOKENSECRET                = "linkedin_token_secret";
    static final String PREF_REQTOKENSECRET             = "linkedin_request_token_secret";
    static final String scopeParams							="rw_nus+r_basicprofile";
    
    final LinkedInOAuthService oAuthService             = LinkedInOAuthServiceFactory.getInstance().createLinkedInOAuthService(CONSUMER_KEY, CONSUMER_SECRET,scopeParams);
    final LinkedInApiClientFactory factory              = LinkedInApiClientFactory.newInstance(CONSUMER_KEY, CONSUMER_SECRET);
    LinkedInRequestToken liToken;
    LinkedInApiClient client;
    
    
    
	private Button btnLoginLinked;
	private Button btnUpdateStatus;
	private Button btnLogoutLinked;
	private Button btnReadProfileData;
	private ImageButton btnspeak;
	private EditText txtUpdate;
	private TextView lblPost;
	private TextView lblUserName;

	String token;
	String tokenSecret;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.linkedin_tab);
        init_LinkedinVars();
        final SharedPreferences pref    = getSharedPreferences(OAUTH_PREF, MODE_PRIVATE);
        token              = pref.getString(PREF_TOKEN, null);
        tokenSecret        = pref.getString(PREF_TOKENSECRET, null);
        
		if (pref.getString(PREF_TOKEN, null) != null){	
            //Toast.makeText(this, "No tokens", Toast.LENGTH_LONG).show();
			Log.d("accepted tokens", PREF_TOKEN);
        	LinkedInAccessToken accessToken = new LinkedInAccessToken(token, tokenSecret);
        	new loadingView().execute(accessToken);
        	//showCurrentUser(accessToken);
        }

    }//end method
    
    
    ProgressDialog pDialog;
    
    class loadingView extends AsyncTask<LinkedInAccessToken, String, String> {
    	
    	
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pDialog = new ProgressDialog(AndroidLinkedinConnectActivity.this);
			pDialog.setMessage("Loading...");
			pDialog.setIndeterminate(false);
			pDialog.setCancelable(false);
			pDialog.show();
		}
    	
    	
		@Override
		protected String doInBackground(LinkedInAccessToken... params) {
			showCurrentUser(params[0]);
			return null;
		}
		
		@Override
		protected void onPostExecute(String result) {
			pDialog.dismiss();
		}

    	
    }
    

    private void init_LinkedinVars() {
		// TODO Auto-generated method stub
    	
		btnLoginLinked = (Button) findViewById(R.id.btnLoginLinked);
		btnUpdateStatus = (Button) findViewById(R.id.btnUpdateStatus);
		btnLogoutLinked = (Button) findViewById(R.id.btnLogoutLinked);
		btnReadProfileData  = (Button) findViewById(R.id.btnReadProfileData);
		btnspeak=(ImageButton)findViewById(R.id.img_btn);
		txtUpdate = (EditText) findViewById(R.id.txtUpdateStatus);
		lblPost = (TextView) findViewById(R.id.lblPost);
		lblUserName = (TextView) findViewById(R.id.lblUserName);

		btnLoginLinked.setOnClickListener(this);
		btnUpdateStatus.setOnClickListener(this);
		btnReadProfileData.setOnClickListener(this);
		btnLogoutLinked.setOnClickListener(this);
		btnspeak.setOnClickListener(this);
		
		
	}

	void startAutheniticate() {
        new Thread(){//added because this will make code work on post API 10 
            @Override
            public void run(){
                final LinkedInRequestToken liToken  = oAuthService.getOAuthRequestToken(OAUTH_CALLBACK_URL); 
                final String uri                    = liToken.getAuthorizationUrl();
                final SharedPreferences pref        = getSharedPreferences(OAUTH_PREF, MODE_PRIVATE);
                SharedPreferences.Editor editor     = pref.edit(); 
                editor.putString(PREF_REQTOKENSECRET, liToken.getTokenSecret());
                editor.commit();
                Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                startActivity(i);
             }
        }.start();
    }//end method

    void finishAuthenticate() {
    	final Uri uri = getIntent().getData();
        new Thread(){
            @Override
            public void run(){
                Looper.prepare();
                if (uri != null && uri.getScheme().equals(OAUTH_CALLBACK_SCHEME)) {
                    final String problem = uri.getQueryParameter(OAUTH_QUERY_PROBLEM);
                    if (problem == null) {
                        final SharedPreferences pref                = getSharedPreferences(OAUTH_PREF, MODE_PRIVATE);
                        final String request_token_secret           = pref.getString(PREF_REQTOKENSECRET, null);
                        final String query_token                    = uri.getQueryParameter(OAUTH_QUERY_TOKEN);
                        final LinkedInRequestToken request_token    = new LinkedInRequestToken(query_token, request_token_secret);
                        final LinkedInAccessToken accessToken       = oAuthService.getOAuthAccessToken(request_token, uri.getQueryParameter(OAUTH_QUERY_VERIFIER));
                        SharedPreferences.Editor editor = pref.edit(); 
                        editor.putString(PREF_TOKEN, accessToken.getToken());
                        editor.putString(PREF_TOKENSECRET, accessToken.getTokenSecret());
                        editor.remove(PREF_REQTOKENSECRET);
                        editor.commit();
                        new loadingView().execute(accessToken);
                        //showCurrentUser(accessToken);
                    } else {
                        Toast.makeText(getApplicationContext(), "Application down due OAuth problem: " + problem, Toast.LENGTH_LONG).show();
                        finish();
                    }
                }
                Looper.loop();
            }
        }.start();
    }//end method

    void clearTokens() {
        getSharedPreferences(OAUTH_PREF, MODE_PRIVATE).edit().remove(PREF_TOKEN).remove(PREF_TOKENSECRET).remove(PREF_REQTOKENSECRET).commit();
    }//end method

    void showCurrentUser(final LinkedInAccessToken accessToken) {
   
                client = factory.createLinkedInApiClient(accessToken);
                try {
                    final Person p = client.getProfileForCurrentUser();                
                    // /////////////////////////////////////////////////////////
                    // here you can do client API calls ...
                    // client.postComment(arg0, arg1);
                    // client.updateCurrentStatus(arg0);
                    // or any other API call (this sample only check for current user
                    // and shows it in TextView)
                    // /////////////////////////////////////////////////////////             
                    runOnUiThread(new Runnable() {//updating UI thread from different thread not a good idea...
                        public void run() {
                        	
                    		// Hide login button
                    		btnLoginLinked.setVisibility(View.GONE);

                    		// Show Update Twitter
                    		lblPost.setVisibility(View.VISIBLE);
                    		txtUpdate.setVisibility(View.VISIBLE);
                    		btnUpdateStatus.setVisibility(View.VISIBLE);
                    		btnLogoutLinked.setVisibility(View.VISIBLE);
                    		btnReadProfileData.setVisibility(View.VISIBLE);
                    		btnspeak.setVisibility(View.VISIBLE);
                        	
                    		String namelabel= p.getFirstName()+p.getLastName();
                    		// Displaying in xml ui
                    		lblUserName.setText(Html.fromHtml("<b>Welcome " + namelabel
                    				+ "</b>"));

                    		lblUserName.setVisibility(View.VISIBLE);
                        }
                    });
                    //or use Toast
                    //Toast.makeText(getApplicationContext(), "Lastname:: "+p.getLastName() + ", First name: " + p.getFirstName(), 1).show();
                } catch (LinkedInApiClientException ex) {
                    clearTokens();
                    Toast.makeText(getApplicationContext(),
                        "Application down due LinkedInApiClientException: "+ ex.getMessage() + " Authokens cleared - try run application again.",
                        Toast.LENGTH_LONG).show();
                    finish();
                }
                
            
        
    }//end method
    
    

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		finishAuthenticate();
	}
        


	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		
		switch (v.getId()) {
		case R.id.btnLoginLinked:
				final SharedPreferences pref= getSharedPreferences(OAUTH_PREF, MODE_PRIVATE);
				if (pref.getString(PREF_TOKEN, null) == null){	
		            startAutheniticate();
		        } else {
		        	LinkedInAccessToken accessToken = new LinkedInAccessToken(token, tokenSecret);
		        	new loadingView().execute(accessToken);
		            //showCurrentUser(accessToken);
		        }
			break;

		case R.id.btnUpdateStatus:
			String msg = txtUpdate.getText().toString();
			client.postNetworkUpdate(msg);

	        //alertDialog
	        final AlertDialog alertDialog = new AlertDialog.Builder(this).create();
	        alertDialog.setTitle("Done");
	        alertDialog.setMessage("Your msg has been successfuly Posted");
	        alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					alertDialog.dismiss();
					txtUpdate.setText("");
				}
			});
	        
	        alertDialog.show();
			
			break;
		
			
		case R.id.btnReadProfileData:
						
			Person profile = client.getProfileForCurrentUser(EnumSet.of(
	                ProfileField.ID, ProfileField.FIRST_NAME,
	                ProfileField.LAST_NAME, ProfileField.HEADLINE,
	                ProfileField.INDUSTRY, ProfileField.PICTURE_URL,
	                ProfileField.DATE_OF_BIRTH, ProfileField.LOCATION_NAME,
	                ProfileField.MAIN_ADDRESS, ProfileField.LOCATION_COUNTRY));
			DateOfBirth dateOfBirth = profile.getDateOfBirth();
			Location location = profile.getLocation();
			String data=
	        ("PersonID : " + profile.getId())+"\n"+
	        ("Name : " + profile.getFirstName() + " "
	                + profile.getLastName())+"\n"+
	        ("Headline : " + profile.getHeadline())+"\n"+
	        ("Industry : " + profile.getIndustry())+"\n"+
	        ("Picture : " + profile.getPictureUrl())+"\n"+
	        ("DateOfBirth : " + dateOfBirth)+"\n"+
	        ("MAin Address : " + profile.getMainAddress())+"\n"+
	        ("Location:" + location.getName() + " - "
	                + location.getCountry().getCode());
	        
			Bundle b=new Bundle();
			b.putString("textdata",data);
			Intent in =new Intent();
			in.putExtras(b);
			in.setClass(this,AboutUs.class);
			startActivity(in);
			
			break;
			
			
			case R.id.btnLogoutLinked:
				
				logoutFromLinkedin();
				
				break;
				
			case R.id.img_btn:
				
				Intent inten =new Intent();
				inten.setClass(this,RecoSpeech.class);
				startActivityForResult(inten, 0);
				
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

	private void logoutFromLinkedin() {
		// TODO Auto-generated method stub
		clearTokens();
		
		// After this take the appropriate action
		// I am showing the hiding/showing buttons again
		// You might not needed this code
		btnLogoutLinked.setVisibility(View.GONE);
		btnUpdateStatus.setVisibility(View.GONE);
		btnReadProfileData.setVisibility(View.GONE);
		btnspeak.setVisibility(View.GONE);
		txtUpdate.setVisibility(View.GONE);
		lblPost.setVisibility(View.GONE);
		lblUserName.setText("");
		lblUserName.setVisibility(View.GONE);

		btnLoginLinked.setVisibility(View.VISIBLE);
		
		
	}


}//endclasss
