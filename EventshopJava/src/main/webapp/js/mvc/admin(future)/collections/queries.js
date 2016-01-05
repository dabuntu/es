define([
'jquery',
'underscore',
'backbone',
'mvc/admin/models/query'
], function($, _, Backbone, query) {
	var queries = Backbone.Collection.extend({
		
		model: query,
		url: "/eventshoplinux/webresources/adminservice/queries",
		parse: function(response){
				//console.log("Response From Server: "+JSON.stringify(response));
		    	return response;
		}
			
	});
	
	return queries;
});