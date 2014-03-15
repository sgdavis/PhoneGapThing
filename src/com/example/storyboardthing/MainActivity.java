package com.example.storyboardthing;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.storyboardthing.R;

public class MainActivity extends Activity 
{
	private long lastChecked;
	
	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);	
		setContentView(R.layout.activity_main);
		
		TextView textview = (TextView) findViewById(R.id.textView1);
			
		if(getIntent().getBooleanExtra("FromPrevious",false) == true)
		{
			long lastCalledTime = getIntent().getLongExtra("LastCalled", -1);
			long lastCheckedTime = getIntent().getLongExtra("LastChecked", -1);
			SimpleDateFormat formatter = new SimpleDateFormat("EEEE, MMMM d, yyyy HH:mm");
			String callString = formatter.format(new Date(lastCalledTime));
			String checkString = formatter.format(new Date(lastCheckedTime));
			if(textview != null)
			{
				textview.setText("Last called at " + callString + "\nLast viewed at" + checkString);
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
					Notify("Title: Meeting with Business","Msg:Pittsburg 10:00 AM EST ");
				}
			}
		);
		
		lastChecked = System.currentTimeMillis();
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
}
