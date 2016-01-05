define([
  'jquery',
  'underscore',
  'backbone',
  'text!templates/user/datasource/registerDatasource.html',
  'mvc/user/collections/datasources'  
 ], function($, _, Backbone,registerDatasourceTpl, dataSourceCollection){
	var self;
	var registerDatasourceView = Backbone.View.extend({
    el: $("#dsTableContainer"),

	initialize: function(opts){
		  self = this;
		  self.app_router = opts.router;
		  this.sessionModel = opts.sessionModel;		 
	  },
	  
    render: function(){
    	
    	this.fetchDatasource();

    },
    fetchDatasource: function() {
    	
    	var dsList = new dataSourceCollection();
    	dsOptions='';
    	customDsOptions = '<option>None</option>';
		dsList.fetch({
			
			data: $.param({"userId":this.sessionModel.id}),
		    success: function(){
			    
		    	var q_temp = JSON.parse(JSON.stringify(dsList));
				var compiledTemplate = _.template(registerDatasourceTpl,{Data: q_temp});
			    self.$el.html(compiledTemplate);

	            $.each(q_temp, function(i, obj) {

//	            	dsOptions += '<option value="ds'+obj['srcID']+'">DS'+obj['srcID']+':'+obj['srcName']+'</option>';
	            	   customDsOptions += '<option>ds'+obj['srcID']+'</option>'
	            });

	            $('#customQueryDS').html(customDsOptions);
	            /*$('#sSource').html(dsOptions);
	            $('#sSource').multiSelect('refresh');
	            $('#gSource').html(dsOptions);
	            $('#gSource').multiSelect('refresh');
	            $('#aSource').html(dsOptions);
	            $('#aSource').multiSelect('refresh');
	            $('#pSource').html(dsOptions);
	            $('#pSource').multiSelect('refresh');
	            $('#tSource').html(dsOptions);
	            $('#tSource').multiSelect('refresh');
	            $('#spaceCharSource').html(dsOptions);
	            $('#spaceCharSource').multiSelect('refresh');
	            $('#tempCharSource').html(dsOptions);
	            $('#tempCharSource').multiSelect('refresh');*/
			}			   
	 });
    }

  });

  return registerDatasourceView;
  
});
