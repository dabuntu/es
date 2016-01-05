define([
  'jquery',
  'jqueryui',
  'underscore',
  'backbone',  
  'dalgorithms',
  'ddalgorithms',
  'dgraffle',
  'dgraph',
  'modal',
  'paginate',
  'sortable',  
  'queryGraph',
  'd3',
  'topojson',
  'basic',
  'bootMin',
  'datetimepicker',
  'text!templates/user/userHome.html',
  'mvc/user/models/session',
  'mvc/user/views/query/filterOp',
  'mvc/user/views/query/groupingOp',
  'mvc/user/views/query/aggregationOp',
  'mvc/user/views/query/spatialPatternOp',
  'mvc/user/views/query/temporalPatternOp',
  'mvc/user/views/query/spatialCharOp',
  'mvc/user/views/query/temporalCharOp',
  'mvc/user/views/datasource/registerDatasource',
  'mvc/user/views/datasource/addDatasource',
  'mvc/user/models/datasource',
  'mvc/user/collections/datasources',
  'mvc/user/views/query/registerQuery',
  'mvc/user/models/query',
  'mvc/user/views/query/queryGraph',
  'mvc/user/collections/queries',
  'mvc/user/models/user',
  'mvc/user/views/profile/editUser'
 
], function($,jqueryui, _, Backbone,dalgorithms,ddalgorithms,dgraffle,dgraph,modal,paginate,sortable,queryGraph,d3,topojson,basic,bootMin,datetimepicker,
  homePageTemplate,sessionModel,filterOpView,groupingOpView,aggregationOpView,spatialPatternOpView,temporalPatternOpView,spatialCharOpView,
  temporalCharOpView,registerDatasource,addDatasourceView,dataSourceModel,dataSourceCollection,registerQueryView,queryModel,queryGraphView,queryListCollection,userModel,editUserView){
  
  var homePageView = Backbone.View.extend({
  
    el: $("body"),
    //template: homePageTemplate,
      
     initialize: function(opts){
      
     this.app_router = opts.router;          
       
     this.queryModel = new queryModel();
     this.queryListCollection = new queryListCollection();
     
      
     this.datasourceModel = new dataSourceModel();
     this.sessionModel = new sessionModel();    
     var loc = this.sessionModel.checkSession();

     if(loc == "login") {        
         window.location.hash = "";
         this.app_router.navigate("*actions",true);
         window.location.reload();
      }
       
     
      this.generatedQueriesArr = [];
      this.queryCount = 1;
      this.dataSourceCollection = new dataSourceCollection();
      
      
    },
    events :{       
      "click #addDataSourceBtn":"validateAddDataSource",
      "click #q_play":"runQuery",
      "click #ds_play":"runDs",
      "click #ds_stop":"stopDs",
      "click #q_stop":"stopQuery",
      "click #imgaddDS":"showViewDSModal", 
      "click #datasourceTable td span":"showViewDSModal", 
      "click #exeQuery":"queryExecute" ,
      "click #resetQueryValues": "resetQuery",
      "click #showEditUser": "showEditUser"      
     },

    render: function(){     
    
     $("#page").css("display","none");     
     this.$el.html(homePageTemplate); 
     
     /*this.datasourceUpdate();*/
    /* this.queryListUpdate();*/
     this.subRender();
     this.queryGraphView = new queryGraphView({router:this, generatedQueriesArr:this.generatedQueriesArr, queryCount:this.queryCount});
     
     this.queryGraphView.setElement('#flowGraph').render();
     
     this.registerDatasource = new registerDatasource({router:this, sessionModel:this.sessionModel});
     this.registerDatasource.setElement('#dsTableContainer').render();
          
     this.registerQueryView = new registerQueryView({router:this, sessionModel:this.sessionModel});
     this.registerQueryView.setElement('#queryTableContainer').render();
     
     return this;
    },
    
/*    datasourceUpdate : function() {
      dsOptions = '';
       this.dataSourceCollection.fetch({
         data: $.param({"userId":this.sessionModel.id}),
         success: function(dataSourceCollection){        
            var q_temp = JSON.parse(JSON.stringify(dataSourceCollection));
            dsOptions +='<option>Select Datasource</option>';
            $.each(q_temp, function(i, obj) {

            dsOptions += '<option value="ds'+obj['srcID']+'">DS'+obj['srcID']+':'+obj['srcName']+'</option>';   
              });
            
            $('#sSource').html(dsOptions);
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
            $('#tempCharSource').multiSelect('refresh');
            
            
            
         }
         
       });
      
    },*/
    
 /*   queryListUpdate : function() {
    	var queryOptions = '';
    	 this.queryListCollection.fetch({
  		   data: $.param({"userId":this.sessionModel.id}),
  		   success: function(queryListCollection){
  			 var self = this;
  			    var q_temp = JSON.parse(JSON.stringify(queryListCollection));
  			    $.each(q_temp, function(i, obj) {
  			    	queryOptions += '<option value="ds'+obj['qID']+'">DS'+obj['qID']+':'+obj['queryName']+'</option>'; 	
  		        });
  			    }
  		   
  	   });
    	
    },*/
    subRender: function() {
      
       this.filterOpView = new filterOpView({router:this, generatedQueriesArr:this.generatedQueriesArr, queryCount:this.queryCount,dsOptions:this.dsOptions});
         this.filterOpView.setElement('#tabs-1').render();
                  
         this.groupingOpView = new groupingOpView({router:this, generatedQueriesArr:this.generatedQueriesArr, queryCount:this.queryCount});
         this.groupingOpView.setElement('#tabs-2').render();
                  
         this.aggregationOpView = new aggregationOpView({router:this, generatedQueriesArr:this.generatedQueriesArr, queryCount:this.queryCount});
         this.aggregationOpView.setElement('#tabs-3').render();
                  
         this.spatialPatternOpView = new spatialPatternOpView({router:this, generatedQueriesArr:this.generatedQueriesArr, queryCount:this.queryCount});
         this.spatialPatternOpView.setElement('#tabs-4').render();
         
         this.temporalPatternOpView = new temporalPatternOpView({router:this, generatedQueriesArr:this.generatedQueriesArr, queryCount:this.queryCount});
         this.temporalPatternOpView.setElement('#tabs-5').render();
          
         this.spatialCharOpView = new spatialCharOpView({router:this, generatedQueriesArr:this.generatedQueriesArr, queryCount:this.queryCount});
         this.spatialCharOpView.setElement('#tabs-6').render();
          
         this.temporalCharOpView = new temporalCharOpView({router:this, generatedQueriesArr:this.generatedQueriesArr, queryCount:this.queryCount});
         this.temporalCharOpView.setElement('#tabs-7').render();
         
    },
  
    queryExecute: function(){
    	
      var timeWindow = $('#dsFTimeP').val();
      var dsFSyncP = $('#dsFSyncP').val();
      var latitudeUnit = $('#dsFUnitLatP').val();
      var longitudeUnit = $('#dsFUnitLongP').val();
      
      var dsFSouthLatP = $('#dsFSouthLatP').val();
      var dsFSouthLongP = $('#dsFSouthLongP').val();	
      var dsFNorthLatP = $('#dsFNorthLatP').val();
      var dsFNorthLongP = $('#dsFNorthLongP').val();
      if(timeWindow=="") timeWindow=300;
      if(dsFSyncP=="") dsFSyncP=0;
      if(latitudeUnit=="") latitudeUnit=2;
      if(longitudeUnit=="") longitudeUnit=2;
      if(dsFSouthLatP=="") dsFSouthLatP=24;
      if(dsFSouthLongP=="") dsFSouthLongP=-125;
      if(dsFNorthLatP=="") dsFNorthLatP=50;
      if(dsFNorthLongP=="") dsFNorthLongP=-66;
    
      
var finaltabparms1={};  


var mask = $('#maskMethodVal option:selected').val();
		if(mask==3){
			
			  finaltabparms1.mapcords = $('#map_data').val();
		}else if(mask==4){
			
			finaltabparms1.coords = $('#latlong2').val();
			finaltabparms1.placeName = $('#placeName').val();
		}else if(mask==5){
			
			finaltabparms1.filePath = $('#file_data_st').html();
		}
        finaltabparms1.dataSrcID=  $("#sSource").val();
        finaltabparms1.SelectedDsaSource=  $("#aSource").val();
        finaltabparms1.SelectedDsgSource=  $("#gSource").val();
        finaltabparms1.SelectedDsspaceCharSource=  $("#spaceCharSource").val();
        finaltabparms1.SelectedDspSource=  $("#pSource").val();
        finaltabparms1.SelectedDstempCharSource=  $("#tempCharSource").val();
        finaltabparms1.SelectedDstSource=  $("#tSource").val();
        finaltabparms1.valRange=[];
        finaltabparms1.valRange[0]= $('#valRangeMin').val();
        finaltabparms1.valRange[1]=$('#valRangeMax').val();
        finaltabparms1.normVals=[];
	    finaltabparms1.normVals[0] = $('#normValsMin').val();
	    finaltabparms1.normVals[1] = $('#normValsMax').val();
        finaltabparms1.FtemporalBoundsInMethod = $('#temporalBoundsInMethod').val();
        finaltabparms1.timeRange = $('#timeRangeSecs').val();
        finaltabparms1.Fdatepicker = $('#datepicker').val();
        finaltabparms1.Fdatepicker1 = $('#datepicker1').val();
        finaltabparms1.maskMethod = mask;
        finaltabparms1.startDateFilter=$('#startDateFilter').val();
        finaltabparms1.endDateFilter=$('#endDateFilter').val();
        finaltabparms1.method = $('#groupBy').val();
        finaltabparms1.numGroup = $('#num_of_groups').val();
        finaltabparms1.split = $('#seperate_group_images').val();
        finaltabparms1.doColoring = $('#group_in_colors').val();
        finaltabparms1.colorCodes=[];
        finaltabparms1.colorCodes[0] = $('#group_colors').val();
      
      
      finaltabparms1.thresholds=[];
      finaltabparms1.thresholds[0] = $('#threshol_vals').val();
      
      finaltabparms1.values=[];
      
      finaltabparms1.values[0] = $('#aggNormMin').val();
      finaltabparms1.values[1] = $('#aggNormMax').val();
      finaltabparms1.aggOperator = $('#aggBy').val();
      finaltabparms1.sizeNorm = $('#sp_size_norm').val();
      finaltabparms1.SPsp_value_norm = $('#sp_value_norm').val();
      finaltabparms1.patternSrc = $('#sp_pattern_type').val();
      finaltabparms1.TPtp_norm_pattern = $('#tp_norm_pattern').val();
        finaltabparms1.TPtp_norm_value = $('#tp_norm_value').val();
        finaltabparms1.TPtp_time_window = $('#tp_time_window').val();
        finaltabparms1.TPtp_pattern_type = $('#tp_pattern_type').val();
        finaltabparms1.spCharoperator = $('#sc_spatial_char_operator').val();
        finaltabparms1.timeWindow = $('#date_time_window').val();
        finaltabparms1.tmplCharOperator = $('#tc_char_operator').val();
      
    finaltabparms1.numRows = $('#sp_num_rows').val();
    finaltabparms1.numCols = $('#sp_num_cols').val();
    finaltabparms1.gaussianCenter = $('#gs_center').val();
    finaltabparms1.gaussianDeviation = $('#gs_sd').val();
    finaltabparms1.gaussianAmplitude = $('#gs_amp').val();
    finaltabparms1.linearStartPosition = $('#ln_start_position').val();
    finaltabparms1.linearStartValue = $('#ln_start_value').val();
    finaltabparms1.linearDirection = $('#ln_direct_gradient').val();
    finaltabparms1.patternSamplingRate = $('#tp_samp_rate').val();
    finaltabparms1.patternDuration = $('#tp_samp_dur').val();
    finaltabparms1.patternSamplingRate = $('#tp_samp_rate').val();
  //  finaltabparms1.templinearParam.slope = $('#ln_slope').val();
    //finaltabparms1.templinearParam.yIntercept = $('#ln_intercept').val();
    //finaltabparms1.expParam.base = $('#exp_base_value').val();
    //finaltabparms1.expParam.scale = $('#exp_scale_factor').val();
    //finaltabparms1.periodicParam.frequency = $('#pr_frq').val();
    //finaltabparms1.periodicParam.amplitude = $('#pr_amplitude').val();
    //finaltabparms1.periodicParam. phaseDelay = $('#pr_phase_delay').val();
    //finaltabparms1.filePath = $('#filterfilef').val();
    
    finaltabparms1.filePathSpatial = $('#file_data1_pattern').html();
    finaltabparms1.QueryName1 = $('#QueryName1').val();
    
    finaltabparms1.filePathTemporal = $('#file_data1_temporal').html();
   
     //finaltabparms1.coordrs=[];
  
   
 
   
   if($("#QueryName1").val()==""){
	   alert("Please enter the Query name");
	   return false;
   }
   
    //console.log($('#file_data1_pattern').html());
     console.log(finaltabparms1);
    
      var queryStatus = "S";      
      
      var qryCreatorId = this.sessionModel.id;
      //var boundingBox = dsFSouthLongP+','+dsFNorthLatP+','+dsFSouthLatP+','+dsFNorthLongP;
      // change the order of boundingBox -> swLat, swLong, neLat, neLong
      var boundingBox = dsFSouthLatP+','+dsFSouthLongP+','+dsFNorthLatP+','+dsFNorthLongP;
      
      var finalObjArr = [];
      $.each(this.generatedQueriesArr, function(i, obj) { 
    	  
          obj['qID'] = i+1;     
          obj['queryName'] =   finaltabparms1.QueryName1;       
          obj['timeWindow'] = timeWindow;
          obj['latitudeUnit'] = latitudeUnit;
          obj['longitudeUnit'] = longitudeUnit;
          obj['queryStatus'] = queryStatus;
          obj['qryCreatorId'] = qryCreatorId;           
          obj['boundingBox'] = boundingBox;
         // obj['timeRange'] = [$("#startDateFilter").val(),$("#endDateFilter").val()]; 
          obj['timeRange'] = [1,1];// hardcoding for now as the field doesnt exist in UI -- sanjukta
          
          finalObjArr.push(obj);
            
        });
      
          
      var finalStr = finalObjArr.join();
      console.log("our data"+finalStr);
      
      this.queryModel.set({query:finalObjArr});
      this.queryModel.save(null,
            {   
    	    //url: "/eventshoplinux/webresources/queryservice/createQuery",
    	    type:"POST",
    	    
            success: function (queryModel,responseText) {                  
              alert("Success Text: "+responseText); // It will be invoked if you pass only json from server
              $("#toPopup2").fadeOut("normal");  
              $("#backgroundPopup2").fadeOut("normal");
            },
            error: function(queryModel,responseText) {
              //alert("Could not save query"+responseText);
              $("#toPopup2").fadeOut("normal");  
              $("#backgroundPopup2").fadeOut("normal");
            }
                
            });
         //this.queryListUpdate();
		 this.registerQueryView.fetchquery();
      
    },
    resetQuery: function(){
    		
             $('#sSource').html(dsOptions);
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
             $('#tempCharSource').multiSelect('refresh');
    	
        
    
    	 //$("#temporalBoundsInMethod")[0].selectedIndex=0;
    	
   
      // alert("hello");
          /*}
          });*/
        
        },
     //console.log(finaltabparms1);
 
    runDs: function(e){
      e.preventDefault();
      var dsID = [];      
      $("input[name='cboxDS[]']:checked").each(function(i){       
           dsID[i] = $(this).val();
        });
      $.ajax({
          type: "POST",
          url: "register",
          data: { "type": "startds", "dsID": dsID.join(',') }
      }).done(function(output){
            console.log("Success 1");
            for (var i=0;i<output.length;i++) {
              console.log(output[i].queryId+ "this one" + output[i].output);
            }
            
            $("#mapstatus").hide();
            $('#map-inner-block').html("");             
            $("#imageStatus").attr('src',"imgs/run.png");
            
            var m=0;
            for (var p=0;p<output.length;p++) {
              var newDiv = "";
               if(m == 0){
                        var active = ' active';
                 }else{                  
                        var active = '';                    
                 }
                  
                  m++;               
                  divName = "usCon"+ output[p].resId;
          
                  newDiv += '<div class="item '+active+'" id='+divName+' style="width: 960px;"><a href="javascript:zoomOut();" class="zoom'+output[p].resId+'"> Zoom Back</a></div>'; 
                  divName = "#" +divName;
                  $(".carousel-control").removeAttr("style");
                  $('#map-inner-block').append(newDiv);
                  mapDataLoader(output[p].output,divName);                
          
         }
                        
        
             
            $("#map-carousel").carousel("pause").removeData();
               $("#map-carousel").carousel({
                 interval: 5000
               });
            
           }).fail( function(jqXHR, textStatus, errorThrown,output){
             console.log("sucessfaile "+textStatus);
             console.error(errorThrown);
             /*for(var i=0;i<data.length;i++){
               console.log("data array is as follows "+data[i]);
             }*/
             
             //console.log(output[0].id);
                     
                       
           });          
          
              
   
  
    },
    stopDs: function(e){
        e.preventDefault();
        var dsID = [];        
        $("input[name='cboxDS[]']:checked").each(function(i){       
             dsID[i] = $(this).val();
          });
        $.ajax({
            type: "POST",
            url: "register",
            data: { "type": "stopds", "dsID": dsID.join(',') },
        }).done(function(output){
            console.log("the stop was successful");  
            data ="";
            $('#map-inner-block').html("");
                          
            
            
    }).fail( function(jqXHR, textStatus, errorThrown,output){
        console.log("sucessfaile "+textStatus);
        console.error(errorThrown);
        /*for(var i=0;i<data.length;i++){
          console.log("data array is as follows "+data[i]);
        }*/
        
        //console.log(output[0].id);
                
                  
      });
          e.preventDefault();        
                
     
    
      },
     runQuery: function(e){
      e.preventDefault();      
      var qID = [];
        
      $("input[name='cboxQR[]']:checked").each(function(i){       
        qID[i] = $(this).val();
    });
      //qidstart
      $.ajax({
          type: "POST",
          url: "register",
          
          data: { "type": "startq", "qIDList": qID.join(',') }
      }).done(function(output){
          	console.log("Success");
          	/*for (var i=0;i<output.length;i++) {
          		console.log(output[i].queryId+ "this one" + output[i].output);
          	}*/
          	
            $("#mapstatus").hide();
          	$('#map-inner-block').html("");
          	$("#imageStatus").attr('src',"imgs/run.png");
            var m=0;
            for (var p=0;p<output.length;p++) {
          		var newDiv = "";
          		 if(m == 0){
          	            var active = ' active';
          	     }else{                  
          	            var active = '';                    
          	     }
          	      
          	      m++;          	   
          	      divName = "usCon"+ output[p].resId;          	      
          	      newDiv += '<div class="item '+active+'" id='+divName+' style="width: 960px;"><a href="javascript:zoomOut();" class="zoom'+output[p].resId+'"> Zoom Back</a></div>'; 
          	      divName = "#" +divName;
          	      $(".carousel-control").removeAttr("style");
          	      $('#map-inner-block').append(newDiv);
          	      //console.log(" query id "+output[p].resId);
          	      mapDataLoader(output[p].output,divName);     	      
          
         }
                   	 
          	$("#map-carousel").carousel("pause").removeData();
               $("#map-carousel").carousel({
              	 interval: 5000
               });
          	
           }).fail( function(jqXHR, textStatus, errorThrown,output){
          	 console.log("successData "+jqXHR);
        	   console.error(errorThrown);
          	 /*for(var i=0;i<data.length;i++){
          		 console.log("data array is as follows "+data[i]);
          	 }*/
          	 
          	 //console.log(output[0].id);
                     
                       
           });
          
          
              
  
  
    },    
     stopQuery: function(e){
      var qID = [];
        
      $("input[name='cboxQR[]']:checked").each(function(i){       
        qID[i] = $(this).val();
    });
        console.log("we are trying to stop the query");
      //qidstop
      $.ajax({
        type: "POST",
        url: "register",
        data: { "type": "stopq", "qIDList": qID.join(',') 
        }}).done(function(output){
                
        	$('#map-inner-block').html("");
           data = ""; // variable for the servlet use
           console.log("empty");
        }).fail( function(jqXHR, textStatus, errorThrown,output){
         	 console.log("Could not stop query "+jqXHR);
       	   console.error(errorThrown);
         	 /*for(var i=0;i<data.length;i++){
         		 console.log("data array is as follows "+data[i]);
         	 }*/
         	 
         	 //console.log(output[0].id);
             
                      
          });
      e.preventDefault();
    },
    
    showEditUser:function(e) {
    	var userProfile = new userModel();
    	// edit profile code
    	// coming soon
    	
    },    
    
    showMap: function(){
      
      var getDsJson = new getMapDsModel({id:21});
      getDsJson.toJSON();
      getDsJson.fetch({success:function(getDsJson){
        
    }});
      
    },
    showAddDSModal:function(){
      
    },
    showViewDSModal:function(ev){
 
      if ($(ev.target).attr('id') == 'imgaddDS') {
        
        var viewDSList = new dataSourceModel({id:DSID});  
        var viewDSAttributes = {};
        $('input').val('');
        $('#addDataSourceBtn').val("Add");
        
      } else {
        
    	  
      var DSID = $(ev.target).attr('id');
      $('#addDataSourceBtn').val("Save");     
      
      $('#dataSourceId').val(DSID);
      var viewDSList = new dataSourceModel({id:DSID});            
      viewDSList.toJSON();      
      viewDSList.fetch({success:function(){    	  
        var viewDSAttributes = viewDSList.attributes;
          
        $('#dsTheme').val(viewDSAttributes.srcTheme);
        $('#dsName').val(viewDSAttributes.srcName);
        $('#dsPath').val(viewDSAttributes.url);
        $('#dsTime').val(viewDSAttributes.initParam.timeWindow/1000);
        $('#dsSync').val(viewDSAttributes.initParam.syncAtMilSec);
  
        $('#dsUnitLat').val(viewDSAttributes.initParam.latUnit);
        $('#dsUnitLong').val(viewDSAttributes.initParam.longUnit);
        $('#dsSouthLat').val(viewDSAttributes.initParam.swLat);
        $('#dsSouthLong').val(viewDSAttributes.initParam.swLong);
        $('#dsNorthLat').val(viewDSAttributes.initParam.neLat);
        $('#dsNorthLong').val(viewDSAttributes.initParam.neLong);
        $('#dsFTime').val(viewDSAttributes.finalParam.timeWindow);
        $('#dsFSync').val(viewDSAttributes.finalParam.syncAtMilSec);
        $('#dsFUnitLat').val(viewDSAttributes.finalParam.latUnit);
        $('#dsFUnitLong').val(viewDSAttributes.finalParam.longUnit);
        $('#dsFSouthLat').val(viewDSAttributes.finalParam.swLat);
        $('#dsFSouthLong').val(viewDSAttributes.finalParam.swLong);
        $('#dsFNorthLat').val(viewDSAttributes.finalParam.neLat);
        $('#dsFNorthLong').val(viewDSAttributes.finalParam.neLong);
        $("#dsDataFormat").val(viewDSAttributes.srcFormat);
        
        $('#wrapperId').val(viewDSAttributes.wrapper.wrprId);
       // $('#dfTextArea').val(viewDSAttributes.wrapper.wrprBagOfWords);
        $('#dataFormat').val(viewDSAttributes.srcFormat);
        if(viewDSAttributes.srcFormat=="stream")	{
        	$('#visual').hide();
        	$('#csv').hide();        
	        document.getElementById('dfTransPath').addEventListener('change', readSingleFile, false);          
	        $('#dfSupport').val(viewDSAttributes.supportedWrapper);
	        $('textarea#dfTextArea').val(viewDSAttributes.wrapper.wrprBagOfWords);         
	        $('#data').show();
        }
        else if(viewDSAttributes.srcFormat=="visual")
        {
        	$('#data').hide();
        	$('#csv').hide();
	        var visual=viewDSAttributes.visualParam;
	        var visualTransTable="<table>";
	        if (visual.translationMatrix != null) {
	        	for(var i=0;i< visual.translationMatrix.row;i++)  {
	        		visualTransTable +='<tr>';
	        		for(var k=0;k<visual.translationMatrix.column;k++)  {
	        			visualTransTable +="<td>"+visual.translationMatrix.matrix[i][k]+"</td>";
	        		}
	        		visualTransTable +='</tr>';
	        	}
             } else {
                    visualTransTable +='<tr><td></td></tr>';
             }

	        visualTransTable +="</table>";
          
	        $("#tmpData").html(visualTransTable);
	        $('#visual').show();
        } else if (viewDSAttributes.srcFormat=="csv") {
        	 $('#visual').hide();
        	 $('#data').hide();
        	 $('#csv').show();
        	 //alert('wrapper support ' + viewDSAttributes.supportedWrapper + ', key-value ' + viewDSAttributes.wrapper.wrprKeyValue);
        	 $('#Data_foramt_type').val(viewDSAttributes.supportedWrapper);
        	 $('textarea#key-value_pair').val(viewDSAttributes.wrapper.wrprKeyValue);
        	 //var dataFormatArr = viewDSAttributes.wrapper.wrprName.split(" ");
        	 //$('#Data_foramt_type').val(dataFormatArr[1]);
        	 //var wrprKeyValueArr = viewDSAttributes.wrapper.wrprKeyValue.split("} ");
        	//$('#key-value_pair').val(wrprKeyValueArr[1]);
        }
          
      }});               
      
      } // if not imgaddDS
      //Start Code to display popup         
      $("div.loader").show();     
      //var popupStatus = 0; // set value
      //if(popupStatus == 0) { // if value is 0, show popup
        $("div.loader").fadeOut('normal'); // fadeout loading
        $("#toPopup").fadeIn(0500); // fadein popup div
        $("#backgroundPopup").css("opacity", "0.7"); // css opacity, supports IE7, IE8
        $("#backgroundPopup").fadeIn(0001); 
       
      //End code to display popup
      
      
      
      $("div#backgroundPopup, div.close").click(function() {
        
          $("#toPopup").fadeOut("normal");  
          $("#backgroundPopup").fadeOut("normal");  
          
      });
        
    },    
    validateAddDataSource:function(){
    var flag=0;
      var self = this;
      if($("#dsUnitLat").val()!=""){
      flag=1;
    }
    
 if($('#dsTheme').val()=="")
      {
        alert("Please Enter The Theme1");
        return false;
      }
      else if($('#dsName').val()=="")
      {
        alert("Please Enter The Name");
        return false;
      }
      else if($('#dsPath').val()=="")
      {
        
        alert("Please Enter The URL");
        return false;
      }
      else if(!(/^[A-Za-z0-9.\/%&=\?_:;-]+$/.test($('#dsPath').val()))){
        
        alert("Please Enter Valid URL");
        return false;
      }
      if(flag==0){

      if((!(/^[0-9]+$/.test($('#dsTime').val()))) && ($('#dsTime').val())!="") 
      {
    	  
    	 alert("Please Enter The Time Window in Sec");
        return false;
    	 
      }
      
      else if(!(/^[0-9]+$/.test($('#dsSync').val())) && ($('#dsSync').val()!=""))
      {
        alert("Please Enter The Synchronize at nth Sec");
        return false;
      }
    
     if(!(/^[-+]?[0-9]+\.[0-9]+$/.test($('#dsUnitLat').val()))  && ($('#dsUnitLat').val()!="")) // can be -ve and decimal --sanjukta
      {
        alert("Please Enter The Unit of Latitude");
        return false;
      }
      else if( !(/^[-+]?[0-9]+\.[0-9]+$/.test($('#dsUnitLong').val()))  && ($('#dsUnitLong').val()!=""))   // can be decimal --sanjukta
      {
        alert("Please Enter The Unit of Longitude");//can be -ve and decimal --sanjukta
        return false;
      }
      else if( !(/^[-+]?[0-9]+\.[0-9]+$/.test($('#dsSouthLat').val()))  && ($('#dsSouthLat').val()!=""))
      {
        alert("Please Enter The Southwest of Latitude");
        return false;
      }
      else if( !(/^[-+]?[0-9]+\.[0-9]+$/.test($('#dsSouthLong').val()))  && ($('#dsSouthLong').val()!=""))  //can be -ve and decimal --sanjukta
      {
        alert("Please Enter The Southwest of Longitude");
        return false;
      }
      else if(!(/^[-+]?[0-9]+\.[0-9]+$/.test($('#dsNorthLat').val()))  && ($('#dsNorthLat').val()!=""))  //can be -ve and decimal --sanjukta
      {
        alert("Please Enter The Northeast of Latitude");
        return false;
      }
      else if( !(/^[-+]?[0-9]+\.[0-9]+$/.test($('#dsNorthLong').val()))  && ($('#dsNorthLong').val()!=""))  //can be -ve and decimal --sanjukta
      {
        alert("Please Enter The Northeast of Longitude");
        return false;
      }
     }
      //got to referesh the list of datasources in the parent once name is changed -- sanjukta
      
      var dataObj={};     
      
      dataObj.srcID= (($('#addDataSourceBtn').val() == "Add")?"":$('#dataSourceId').val()); // datasource id is empty for new
      dataObj.userId= this.sessionModel.id;
      dataObj.srcTheme=$('#dsTheme').val();
      dataObj.srcName=$('#dsName').val();
      dataObj.url=$('#dsPath').val();
      dataObj.srcFormat=$('#dataFormat').val();
      dataObj.srcVarName = "";  // dont know wt is this for?
      dataObj.bagofwords= [];
      console.log(JSON.stringify(dataObj));
      
      
      var bagOfWords = "";
      
      if(dataObj.srcFormat=="stream")
      {
        dataObj.supportedwrapper=$('#dfSupport').val();
       // var tempArray = $("#dataFormatData").find('#dfTextArea').val().split(",");
        //alert($("#dataFormatData").find('#dfTextArea').val());
        //dataObj.bagofwords=tempArray;
        bagOfWords = $("#dataFormatData").find('#dfTextArea').val();
        dataObj.csvkeyvalue='{"datasource_type":"point", "spatial_wrapper":"count"}';
      }
      
      else if(dataObj.srcFormat=="visual")
      {
        
        dataObj.supportedwrapper="visual";
      
      }
      else if(dataObj.srcFormat=="csv")
      {
        
        dataObj.supportedwrapper=$('#Data_foramt_type').val();
        var addtoKeyValue = ''; //'{"datasource_type":"point", "spatial_wrapper":"sum", "lat_index" :0, "lon_index":1, "val_index":2}';
        if ($('#Data_foramt_type').val() == "csvArray") {
        	addtoKeyValue = '{"datasource_type":"grid", "spatial_wrapper":"sum"}';
        } else if ($('#Data_foramt_type').val() == "csvField"){
        	if($('#key-value_pair').val() != ""){
        		//var index = $('#key-value_pair').val().split(",");
        		//addtoKeyValue = '{"datasource_type":"point", "spatial_wrapper":"sum", "lat_index" :' + index[0] + ', "lon_index":' + index[1] + ', "val_index":'+ index[2] +'}';
        		addtoKeyValue = $('#key-value_pair').val();
        	} else{
        		addtoKeyValue = '{"datasource_type":"point", "spatial_wrapper":"sum", "lat_index" :0, "lon_index":1, "val_index":2}';
        	}
        }
        dataObj.csvkeyvalue=addtoKeyValue;
      
      }
      console.log(JSON.stringify(dataObj));
      var vdfIgnoreColor = ($('#vdfIgnoreColor').val() == undefined?0:$('#vdfIgnoreColor').val());
      
      
      dataObj.wrapper = {   
          wrprId :  (($('#addDataSourceBtn').val() == "Add")?"":$('#wrapperId').val()),
          wrprName : dataObj.supportedwrapper , // supported or data format value if supported does not exist -- sanjukta
          //wrprType : '',
          wrprType : '',//$('#Data_foramt_type').val(),
          wrprKeyValue : dataObj.csvkeyvalue, //key value  for csv alone     -- sanjukta    
          wrprBagOfWords : bagOfWords,// $('#dfTextArea').val(), // not dfSupport
          //wrprVisualMaskMat : '',
          wrprVisualMaskMat : $('#hidden_id_mask').html(),
          wrprVisualIgnore : vdfIgnoreColor,
          wrprArchStartTime : '',           
          wrprArchEndTime : '',           
          wrprArchGenRate : '',
          //wrprVisualColorMat : '', 
          wrprVisualColorMat : $('#hidden_id_color').html(),
          //wrprVisualTransMat : '',
          wrprVisualTransMat : $('#hidden_id_trans').html(),
          //wrprCSVFile : $('#hidden_id_File').html()
          //wrprCSVFileURL : $('#CSV_URL').val(), // we dont need this as we already have a url field on top -- sanjukta
          // url field validation to ignore www -- sanjukta
          
      };
      
      //alert(dataObj.wrapper.wrprId+" before sub");
     //alert("wrapper: "+JSON.stringify(dataObj.wrapper));
      
      
      
      
      
      
      dataObj.Visualparam = {
          
          
          "tranMatPath" : $('#dfTransPath').val(),//$('#vdfIgnoreColor').val(), //document.getElementById('dfTransPath').addEventListener('change', readSingleFile, false), //$('#byte_content').val(),   document.getElementById('dfTransPath').addEventListener('change', readSingleFile, false),
            
          
          "colorMatPath" :  $('#dfColorPath').val(),//$('#vdfIgnoreColor').val(), //document.getElementById('dfColorPath').addEventListener('change', readSingleFile, false), //$('#dfColorPath').val(),  //document.getElementById('dfColorPath').addEventListener('change', readSingleFile, false),
          
          
          "maskPath" : $('#dfMaskPath').val(), // $('#vdfIgnoreColor').val(), //document.getElementById('dfMaskPath').addEventListener('change', readSingleFile, false), // $('#dfMaskPath').val(),     //document.getElementById('dfMaskPath').addEventListener('change', readSingleFile, false),
          
         
      };  
      
      dataObj.initParam={
        "timeWindow":$('#dsTime').val()*1000, // as per Siripen its msec -- sanjukta
        "syncAtMilSec":$('#dsSync').val(),
        "latUnit":$('#dsUnitLat').val(),
        "longUnit":$('#dsUnitLong').val(),
        "swLat":$('#dsSouthLat').val(),
        "swLong":$('#dsSouthLong').val(),
        "neLat":$('#dsNorthLat').val(),
        "neLong":$('#dsNorthLong').val()
        };
      if(dataObj.initParam.timeWindow=="")dataObj.initParam.timeWindow=300000;
      if(dataObj.initParam.syncAtMilSec=="")dataObj.initParam.syncAtMilSec=0;
      if(dataObj.initParam.latUnit=="")dataObj.initParam.latUnit=2;
      if(dataObj.initParam.longUnit=="")dataObj.initParam.longUnit=2;
      if(dataObj.initParam.swLat=="")dataObj.initParam.swLat=24;
      if(dataObj.initParam.swLong=="")dataObj.initParam.swLong=-125;
      if(dataObj.initParam.neLat=="")dataObj.initParam.neLat=50;
      if(dataObj.initParam.neLong=="")dataObj.initParam.neLong=-66;
      dataObj.finalParam={
        "timeWindow":$('#dsFTime').val()*1000,
        "syncAtMilSec":$('#dsFSync').val(),
        "latUnit":$('#dsFUnitLat').val(),
        "longUnit":$('#dsFUnitLong').val(),
        "swLat":$('#dsFSouthLat').val(),
        "swLong":$('#dsFSouthLong').val(),
        "neLat":$('#dsFNorthLat').val(),
        "neLong":$('#dsFNorthLong').val()
        };
      if(dataObj.finalParam.timeWindow=="")dataObj.initParam.timeWindow=300;
      if(dataObj.finalParam.syncAtMilSec=="")dataObj.initParam.syncAtMilSec=0;
      if(dataObj.finalParam.latUnit=="")dataObj.initParam.latUnit=2;
      if(dataObj.finalParam.longUnit=="")dataObj.initParam.longUnit=2;
      if(dataObj.finalParam.swLat=="")dataObj.initParam.swLat=24;
      if(dataObj.finalParam.swLong=="")dataObj.initParam.swLong=-125;
      if(dataObj.finalParam.neLat=="")dataObj.initParam.neLat=50;
      if(dataObj.finalParam.neLong=="")dataObj.initParam.neLong=-66;
      
      this.datasourceModel.set(dataObj);
      
      this.datasourceModel.save(null,
                {
                  type:"PUT",
                  success: function (datasourceModel,responseText) {
                	  alert("Datasource saved successfully");                     
                      $("#toPopup").fadeOut("normal");  
                      $("#backgroundPopup").fadeOut("normal");  
                      return true;
                    },
                    error: function(datasourceModel,responseText) {
                    	// var result = JSON.parse(responseText);
                         alert("Data could not be saved due to error at the server side");        // need to pass json or accept string otherwise goes to error -- sanjukta
                      $("#toPopup").fadeOut("normal");  
		              $("#backgroundPopup").fadeOut("normal");  
		    
		              self.registerDatasource.fetchDatasource();     
              
            }

                });     
    }


  });
  return homePageView;
});
