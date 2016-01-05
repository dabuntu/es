define([
  'jquery',
  'underscore',
  'backbone',
  'text!templates/admin/datasource/datasourceTpl.html',
  'mvc/admin/models/datasource',
  'mvc/admin/models/datasourceUser',
  'mvc/admin/collections/datasources'
 ], function($, _, Backbone,datasourceTpl,Datasource,datasourceUsers,Datasources){
 	var datasourceView = Backbone.View.extend({
 		el: $("#tabs-2"),
 		initialize: function(){
 			self = this;
 			this.datasource= new Datasource();
            this.datasourceList=new Datasources()
 			this.render();

 		},
 		events : {
 			"click #ds_view":"showDSProfiles",
 			"click #select_all":"selectAllUsers",
			"click #update_users_ds":"updateAssociatedDSUsers",
 			
 		},
 	render: function(){
 		
      	this.fetchDataSource();
    }, 
	fetchDataSource: function() {
    	self=this;
    	datasourceList = new Datasources();
    	datasourceList.toJSON();
    	datasourceList.fetch({
    		success: function(){

    			 var dataSourceArr = JSON.parse(JSON.stringify(datasourceList));
			 	
			 	
			 	var compiledTemplate = _.template(datasourceTpl,{dataSourceArr: dataSourceArr});
			    self.$el.html(compiledTemplate);
			    self.dataSourceTable(dataSourceArr);
			 
			}		
    	});
    },
	dataSourceTable:function(dataSourceArr){
    	var objs_arr = [];
	    window.dataSourceStore = dataSourceArr; 
		var ds_objs_arr = [];
		$.each(dataSourceArr, function(i, obj) {
	        var ds_control = obj['control'];
        	if(obj['access'] == 'Public')
        		obj['ds_access'] = '<a href="javascript:void(0);" class="change_access" id="change_access_link" name="'+obj['srcID']+'">'+obj['access']+'</a>';
        	else
        		obj['ds_access'] = obj['access'];
        	
        	obj['ds_checked'] = '<input type="checkbox" value="'+obj['srcID']+'" name="ds_checked[]">';
        	
        	if(ds_control != 1){        		
        		obj['ds_control'] = '<a href="javascript:void(0);" class="btn btn-small btn-inverse h16" ><img src="imgs/play_32.png" name="'+obj['srcID']+'" id="ds_play_'+obj['srcID']+'" class="img_class"  /></a>';
        	}else{        		
        		obj['ds_control'] = '<a href="javascript:void(0);" class="btn btn-small btn-inverse h16" ><img src="imgs/stop_32.png" name="'+obj['srcID']+'" id="ds_stop_'+obj['srcID']+'" class="img_class_stop"  /></a>';
        	}
        	ds_objs_arr.push(obj);
        });
            
	      $('#ds_table').dataTable({  
   		 "bProcessing": true,
   		 "aaSorting": [[ 1, "asc" ]],
   		 "aaData": ds_objs_arr,
   		 "aoColumns": [
   		    { "sTitle": '<input type="checkbox" name="ds_select_all" id="ds_select_all">',   "mDataProp": "ds_checked","bSortable":false },
   		    { "sTitle": "Id",  "mDataProp": "srcID" },
   		    { "sTitle": "Name", "mDataProp": "srcName" },
   		    { "sTitle": "Desc",  "mDataProp": "desc" },
   		    { "sTitle": "Format",    "mDataProp": "format" },
   		    { "sTitle": "Status",  "mDataProp": "status" },
   		    { "sTitle": "Resolution",  "mDataProp": "boundingbox" },
   		    { "sTitle": "Access",  "mDataProp": "ds_access" },
   		    { "sTitle": "Control",  "mDataProp": "ds_control" }
   		  ]

    	});
    },
showDSProfiles: function(){
    	
	//Start script for DataSource List			
			var num_checked = $( "#ds_table td input:checked" ).length;					
			if( num_checked == 0 ){
				alert("Please select at least one checkbox to view DS profile");
				return false;
			}
		     $('#view_dsaccess_block').html('');
			//var countChecked = function() {
			  var n = $( "#ds_table td input:checked" ).length;			  
			  var ds_html_block_arr = new Array();								  
			  var checkValues = $('#ds_table td input:checked').map(function() {return this.value;}).get().join(',');
			  //alert(checkValues);
			  var ds_html_block_arr1 = new Array();
			  
			 
			  var chk_array = checkValues.split(',');
			  
			    var count=chk_array.length;
			  
	            populateDsQueries(dataSourceStore,count); 
			   	
			    //Start custom function
									function in_array (needle, haystack, argStrict) {
										var key = '',
										strict = !! argStrict;

										if (strict) {
											for (key in haystack) {
												if (haystack[key] === needle) {
													return true;
												}
											}
										}else {
											for (key in haystack) {
												if (haystack[key] == needle) {
													return true;
												}
											}
										}

									return false;
									}
									//End custom function
									
	            		function populateDsQueries(dataSourceStore,count) {
	            			
	            			  var ds_list_dummy_var = JSON.parse(JSON.stringify(dataSourceStore));
	          			      var count=0;
		          			  $.each(ds_list_dummy_var, function(r, obj){
							  
		          				if(in_array(obj['srcID'],chk_array)){
								
		          				var srcID = obj['srcID'];
		          				var srcName = obj['srcName'];
		          				var url = obj['url'];
		          				var creater = obj['emailOfCreater'];
		          				var archive = obj['archive'];
		          				var unit = obj['unit'];
		          				var createdDate = obj['createdDate'];
		          				var desc = (desc == null) ? "" : obj['desc'];
		          				var format = (format == '') ? "" : obj['format'];
		          				var type = (type == null || type == '') ? "" : obj['type'];
								if(creater == '') creater = '';
		          				if(archive == '0') archive = '';
		          				if(unit == '0') unit = '';
		          				if(count == 0){
		        					var active = 'active';
		        				}else{				
		        					var active = '';					
		        				}

		          				/* ds multi select */
								var dsUsersList = new datasourceUsers();
								dsUsersList.set("id",srcID);

								
								
								dsUsersList.save(null, {
               
									type:"GET", 
									
									error: function(datasourceUsers,responseText,xhr) {
									if(responseText.responseText = 'Success'){
									var mul_sel_arr = new Array();
									dsUsersList.toJSON();
									var dummy_var = JSON.parse(JSON.stringify(dsUsersList));

    			    			    console.log(dummy_var);	
									/*for (e in dummy_var) {

										if(e != 'id'){
											mul_sel_arr.push(dummy_var[e]);
										}
									}
									
									//Start code for multi user select
									var temp = JSON.parse(JSON.stringify(usersListStore));
									var mul_select_options = new Array();
			  	
			  	                    if(active =='active')
									$('#selected_ds_id').val(srcID);
									$('#view_dsaccess_block').append('<div id="select_block'+srcID+'" class="item '+active+'" ></div>');
									$('#select_block'+srcID).html('');
									$('#select_block'+srcID+'').append('<select id="m-selected'+srcID+'" name="m-selected'+srcID+'" multiple  class="multi"></select>');
												  	
									$.each(temp, function(i, objs) {			  			
									var id = objs['id'];
									var name = objs['userName'];
									if(in_array(id,mul_sel_arr)){			  		 		
										//mul_select_options += '<option value="'+id+'" selected="selected" >'+name+'</option>';
										$('#m-selected'+srcID).append('<option value="'+id+'" selected="selected" >'+name+'</option>');
									}else{			  		 		
									   //mul_select_options += '<option value="'+id+'">'+name+'</option>';
										$('#m-selected'+srcID).append('<option value="'+id+'">'+name+'</option>');
									}
		     	
								});
			  				  	 $('#m-selected'+srcID).multiSelect('refresh');
									
									$("#mul_users").css("display","block");
			  			  	
									//End code for multi user select
			  	                   

									*/

								} 

								}
								});

								/* ds multi select */
		          				ds_html_block_arr1 += '<div class="item '+active+'"><input type="hidden" value="'+srcID+'" name="current_ds_id"><div class="row"><div class="span12 adjWidth"><table align="center" cellpadding="11" ><tr><td><table align="center" cellpadding="5" cellspacing="5"><tr><td>Data Source Id</td><td>:</td><td>'+srcID+'</td></tr><tr><td>Data Souce Name</td><td>:</td><td>'+srcName+'</td></tr><tr><td>Description</td><td>:</td><td>'+desc+'</td></tr><tr><td>URL</td><td>:</td><td>'+url+'</td></tr><tr><td>Format</td><td>:</td><td>'+format+'</td></tr><tr><td>Type</td><td>:</td><td>'+type+'</td></tr><tr><td>Creator</td><td>:</td><td>'+creater+'</td></tr><tr><td>Archive</td><td>:</td><td>'+archive+'</td></tr><tr><td>Unit</td><td>:</td><td>'+unit+'</td></tr><tr><td>Created Date</td><td>:</td><td>'+createdDate+'</td></tr></table></td><td><table align="center" cellpadding="5" cellspacing="5"><tr><td>View Map :</td></tr><tr><td><textarea rows="2" cols="10"></textarea></td></tr><tr><td>Resolution :</td></tr><tr><td><textarea rows="2" cols="10"></textarea></td></tr></table></td></tr></table></div></div></div>';
							count++;	
							}	
		          			  });
		          			 //console.log("Data Source Name:"+ds_access);
		          			
		          			$('#view_dslist_block').html(ds_html_block_arr1);
	            			
	            		
	            	}
	            	
	       		
			  
			  
			 
			//};
			//countChecked();
			 
			//$( "#ds_table input[type=checkbox]" ).on( "click", countChecked );
						
			$('#view_ds_block').css('display','block');
			$('#view_ds_access').css('display','block');
			$("#ds-carousel").carousel("pause").removeData();
			$("#ds-carousel").carousel();			
		    
		//End script for DataSource List		
    	
    }

 	});
 	return datasourceView;
 });
