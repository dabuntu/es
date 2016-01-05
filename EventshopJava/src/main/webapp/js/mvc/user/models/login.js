define([
	'underscore',
	'backbone'
], function(_, Backbone) {
	var loginModel = Backbone.Model.extend({
	
    	defaults: function(){
            return{
              		userName : "",
              		password:""  
                };
        },
	    urlRoot: "webresources/loginservice/login",
         	
		initialize: function(){
			
	    }
	});
	return loginModel;
});
