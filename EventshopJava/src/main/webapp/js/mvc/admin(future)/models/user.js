define([
  'underscore',
  'backbone'
], function(_, Backbone) {
    var user = Backbone.Model.extend({

	
    	defaults: function(){
            return{
            	//selectedUserIds:{},
            	//"id":"",
            	"userName":"",
            	"password":"",
            	"emailId":"",
            	"gender":"",
            	"authentication":"",
            	"roleId":"",
            	"roleType":"",
            	"userLastAccessed":"",
            	"status":"",
            	"checkAdmin":""	
            }
        },
	    
	urlRoot: "/eventshoplinux/webresources/adminservice/users",
         	
	initialize: function(){
		//var ds_query_list;		
    }

	});
  

  return user;


});
