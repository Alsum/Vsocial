package com.src.vsocial;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

public class AboutUs extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.about);
		TextView tv=(TextView)findViewById(R.id.tv_about);
		Intent i=getIntent();
		Bundle b=i.getExtras();
		String  data=b.getString("textdata");
		System.out.println("textdata : " + data);
		tv.setText(data);
		
	}

}
