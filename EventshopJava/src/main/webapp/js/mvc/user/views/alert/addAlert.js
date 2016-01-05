define([
  'jquery',
  'underscore',
  'backbone',
  'text!templates/user/alert/addAlert.html',
  'mvc/user/models/alert', 
 ], function($, _, Backbone,addAlertTpl,addAlertModel){
	var self;
	var addAlertView = Backbone.View.extend({
    el: $("#basic-modal-alert-content"),

	initialize: function(opts){
		  self = this;
		  self.app_router = opts.router;
		  this.sessionModel = opts.sessionModel;
	  },
	  
	  render:
	        function()
	        {
	            $(this.el).html(addAlertTpl);
	            return this;
	        },

  });

  return addAlertView;
  
});
