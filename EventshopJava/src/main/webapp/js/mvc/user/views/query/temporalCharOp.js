define([
  'jquery',
  'underscore',
  'backbone',
  'text!templates/user/query/temporalCharOpTpl.html'
 ], function($, _, Backbone,temporalCharOpTpl){
	var self;
	var temporalCharOpView = Backbone.View.extend({
    el: $("#tabs-7"),

	initialize: function(opts){
		  self = this;
		  self.app_router = opts.router;
		  this.generatedQueriesArr = opts.generatedQueriesArr;
		  this.queryCount = opts.queryCount;
	},
	events :{
		'click #tpcharQuery':'generateTpcharQuery'
	},
	generateTpcharQuery: function(e){
		 e.preventDefault();
		var tpcharQuery = new Object();
		tpcharQuery.qID = this.queryCount;
		tpcharQuery.patternType = 'tpchar';
		tpcharQuery.dataSources = [];
		tpcharQuery.tcTimeWindow = $('#date_time_window').val();
		tpcharQuery.tmplCharOperator = $('#tc_char_operator option:selected').text();
		    		
		$('#tempCharSource option:selected').each(function(){
	       	var selectedValue = $(this).val();
	       	tpcharQuery.dataSources.push(selectedValue);			
		}); 	
		    		    		
		this.generatedQueriesArr.push(tpcharQuery);
		
		console.log("tpcharQuery : "+JSON.stringify(tpcharQuery));
		this.queryCount++;
		
	},
    render: function(){
     this.$el.html(temporalCharOpTpl);
    }

  });

  return temporalCharOpView;
  
});
