define([
'jquery',
'underscore',
'backbone', 
'basic',
'mvc/user/models/datasource',
], function($, _, Backbone,basic,datasource) {
	var datasources = Backbone.Collection.extend({
		

		model: datasource,
		url: "webresources/datasourceservice/getUserDatasourceList",

		parse: function(response){
				//console.log("COLLLLResponse From Server: "+JSON.stringify(response));
		    	return response;
		}
			
	});
	
	return datasources;
});