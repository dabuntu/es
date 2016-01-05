define([
  'jquery',
  'underscore',
  'backbone',
  'text!templates/user/alert/registerAlertTpl.html',
  'mvc/user/collections/alerts',
  'mvc/user/models/user',
 ], function($, _, Backbone,registerAlertTpl, alertListCollection, userModel){
	var self;
	var registerAlertView = Backbone.View.extend({
    el: $("#alertTableContainer"),

	initialize: function(opts){
		  self = this;
		  self.app_router = opts.router;
		  this.sessionModel = opts.sessionModel;
	  },
	  
    render: function(){
     
     	this.fetchalert();
		
		//console.log("Login Id: "+this.sessionModel.id);
		
		var userObj = {			
			"userName" : this.sessionModel.userName,
      	    "roleId" : this.sessionModel.roleId,
      	    "id" : this.sessionModel.id			
		};
		
		// var user = new userModel();
		 //{"userName" : this.sessionModel.userName,"roleId" :this.sessionModel.roleId,"id" : this.sessionModel.id}
			
//		user.set({"userName" : this.sessionModel.userName});
//		user.set({"roleId" :this.sessionModel.roleId});
//		user.set({"id" : this.sessionModel.id});
    },
		fetchalert : function(){
			var alertOptions = '';
			var alertList = new alertListCollection();
		alertList.fetch({
			data: $.param({"userId":this.sessionModel.id}),
		    success: function(){
//		    	console.log("Alert View: "+JSON.stringify(alertList));
			    var a_temp = JSON.parse(JSON.stringify(alertList));
				var compiledTemplate = _.template(registerAlertTpl,{Data: a_temp});
			    //console.log("After Compile inside fetch : "+compiledTemplate);
			    self.$el.html(compiledTemplate);
			    
			  
  			
  			    $.each(a_temp, function(i, obj) {
  			    	//alertOptions += '<option value="ds'+obj['qID']+'">DS'+obj['qID']+':'+obj['queryName']+'</option>'; 	
  		        });
			    
			}			   
	 });
		}
  

  });

  return registerAlertView;
  
});
