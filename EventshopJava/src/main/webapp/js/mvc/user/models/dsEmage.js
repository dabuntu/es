define([
'underscore',
'backbone'
], function(_, Backbone) {
    var dsEmageModel = Backbone.Model.extend({
	
    	defaults: {            	 
    	},
    	urlRoot: "webresources/datasourceservice/dsemage",
    	initialize: function(){
		
    	}
	});
     
  return dsEmageModel;

});
