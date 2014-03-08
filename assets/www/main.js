	
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
		            	<!-- Link to issue hidden under issue name -->
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
		
		msg();
		
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
	
	function getComments()
	{
		window.location = "display.html";
	}
	
	function getClosedIssues()
	{
		window.location = "display.html";
	}
	
	function getMilestoneDates()
	{
		window.location = "display.html";
	}
	
	function clearData()
	{
		window.location = "menu.html";
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
		$.ajax
	    (
	    	{
        		type: "GET",
        		async: false,
        		url: "https://api.github.com/repos/" + localStorage['ownername'] + "/" + localStorage['repository'] + "/issues?state=closed",
        		dataType: "json",
        		success: function(result) 
        		{
        			return result;
        		}
        	}
        )
	}

	function getData() 
	{
		msg();
		var text = "WORKING";
		var totalIssues = 0;
	    $.ajax
	    (
	    	{
        		type: "GET",
        		async: false,
        		url: "https://api.github.com/repos/" + localStorage['ownername'] + "/" + localStorage['repository'] + "/issues",
        		dataType: "json",
        		success: function(result) 
        		{	
        			totalIssues += result.length;
            		for( i in result ) 
            		{
                		<!-- Link to issue hidden under issue name -->
                		$("#issue_list").append
                		(
                    		"<li><a href='" + result[i].url + "' target='_blank'>" +
                    		result[i].title + "</a></li>"
                		);
                
                		text = text + "Author: " + result[i].user.login + "\n";
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
            		}
		            console.log(result);
	        	}
	    	}
		);
		$.ajax
	    (
	    	{
        		type: "GET",
        		async: false,
        		url: "https://api.github.com/repos/" + localStorage['username'] + "/" + localStorage['repository'] + "/issues?state=closed",
        		dataType: "json",
        		success: function(result) 
        		{	
        			totalIssues += result.length;
            		for( i in result ) 
            		{
                		<!-- Link to issue hidden under issue name -->
                		$("#issue_list").append
                		(
                    		"<li><a href='" + result[i].url + "' target='_blank'>" +
                    		result[i].title + "</a></li>"
                		);
                
                		text = text + "Author: " + result[i].user.login + "\n";
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
            		}

		            console.log(result);
		            document.getElementById("issue_count").innerHTML = "";
					$("#issue_count").append("Total Issues: " + totalIssues);
	        	}
	    	}
		);
	}