define([
  'jquery',
  'underscore',
  'backbone',
  'text!templates/user/query/aggregationOpTpl.html'
 ], function($, _, Backbone,aggregationOpTpl){
	var self;
	var aggregationOpView = Backbone.View.extend({
    el: $("#tabs-3"),

	initialize: function(opts){
		  self = this;
		  self.app_router = opts.router;
		  this.generatedQueriesArr = opts.generatedQueriesArr;
		  this.queryCount = opts.queryCount;		  
		  this.dsOptions = opts.dsOptions;
	},
	events :{
		'click #aggrQuery':'generateAggrQuery'
	},
	generateAggrQuery: function(e){
		 e.preventDefault();
		var aggrQuery = new Object();
		aggrQuery.qID = this.queryCount;
		aggrQuery.patternType = 'aggregation';
		aggrQuery.dataSources = [];
		aggrQuery.values = [];
		aggrQuery.scalarFirst = 'false';
		aggrQuery.aggOperator = 'Agg'+$('#aggBy option:selected').text();
		aggrQuery.valueNorm = 'false';
		aggrQuery.normedRange = [];
		
		$('#aSource option:selected').each(function(){
	       	var selectedValue = $(this).val();
	       	aggrQuery.dataSources.push(selectedValue);			
		});    		
		var chkVal = $('#normBounds:checked').val();
		
		if(chkVal == 'on'){
			aggrQuery.valueNorm = 'true';
		}
		
		var aggNormMin = $('#aggNormMin').val();
		var aggNormMax = $('#aggNormMax').val();
		if(aggNormMin == '') aggNormMin = 0;
		if(aggNormMax == '') aggNormMax = 100;
		aggrQuery.normedRange.push(aggNormMin);
		aggrQuery.normedRange.push(aggNormMax);
		    		
		this.generatedQueriesArr.push(aggrQuery);
		
		console.log("aggregationQuery : "+JSON.stringify(aggrQuery));
		
		this.queryCount++;
	},
    render: function(){
     this.$el.html(aggregationOpTpl);
    }

  });

  return aggregationOpView;
  
});
