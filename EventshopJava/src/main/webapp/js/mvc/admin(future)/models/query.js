define([
  'underscore',
  'backbone'  
], function(_, Backbone) {
    var query = Backbone.Model.extend({
	

    	defaults: function(){
            return{
            	"qID": "",
            	"queryName" : "",
            	"status" : "",
            	"boundingbox" : "",
            	"dsmasterId" : "",
            	"control" : "",
            	"timeWindow" : "",
            	"latitudeUnit": "",
            	"longitudeUnit" : ""	 
            }
        },
	    
        urlRoot: "/eventshoplinux/webresources/adminservice/queries",

         	
	initialize: function(){
		
    }

	});
    
    
  
  return query;

});
