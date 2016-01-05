define([
'underscore',
'backbone'
], function(_, Backbone) {
    var dataSourceModel = Backbone.Model.extend({
	
    	defaults: {
    		/*"srcName": "",
			"access": "",
			"userListAssToDSAccess": null,
			"srcID": "",
			"url": null,
			"creater": 0,
			"archive": 0,
			"unit": 0,
			"createdDate": null,
			"emailOfCreater": null,
			"resolution_type": null,
			"boundingbox": "",
			"dsTitle": null,
			"finalParam": null,
			"type": null,
			"control": 0,
			"format": "",
			"desc": null,
			"status": "" */   		            	 
    	},
    	urlRoot: "webresources/datasourceservice/datasources",
    	initialize: function(){
		
    	}
	});
     
  return dataSourceModel;

});
