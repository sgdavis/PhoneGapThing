package com.example.storyboardthing;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.cordova.DroidGap;
import org.apache.cordova.api.LOG;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.kohsuke.github.GHEvent;
import org.kohsuke.github.GHEventInfo;
import org.kohsuke.github.GHEventPayload;
import org.kohsuke.github.GHIssue;
import org.kohsuke.github.GHIssueComment;
import org.kohsuke.github.GHIssueState;
import org.kohsuke.github.GHMyself;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.PagedIterable;

import android.text.method.ScrollingMovementMethod;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.example.storyboardthing.R;

// https://api.github.com/repos/sgdavis/PhoneGapThing/events

public class MainActivity extends DroidGap 
{
	private long lastChecked;
	public TextView textview;
	private Context myContext;
	private Button notificationButton;
	
	private String repoName = "PhoneGapThing";
	private String ownerName = "sgdavis";
	
	private String username = "";
	private String password = "";
	
	private String stage = "login";
	
	private int minutes;
	private int seconds;
	private int delayTimer;
	private int minimumDelay = 60000;
	
	private long lastCalledTime;
	private long lastCheckedTime;
	
	private List<GHRepository> repositoryList;
	private List<GHIssue> issueList;
	private List<GHIssueComment> commentList;
	private List<JSONObject> jsonEventList;
	
	private PopupWindow popupWindow;
	
	private ArrayList<String> repositoryOwnerList = new ArrayList<String>();
	
	private ArrayList<String> stringList;
	private ArrayList<String> longStringList;
	private ArrayList<String> linkStringList;
	
	private AsyncTaskRunner runner;
	private Handler handler;
	
	private boolean notWorking;
	private int MaxEventListSize = 30;
	
	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		notWorking = true;
		myContext = this;
		super.onCreate(savedInstanceState);	
		super.init();
		super.loadUrl("file:///android_asset/www/index.html");
		super.setIntegerProperty("splashscreen", R.drawable.splash);
		setContentView(R.layout.activity_main);
		
		textview = (TextView) findViewById(R.id.textView1);
		textview.setMovementMethod(new ScrollingMovementMethod());
		notificationButton = (Button) findViewById(R.id.notificationButton);
			
		if(getIntent().getBooleanExtra("FromPrevious",false) == true)
		{
			lastCalledTime = getIntent().getLongExtra("LastCalled", -1);
			lastCheckedTime = getIntent().getLongExtra("LastChecked", -1);
			SimpleDateFormat formatter = new SimpleDateFormat("EEEE, MMMM d, yyyy HH:mm");
			String callString = formatter.format(new Date(lastCalledTime));
			String checkString = formatter.format(new Date(lastCheckedTime));
			if(textview != null)
			{
				textview.setText("Last called at " + callString + "\nLast viewed at " + checkString);
			}
			
			repoName = getIntent().getStringExtra("RepoName");
			ownerName = getIntent().getStringExtra("OwnerName");
			username = getIntent().getStringExtra("UserName");
			password = getIntent().getStringExtra("PassWord");
			stage = getIntent().getStringExtra("Stage");
			minutes = getIntent().getIntExtra("Minutes",0);
			seconds = getIntent().getIntExtra("Seconds",0);
			delayTimer = getIntent().getIntExtra("DelayTimer", minimumDelay);
			
			EditText tempEdit = ( (EditText) findViewById(R.id.editText1) );
			tempEdit.setText("");
			tempEdit.setHint("");
			tempEdit = ( (EditText) findViewById(R.id.editText2) );
			tempEdit.setText("");
			tempEdit.setHint("");
			
			if(stage.equalsIgnoreCase("events"))
			{
				stage = "eventsSpecial";
			}
			
			android.os.Process.killProcess( getIntent().getIntExtra("OlderPID", -1) );
			
			notificationButton.setText("WORKING");
			startChecking();
		}
		else
		{
			if(textview != null)
			{
				textview.setText("This is the original call, not from alert");
			}
		}

		notificationButton.setOnClickListener
		(
			new View.OnClickListener() 
			{
				@Override
				public void onClick(View v) 
				{
					continueToNextStage();
				}
			}
		);
		
