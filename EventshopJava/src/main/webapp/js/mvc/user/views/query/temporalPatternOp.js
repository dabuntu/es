define([
  'jquery',
  'underscore',
  'backbone',
  'text!templates/user/query/temporalPatternOpTpl.html'
 ], function($, _, Backbone,temporalPatternOpTpl){
	var self;
	var temporalPatternOpView = Backbone.View.extend({
    el: $("#tabs-5"),

	initialize: function(opts){
		  self = this;
		  self.app_router = opts.router;
		  this.generatedQueriesArr = opts.generatedQueriesArr;
		  this.queryCount = opts.queryCount;
	},
	events: {
		 'click #tpmatchingQuery':'generateTpmatchingQuery'		
	},
	generateTpmatchingQuery: function(e){
		 e.preventDefault();
		var tpmatchingQuery = new Object();
		tpmatchingQuery.qID = this.queryCount;
		tpmatchingQuery.patternType = 'tpmatching';
		tpmatchingQuery.dataSources = [];
		tpmatchingQuery.durationNorm = $('#tp_norm_pattern option:selected').text();
		tpmatchingQuery.valueNorm = $('#tp_norm_value option:selected').text();
		tpmatchingQuery.dataDuration = $('#tp_time_window').val();		
		tpmatchingQuery.patternSrc = '';
		tpmatchingQuery.filePath = '';
		
		tpmatchingQuery.patternSamplingRate = '';
		tpmatchingQuery.patternDuration = '';
		tpmatchingQuery.parmType = 'Input';
		
		    		
		$('#tSource option:selected').each(function(){
	       	var selectedValue = $(this).val();
	       	tpmatchingQuery.dataSources.push(selectedValue);			
		});
		
		var ptSrcType = $('#tp_pattern_type option:selected').val();
		
		if(ptSrcType == '12'){
			tpmatchingQuery.patternSrc = 'file';
			tpmatchingQuery.filePath = $('#temporalPatternInputFile').val();
		}
		
		if(ptSrcType == '13'){
			tpmatchingQuery.patternSrc = 'create';
			tpmatchingQuery.patternSamplingRate = $('#tp_samp_rate').val();
			tpmatchingQuery.patternDuration = $('#tp_samp_dur').val();
			tpmatchingQuery.parmType = $('#prtn_ln_exp_per option:selected').text();
			if(tpmatchingQuery.parmType == 'Linear'){
				tpmatchingQuery.linearParam = {};
				tpmatchingQuery.linearParam.slope = $('#ln_slope').val();
				tpmatchingQuery.linearParam.yIntercept = $('#ln_intercept').val();				
			}
			if(tpmatchingQuery.parmType == 'Exponential'){
				tpmatchingQuery.expParam = {};
				tpmatchingQuery.expParam.base = $('#exp_base_value').val();
				tpmatchingQuery.expParam.scale = $('#exp_scale_factor').val();
				
			}
			if(tpmatchingQuery.parmType == 'Periodic'){
				tpmatchingQuery.periodicParam = {};
				
				tpmatchingQuery.periodicParam.frequency = $('#pr_frq').val();
				tpmatchingQuery.periodicParam.amplitude = $('#pr_amplitude').val();
				tpmatchingQuery.periodicParam.phaseDelay = $('#pr_phase_delay').val();
				
			}
			
		}
	
		this.generatedQueriesArr.push(tpmatchingQuery);		
		console.log("tpmatchingQuery : "+JSON.stringify(tpmatchingQuery));
		this.queryCount++;
		
		
	},
    render: function(){
     this.$el.html(temporalPatternOpTpl);
    }

  });

  return temporalPatternOpView;
  
});
