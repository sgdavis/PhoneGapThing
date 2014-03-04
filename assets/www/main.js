	
	function msg()
	{
	    alert("button clicked");
	}

	function getData() 
	{
		var text = "WORKING";
	    $.ajax
	    (
	    		{
	        		type: "GET",
	        		url: "https://api.github.com/repos/" + $('#username').val() + "/" + $('#repository').val() + "/issues",
	        		dataType: "json",
	        		success: function(result) 
	        		{
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
			                    "User assigned: " + result[i].assignee + "<br>"
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
			            $("#issue_count").append("Total Issues: " + result.length);
	        		}
	    		}
			);
			
			alert(text);
		}