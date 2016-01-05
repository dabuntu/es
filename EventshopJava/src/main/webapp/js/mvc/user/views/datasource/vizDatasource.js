define([
  'jquery',
  'underscore',
  'backbone',
  'text!templates/user/datasource/vizDatasource.html',
  'mvc/user/collections/dsEmage'  
 ], function($, _, Backbone,vizDatasourceTpl, dsEmageCollection){
	var self;
	var vizDatasourceView = Backbone.View.extend({
    el: $("#dsMapContainer"),

	initialize: function(opts){
		  self = this;
		  self.app_router = opts.router;
		  this.sessionModel = opts.sessionModel;		 
	  },
	  
    render: function(){
    	
    	this.fetchDSEmage();

    },
    fetchDSEmage: function() {
    	
    	var dsEmage = new dsEmageCollection();
    	dsEmage.fetch({
    		data: $.param({"dsId": self.sessionModel.selectedDS}),
    		success: function(dsEmage, responseText){
    			console.log("viz getdsEmage: " + JSON.stringify(dsEmage));
    			var temp = JSON.parse(JSON.stringify(dsEmage));
    			console.log("temp is " + temp);
    			if(temp[0].status == "Success"){
    				var compiledTemplate = _.template(vizDatasourceTpl,{dsID: self.sessionModel.selectedDS});
    			    //console.log("After Compile inside fetch : "+compiledTemplate);
    			    self.$el.html(compiledTemplate);
    			} else{
    				alert("ds emage is not ready yet! status: " + temp[0].status);
    			}
    		}
    	});
    	
    	
	    
	    /*
    	var dsList = new dsEmageCollection();
    	dsOptions='';
		dsList.fetch({
			
			data: $.param({"userId":this.sessionModel.id}),
		    success: function(){
		    	console.log("viz DS: "+JSON.stringify(dsList));
		    	
		    	var q_temp = JSON.parse(JSON.stringify(dsList));
			 	//var variables = [{  "srcName": "asthama", "access": "Public", "userListAssToDSAccess": null, "srcID": 13, "url": null, "creater": 0, "archive": 0, "unit": 0, "createdDate": null, "emailOfCreater": null, "resolution_type": null, "boundingbox": "1,1,1,1", "dsTitle": null, "finalParam": null, "type": null, "control": 0, "format": "visual", "desc": null, "status": "Connecting" }, { "srcName": "asthama", "access": "Public", "userListAssToDSAccess": null, "srcID": 14, "url": null, "creater": 0, "archive": 0, "unit": 0, "createdDate": null, "emailOfCreater": null, "resolution_type": null, "boundingbox": "1,2,1,1", "dsTitle": null, "finalParam": null, "type": null, "control": 0, "format": "visual", "desc": null, "status": "Connecting" }, { "srcName": "asthama", "access": "Public", "userListAssToDSAccess": null, "srcID": 16, "url": null, "creater": 0, "archive": 0, "unit": 0, "createdDate": null, "emailOfCreater": null, "resolution_type": null, "boundingbox": "1,1,1,1", "dsTitle": null, "finalParam": null, "type": null, "control": 0, "format": "visual", "desc": null, "status": "Connecting" }];
				var compiledTemplate = _.template(vizDatasourceTpl,{Data: q_temp});
			    //console.log("After Compile inside fetch : "+compiledTemplate);
			    self.$el.html(compiledTemplate);
			     
			}
					   
	 	});*/	
    }

  });

  return vizDatasourceView;
  
});
