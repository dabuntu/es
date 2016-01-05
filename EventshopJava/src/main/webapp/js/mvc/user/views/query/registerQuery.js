define([
  'jquery',
  'underscore',
  'backbone',
  'text!templates/user/query/registerQueryTpl.html',
  'mvc/user/collections/queries',
  'mvc/user/models/user',
 ], function($, _, Backbone,registerDatasourceTpl, queryListCollection, userModel){
	var self;
	var registerQueryView = Backbone.View.extend({
    el: $("#queryTableContainer"),

	initialize: function(opts){
		  self = this;
		  self.app_router = opts.router;
		  this.sessionModel = opts.sessionModel;
	  },
	  
    render: function(){
     
     	this.fetchquery();
		
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
		fetchquery : function(){
			var queryOptions = '';
			var queryList = new queryListCollection();
			queryList.toJSON();
		queryList.fetch({			
			data: $.param({"userId":this.sessionModel.id}),
		    success: function(){		    	
			    var q_temp = JSON.parse(JSON.stringify(queryList));
			 	//var variables = [{  "srcName": "asthama", "access": "Public", "userListAssToDSAccess": null, "srcID": 13, "url": null, "creater": 0, "archive": 0, "unit": 0, "createdDate": null, "emailOfCreater": null, "resolution_type": null, "boundingbox": "1,1,1,1", "dsTitle": null, "finalParam": null, "type": null, "control": 0, "format": "visual", "desc": null, "status": "Connecting" }, { "srcName": "asthama", "access": "Public", "userListAssToDSAccess": null, "srcID": 14, "url": null, "creater": 0, "archive": 0, "unit": 0, "createdDate": null, "emailOfCreater": null, "resolution_type": null, "boundingbox": "1,2,1,1", "dsTitle": null, "finalParam": null, "type": null, "control": 0, "format": "visual", "desc": null, "status": "Connecting" }, { "srcName": "asthama", "access": "Public", "userListAssToDSAccess": null, "srcID": 16, "url": null, "creater": 0, "archive": 0, "unit": 0, "createdDate": null, "emailOfCreater": null, "resolution_type": null, "boundingbox": "1,1,1,1", "dsTitle": null, "finalParam": null, "type": null, "control": 0, "format": "visual", "desc": null, "status": "Connecting" }];
				var compiledTemplate = _.template(registerDatasourceTpl,{Data: q_temp});
			    //console.log("After Compile inside fetch : "+compiledTemplate);
			    self.$el.html(compiledTemplate);
			    
			  
  			
  			    $.each(q_temp, function(i, obj) {
  			    	queryOptions += '<option value="ds'+obj['qID']+'">DS'+obj['qID']+':'+obj['queryName']+'</option>'; 	
  		        });
			    
			}			   
	 });
		}
  

  });

  return registerQueryView;
  
});
