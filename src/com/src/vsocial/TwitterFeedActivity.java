package com.src.vsocial;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Locale;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class TwitterFeedActivity extends ListActivity {

	ArrayList<Tweet> tweets = new ArrayList<Tweet>();
	final static String URL ="http://search.twitter.com/search.json?q=";
	String user;
	TextToSpeech tts;
	LinkedList<String>data;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		Intent i=getIntent();
		Bundle b=i.getExtras();
		user=b.getString("user_name");
		new LoadTweets().execute(user);
		
		data=new LinkedList<String>();
		tts = new TextToSpeech(TwitterFeedActivity.this, new TextToSpeech.OnInitListener() {
			
			public void onInit(int status) {
				// TODO Auto-generated method stub
				if (status != TextToSpeech.ERROR){
					tts.setLanguage(Locale.US);
				}else{
					Toast.makeText(TwitterFeedActivity.this, "error in tts", Toast.LENGTH_LONG).show();
				}
			}
		});
	}
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		super.onCreateOptionsMenu(menu);
		MenuInflater blowUp = getMenuInflater();
		blowUp.inflate(R.menu.tts_menu, menu);
		return true;
	}
	
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		switch (item.getItemId()) {
		case R.id.rtl:
			
			for (String tweet : data) {
						
				tts.speak(tweet, TextToSpeech.QUEUE_FLUSH, null);
				
				try {
					Thread.sleep(7000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}		
			
			
			
			break;

		}
		return false;
	}
	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		if(tts !=null){
			tts.stop();
			tts.shutdown();
		}
		super.onPause();
	}

	private class LoadTweets extends AsyncTask<String, Integer, Void> {

		ProgressDialog dialog;

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
			dialog = new ProgressDialog(TwitterFeedActivity.this);
			dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			dialog.setMax(100);
			dialog.show();
		}

		@Override
		protected Void doInBackground(String... params) {

			for (int i = 0; i < 20; i++) {
				publishProgress(5);
				try {
					Thread.sleep(88);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			dialog.dismiss();

			try {
				HttpClient hc = new DefaultHttpClient();
				StringBuilder url=new StringBuilder(URL);
				url.append(params[0]);
				HttpGet get = new HttpGet(url.toString());
				Log.d("URL", "> " + url);
				HttpResponse rp = hc.execute(get);
				if (rp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {

					HttpEntity e = rp.getEntity();
					String result = EntityUtils.toString(e);
					try {
						JSONObject root = new JSONObject(result);
						JSONArray sessions = root.getJSONArray("results");
						for (int i = 0; i < sessions.length(); i++) {

							JSONObject session = sessions.getJSONObject(i);
							Tweet tweet = new Tweet();
							tweet.content = session.getString("text");
							tweet.author = session.getString("from_user");
							tweets.add(tweet);
							
						}

					} catch (JSONException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}

				}

			} catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			// TODO Auto-generated method stub
			super.onProgressUpdate(values);
			dialog.incrementProgressBy(values[0]);
		}

		@Override
		protected void onPostExecute(Void result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			
		/*	Bundle b=new Bundle();
			b.putString("tweetcontent",tweets.toString());
			Class ourClass = null;
			try {
				ourClass = Class.forName("com.src.vsocial.TextVoice");
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Intent in = new Intent(TwitterFeedActivity.this, ourClass);
			in.putExtras(b);
			startActivity(in);   */    

			setListAdapter(new TweetListAdaptor(getApplicationContext(),R.layout.list_item, tweets));
		}


	}
	
	
	private class TweetListAdaptor extends ArrayAdapter<Tweet> {

		private ArrayList<Tweet> tweets;

		public TweetListAdaptor(Context context, int textViewResourceId,ArrayList<Tweet> items) {
			super(context, textViewResourceId, items);
			this.tweets = items;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			View v = convertView;
			if (v == null) {
				LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				v = vi.inflate(R.layout.list_item, null);
			}
			Tweet mytweet = tweets.get(position);
			TextView content = (TextView) v.findViewById(R.id.tv_content);
			TextView username = (TextView) v.findViewById(R.id.tv_username);
			content.setText(mytweet.getContent());
			username.setText(mytweet.getAuthor());
			Log.d("tweets : ",mytweet.getContent());
			
			
			data.add(mytweet.getContent());
			
			
			return v;
			
		}
		
		

	}

}
