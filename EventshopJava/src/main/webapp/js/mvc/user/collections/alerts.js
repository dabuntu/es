//new code
define([
'jquery',
'underscore',
'backbone',
'mvc/user/models/alert'
], function($, _, Backbone, alertListModel) {

	var alerts = Backbone.Collection.extend({

		model: alertListModel,
		url: "webresources/alert/getallalerts",
		parse: function(response){
//				console.log("Response From Server: " + JSON.stringify(response));
		    	return response;
		}

	});

	return alerts;
});
