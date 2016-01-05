define([
  'jquery',
  'underscore',
  'backbone',
  'text!templates/user/query/spatialCharOpTpl.html'
 ], function($, _, Backbone,spatialCharOpTpl){
	var self;
	var spatialCharOpView = Backbone.View.extend({
    el: $("#tabs-6"),

	initialize: function(opts){
		  self = this;
		  self.app_router = opts.router;
		  this.generatedQueriesArr = opts.generatedQueriesArr;
		  this.queryCount = opts.queryCount;
	},
	events :{
		'click #spcharQuery':'generateSpcharQuery'
	},
	generateSpcharQuery: function(e){
		 e.preventDefault();
		var spcharQuery = new Object();
		spcharQuery.qID = this.queryCount;
		spcharQuery.patternType = 'spchar';
		spcharQuery.dataSources = [];
		spcharQuery.spCharoperator = $('#sc_spatial_char_operator option:selected').text();
		    		
		$('#spaceCharSource option:selected').each(function(){
	       	var selectedValue = $(this).val();
	       	spcharQuery.dataSources.push(selectedValue);			
		});    		
		    		
		    		    		
		this.generatedQueriesArr.push(spcharQuery);
		
		console.log("spcharQuery : "+JSON.stringify(spcharQuery));
		this.queryCount++;
	},
    render: function(){
     this.$el.html(spatialCharOpTpl);
    }

  });

  return spatialCharOpView;
  
});
