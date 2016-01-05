define([
  'jquery',
  'underscore',
  'backbone',
  'text!templates/admin/login/adminLoginTemplate.html',
  'mvc/admin/models/login',
 ], function($, _, Backbone,adminLoginTemplate,login){
	var self;
	var adminLoginView = Backbone.View.extend({
    el: $("#page"),
	
	events: {
            "click #login": "loginValidate",
            "keypress #loginPWD" : "enterKeyPressEvent",
			"click #signup": "openRegisterPage"
       },
       
       
	  initialize: function(opts){
		  self = this;
		  self.app_router = opts.router;
		  function getCookie(c_name)
	   		{
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
	   		}
		  var checkAdmin = getCookie("checkAdmin");
			//console.log("loginName: "+login);
			
		  if((checkAdmin != null)&&(checkAdmin == 1)){
				//this.app_router.navigate('admin', {trigger:true});
				window.location.hash = 'adminHome';
			
			}
		  
	 
	  },
	  
    render: function(){
     this.$el.html(adminLoginTemplate);
    },
    enterKeyPressEvent: function(event){
    	
    	if(event.keyCode == 13){
            this.$("#login").click();
            
        }
    	
    	
    },
	loginValidate: function(){
		
	
	var loginId=document.getElementById('loginId').value;
	var loginPwd=document.getElementById('loginPWD').value;
	
	this.model = new login();
	this.model.set({"userName":loginId});
	this.model.set({"password":loginPwd});
	this.model.set({"checkAdmin":true});
	
	this.model.save(null,
           {
                success: function (model,responseText) {
                	//alert(JSON.stringify(responseText));
                	
                	//console.log(JSON.stringify(responseText));
                	
                	if((responseText.id != "-1") &&(responseText.id != 0) && (responseText.id != 'null'))
                	{
                		
                		function setCookie(c_name,value,exdays)
                		{
                		var exdate=new Date();
                		exdate.setDate(exdate.getDate() + exdays);
                		var c_value=escape(value) + ((exdays==null) ? "" : "; expires="+exdate.toUTCString());
                		document.cookie=c_name + "=" + c_value;
                		}
                		setCookie("userName",responseText.userName,1);
                		setCookie("id",responseText.id,1);
                		setCookie("roleId",responseText.roleId,1);
                		setCookie("checkAdmin","1",1);    		
                		self.app_router.navigate('adminHome', {trigger:true});
                		
                	}
                	else
                	{
                		
                		alert("Invalid UserName And Password.");
               
                	
                	}
                },
                error: function(model,responseText) {
                	alert("Error."+responseText);                                             
                }

            });

	
	},
	
	openRegisterPage: function(){
		self.app_router.navigate('registerUserPage', {trigger:true});
	}
	

  });

  return adminLoginView;
  
});
