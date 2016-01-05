define([
  'underscore',
  'backbone'
], function(_, Backbone) {
    var login = Backbone.Model.extend({
	
    	defaults: function(){
            return{
              		userName : "",
              	    password:"",  
              	    checkAdmin: ""
            }
        },
	    
	urlRoot: "/eventshoplinux/webresources/loginservice/login",
         	
	initialize: function(){
		
    }

	});
  
  return login;

});
