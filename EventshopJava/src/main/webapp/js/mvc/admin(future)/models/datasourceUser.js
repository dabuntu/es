define([
  'underscore',
  'backbone'  
], function(_, Backbone) {
    var getDsUsersModel = Backbone.Model.extend({
	
    	defaults: function(){
		
            return{
			
           			
            }
        },	    
          urlRoot: "/eventshoplinux/webresources/adminservice/datasources/users",
         	
	initialize: function(){
		
    }

	});
    
    
  
  return getDsUsersModel;

});
