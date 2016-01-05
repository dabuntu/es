//new code
define([
'jquery',
'underscore',
'backbone',
'mvc/user/models/rule'
], function($, _, Backbone, ruleListModel) {

	var rules = Backbone.Collection.extend({

		model: ruleListModel,
		url: "webresources/uiRuleService/allRules",
		parse: function(response){
//				console.log("Response From Server: " + JSON.stringify(response));
		    	return response;
		}

	});

	return rules;
});
