define([
  'underscore',
  'backbone'
], function(_, Backbone) {
    var sessionModel = Backbone.Model.extend({
	
    	defaults: function(){
            return{
              		userName : "",	// email
              	    roleId:"",		// admin or normal user
              	    id:"",  			// user'id in database
              	    selectedDS:"",	// selected data source ID
              	    selectedQuery: ""	// selected query ID
            }
            
        },
	   
	checkSession: function(){		
		 //console.out("inside model session checksession");
		    this.userName = this.getCookie("userName");
		    this.id = this.getCookie("id");
		    this.roleId = this.getCookie("roleId");
			var loc = '';
			//console.log("role id is "+this.roleId);
			   if (this.roleId == null || this.roleId == ''){
    				//this is for normal user
				  loc =  'login';
			   }
			   return loc;
		    	
	},
	getCookie: function (c_name)
		{
		//console.out("inside model session getcookie");
   		var c_value = document.cookie;
   		var c_start = c_value.indexOf(" " + c_name + "=");
   		if (c_start == -1)
   		  {
   		  c_start = c_value.indexOf(c_name + "=");
   		  }
   		if (c_start == -1)
   		  {
   		  c_value = null;
   		  }
   		else
   		 {
   		  c_start = c_value.indexOf("=", c_start) + 1;
   		  var c_end = c_value.indexOf(";", c_start);
   		  if (c_end == -1)
   		  	{
   		    c_end = c_value.length;
   		    }
   		  c_value = unescape(c_value.substring(c_start,c_end));
   		 }
   		return c_value;
		},
	initialize: function(){
		
    }

	});
    
    
  
  return sessionModel;

});
