define([
  'jquery',
  'underscore',
  'backbone',
  'text!templates/user/datasource/addDatasource.html',
  'mvc/user/models/datasource', 
 ], function($, _, Backbone,addDatasourceTpl,addDataSourceModel){
	var self;
	var addDatasourceView = Backbone.View.extend({
    el: $("#basic-modal-content"),

	initialize: function(opts){
		  self = this;
		  self.app_router = opts.router;
		  this.sessionModel = opts.sessionModel;
	  },
	  
	  render:
	        function()
	        {
	            $(this.el).html(addDatasourceTpl);
	            return this;
	        },

  });

  return addDatasourceView;
  
});
