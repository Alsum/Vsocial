package com.src.vsocial;


import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;

public class AndroidTabLayoutActivity extends TabActivity {
	
	AlertDialogManager alert = new AlertDialogManager();
	private ConnectionDetector cd;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		loadTabs();
		
		cd = new ConnectionDetector(getApplicationContext());

		// Check if Internet present
		if (!cd.isConnectingToInternet()) {
			// Internet Connection is not present
			alert.showAlertDialog(this, "Internet Connection Error",
					"Please connect to working Internet connection", false);
			// stop executing code by return
			return;
		}
	}
	
	private void loadTabs() {
		TabHost th = getTabHost();
		
		TabSpec face_specs = th.newTabSpec("FaceBook");
		face_specs.setIndicator("",getResources().getDrawable(R.drawable.icon_face_tab));
		Intent  face_Intent = new Intent(this,AndroidFacebookConnectActivity.class);
		face_specs.setContent(face_Intent);
		

		TabSpec twitter_specs = th.newTabSpec("Twitter");
		twitter_specs.setIndicator("",getResources().getDrawable(R.drawable.icon_twitter_tab));
		Intent  twitter_Intent = new Intent(this,AndroidTwitterConnectActivity.class);
		twitter_specs.setContent(twitter_Intent);
		
		

		TabSpec gplus_specs = th.newTabSpec("Linkedin");
		gplus_specs.setIndicator("",getResources().getDrawable(R.drawable.icon_linkedin_tab));
		Intent  gplus_Intent = new Intent(this,AndroidLinkedinConnectActivity.class);
		gplus_specs.setContent(gplus_Intent);
		
		
		th.addTab(face_specs);
		th.addTab(twitter_specs);
		th.addTab(gplus_specs);

	}
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		
		super.onCreateOptionsMenu(menu);
		MenuInflater blowUp = getMenuInflater();
		blowUp.inflate(R.menu.aboutus, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		switch (item.getItemId()) {
		case R.id.about:
			
			String data="This application enable you to signin with multiple social network accounts\n" +
					    "it also have a Voice Recognition and a reader for your updates\n" +
					    "Developed by: Mohamed Alsum & Mohamed Diaf";
			Bundle b=new Bundle();
			b.putString("textdata",data);
			Intent in =new Intent();
			in.putExtras(b);
			in.setClass(this,AboutUs.class);
			startActivity(in);
			break;
		
		case R.id.exit:
			finish();
			break;

		default:
			break;
		}
		return false;
	}


}
