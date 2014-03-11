	
	function msg()
	{
	    alert("button clicked : " 	+ localStorage['username'] + " : " 
	    							+ localStorage['password'] + " : " 
	    							+ localStorage['repository'] + " : " 
	    							+ localStorage['resultNum']);
	}
	
	function pageLoader()
	{
		if(window.location == "file:///android_asset/www/display.html" && localStorage['whatToLoad'] == "MyIssues")
		{
			getMyIssues();
		}
		else if(window.location == "file:///android_asset/www/display.html" && localStorage['whatToLoad'] == "SelectForComments")
		{
			getComments();
		}
		else if(window.location == "file:///android_asset/www/display.html" && localStorage['whatToLoad'] == "ClosedIssues")
		{
			getClosedIssues();
		}
		else if(window.location == "file:///android_asset/www/display.html" && localStorage['whatToLoad'] == "Milestones")
		{
			getMilestoneDates();
		}
		else
		{
			alert(window.location + " : " + localStorage['whatToLoad']);
		}
	}
	
	function callMenuPage()
	{
		window.location = "menu.html";
    }
    
    function checkUserRepos() 
	{
		$("#repo_list").html("");
		
	    $.ajax
	    (
    		{
        		type: "GET",
        		async: false,
        		url: "https://api.github.com/users/" + $('#username').val() + "/repos?type=all",
        		dataType: "json",
        		success: function(result) 
        		{
        			if(result.length != 0)
		            {
		            	localStorage['username'] = $('#username').val();
		            	localStorage['password'] = $('#password').val();
		            }
		            
		            for(i in result)
		            {
		            	<!-- Button to set whta repo we're looking into -->
                		$("#repo_list").append
                		(
                    		"<input type='button' name='sel' value='" 
                    		+ result[i].name 
                    		+ "' onclick=validateData('" 
                    		+ String(result[i].owner.login) 
                    		+ "','" 
                    		+ String(result[i].name) 
                    		+ "');> <br>"
                		);
		            }
		        }
		    }
		)
	}
	
	function callFilterPage()
	{
		localStorage['whatToLoad'] = "MyIssues";
		window.location = "display.html";
	}
	
	function loadMyIssues()
	{
		localStorage['whatToLoad'] = "MyIssues";
		window.location = "display.html";
	}
	
	function getMyIssues()
	{
		result = getOpenData();
		var totalIssues = 0;
		
		for( i in result ) 
		{			
			if(result[i].assignee && result[i].assignee.login == localStorage['username'])
			{
	    		<!-- Link to issue hidden under issue name -->
	    		$("#issue_list").append
	    		(
	        		"<li><a href='" + result[i].url + "' target='_blank'>" +
	        		result[i].title + "</a></li>"
	    		);
	    
	            <!-- Author name -->
	            $("#issue_list").append
	            (
	                "Author: " + result[i].user.login + "<br>"
	            );
	    
	            <!-- Issue body -->
	            $("#issue_list").append
	            (
	                "Details: " + result[i].body + "<br>"
	            );
	    
	            <!-- Issue state -->
	            $("#issue_list").append
	            (
	                "Open/Closed: " + result[i].state + "<br>"
	            );
	            
	            <!-- Issue assignee -->
	            $("#issue_list").append
	            (
	                "User assigned: " + result[i].assignee.login + "<br>"
	            );
	            
	            <!-- Milestone -->
	            $("#issue_list").append
	            (
	                "Milestone: " + result[i].milestone + "<br>"
	            );
	            
	            <!-- Comments -->
	            $("#issue_list").append
	            (
	                "Number of comments: " + result[i].comments + "<br>"
	            );
	            
	            <!-- Date created -->
	            $("#issue_list").append
	            (
	                "Date created: " + result[i].created_at + "<br>"
	            );
	            
	            <!-- Date updated -->
	            $("#issue_list").append
	            (
	                "Date updated: " + result[i].updated_at + "<br>"
	            );
	            
	            <!-- Date Closed -->
	            $("#issue_list").append
	            (
	                "Date Closed: " + result[i].closed_at + "<br>"
	            );
	
	    		console.log("i: " + i);
	    		totalIssues = totalIssues+ 1;
			}
		}
		
		result = getClosedData();
		
		for( i in result ) 
		{			
			if(result[i].assignee && result[i].assignee.login == localStorage['username'])
			{
	    		<!-- Link to issue hidden under issue name -->
	    		$("#issue_list").append
	    		(
	        		"<li><a href='" + result[i].url + "' target='_blank'>" +
	        		result[i].title + "</a></li>"
	    		);
	    
	            <!-- Author name -->
	            $("#issue_list").append
	            (
	                "Author: " + result[i].user.login + "<br>"
	            );
	    
	            <!-- Issue body -->
	            $("#issue_list").append
	            (
	                "Details: " + result[i].body + "<br>"
	            );
	    
	            <!-- Issue state -->
	            $("#issue_list").append
	            (
	                "Open/Closed: " + result[i].state + "<br>"
	            );
	            
	            <!-- Issue assignee -->
	            $("#issue_list").append
	            (
	                "User assigned: " + result[i].assignee.login + "<br>"
	            );
	            
	            <!-- Milestone -->
	            $("#issue_list").append
	            (
	                "Milestone: " + result[i].milestone + "<br>"
	            );
	            
	            <!-- Comments -->
	            $("#issue_list").append
	            (
	                "Number of comments: " + result[i].comments + "<br>"
	            );
	            
	            <!-- Date created -->
	            $("#issue_list").append
	            (
	                "Date created: " + result[i].created_at + "<br>"
	            );
	            
	            <!-- Date updated -->
	            $("#issue_list").append
	            (
	                "Date updated: " + result[i].updated_at + "<br>"
	            );
	            
	            <!-- Date Closed -->
	            $("#issue_list").append
	            (
	                "Date Closed: " + result[i].closed_at + "<br>"
	            );
	
	    		console.log("i: " + i);
	    		totalIssues = totalIssues + 1;
			}
		}
		
		console.log(result);
        document.getElementById("issue_count").innerHTML = "";
		$("#issue_count").append("Total Issues: " + totalIssues);
	}
	
	function loadComments()
	{
		localStorage['whatToLoad'] = "SelectForComments";
		window.location = "display.html";
	}
	
	function getComments()
	{
		result = getOpenData();
		
		for( i in result ) 
		{			
			if(result[i].comments != 0)
			{
	    		<!-- Button to select what issue we're looking into -->
        		$("#issue_list").append
        		(
            		"<input type='button' name='sel' value='" 
            		+ result[i].title 
            		+ "' onclick=showComments('" 
            		+ String(result[i].number)
            		+ "');> <br>"
        		);
	
	    		console.log("i: " + i);
			}
		}
		
		result = getClosedData();
		
		for( i in result ) 
		{			
			if(result[i].comments != 0)
			{
	    		<!-- Button to select what issue we're looking into -->
        		$("#issue_list").append
        		(
            		"<input type='button' name='sel' value='" 
            		+ result[i].title 
            		+ "' onclick=showComments('" 
            		+ String(result[i].number)
            		+ "');> <br>"
        		);
	
	    		console.log("i: " + i);
			}
		}
	}
	
	function showComments(issueNumber)
	{
		$("#issue_list").html("");
		localStorage['whatToLoad'] = "ShowingComments";		

		$.ajax
	    (
    		{
        		type: "GET",
        		async: false,
        		url: "https://api.github.com/repos/" + localStorage['ownername'] + "/" + localStorage['repository'] + "/issues/" + issueNumber + "/comments",
        		dataType: "json",
        		success: function(result) 
        		{
        			if(result.length != 0)
		            {
		            	for( i in result ) 
						{			
				    		<!-- Name of user who submitted comment -->
				    		$("#issue_list").append
				    		(
				        		"Comment By: " + result[i].user.login + "<br>"
				    		);
				    		
				    		<!-- Date user submitted comment -->
				    		$("#issue_list").append
				    		(
				        		"Date Commented: " + result[i].updated_at + "<br>"
				    		);
				    		
				    		<!-- Comment body -->
				    		$("#issue_list").append
				    		(
				        		"Comment: " + result[i].body + "<br><br>"
				    		);
				    		
				    		console.log("i: " + i);
						}
		            }
		        }
		    }
		)
	}
	
	function loadClosedIssues()
	{
		localStorage['whatToLoad'] = "ClosedIssues";
		window.location = "display.html";
	}
	
	function getClosedIssues()
	{
		results = getClosedData();
		
		for( i in results ) 
		{		    		
			$.ajax
		    (
		    	{
	        		type: "GET",
	        		async: false,
	        		url: results[i].url,
	        		dataType: "json",
	        		success: function(result) 
	        		{	            	
	        			<!-- Link to issue hidden under issue name -->
			    		$("#issue_list").append
			    		(
			        		"<li><a href='" + result.url + "' target='_blank'>" +
			        		result.title + "</a></li>"
			    		);
			    			
			    		<!-- Name of user who closed issue -->
			    		$("#issue_list").append
			    		(
			        		"Closed By: " + result.closed_by.login + "<br>"
			    		);
			    		
			    		<!-- Date user closed issue -->
			    		$("#issue_list").append
			    		(
			        		"Date Closed: " + result.closed_at + "<br>"
			    		);
			    	}
			    }
			)
		}
	}
	
	function loadMilestoneDates()
	{
		localStorage['whatToLoad'] = "Milestones";
		window.location = "display.html";
	}
	
	function getMilestoneDates()
	{
		$.ajax
	    (
	    	{
        		type: "GET",
        		async: false,
        		url: "https://api.github.com/repos/" + localStorage['ownername'] + "/" + localStorage['repository'] + "/milestones",
        		dataType: "json",
        		success: function(result) 
        		{	
            		for( i in result ) 
            		{
                		<!-- Link to issue hidden under milestone name -->
                		$("#issue_list").append
                		(
                    		"<li><a href='" + result[i].url + "' target='_blank'>" +
                    		result[i].title + "</a></li>"
                		);
                
		                <!-- Author name -->
		                $("#issue_list").append
		                (
		                    "Author: " + result[i].creator.login + "<br>"
		                );
                
		                <!-- Milestone body -->
		                $("#issue_list").append
		                (
		                    "Details: " + result[i].description + "<br>"
		                );
                
		                <!-- Milestone state -->
		                $("#issue_list").append
		                (
		                    "Open/Closed: " + result[i].state + "<br>"
		                );
		                
		                <!-- Date created -->
		                $("#issue_list").append
		                (
		                    "Date created: " + result[i].created_at + "<br>"
		                );
		                
		                <!-- Date updated -->
		                $("#issue_list").append
		                (
		                    "Date updated: " + result[i].updated_at + "<br>"
		                );
		                
		                <!-- Date Closed -->
		                $("#issue_list").append
		                (
		                    "Date Closed: " + result[i].closed_at + "<br>"
		                );
		                
		                <!-- Date Due -->
		                $("#issue_list").append
		                (
		                    "Date Closed: " + result[i].due_on + "<br>"
		                );
		                
		                <!-- Issues -->
		                $("#issue_list").append
		                (
		                	"Progress: " + 100*( result[i].closed_issues / (result[i].closed_issues + result[i].open_issues) ) + "%<br>"
		                	+ "<progress value='" 
		                		+ 100*( result[i].closed_issues / (result[i].closed_issues + result[i].open_issues) ) 
		                		+ "' max='100'>70 %</progress> <br>"
		                    + "( " + result[i].open_issues+ " open : "
		                    + result[i].closed_issues + " closed ) "
		                    + "<br><br>"
		                );
    
                		console.log("i: " + i);
            		}
	        	}
	    	}
		)
	}
	
	function clearData()
	{
		if(localStorage['whatToLoad'] == "ShowingComments")
		{
			localStorage['whatToLoad'] = "SelectForComments";
			window.location = "display.html";
		}
		else
		{
			window.location = "menu.html";
		}
	}
	
	function changeUser()
	{
		window.location = "index.html";
	}
	
	function validateData(ownerName,repoName) 
	{
		$.ajax
	    (
    		{
        		type: "GET",
        		async: false,
        		url: "https://api.github.com/repos/" + ownerName + "/" + repoName + "/issues",
        		dataType: "json",
        		success: function(result) 
        		{
        			if(result.length != 0)
		            {
		            	localStorage['ownername'] = ownerName;
		            	localStorage['repository'] = repoName;
		            	localStorage['resultNum'] = String(result.length);
		            	callMenuPage();
		            }
		        }
		    }
		)
	}
	
	function getOpenData() 
	{
		var returnData;
		$.ajax
	    (
	    	{
        		type: "GET",
        		async: false,
        		url: "https://api.github.com/repos/" + localStorage['ownername'] + "/" + localStorage['repository'] + "/issues",
        		dataType: "json",
        		success: function(result) 
        		{
        			returnData = result;
        		}
        	}
        )

        return returnData;
	}
	
	function getClosedData() 
	{
		var returnData;
		$.ajax
	    (
	    	{
        		type: "GET",
        		async: false,
        		url: "https://api.github.com/repos/" + localStorage['ownername'] + "/" + localStorage['repository'] + "/issues?state=closed",
        		dataType: "json",
        		success: function(result) 
        		{
        			returnData = result;
        		}
        	}
        )
        
        return returnData;
	}