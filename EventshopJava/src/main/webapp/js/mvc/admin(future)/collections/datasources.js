define([
'jquery',
'underscore',
'backbone', 'basic',
 'mvc/admin/models/datasource'
], function($, _, Backbone,basic,datasource) {
	var datasources = Backbone.Collection.extend({
		

		model: datasource,
		url: "/eventshoplinux/webresources/adminservice/datasources",

		parse: function(response){
				//console.log("COLLLLResponse From Server: "+JSON.stringify(response));
		    	return response;
		}
			
	});
	
	return datasources;
});