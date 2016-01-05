define([
  'jquery',
  'underscore',
  'backbone',
   'text!templates/user/query/filterOpTpl.html'
 ], function($, _, Backbone,filterOpTpl){
	var self;
	var queryCount = 1;
	var filterOpView = Backbone.View.extend({
    el: $("#tabs-1"),

	initialize: function(opts){
		  self = this;
		  self.app_router = opts.router;
		  this.generatedQueriesArr = opts.generatedQueriesArr;
		  this.queryCount = opts.queryCount;
		  this.dsOptions = opts.dsOptions;		 
		  $('#newSource').html(this.dsOptions);
	},
	events :{
		'click #filterQuery':'generateFilterQuery'
	},
	generateFilterQuery: function(e){
		 e.preventDefault();
		//var opName = $(e.target).attr('name');
    	//alert("In create query filter: "+opName);
    	    	
    	var filterQuery = new Object();
		filterQuery.qID = this.queryCount;
		filterQuery.patternType = 'filter';
		//filterQuery.dataSrcID = 1;
		filterQuery.dataSources = [];
		filterQuery.maskMethod = '';
		filterQuery.coords = [23.563987128451217, -127.61718750000001, 49.439556958940855, -61.083984375];
		//filterQuery.filePath = '/home/EventShop/Linux/es-install/Temp/q0_filterFile';
		filterQuery.filePath = '';
		filterQuery.valRange = [];
		filterQuery.timeRange = [];
		if ($('#valBound').is(":checked")) {
		console.log("valBound is checked")
        	filterQuery.boundMode = 'true';
		} else {
			filterQuery.boundMode = 'false';
		}
		if ($('#normVal').is(":checked")) {
		console.log("normval is checked")
			filterQuery.normMode = 'true';
		} else {
          	filterQuery.normMode = 'false';
        }

		filterQuery.normVals = [];
	    		
		$('#sSource option:selected').each(function(){
	       	var selectedValue = $(this).val();
	       	filterQuery.dataSources.push(selectedValue);			
		});
		
		var mask = $('#maskMethodVal option:selected').val();
		
		if(mask == 3) {
			filterQuery.maskMethod = 'map';
			
		}
		else if(mask == 4){
			filterQuery.maskMethod = 'textual';
			filterQuery.placeName = $('#latlong2').val();
			console.log("latlong2.val is " + $('#latlong2').val());
		} 
		else if(mask == 5){
			filterQuery.maskMethod = 'martix';
			filterQuery.filePath = $('#filePath').val();
		}
		else{
			filterQuery.maskMethod = '';
		}
		
		var valRangeMin = $('#valRangeMin').val();
		var valRangeMax = $('#valRangeMax').val();
		if(valRangeMin == '') valRangeMin = -99999;
		if(valRangeMax == '') valRangeMax = 99999;
		filterQuery.valRange.push(valRangeMin);
		filterQuery.valRange.push(valRangeMax);
		
		var normValsMin = $('#normValsMin').val();
		var normValsMax = $('#normValsMax').val();
		if(normValsMin == '') normValsMin = 0;
		if(normValsMax == '') normValsMax = 100;
		filterQuery.normVals.push(normValsMin);
		filterQuery.normVals.push(normValsMax);
		
		var temBoundsIn = $('#temporalBoundsInMethod option:selected').val();
		
		if(temBoundsIn == 1){    		
			var timeRangeStart = $('#timeRangeSecs').val();
    		var timeRangeEnd = '-999';
    		filterQuery.timeRange.push(timeRangeStart);
    		filterQuery.timeRange.push(timeRangeEnd);    			
		}
		
		if(temBoundsIn == 2){        		
			var timeRangeStart = $('#startDateFilter').val();
    		var timeRangeEnd = $('#endDateFilter').val();
    		filterQuery.timeRange.push(timeRangeStart);
    		filterQuery.timeRange.push(timeRangeEnd);    			
		}
		
		this.generatedQueriesArr.push(filterQuery);
		console.log("Array Of Objects: "+JSON.stringify(this.generatedQueriesArr));
		
		this.queryCount++;   	
    	
	},
    render: function(){
     this.$el.html(filterOpTpl);
    }

  });
	
  return filterOpView;
  
});
