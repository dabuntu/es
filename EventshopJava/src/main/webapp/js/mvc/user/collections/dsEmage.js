define([
'jquery',
'underscore',
'backbone', 
'basic',
'mvc/user/models/dsEmage',
], function($, _, Backbone,basic,dsEmage) {
	var datasources = Backbone.Collection.extend({
		

		model: dsEmage,
		url: "webresources/datasourceservice/getdsemage",

		parse: function(response){
				console.log("getdsemage From Server: "+JSON.stringify(response));
		    	return response;
		}
			
	});
	
	return datasources;
});