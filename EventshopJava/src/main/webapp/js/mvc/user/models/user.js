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
            	"roleId":"2",
            	"checkAdmin":""	
            }
        },
	    urlRoot: "webresources/userservice/users",
         	
		initialize: function(){
			//var ds_query_list;		
	    }

	});
  

  return user;


});
