define([
  'jquery',
  'underscore',
  'backbone',
  'text!templates/admin/query/queryTpl.html'
 ], function($, _, Backbone,queryTpl){
 	var queryView = Backbone.View.extend({
 		el: $("#tabs-3"),
 		initialize: function(){
 			this.render();
 		},
 	render: function(){
 		
      this.$el.html(queryTpl);
    }


 	});
 	return queryView;
 });