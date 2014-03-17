package com.example.storyboardthing;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.cordova.DroidGap;
import org.apache.cordova.api.LOG;
import org.kohsuke.github.GHEventInfo;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.PagedIterable;

import android.text.method.ScrollingMovementMethod;
import android.view.Menu;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.storyboardthing.R;

public class MainActivity extends DroidGap 
{
	private long lastChecked;
	public TextView textview;
	
	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);	
		super.init();
		super.loadUrl("file:///android_asset/www/index.html");
		super.setIntegerProperty("splashscreen", R.drawable.splash);
		setContentView(R.layout.activity_main);
		
		textview = (TextView) findViewById(R.id.textView1);
		textview.setMovementMethod(new ScrollingMovementMethod());
			
		if(getIntent().getBooleanExtra("FromPrevious",false) == true)
		{
			long lastCalledTime = getIntent().getLongExtra("LastCalled", -1);
			long lastCheckedTime = getIntent().getLongExtra("LastChecked", -1);
			SimpleDateFormat formatter = new SimpleDateFormat("EEEE, MMMM d, yyyy HH:mm");
			String callString = formatter.format(new Date(lastCalledTime));
			String checkString = formatter.format(new Date(lastCheckedTime));
			if(textview != null)
			{
				textview.setText("Last called at " + callString + "\nLast viewed at " + checkString);
			}
		}
		else
		{
			if(textview != null)
			{
				textview.setText("IsNull");
			}
		}

		Button notificationButton = (Button) findViewById(R.id.notificationButton);

		notificationButton.setOnClickListener
		(
			new View.OnClickListener() 
			{
				@Override
				public void onClick(View v) 
				{
					startChecking();
				}
			}
		);
		
		lastChecked = System.currentTimeMillis();
	}
	
	public void startChecking()
	{
		AsyncTaskRunner runner = new AsyncTaskRunner();
	    runner.execute();
	    
		final Handler handler = new Handler();
		handler.postDelayed
		(
				new Runnable() 
				{
					@Override
					public void run() 
					{
						Notify("Title: Meeting with Business","Msg:Pittsburg 10:00 AM EST ");
						startChecking();
					}
				}, 100000
		);
	}
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

	@SuppressWarnings("deprecation")
	private void Notify(String notificationTitle, String notificationMessage) 
	{
		NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		
		@SuppressWarnings("deprecation")
		Notification notification = new Notification(R.drawable.splash, "New Message", System.currentTimeMillis());

		Intent notificationIntent = new Intent(this, MainActivity.class);
		
		notificationIntent.putExtra("FromPrevious", true);
		notificationIntent.putExtra("LastCalled", System.currentTimeMillis());
		notificationIntent.putExtra("LastChecked", lastChecked);
		notificationIntent.putExtra("Waffle", "FUCK");
		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

		notification.setLatestEventInfo(MainActivity.this, notificationTitle, notificationMessage, pendingIntent);
		notificationManager.notify(9999, notification);
	}
	
	@Override
	public void onPause()
	{
		super.onPause();
		if(super.appView != null)
		{
			super.appView.loadUrl("javascript:try{PhoneGap.onResume.fire();}catch(e){};");
			super.appView.resumeTimers();
		}
	}

    public void testJavaAPI() throws IOException {
    	GitHub github = GitHub.connect();
    	GHRepository repository = github.getRepository("example-project");
    	List<GHEventInfo> eventList = repository.listEvents().asList();
    	
    	TextView textview = (TextView) findViewById(R.id.textView1);
    	String tempString = "";
    	
    	if(textview != null)
    	{
    		for(int i = 0 ; i < eventList.size(); i++)
    		{
    			GHEventInfo tempEventInfo = eventList.get(i);
    			tempString = tempString + tempEventInfo.toString();
    		}
    		
    		textview.setText( tempString );
    	}
    }
    
    class AsyncTaskRunner extends AsyncTask<String, String, String> 
    {
  	  	private String resp;

  	  	@Override
  	  	protected String doInBackground(String... params) 
  	  	{
  	  		try
  			{
	  	      	GitHub github = GitHub.connectUsingPassword("sgdavis", "Z4ngetsuSeth");
	  	      	LOG.i("WAFFLE", "connected");
	  	      	GHRepository repository = github.getRepository("ajyong/example-project");
	  	      	LOG.i("WAFFLE", "repod");
	  	      	List<GHEventInfo> eventList = repository.listEvents().asList();
	  	      	LOG.i("WAFFLE", "evented");
	  	      	
	  	      	String tempString = "";
	  	      	
	  	      	if(textview != null)
	  	      	{
	  	      		for(int i = 0 ; i < eventList.size(); i++)
	  	      		{
	  	      			GHEventInfo tempEventInfo = eventList.get(i);
	  	      			tempString = tempString + tempEventInfo.toString() + "\n";
	  	      		}
	  	      		
	  	      		LOG.i("WAFFLE", tempString );
	  	      	}
	  	      	
	  	      	displayText(tempString);
  			}
  			catch(IOException e)
  			{
  				//textview.setText( "exception" );
  			}
  	  		return "";
  	  	}
  	  	
  	  	public void displayText(final String strValue) 
  	  	{         
  	  		runOnUiThread
  	  		(
  	  			new Runnable() 
	  	  		{
	  	  			public void run() 
	  	  			{
	  	  				textview.setText(strValue);   
	  	  			}
	  	  		}
  	  		);
  	  	}

  	  	@Override
  	  	protected void onPostExecute(String result) 
  	  	{
  	  		// execution of result of Long time consuming operation
  	  		//textview.setText(resp);
  	  	}

  	  	@Override
  	  	protected void onPreExecute() 
  	  	{
  	  		resp = "waffle";
  	  		// Things to be done before execution of long running operation. For
  	  		// example showing ProgessDialog
  	  	}

  	  	@Override
  	  	protected void onProgressUpdate(String... text) 
  	  	{
  	  		//textview.setText(resp);
  	  		// Things to be done while execution of long running operation is in
  	  		// progress. For example updating ProgessDialog
  	  	}
  	}
}