		lastChecked = System.currentTimeMillis();	
	}
	
	@Override
	public void onDestroy()
	{
		runner.cancel(true);
		handler.removeCallbacksAndMessages(null);
	}
	
	public void continueToNextStage()
	{
		if(stage.equalsIgnoreCase("login"))
	    {
			stage = "repos";
			EditText tempEdit = ( (EditText) findViewById(R.id.editText1) );
			username = tempEdit.getText().toString();
			tempEdit.setText("");
			tempEdit.setHint("Minutes");
			tempEdit = ( (EditText) findViewById(R.id.editText2) );
			password = tempEdit.getText().toString();
			tempEdit.setText("");
			tempEdit.setHint("Seconds");
	    }
		else if(stage.equalsIgnoreCase("repos"))
		{
			EditText tempEdit = ( (EditText) findViewById(R.id.editText1) );
			minutes = Integer.parseInt(tempEdit.getText().toString());
			tempEdit.setText("");
			tempEdit.setHint("");
			tempEdit = ( (EditText) findViewById(R.id.editText2) );
			seconds = Integer.parseInt(tempEdit.getText().toString());
			tempEdit.setText("");
			tempEdit.setHint("");
			
			delayTimer = minutes * 60 * 1000 + seconds * 1000;
			if(delayTimer < minimumDelay)
			{
				delayTimer = minimumDelay;
			}
		}
		else if(stage.equalsIgnoreCase("events") || stage.equalsIgnoreCase("eventsSpecial"))
	    {
			stage = "repos";
	    }
		
		notificationButton.setText("WORKING");
		startChecking();
	}
	
	public void startChecking()
	{
		if(notWorking == true)
		{
			LOG.i("WAFFLE", "firing new thread" + delayTimer);
			notWorking = false;
			runner = new AsyncTaskRunner();
		    
		    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
		    	runner.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		    else
		    	runner.execute();
		}
		else
		{
			LOG.i("WAFFLE", "tried to fire new thread");
		}
	    
	    if(stage.equalsIgnoreCase("events"))
	    {
			handler = new Handler();
			handler.postDelayed
			(
					new Runnable() 
					{
						@Override
						public void run() 
						{
							startChecking();
						}
					}, delayTimer
			);
	    }
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
		notificationIntent.putExtra("UserName", username);
		notificationIntent.putExtra("PassWord", password);
		notificationIntent.putExtra("Stage", stage);
		notificationIntent.putExtra("RepoName", repoName);
		notificationIntent.putExtra("OwnerName", ownerName);
		notificationIntent.putExtra("Minutes", minutes);
		notificationIntent.putExtra("Seconds", seconds);
		notificationIntent.putExtra("DelayTimer", delayTimer);
		notificationIntent.putExtra("OlderPID", android.os.Process.myPid());
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
	
	public String getEventLongDescription(JSONObject event)
	{
		String ret = "";
		return ret;
	}
	
	public String getEventPageLink(JSONObject event)
	{
		String ret = "";
    	String supportString = "";
    	String toReplace = "https://api.github.com/repos/";
    	String replaceWith = "https://github.com/";
    	
    	try
    	{
    		String eventType = event.getString("type");
    		
    		JSONObject payload = new JSONObject( event.getString("payload") );
    		JSONObject actor = new JSONObject( event.getString("actor") );
    		
    		if(eventType.equals("CommitCommentEvent"))
    		{
    			JSONObject comment = new JSONObject( payload.getString("comment") );
    			ret = comment.getString("html_url");
    		}
    		else if(eventType.equals("CreateEvent"))
    		{
    			JSONObject repo = new JSONObject( event.getString("repo") );
    			supportString = repo.getString("url");
    			ret = supportString.replace(toReplace, replaceWith);
    		}
    		else if(eventType.equals("DeleteEvent"))
    		{
    			JSONObject repo = new JSONObject( event.getString("repo") );
    			supportString = repo.getString("url");
    			ret = supportString.replace(toReplace, replaceWith);
    		}
    	    else if(eventType.equals("DeploymentEvent"))
    		{
    	    	JSONObject innerPayload = new JSONObject( payload.getString("payload") );
    			supportString = innerPayload.getString("url");
    			ret = supportString.replace(toReplace, replaceWith);
    		}
    	    else if(eventType.equals("DeploymentStatusEvent"))
    		{
    	    	JSONObject innerPayload = new JSONObject( payload.getString("payload") );
    			supportString = innerPayload.getString("url");
    			ret = supportString.replace(toReplace, replaceWith);
    		}
    	    else if(eventType.equals("DownloadEvent"))
    		{
    	    	//no longer created
    	    	ret = "DownloadEvent";
    		}
    	    else if(eventType.equals("FollowEvent"))
    		{
    	    	//only user to user
    	    	ret = "FollowEvent";
    		}
    	    else if(eventType.equals("ForkEvent"))
    		{
    	    	JSONObject forkee = new JSONObject( payload.getString("payload") );
    	    	ret = forkee.getString("html_url");
    		}
    	    else if(eventType.equals("ForkApplyEvent"))
    		{
    	    	//no longer created
    	    	ret = "ForkApplyEvent";
    		}
    	    else if(eventType.equals("GistEvent"))
    		{
    	    	//no longer created
    	    	ret = "GistEvent";
    		}
    	    else if(eventType.equals("GollumEvent"))
    		{
    	    	JSONArray pages = new JSONArray( payload.getString("pages") );
    	    	JSONObject page = pages.getJSONObject(0);
    	    	ret = page.getString("html_url");
    		}
    	    else if(eventType.equals("IssueCommentEvent"))
    		{
    	    	JSONObject issue = new JSONObject( payload.getString("issue") );
    	    	ret = issue.getString("html_url");
    		}
    	    else if(eventType.equals("IssuesEvent"))
    		{
    	    	JSONObject issue = new JSONObject( payload.getString("issue") );
    	    	ret = issue.getString("html_url");
    		}
    	    else if(eventType.equals("MemberEvent"))
    		{
    	    	JSONObject member = new JSONObject( payload.getString("member") );
    	    	ret = member.getString("html_url");
    		}
    	    else if(eventType.equals("PageBuildEvent"))
    		{
    	    	JSONObject pagebuild = new JSONObject( payload.getString("pagebuild") );
    	    	supportString = pagebuild.getString("url");
    	    	ret = supportString.replace(toReplace, replaceWith);
    		}
    	    else if(eventType.equals("PublicEvent"))
    		{
    	    	JSONObject repo = new JSONObject( payload.getString("repo") );
    			supportString = repo.getString("url");
    			ret = supportString.replace(toReplace, replaceWith);
    		}
    	    else if(eventType.equals("PullRequestEvent"))
    		{
    	    	JSONObject pullrequest = new JSONObject( payload.getString("pull_request") );
    	    	ret = pullrequest.getString("html_url");
    		}
    	    else if(eventType.equals("PullRequestReviewCommentEvent"))
    		{
    	    	JSONObject comment = new JSONObject( payload.getString("comment") );
    			ret = comment.getString("html_url");
    		}
    	    else if(eventType.equals("PushEvent"))
    		{
    	    	JSONArray commits = new JSONArray( payload.getString("commits") );
    	    	JSONObject commit = commits.getJSONObject(0);
    	    	supportString = commit.getString("url");
    			ret = supportString.replace(toReplace, replaceWith);
    		}
    	    else if(eventType.equals("ReleaseEvent"))
    		{
    	    	JSONObject release = new JSONObject( payload.getString("release") );
    			ret = release.getString("html_url");
    		}
    	    else if(eventType.equals("StatusEvent"))
    		{
    	    	ret = payload.getString("target_url");
    		}
    	    else if(eventType.equals("TeamAddEvent"))
    		{
    	    	//user only
    	    	ret = "TeamAddEvent";
    		}
    	    else if(eventType.equals("WatchEvent"))
    		{
    	    	//user only
    			ret = "WatchEvent";
    		}
    	    else
    	    {
    	    	ret = "Unhandled Event";
    	    }
    	}
    	catch (JSONException e)
    	{
			LOG.i("WAFFLE", "cannot summarize " + e.getMessage());
    	}
    	
    	return ret;
	}

    public String getEventOverview(JSONObject event)
    {
    	String ret = "";
    	String supportString = "";
    	
    	try
    	{
    		String eventType = event.getString("type");
    		
    		JSONObject payload = new JSONObject( event.getString("payload") );
    		JSONObject actor = new JSONObject( event.getString("actor") );
    		
    		if(eventType.equals("CommitCommentEvent"))
    		{
    			ret = "Comment committed by " + actor.getString("login") + " at " + event.getString("created_at");
    		}
    		else if(eventType.equals("CreateEvent"))
    		{
    			String refType = payload.getString("ref_type");
    			String capitol   = Character.toString(refType.charAt(0)).toUpperCase();
    			refType = capitol + refType.substring(1,refType.length());
    			ret = refType + " created by " + actor.getString("login") + " at " + event.getString("created_at");
    		}
    		else if(eventType.equals("DeleteEvent"))
    		{
    			String refType = payload.getString("ref_type");
    			String capitol   = Character.toString(refType.charAt(0)).toUpperCase();
    			refType = capitol + refType.substring(1,refType.length());
    			ret = refType + " deleted by " + actor.getString("login") + " at " + event.getString("created_at");
    		}
    	    else if(eventType.equals("DeploymentEvent"))
    		{
    	    	ret = "Deployed to repo " + payload.getString("name") + " at " + event.getString("created_at");
    		}
    	    else if(eventType.equals("DeploymentStatusEvent"))
    		{
    	    	ret = "Deployed to repo " + payload.getString("name") + " became " + payload.getString("state") + " at " + event.getString("created_at");
    		}
    	    else if(eventType.equals("DownloadEvent"))
    		{
    	    	//no longer created
    	    	ret = "DownloadEvent";
    		}
    	    else if(eventType.equals("FollowEvent"))
    		{
    	    	//only user to user
    	    	ret = "FollowEvent";
    		}
    	    else if(eventType.equals("ForkEvent"))
    		{
    	    	ret = "Repository forked by " + payload.getString("name") + " at " + event.getString("created_at");
    		}
    	    else if(eventType.equals("ForkApplyEvent"))
    		{
    	    	//no longer created
    	    	ret = "ForkApplyEvent";
    		}
    	    else if(eventType.equals("GistEvent"))
    		{
    	    	//no longer created
    	    	ret = "GistEvent";
    		}
    	    else if(eventType.equals("GollumEvent"))
    		{
    	    	JSONArray pages = new JSONArray( payload.getString("pages") );
    	    	JSONObject page = pages.getJSONObject(0);
    	    	ret = "Wiki page " + page.getString("page_name") + " updated by " + actor.getString("login") + " at " + event.getString("created_at");
    		}
    	    else if(eventType.equals("IssueCommentEvent"))
    		{
    	    	JSONObject issue = new JSONObject(payload.getString("issue"));
    	    	ret = "Issue " + issue.getString("title") + " commented on by " + actor.getString("login") + " at " + event.getString("created_at");
    		}
    	    else if(eventType.equals("IssuesEvent"))
    		{
    	    	JSONObject issue = new JSONObject(payload.getString("issue"));
    	    	ret = "Issue " + issue.getString("title") + " " + payload.getString("action") + " by " + actor.getString("login") + " at " + event.getString("created_at");
    		}
    	    else if(eventType.equals("MemberEvent"))
    		{
    	    	JSONObject member = new JSONObject(payload.getString("member"));
    	    	ret = "Member " + member.getString("login") + " " + payload.getString("action") + " by " + actor.getString("login") + " at " + event.getString("created_at");
    		}
    	    else if(eventType.equals("PageBuildEvent"))
    		{
    	    	ret = "Page build by " + actor.getString("login") + " at " + event.getString("created_at");
    		}
    	    else if(eventType.equals("PublicEvent"))
    		{
    	    	ret = "Repository made public by " + actor.getString("login") + " at " + event.getString("created_at");
    		}
    	    else if(eventType.equals("PullRequestEvent"))
    		{
    	    	ret = "Pull request " + payload.getInt("number") + " " + payload.getString("action") + " by " + actor.getString("login") + " at " + event.getString("created_at");
    		}
    	    else if(eventType.equals("PullRequestReviewCommentEvent"))
    		{
    	    	ret = "Pull request commented on by " + actor.getString("login") + " at " + event.getString("created_at");
    		}
    	    else if(eventType.equals("PushEvent"))
    		{
    	    	ret = payload.getInt("size") + " commit(s) pushed by " + actor.getString("login") + " at " + event.getString("created_at");
    		}
    	    else if(eventType.equals("ReleaseEvent"))
    		{
    	    	ret = "Release published by " + actor.getString("login") + " at " + event.getString("created_at");
    		}
    	    else if(eventType.equals("StatusEvent"))
    		{
    	    	ret = "Status changed to " + payload.getString("state") + " by " + actor.getString("login") + " at " + event.getString("created_at");
    		}
    	    else if(eventType.equals("TeamAddEvent"))
    		{
    	    	//user only
    	    	ret = "TeamAddEvent";
    		}
    	    else if(eventType.equals("WatchEvent"))
    		{
    	    	//user only
    			ret = "WatchEvent";
    		}
    	    else
    	    {
    	    	ret = "Unhandled Event";
    	    }
    	}
    	catch (JSONException e)
    	{
			LOG.i("WAFFLE", "cannot summarize " + e.getMessage());
    	}
    	
    	return ret;
    }
    
    class AsyncTaskRunner extends AsyncTask<String, String, String> 
    {
  	  	private String resp;

  	  	@Override
  	  	protected String doInBackground(String... params) 
  	  	{
  	  		if(stage.equalsIgnoreCase("repos"))
	  		{
  	  			getListOfRepos();
	  		}
  	  		else if(stage.equalsIgnoreCase("events") || stage.equalsIgnoreCase("eventsSpecial"))
	  		{
  	  			getListOfEvents();
  	  			
	  	  		if(stage.equalsIgnoreCase("eventsSpecial") == false 
	  	  				&& delayTimer > minimumDelay 
	  	  				&& jsonEventList != null
	  	  				&& jsonEventList.size() > 0)
				{
					Notify("GitChecker",jsonEventList.size() + " new events detected!");
				}
	  		}
  	  		
  	  		notWorking = true;
  	  		return "";
  	  	}
  	  
  	  	public void getListOfEvents()
	  	{
  	  		getJSONEvents();
  	  		displayEvent();
	  	}
  	  
  	  	public void getListOfRepos()
	  	{
	  		try
			{
		      	GitHub github = GitHub.connectUsingPassword(username, password);
		      	LOG.i("WAFFLE", "connected");
		      	GHMyself myself = github.getMyself();
		      	LOG.i("WAFFLE", "found myself");
		      	repositoryList = myself.listAllRepositories().asList();
		      	LOG.i("WAFFLE", "got repo list");
		      	
		      	String tempString = "";
		      	
		      	if(textview != null)
		      	{
		      		for(int i = 0 ; i < repositoryList.size(); i++)
		      		{
		      			GHRepository tempRepository = repositoryList.get(i);
		      			tempString = tempString + tempRepository.getName() + "\n";
		      			
		      			try
	  	  		    	{
		      				repositoryOwnerList.add( i, tempRepository.getOwner().getLogin() );
	  	  		    	}
	  	  		    	catch (IOException e)
	  	  		    	{
	  	  		    		repositoryOwnerList.add("unknown");
	  	  		    	}
		      		}
		      		
		      		LOG.i("WAFFLE", tempString );
		      	}
		      	
		      	displayRepo();
			}
			catch(IOException e)
			{
				LOG.i("WAFFLE", "FAILED");
				
				runOnUiThread
		  		(
		  			new Runnable() 
		  	  		{
		  	  			public void run() 
		  	  			{ 
		  	  				stage = "login";
			  				EditText tempEdit = ( (EditText) findViewById(R.id.editText1) );
			  				username = tempEdit.getText().toString();
			  				tempEdit.setText("");
			  				tempEdit.setHint("Username");
			  				tempEdit = ( (EditText) findViewById(R.id.editText2) );
			  				password = tempEdit.getText().toString();
			  				tempEdit.setText("");
			  				tempEdit.setHint("Password");
			  				notificationButton.setText("LOGIN");
			  				final String item = "Login failed, please try again.";
		  	  		    	Toast.makeText(getBaseContext(), item, Toast.LENGTH_LONG).show();
		  	  			}
		  	  		}
		  		);
			}
	  	}
  	  	
  	  	public void displayEvent() 
	  	{         
	  	  	if(stage.equalsIgnoreCase("events") == false && stage.equalsIgnoreCase("eventsSpecial") == false)
		    {
	  	  		return;
		    }
	  		runOnUiThread
	  		(
	  			new Runnable() 
	  	  		{
	  	  			public void run() 
	  	  			{ 
	  	  				notificationButton.setText("BACK");
		  	  			final ListView listview = (ListView) findViewById(R.id.listview1);		
			  	  	    final ArrayAdapter adapter = new ArrayAdapter(myContext, android.R.layout.simple_list_item_1, stringList);
			  	  	    listview.setAdapter(adapter);
		
			  	  	    listview.setOnItemClickListener
			  	  	    (
			  	  	    	new AdapterView.OnItemClickListener() 
			  	  		    {
			  	  		      @Override
			  	  		      public void onItemClick(AdapterView<?> parent, final View view, int position, long id) 
			  	  		      {
			  	  		    	  showEventPopup(position);
			  	  		      }
			  	  		    }
			  	  	    );
	  	  			}
	  	  		}
	  		);
	  	}
  	  
  	  	public void displayRepo() 
	  	{        
	  	  	if(stage.equalsIgnoreCase("repos") == false)
		    {
	  	  		return;
		    }
	  		runOnUiThread
	  		(
	  			new Runnable() 
	  	  		{
	  	  			public void run() 
	  	  			{ 
	  	  				notificationButton.setText("OK");
		  	  			final ListView listview = (ListView) findViewById(R.id.listview1);		
			  	  	    final ArrayList<String> list = new ArrayList<String>();
			  	  	    
				  	  	for(int i = 0 ; i < repositoryList.size(); i++)
			      		{
			      			GHRepository tempRepository = repositoryList.get(i);
			      			list.add( tempRepository.getName() );
			      		}
			  	  	    final ArrayAdapter adapter = new ArrayAdapter(myContext, android.R.layout.simple_list_item_1, list);
			  	  	    listview.setAdapter(adapter);
		
			  	  	    listview.setOnItemClickListener
			  	  	    (
			  	  	    	new AdapterView.OnItemClickListener() 
			  	  		    {
			  	  		      @Override
			  	  		      public void onItemClick(AdapterView<?> parent, final View view, int position, long id) 
			  	  		      {
			  	  		    	  final String item = (String) parent.getItemAtPosition(position);
			  	  		    	  repoName = item;
			  	  		    	  ownerName = repositoryOwnerList.get(position);			  	  		    	  
			  	  		    	  stage = "events";
			  	  		    	  notificationButton.setText("WORKING");
			  	  		    	  
			  	  		    	  minutes = 0;
			  	  		    	  seconds = 0;
			  	  		    	  EditText tempEdit = ( (EditText) findViewById(R.id.editText1) );
			  	  		    	  if(tempEdit.getText().length() != 0)
			  	  		    	  {
			  	  		    		  minutes = Integer.parseInt(tempEdit.getText().toString());
			  	  		    	  }
			  	  		    	  tempEdit.setText("");
			  	  		    	  tempEdit.setHint("");
			  	  		    	  tempEdit = ( (EditText) findViewById(R.id.editText2) );
			  	  		    	  if(tempEdit.getText().length() != 0)
			  	  		    	  {
			  	  		    		  seconds = Integer.parseInt(tempEdit.getText().toString());
			  	  		    	  }
			  	  		    	  tempEdit.setText("");
			  	  		    	  tempEdit.setHint("");
					  	  			
			  	  		    	  delayTimer = minutes * 60 * 1000 + seconds * 1000;
			  	  		    	  if(delayTimer < minimumDelay)
			  	  		    	  {
			  	  		    		  delayTimer = minimumDelay;
			  	  		    	  }
					  	  			
			  	  		    	  startChecking();
			  	  		      }
			  	  		    }
			  	  	    );
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
    
    public String getJSONEvents()
    {
    	Date cutoffDate;
  		if(stage.equalsIgnoreCase("events"))
  		{
  			cutoffDate = new Date(System.currentTimeMillis() - delayTimer);
  		}
  		else //eventsSpecial
  		{
  			cutoffDate = new Date(lastCalledTime - delayTimer);
  		}
  		
    	try 
    	{
    		DefaultHttpClient httpClient = new DefaultHttpClient();
    		HttpGet getRequest = new HttpGet("https://api.github.com/repos/" + ownerName + "/" + repoName + "/events");
    		getRequest.addHeader("accept", "application/json");
     
    		HttpResponse response = httpClient.execute(getRequest);
     
    		if (response.getStatusLine().getStatusCode() != 200) 
    		{
    			throw new RuntimeException("Failed : HTTP error code : "  + response.getStatusLine().getStatusCode());
    		}
     
    		BufferedReader br = new BufferedReader(new InputStreamReader((response.getEntity().getContent())));
     
    		String output;
    		System.out.println("Output from Server .... \n");
    		
    		JSONArray mJsonArray;
			try 
			{
				mJsonArray = new JSONArray(br.readLine());
				JSONObject mJsonObject = new JSONObject();
				int listSize = mJsonArray.length();
				if(listSize > MaxEventListSize)
				{
					listSize = MaxEventListSize;
				}
				
				if(stringList != null)
		      	{
		      		stringList.clear();
		      	}
		      	stringList = new ArrayList<String>();
		      	
		      	if(longStringList != null)
		      	{
		      		longStringList.clear();
		      	}
		      	longStringList = new ArrayList<String>();
		      	
		      	if(linkStringList != null)
		      	{
		      		linkStringList.clear();
		      	}
		      	linkStringList = new ArrayList<String>();
		      	
		      	if(jsonEventList != null)
		      	{
		      		jsonEventList.clear();
		      	}
		      	jsonEventList = new ArrayList<JSONObject>();
		      	
	    		for (int i = 0; i < listSize; i++) 
	    		{
	    			JSONObject tempEventInfo = mJsonArray.getJSONObject(i);
	    			
	    			String dateString = tempEventInfo.getString("created_at");
	    			
			    	SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
			    	Date eventDate = null;
			    	try 
			    	{
			    		eventDate = formatter.parse(dateString);
			    		Calendar cal = Calendar.getInstance();
			    		cal.setTime(eventDate);
			    		cal.add(Calendar.HOUR, -6);
			    		eventDate = cal.getTime();
					} 
			    	catch (ParseException e) 
					{
						e.printStackTrace();
					}
	    	
	    			if( delayTimer > minimumDelay && (eventDate == null || eventDate.before(cutoffDate)) )
	    			{
	    				continue;
	    			}
	    			else if(eventDate != null)
	    			{
	    				LOG.i("MEGAWAFFLE","event date: " + eventDate.toString() + " - cutoff date: " + cutoffDate.toString());
	    			}
	    			
	    			jsonEventList.add( tempEventInfo );
	    			
	    			//short description
	    			String temp = getEventOverview( tempEventInfo );
	      			stringList.add(temp);
	    			
	    			//long description
	    			temp = getEventLongDescription( tempEventInfo );
	      			longStringList.add(temp);
	    			
	    			//link to page
	    			temp = getEventPageLink( tempEventInfo );
	      			linkStringList.add(temp);
	    		}
			} 
			catch (JSONException e) 
			{
				e.printStackTrace();
			}
     
    		httpClient.getConnectionManager().shutdown();
    		return "";
     
    	} 
    	catch (ClientProtocolException e) 
    	{
    		e.printStackTrace();
     
    	} 
    	catch (IOException e) 
    	{
    		e.printStackTrace();
    	}
        LOG.i("MEGAWAFFLE","FAILED");
        return "";
    }
    
    // The method that displays the popup.
  	private void showEventPopup(final int index) 
  	{
  		LOG.i("WAFFLE","calling popup");
  		int popupWidth = 300;
  		int popupHeight = 300;
  		
  		Display display = getWindowManager().getDefaultDisplay();
  		Point size = new Point();
  		display.getSize(size);
  		int width = size.x;
  		int height = size.y;
   
  		//setContentView(R.layout.popup);
  	
  		// Inflate the popup_layout.xml
  		LinearLayout viewGroup = (LinearLayout) findViewById(R.id.popupPanel);
  		LayoutInflater layoutInflater = (LayoutInflater) myContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
  		View layout = layoutInflater.inflate(R.layout.popup, viewGroup, false);
   
  		// Creating the PopupWindow
  		popupWindow = new PopupWindow(myContext);
  		popupWindow.setContentView(layout);
  		popupWindow.setWidth(popupWidth);
		popupWindow.setHeight(popupHeight);
		popupWindow.setFocusable(true);
   
  		// Clear the default translucent background
  		popupWindow.setBackgroundDrawable(new BitmapDrawable());
   
  		// Displaying the popup at the specified location, + offsets.
  		popupWindow.showAtLocation(layout, Gravity.NO_GRAVITY, (width - popupWidth)/2, (height - popupHeight)/2);
   
  		// Getting a reference to Close button, and close the popup when clicked.
  		Button gotoPage = (Button) layout.findViewById(R.id.goToPage);
  		gotoPage.setOnClickListener
  		(
  			new View.OnClickListener() 
  			{
  				@Override
  				public void onClick(View v) 
  				{
  					Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse( linkStringList.get(index) ));
  					startActivity(intent);
  				}
  			}
  		);
  		
  		Button close = (Button) layout.findViewById(R.id.close);
  		close.setOnClickListener
  		(
  			new View.OnClickListener() 
  			{
  				@Override
  				public void onClick(View v) 
  				{
  					popupWindow.dismiss();
  				}
  			}
  		);
  		
  		//setContentView(R.layout.activity_main);
  	}
}