define([
  'jquery',
  'underscore',
  'backbone',
  'text!templates/user/query/groupingOpTpl.html'
 ], function($, _, Backbone,groupingOpTpl){
	var self;
	var groupingOpView = Backbone.View.extend({
    el: $("#tabs-2"),

	initialize: function(opts){
		  self = this;
		  self.app_router = opts.router;
		  this.generatedQueriesArr = opts.generatedQueriesArr;
		  this.queryCount = opts.queryCount;		  
	  },
	events :{
		'click #groupingQuery':'generateGroupingQuery'
	},
	generateGroupingQuery: function(e){
		 e.preventDefault();
		//var opName = $(e.target).attr('name');
    	//alert("In create query grouping: "+opName);
		
		var groupingQuery = new Object();
		groupingQuery.qID = this.queryCount;
		groupingQuery.patternType = 'grouping';
		//groupingQuery.dataSrcID = 1;
		groupingQuery.dataSources = [];
		groupingQuery.method = '';
		groupingQuery.numGroup = '';
		groupingQuery.thresholds = [];
		groupingQuery.split = 'False';
		groupingQuery.doColoring = 'True';
		groupingQuery.colorCodes = [];
		
		$('#gSource option:selected').each(function(){
	       	var selectedValue = $(this).val();
	       	groupingQuery.dataSources.push(selectedValue);			
		});
		
		var method = $('#groupBy option:selected').text();
		if(method == 'K-Means'){
			groupingQuery.method = 'K-Means';
			groupingQuery.numGroup = $('#num_of_groups').val();
		}
		if(method == 'Threshold'){
			groupingQuery.method = 'Threshold';
			var thrVal = $('#threshol_vals').val();
			var arrThr = thrVal.split(',');
			for(var i=0; i<arrThr.length; i++){
				groupingQuery.thresholds.push(arrThr[i]);   				
			}    			
		}
		
		groupingQuery.split = $('#seperate_group_images option:selected').text();
		groupingQuery.doColoring = $('#group_in_colors option:selected').text();
		
		var grpCVal = $('#group_colors').val();
		var arrGrpC = grpCVal.split(',');
		for(var j=0; j<arrGrpC.length; j++){
			groupingQuery.colorCodes.push(arrGrpC[j]);   				
		}
		    		    		
		this.generatedQueriesArr.push(groupingQuery);
		
		console.log("groupingQuery : "+JSON.stringify(groupingQuery));
		
		this.queryCount++;
		alert("the query count"+this.queryCount);
		
	},
    render: function(){    	
     this.$el.html(groupingOpTpl);
    }

  });

  return groupingOpView;
  
});
