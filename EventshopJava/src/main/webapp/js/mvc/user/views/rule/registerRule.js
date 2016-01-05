define([
  'jquery',
  'underscore',
  'backbone',
  'text!templates/user/rule/registerRuleTpl.html',
  'mvc/user/collections/rules',
  'mvc/user/models/user',
 ], function($, _, Backbone,registerRuleTpl, ruleListCollection, userModel){
	var self;
	var registerRuleView = Backbone.View.extend({
    el: $("#ruleTableContainer"),

	initialize: function(opts){
		  self = this;
		  self.app_router = opts.router;
		  this.sessionModel = opts.sessionModel;
	  },
	  
    render: function(){
     
     	this.fetchRule();
		
		var userObj = {			
			"userName" : this.sessionModel.userName,
      	    "roleId" : this.sessionModel.roleId,
      	    "id" : this.sessionModel.id			
		};
    },
		fetchRule : function(){
			var ruleOptions = '';
			var ruleList = new ruleListCollection();
			ruleList.fetch({
				data: $.param({"userId":this.sessionModel.id}),
		    	success: function(){
			    	var r_temp = JSON.parse(JSON.stringify(ruleList));
					var compiledTemplate = _.template(registerRuleTpl,{Data: r_temp});
			    	self.$el.html(compiledTemplate);
			    
				}
	 		});
		}
  });

  return registerRuleView;
  
});
