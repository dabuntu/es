define([
  'jquery',
  'underscore',
  'backbone',
  'text!templates/user/query/queryGraphTpl.html'
 ], function($, _, Backbone,queryGraphTpl){
	var self;
	var queryGraphView = Backbone.View.extend({
    el: $("#flowGraph"),

	initialize: function(opts){
		  self = this;
		  self.app_router = opts.router;	 
	  },

    render: function(){    	
     this.$el.html(queryGraphTpl);
    }

  });

  return queryGraphView;
  
});
