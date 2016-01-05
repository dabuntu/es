define([
'jquery',
'underscore',
'backbone',
'mvc/user/models/query'
], function($, _, Backbone, queryListModel) {
	var queryListCollection = Backbone.Collection.extend({
		
		model: queryListModel,
		url: "webresources/queryservice/queries",
		parse: function(response){
				//console.log("Response From QueriesServer: "+JSON.stringify(response));
		    	return response;
		}
			
	});
	
	return queryListCollection;
});