package com.example.storyboardthing;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.cordova.DroidGap;
import org.apache.cordova.api.LOG;
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
	
	private long lastCalledTime;
	private long lastCheckedTime;
	
	private List<GHEventInfo> eventList;
	private List<GHRepository> repositoryList;
	private List<GHIssue> issueList;
	private List<GHIssueComment> commentList;
	
	private PopupWindow popupWindow;
	
	private ArrayList<String> repositoryOwnerList = new ArrayList<String>();
	
	private ArrayList<String> stringList;
	
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
			delayTimer = getIntent().getIntExtra("DelayTimer", 10000);
			
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
				textview.setText("IsNull");
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
			if(delayTimer < 30000)
			{
				delayTimer = 30000;
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

    public String getEventOverview(GHEventInfo event)
    {
    	String ret = "";
    	GHEventPayload temp;
    	GHEventPayload.IssueComment issueComment = new GHEventPayload.IssueComment();
    	GHEventPayload.PullRequest pullRequest = new GHEventPayload.PullRequest();
    	GHEventPayload.Push push = new GHEventPayload.Push();
    	String supportString = "";
    	int i = 0;
    	int j = 0;
    	try
    	{
    		switch(event.getType())
    		{
    		case ISSUE_COMMENT:
    			//issueComment = event.getPayload(issueComment.getClass());
    			supportString = "ERROR";
    			if(issueList != null)
    			{
	    			for(i = 0; i < issueList.size(); i++)
	    			{
	    				if( issueList.get(i).getUpdatedAt().after(event.getCreatedAt()) || issueList.get(i).getUpdatedAt().equals(event.getCreatedAt()))
	    				{
	    					commentList = issueList.get(i).getComments();
	    					
	    					for(j = 0; j < commentList.size(); j++)
	    					{
	    						if( commentList.get(j).getCreatedAt().equals(event.getCreatedAt()) || commentList.get(j).getUpdatedAt().equals(event.getCreatedAt()) )
	    						{
	    							supportString = issueList.get(i).getTitle();
	    							break;
	    						}
	    					}
	    				}
	    				
	    				if(!supportString.equals("ERROR"))
    					{
    						break;
    					}
	    			}
    			}
				ret = "Comment on issue *" + supportString + "* by user *" + event.getActorLogin() + "* on " + event.getCreatedAt();
				break;
    		case PULL_REQUEST:
    			pullRequest = event.getPayload(pullRequest.getClass());
    			ret = "Data pulled by user *" + event.getActorLogin() + "* on " + event.getCreatedAt();
				break;
    		case PUSH:
    			push = event.getPayload(push.getClass());
    			ret = push.getSize() + " commits pushed by user *" + event.getActorLogin() + "* on " + event.getCreatedAt();
				break;
    		case COMMIT_COMMENT:
    			ret = "Comments on commit *" + "" + "* by user *" + event.getActorLogin() + "* on " + event.getCreatedAt();
				break;
    		case CREATE:
    			//payload ref_type Can be one of “repository”, “branch”, or “tag”
    			if(false)
    			{
    				supportString = "Branch *" + "" + "*";
    			}
    			if(false)
    			{
    				supportString = "Tag *" + "" + "*";
    			}
    			else
    			{
    				supportString = "Repository";
    			}
    			ret = supportString + " created by user *" + event.getActorLogin() + "* on " + event.getCreatedAt();
				break;
    		case DELETE:
    			if(false)
    			{
    				supportString = "Branch *" + "" + "*";
    			}
    			if(false)
    			{
    				supportString = "Tag *" + "" + "*";
    			}
    			else
    			{
    				supportString = "Repository";
    			}
    			//payload ref_type Can be one of “branch”, or “tag”
    			ret = supportString + " destroyed by user *" + event.getActorLogin() + "* on " + event.getCreatedAt();
				break;
    		case DOWNLOAD:
    			//deprecated by github
    			ret = "Repository downloaded by user *" + event.getActorLogin() + "* on " + event.getCreatedAt();
				break;
    		case FOLLOW:
    			//only user to user
    			ret = "User followed by user *" + event.getActorLogin() + "* on " + event.getCreatedAt();
				break;
    		case FORK:
    			ret = "Repository forked by user *" + event.getActorLogin() + "* on " + event.getCreatedAt();
				break;
    		case FORK_APPLY:
    			//deprecated by github
    			ret = "Fork application by user *" + event.getActorLogin() + "* on " + event.getCreatedAt();
				break;
    		case GIST:
    			//deprecated by github
    			ret = "GIST created by user *" + event.getActorLogin() + "* on " + event.getCreatedAt();
				break;
    		case GOLLUM:
    			ret = "Wiki page *" + "" + "* updated by user *" + event.getActorLogin() + "* on " + event.getCreatedAt();
				break;
    		case ISSUES:
    			//payload action Can be one of “opened”, “closed”, or “reopened”.
    			ret = "Issue *" + "" + "* created by user *" + event.getActorLogin() + "* on " + event.getCreatedAt();
				break;
    		case MEMBER:
    			ret = "Collaborator *" + "" + "* added by *" + event.getActorLogin() + "* on " + event.getCreatedAt();
				break;
    		case PUBLIC:
    			ret = "Repository made public by *" + event.getActorLogin() + "* on " + event.getCreatedAt();
				break;
    		case PULL_REQUEST_REVIEW_COMMENT:
    			ret = "Repository publicity changed by *" + event.getActorLogin() + "* on " + event.getCreatedAt();
				break;
    		case TEAM_ADD:
    			//user or repo added to a team
				break;
    		case WATCH:
    			ret = "Repository starred by *" + event.getActorLogin() + "* on " + event.getCreatedAt();
				break;
			default:
				ret = "Unhandled Event";
				break;
    		}
    	}
    	catch (IOException e)
    	{
				LOG.i("WAFFLE", "cannot summarize");
    	}
    	
    	LOG.i("WAFFLE", "made summary");
    	
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
	  	  				&& delayTimer > 60000 
	  	  				&& eventList != null
	  	  				&& eventList.size() > 0)
				{
					Notify("GitChecker",eventList.size() + " new events detected!");
				}
	  		}
  	  		
  	  		notWorking = true;
  	  		return "";
  	  	}
  	  
  	  	public void getListOfEvents()
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
		      	GitHub github = GitHub.connectUsingPassword(username, password);
		      	LOG.i("WAFFLE", "connected");
		      	GHRepository repository = github.getRepository(ownerName + "/" + repoName);
		      	LOG.i("WAFFLE", "repod");
		      	eventList = repository.listEvents().asList();
		      	LOG.i("WAFFLE", "evented");
		      	
		      	//Can be pulled after the getPayload function is fixed for issue comments
		      	issueList = repository.getIssues(GHIssueState.OPEN);
		      	issueList.addAll( repository.getIssues(GHIssueState.CLOSED) );
		      	LOG.i("WAFFLE", "ISSUE #" + issueList.size() );
		      	
		      	String tempString = "";
		      	if(stringList != null)
		      	{
		      		stringList.clear();
		      	}
		      	stringList = new ArrayList<String>();
		      	
		      	int listSize = eventList.size();
		      	if(listSize > MaxEventListSize)
		      	{
		      		listSize = MaxEventListSize;
		      	}
		      	
		      	if(textview != null)
		      	{
		      		for(int i = 0 ; i < listSize; i++)
		      		{
		      			GHEventInfo tempEventInfo = eventList.get(i);
		      			String temp = getEventOverview(tempEventInfo);
		      			stringList.add(temp);
		      			tempString = tempString + temp + "\n";
		      			
		      			if(delayTimer > 60000 && tempEventInfo.getCreatedAt().before(cutoffDate))
		      			{
		      				eventList.remove(i--);
		      			}
		      		}
		      		
		      		LOG.i("WAFFLE", tempString );
		      	}
		      	
		      	displayEvent();
		      	}
			catch(IOException e)
			{
				LOG.i("WAFFLE", "FAILED");
			}
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
			  	  		    	  final String item = (String) parent.getItemAtPosition(position);
			  	  		    	  Toast.makeText(getBaseContext(), item, Toast.LENGTH_LONG).show();
			  	  		    	  showEventPopup();
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
			  	  		    	  if(delayTimer < 10000)
			  	  		    	  {
			  	  		    		  delayTimer = 10000;
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
    
    // The method that displays the popup.
  	private void showEventPopup()//(GHEventInfo eventInfo) 
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