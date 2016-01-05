define([
  'underscore',
  'backbone'  
], function(_, Backbone) {

    var datasource = Backbone.Model.extend({
	

    	defaults: function(){
            return {
            	"srcID":"",
            	"srcTheme":"",
            	"srcName":"",
            	"url":"",
            	"srcFormat": {},
            	"supportedWrapper":"",
            	"bagOfWords" : {},
            	"visualParam": 
            		{
            			"tranMatPath":"",
            			"colorMatPath":"",
            			"translationMatrix" : "",
            			"colorMatrix" : "",
            			"maskPath" : "",
            			"ignoreSinceNumber" : ""
            		},
            	"initParam" :
            		{
            		"start":"",
            		"end": "",
            		 "timeWindow" :"",
               	    "syncAtMilSec" : "",
               	    "timeType" : "",
               	    "latUnit" : "",
               	    "longUnit" : "",
               	    "swLat" : "",
               	  "swLong" : "",
               	  "neLat" : "",
               	  "neLong" : "",
            		"numOfColumns": "",
            		"numOfRows":"",
            		"context" : ""
            		},
            	"finalParam": {}, //not needed anymore for datasources therefore pass object as null
            	"wrapper" : 
            		{
            			"wrprId":"",
            			"wrprName":"",
            			"wrprType":"",
            			"wrprKeyValue":"",
            			"wrprBagOfWords":"",
            			"wrprVisualMaskMat":"",	
            			"wrprVisualIgnore":"",
            			"wrprArchStartTime":"",
            			"wrprArchEndTime":"",
            			"wrprArchGenRate":"",
            			"wrprVisualColorMat":""
            		},
            	"srcVarName": "",
            	"userId" : ""      
            	
            }
        },
	    
        urlRoot: "/eventshoplinux/webresources/adminservice/datasources",
         	
	initialize: function(){
		
    }


	});
    
    
  

  return datasource;


});
