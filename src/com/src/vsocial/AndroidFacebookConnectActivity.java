package com.src.vsocial;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
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

import com.facebook.android.AsyncFacebookRunner;
import com.facebook.android.AsyncFacebookRunner.RequestListener;
import com.facebook.android.DialogError;
import com.facebook.android.Facebook;
import com.facebook.android.Facebook.DialogListener;
import com.facebook.android.FacebookError;


public class AndroidFacebookConnectActivity extends Activity implements OnClickListener{

	private static String APP_ID = "APP_ID";
	
	// Instance of Facebook Class
	private Facebook facebook = new Facebook(APP_ID);
	private AsyncFacebookRunner mAsyncRunner;
	private SharedPreferences mPrefs;
	
	// Buttons
	Button btnFbLogin;
	Button btnFbGetProfile;
	Button btnPostToWall;
	Button btn_logout;
	ImageButton btn_mic;
	EditText et_posttowall;
	TextView lblUserName;
	TextView lblPost;
	String user_name;
	String access_token;
	long expires;

	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.facebook_tab);
		mPrefs = getPreferences(MODE_PRIVATE);
		init_fb();
		getloginData();
		check_access_token();
		
		
	}


	private void init_fb() {
		btnFbLogin = (Button) findViewById(R.id.btn_fblogin);
		btnFbGetProfile = (Button) findViewById(R.id.btn_get_profile);
		btnPostToWall = (Button) findViewById(R.id.btn_fb_post_to_wall);
		btn_logout = (Button) findViewById(R.id.btn_logout);
		btn_mic=(ImageButton)findViewById(R.id.btn_mic);
		et_posttowall=(EditText)findViewById(R.id.et_posttowall);
		lblUserName=(TextView)findViewById(R.id.fb_UserName);
		lblPost=(TextView)findViewById(R.id.lblpost);
		mAsyncRunner = new AsyncFacebookRunner(facebook);
		
		btnFbLogin.setOnClickListener(this);
		btnFbGetProfile.setOnClickListener(this);
		btnPostToWall.setOnClickListener(this);
		btn_logout.setOnClickListener(this);
		btn_mic.setOnClickListener(this);
	}


	@Override
	public void onClick(View v) {
		
		switch (v.getId()) {
		case R.id.btn_fblogin:
				Log.d("login_btn", "button Clicked");
				loginToFacebook();
			break;
			
		case R.id.btn_fb_post_to_wall:
				//postToWall();
				String msg=et_posttowall.getText().toString();
				postOnWall(msg);
				
			break;
			
		case R.id.btn_get_profile:
				
				getProfileInformation();
			
			break;
			
		case R.id.btn_logout:	
				logout();
			break;
			
			
		case R.id.btn_mic:
			Intent in =new Intent();
			in.setClass(this,RecoSpeech.class);
			startActivityForResult(in, 0);
			break;

		default:
			break;
		}
	
		
	}
	
	
	


	public void getProfileInformation() {
		mAsyncRunner.request("me", new RequestListener() {
			@Override
			public void onComplete(String response, Object state) {
				Log.d("Profile", response);
				String json = response;
				try {
					// Facebook Profile JSON data
					JSONObject profile = new JSONObject(json);
							
					String data=
				        ("PersonID : " + profile.getString("id"))+"\n"+
				        ("Name : " + profile.getString("first_name") + " "
				                + profile.getString("last_name"))+"\n"+
				        ("bio : " + profile.getString("bio"))+"\n"+
				        ("education : " + profile.getString("education"))+"\n"+
				        ("location : " + profile.getString("location"));

						Bundle b=new Bundle();
						b.putString("textdata",data);
						Class ourClass = null;
						try {
							ourClass = Class.forName("com.src.vsocial.AboutUs");
						} catch (ClassNotFoundException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						Intent in = new Intent(AndroidFacebookConnectActivity.this, ourClass);
						in.putExtras(b);
						startActivity(in);
							
				} catch (final JSONException e) {
					e.printStackTrace();
					Looper.prepare();
					Toast.makeText(AndroidFacebookConnectActivity.this, "Error" + e.getMessage(), Toast.LENGTH_LONG).show();		
					Looper.loop();
				}
			}

			@Override
			public void onIOException(IOException e, Object state) {
			}

			@Override
			public void onFileNotFoundException(FileNotFoundException e,
					Object state) {
			}

			@Override
			public void onMalformedURLException(MalformedURLException e,
					Object state) {
			}

			@Override
			public void onFacebookError(FacebookError e, Object state) {
			}
		});
	}

	
	private void getUsername() {
		// TODO Auto-generated method stub
		mAsyncRunner.request("me", new RequestListener() {

			@Override
			public void onComplete(String response, Object state) {
				// TODO Auto-generated method stub
				String json = response;
				
				JSONObject profile;
				try {
					profile = new JSONObject(json);
					// getting name of the user
					final String name= profile.getString("name");
					
					runOnUiThread(new Runnable() {

						@Override
						public void run() {
							// Displaying in xml ui
							lblUserName.setText(Html.fromHtml("<b>Welcome " + name+ "</b>"));
						}
					

					});
					
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				

				
			}

			@Override
			public void onIOException(IOException e, Object state) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onFileNotFoundException(FileNotFoundException e,
					Object state) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onMalformedURLException(MalformedURLException e,
					Object state) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onFacebookError(FacebookError e, Object state) {
				// TODO Auto-generated method stub
				
			}
			
		});
		
	}

	private void postToWall() {
		// TODO Auto-generated method stub
		
		
		facebook.dialog(this,"feed", new DialogListener() {
			
			@Override
			public void onFacebookError(FacebookError e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onError(DialogError e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onComplete(Bundle values) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onCancel() {
				// TODO Auto-generated method stub
				
			}
		});
		
	}
	private void check_access_token() {
		if (access_token != null) {
				facebook.setAccessToken(access_token);
			
			if (expires != 0) {
				facebook.setAccessExpires(expires);
			}
	
			
			
			btnFbLogin.setVisibility(View.GONE);

			// Making get profile button visible
			btnFbGetProfile.setVisibility(View.VISIBLE);

			// Making post to wall visible
			btnPostToWall.setVisibility(View.VISIBLE);

			// Making show access tokens button visible
			btn_logout.setVisibility(View.VISIBLE);
			
			et_posttowall.setVisibility(View.VISIBLE);
			
			lblPost.setVisibility(View.VISIBLE);
			
			lblUserName.setVisibility(View.VISIBLE);
			
			btn_mic.setVisibility(View.VISIBLE);
			
			getUsername();

			Log.d("FB Sessions", "" + facebook.isSessionValid());
			
			
		}
		
	}
	



	private void getloginData(){
		access_token = mPrefs.getString("access_token", null);
	    expires = mPrefs.getLong("access_expires", 0);
	}

	private void loginToFacebook() {		

		if(!facebook.isSessionValid()){

			facebook.authorize(this, new String[] {"email","publish_stream"},Facebook.FORCE_DIALOG_AUTH ,new DialogListener() { 
								
				@Override
				public void onFacebookError(FacebookError e) {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void onError(DialogError e) {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void onComplete(Bundle values) {
					// TODO Auto-generated method stub
					SharedPreferences.Editor editor = mPrefs.edit();
					editor.putString("access_token",facebook.getAccessToken());
					editor.putLong("access_expires",facebook.getAccessExpires());
					editor.commit();
					getloginData();
					check_access_token();

				}
				
				@Override
				public void onCancel() {
					// TODO Auto-generated method stub
					
				}
				
				
			});
			
			
			
		}
		
		
	}
	
	 protected void onActivityResult(int requestCode, int resultCode, Intent data) { 
		// facebook.authorizeCallback(requestCode, resultCode, data);
		 
			if(resultCode==RESULT_OK){
				Bundle b=data.getExtras();
				String retdata=b.getString("answer");
				et_posttowall.setText(retdata);
			}
	 }
	
	public void postOnWall(String msg) {
        Log.d("Tests", "Testing graph API wall post");
         try {
                String response = facebook.request("me");
                Bundle parameters = new Bundle();
                parameters.putString("message", msg);
                parameters.putString("description", "test test test");
                response = facebook.request("me/feed", parameters,"POST");
                Log.d("Tests", "got response: " + response);
                if (response == null || response.equals("") || 
                        response.equals("false")) {
                   Log.v("Error", "Blank response");
                }
                //alertDialog
                final AlertDialog alertDialog = new AlertDialog.Builder(this).create();
                alertDialog.setTitle("Done");
                alertDialog.setMessage("Your msg has been successfuly Posted");
                alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						alertDialog.dismiss();
						et_posttowall.setText("");
					}
				});
                
                alertDialog.show();
                
                
         } catch(Exception e) {
             e.printStackTrace();
         }
    }
	
	private void logout() {
		
		try {
			facebook.logout(this);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		show_loginbtn();
	}


	private void show_loginbtn() {


		btnFbLogin.setVisibility(View.VISIBLE);


		btnFbGetProfile.setVisibility(View.GONE);

		btnPostToWall.setVisibility(View.GONE);

		btn_logout.setVisibility(View.GONE);
		
		et_posttowall.setVisibility(View.GONE);
		
		lblPost.setVisibility(View.GONE);
		
		lblUserName.setVisibility(View.GONE);
		
		btn_mic.setVisibility(View.GONE);
		
	}
	
}










