define([
  'jquery',
  'underscore',
  'backbone',  
  'jqueryui',
  'modal',
  'bootMin',
  'text!templates/admin/index.html',  
  'mvc/admin/models/session',
  'mvc/admin/models/user',
  'mvc/admin/models/datasource',
  'mvc/admin/models/query', 
  'mvc/admin/collections/users',
  'mvc/admin/collections/datasources',
  'mvc/admin/collections/queries',
  'mvc/admin/views/user/user',
  'mvc/admin/views/datasource/datasource',
  'mvc/admin/views/query/query',
  'dataTables',  
  'carousel',
  'multiselectable' 
  //getDsUsersModel,adminHomeModel,adminDataSourceListModel,adminQueryListModel,
], function($, _, Backbone,jqueryui,modal,bootMin,adminViewTemplate,Session,User,Datasource,Query,Users,Datasources,Queries,userView,datasourceView,queryView,dataTables,carousel,multiselectable){
  


  var adminHomeView = Backbone.View.extend({
    
    el: $("body"),
    template: adminViewTemplate,
	initialize: function(opts){
		   this.app_router = opts.router;
		   		   
		   this.session = new Session();		
		   var loc = this.session.checkSession();

		   if(loc == "login") {			   
			   window.location.hash = "";
		   		this.app_router.navigate("admin",true);
		   		window.location.reload();
		   }
		  
		   this.user = new User();
		   this.datasource = new Datasource();
		   this.query = new Query();
		  
		   this.usersList = new Users();
		   this.dataSourceList= new Datasources();
		   this.queryList = new Queries();
		  
       

		  this.callInitialScripts();
		   
		   

		  // this.deleteDsModel = new deleteDsModel();
		  // this.runDsModel = new runDsModel();
		  // this.runQueryModel = new runQueryModel();
		  // this.updateUserStatusModel = new updateUserStatusModel();	   
		   
	  },
	  events :{
		  //"click #view":"showUserProfiles",
		  "click #q_view":"showQueryLists",
		//  "click #ds_view":"showDSProfiles",
		  //"click #select_all":"selectAllUsers",
		  "click #ds_select_all":"selectAllDS",
		  "click #q_select_all":"selectAllQuery",
		  //"click #activate":"activateSelectedUsers",
		  //"click #deactivate":"deActivateSelectedUsers",
		  "click #ds_delete":"deleteDataSources",
		  "click #q_delete":"deleteQuery",
		  //"click #update":"updateUserDetails",
		  "click .change_access":"changeDsAccess",
		//  "click #update_users_ds":"updateAssociatedDSUsers",
		  "click #ds_table td img":"runDs",
		  "click #query_table td img":"runQuery",
		  "click #ds-carousel_next":"getId",
		  "click #ds-carousel_prev":"getId",
		  "click #ds_play_all":"runDsAll",
		  "click #ds_stop_all":"stopDsAll",
		  "click #q_play_all":"runQAll",
		  "click #q_stop_all":"stopQAll",
		  //"click #query_table td img":"runDs",
	  },

    render: function(){     
    	this.$el.html(this.template); 

          this.UserView = new userView({});
          this.UserView.setElement('#tabs-1').render();

          this.DatasourceView = new datasourceView({});
          this.DatasourceView.setElement('#tabs-2').render();

          
          this.QueryView = new queryView({});
          this.QueryView.setElement('#tabs-3').render();


    	return this;
    },
	runDsAll: function(e){
	    
		var num_checked = $( "#ds_table td input:checked" ).length;					
		if( num_checked == 0 ){
			alert("Please select at least one checkbox to run DS profile");
			return false;
		}
		var paramsArray = [];
		var n = $( "#ds_table td input:checked" ).length;
		var ds_html_block_arr = new Array();
		$('#ds_table td input:checked').map(function() {paramsArray.push(this.value);return this.value;}).get().join(',');
		var ds_html_block_arr1 = new Array();
		paramsArray.push('R');
      	//console.log("Array Values :"+paramsArray);
    	this.dataSourceList.set("selectedDSRunIds",paramsArray);
    	this.dataSourceList.save(null,	{
				    type:"POST",
            		error: function(datasource,responseText) {
            			if(responseText.responseText == 'Success'){
						    for(var i=0;i<paramsArray.length;i++){
								$("#ds_play_"+paramsArray[i]).attr('src','imgs/stop_32.png');
	            				$("#ds_play_"+paramsArray[i]).attr('class','img_class_stop'); 
								$("#ds_play_"+paramsArray[i]).attr('id','ds_stop_'+paramsArray[i]);							
                            }					
            			}else{
            				alert("Error. "+responseText.responseText);
            			}            			                                             
            		}
       			});
    	
	
	},
	stopDsAll: function(e){
	
    	
    
		var num_checked = $( "#ds_table td input:checked" ).length;					
		if( num_checked == 0 ){
			alert("Please select at least one checkbox to stop DS profile");
			return false;
		}
		var paramsArray = [];
		var n = $( "#ds_table td input:checked" ).length;
		var ds_html_block_arr = new Array();
		$('#ds_table td input:checked').map(function() {paramsArray.push(this.value);return this.value;}).get().join(',');
		var ds_html_block_arr1 = new Array();
		paramsArray.push('S');
    	
    	//console.log("Array Values :"+paramsArray);
    	this.dataSourceList.set("selectedDSRunIds",paramsArray);
    	this.dataSourceList.save(null,
       			{
				    type:"POST",
            		error: function(datasource,responseText) {
            			if(responseText.responseText == 'Success'){
						    for(var i=0;i<paramsArray.length;i++){
							
            				$("#ds_stop_"+paramsArray[i]).attr('src','imgs/play_32.png');
            				$("#ds_stop_"+paramsArray[i]).attr('class','img_class'); 
							$("#ds_stop_"+paramsArray[i]).attr('id','ds_play_'+paramsArray[i]);
                            }					
            			}else{
            				alert("Error. "+responseText.responseText);
            			}            			                                             
            		}
       			});
    	
	
	},
	runQAll: function(e){
	
    	
    
		var num_checked = $( "#query_table td input:checked" ).length;					
		if( num_checked == 0 ){
			alert("Please select at least one checkbox to view query profile");
			return false;
		}
		var paramsArray = [];
		var n = $( "#query_table td input:checked" ).length;
		$('#query_table td input:checked').map(function() {paramsArray.push(this.value);return this.value;}).get().join(',');
		paramsArray.push('RUN');
    	
    	//console.log("Array Values :"+paramsArray);
    	this.query.set("selectedQryId",paramsArray);
    	this.query.save(null,
       			{
            		error: function(runQueryModel,responseText) {
            			if(responseText.responseText == 'Success'){
						    for(var i=0;i<paramsArray.length;i++){
							
            				$("#q_play_"+paramsArray[i]).attr('src','imgs/stop_32.png');
            				$("#q_play_"+paramsArray[i]).attr('class','img_class_stop'); 
							$("#q_play_"+paramsArray[i]).attr('id','stop_'+paramsArray[i]);							
                            }					
            			}else{
            				alert("Error. "+responseText.responseText);
            			}            			                                             
            		}
       			});
    	
	
	},
	stopQAll: function(e){
	
    	
    
		var num_checked = $( "#query_table td input:checked" ).length;					
		if( num_checked == 0 ){
			alert("Please select at least one checkbox to view query profile");
			return false;
		}
		var paramsArray = [];
		var n = $( "#query_table td input:checked" ).length;
		$('#query_table td input:checked').map(function() {paramsArray.push(this.value);return this.value;}).get().join(',');
		paramsArray.push('S');
    	
    	//console.log("Array Values :"+paramsArray);
    	this.runQueryModel.set("selectedQryId",paramsArray);
    	this.runQueryModel.save(null,
       			{
            		error: function(runQueryModel,responseText) {
            			if(responseText.responseText == 'Success'){
						    for(var i=0;i<paramsArray.length;i++){
							
            				$("#q_stop_"+paramsArray[i]).attr('src','imgs/play_32.png');
            				$("#q_stop_"+paramsArray[i]).attr('class','img_class'); 
							$("#q_stop_"+paramsArray[i]).attr('id','play_'+paramsArray[i]);
                            }					
            			}else{
            				alert("Error. "+responseText.responseText);
            			}            			                                             
            		}
       			});
    	
	
	},
    getId: function(e){
		var ele1=$("#ds-carousel").find('.item.active > input');
		var vals1= $(ele1).attr('value');
        $('#select_block'+vals1+'').css('display','none');
		
		setTimeout(getDsAccess,800);
		function getDsAccess(){
			var ele2=$("#ds-carousel").find('.item.active > input');
			var vals2= $(ele2).attr('value');
			$('#select_block'+vals2+'').css('display','block');
			$("#selected_ds_id").val(vals2);
		}
	},
    
    runDs: function(e){

	var dsID = $(e.target).attr('name');
    var paramsArray = [];
    	paramsArray.push(dsID);
    	paramsArray.push('R');
    	
    	//console.log("Array Values :"+paramsArray);
    	this.dataSourceList.set("selectedDSRunIds",paramsArray);
    	this.dataSourceList.save(null,
       			{
            		type:"POST",
					error: function(datasource,responseText) {
            			if(responseText.responseText == 'Success'){
            				$(e.target).attr('src','imgs/stop_32.png');
            				$(e.target).attr('class','img_class_stop');  
                            					
            			}else{
            				alert("Error. "+responseText.responseText);
            			}            			                                             
            		}
       			});
    	
    },
	runQuery: function(e){
    	
    var qsID = $(e.target).attr('name');
    var paramsArray = [];
    	paramsArray.push(qsID);
    	paramsArray.push('RUN');
    	
    	//console.log("Array Values :"+paramsArray);
    	this.query.set("selectedQryId",paramsArray);
    	this.query.save(null,
       			{
            		error: function(datasource,responseText) {
            			if(responseText.responseText == 'Success'){
            				$(e.target).attr('src','imgs/stop_32.png');
            				$(e.target).attr('class','img_class_stop');  
                            					
            			}else{
            				alert("Error. "+responseText.responseText);
            			}            			                                             
            		}
       			});
    	
    },
    updateAssociatedDSUsers: function(){
    	var DsId = document.getElementById('selected_ds_id').value;    	
    	var selected_users_ids = [];
		var myOpts = [];
		
		
		$( '#m-selected'+DsId+' :selected' ).each( function( i, selected ) {
		myOpts[i] = $( selected ).val();
		
		});

    	for(var i=0; i<myOpts.length; i++){     		
    		selected_users_ids.push(myOpts[i]);
    	}
    	var dsID = $('#selected_ds_id').val();    	
    	selected_users_ids.push(dsID);
    	//console.log("Selected User IDs:"+selected_users_ids);
    	
    	this.user.set("selectedDSIds",selected_users_ids);
	  			  	
    	this.user.save(null,
   			{
        		
        	error: function(user,responseText,xhr) {
        		if(responseText.responseText = 'Success'){
            		alert("Updated Successfully !");
            		//this.usersList= new adminHomeModel();
            	}else{
            		alert("Error:"+JSON.stringify(responseText));            		
            	}
            }
    	 });
    	
    	
    },
    
    changeDsAccess: function(e){
    		
    		var ds_id = $(e.target).attr('name');
    		//alert("ds_id: "+ds_id);
    		//console.log("Data Source Id: "+ds_id);
    		//console.log("Users Store: "+JSON.stringify(usersListStore));
    		
    		//var dsUsersList = new getDsUsersModel({id:ds_id});
    		var dsUsersList = new Users({id:ds_id});
    		dsUsersList.toJSON();
    		$('#view_dsaccess_block').html('');
    		dsUsersList.fetch({success:function(){
    			
    			//console.log("Result:"+JSON.stringify(dsUsersList));
    			
    			var mul_sel_arr = new Array();
    			
    			var dummy_var = JSON.parse(JSON.stringify(dsUsersList));
    			    			
    			for (e in dummy_var) {
    				if(e != 'id'){
    					mul_sel_arr.push(dummy_var[e]);
    				}    				
       		 	}
    			
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
			  	  } else {
			  	    for (key in haystack) {
			  	      if (haystack[key] == needle) {
			  	        return true;
			  	      }
			  	    }
			  	  }

			  	  return false;
			  	}
			  	//End custom function
			  				  	
			  //Start code for multi user select
		    	var temp = JSON.parse(JSON.stringify(usersListStore));
			  	var mul_select_options = new Array();
			  	
			  	
			  	$('#selected_ds_id').val(ds_id);
			  	$('#view_ds_block').css('display','none');
				
			  	$('#view_dsaccess_block').append('<div id="select_block'+ds_id+'"></div>');
				$('#select_block'+ds_id).html('<select id="m-selected'+ds_id+'" name="m-selected'+ds_id+'" multiple="multiple"  class="multi"></select>');
			  	$.each(temp, function(i, obj) {			  			
			  		 	var id = obj['id'];
			  		 	var name = obj['userName'];
			  		 	if(in_array(id,mul_sel_arr)){			  		 		
			  		 		//mul_select_options += '<option value="'+id+'" selected="selected" >'+name+'</option>';
			  		 		$('#m-selected'+ds_id).append('<option value="'+id+'" selected="selected" >'+name+'</option>');
			  		 	}else{			  		 		
			  		 		//mul_select_options += '<option value="'+id+'">'+name+'</option>';
			  		 		$('#m-selected'+ds_id).append('<option value="'+id+'">'+name+'</option>');
			  		 	}
		     	
			  	});
			  	 $('#m-selected'+ds_id).multiSelect('refresh');			  	
			  	$('#users_select').append(mul_select_options);
			  	$("#mul_users").css("display","block");
			  			  	
			  	//End code for multi user select
			  	
			  	//console.log("DS users: "+mul_select_options);    			
    			//console.log("DS users: "+mul_sel_arr);
    			
    		}});
    		
    		$('.change_access').on('click', function(){
    			
    			
    		});    		
    		$('#view_ds_access').css('display','block');	
  	
    },
/*   updateUserDetails: function(e){
  
    		var user_id = $(e.target).attr('name');
    		 		
    		var name = $('#name_'+user_id).val();
    		var email = $('#email_'+user_id).val();
    		//var auth = $('#auth_'+user_id).val();
    		var role = $('#role_'+user_id).val();
       
    		if(role == 0) role = 1;
    		
    		var status = $('#status_'+user_id+':checked').val();    		
    	    		
    		this.user.set({"emailId":email});
    		//this.usersList.set({"authentication":auth});
    		this.user.set({"roleId":role});
    		
    		this.user.set({"status":status});
    		this.user.set({"userName":name});
    		//this.user.set({"id":user_id});
          
    	  			  	
    		/*var a =this.user.save(null,
       			{
            	type:"PUT",	
    			});
            console.log(a);
            var that = this;
            var q= this.usersList.fetch(
                success:function(){
                    console.log(that.userList.toJSON());
                }
            );
            $.when(q).done(function(){
                console.log(that.usersList.findWhere({emailId:'a@hcl.com'}));
            });*/
            /*this.user.save(null,
                {
                type:"PUT",    
            	error: function(user,responseText,xhr) {
            		if(responseText.responseText = 'Success'){
            	

            			this.usersList = new Users();
            	    	this.usersList.toJSON();  
						window.usersListStore = this.usersList;	

            	        this.usersList.fetch({success:function(){
            	        var dummy_var = JSON.parse(JSON.stringify(usersListStore));                
            	        var objs_arr = [];
            	                       
            	        $.each(dummy_var, function(i, obj) {
            	        	
            	        	//alert('User Id Value: '+obj['id']);        	
            	        	obj['users_checked'] = '<input type="checkbox" value="'+obj['id']+'" name="users_checked[]">';
            	        	if(obj['status'] == 'A') obj['users_status'] = 'Active';
            	        	if(obj['status'] == 'I') obj['users_status'] = 'Inactive';
            	         	objs_arr.push(obj);
            	        });
            	        
            	        //console.log("Users List: "+objs_arr);
            	                	
            	    	$('#users_table').dataTable({  
            	    		 "bProcessing": true,
            	    		 "aaSorting": [[ 1, "desc" ]],
            	    		 "aaData": objs_arr,
            	    		 "bDestroy": true,
            	    		 "aoColumns": [
            	    		    { "sTitle": '<input type="checkbox" name="select_all" id="select_all">',   "mDataProp": "users_checked","bSortable":false },
            	    		    { "sTitle": "User Id",  "mDataProp": "id" },
            	    		    { "sTitle": "User Name", "mDataProp": "userName" },
            	    		    { "sTitle": "Email",  "mDataProp": "emailId" },
            	    		    
            	    		    { "sTitle": "Role",  "mDataProp": "roleType" },
            	    		    { "sTitle": "Status",  "mDataProp": "status" },
            	    		    { "sTitle": "Last Login",  "mDataProp": "userLastAccessed" }
            	    		  ]

            	    	});    	
            	    	
            		  	
            	        }});
            	        alert("Updated Successfully !");
                	}else{
                		alert("Error:"+JSON.stringify(responseText));            		
                	}                                             
                }
        	 });
    	
    },*/
    
    deleteQuery: function(){
    	
    	var num_checked = $( "#query_table td input:checked" ).length;					
		if( num_checked == 0 ){
			alert("Please select at least one checkbox !");
			return false;
		}
		
		if(confirm("Are you sure to delete !")){
    	var checkValues = $('#query_table td input:checked').map(function() {return this.value;}).get().join(',');
	  	
    	//alert(checkValues);
	  	var chk_array = checkValues.split(',');
	  	//deleteQueryModel = new deleteQueryModel();
	  	this.query.set("selectedQueryIds",chk_array);
	  			  	
	  	this.deleteQueryModel.save(null,
   			{
        		
        	error: function(deleteQueryModel,responseText,xhr) {
        		if(responseText.responseText = 'Success'){
            		alert("Deleted Successfully !");
            		var newFragment = Backbone.history.getFragment($(this).attr('button'));
                    if (Backbone.history.fragment == newFragment) {
                    	//Start script for Query list
                		
                		var queryList= new adminQueryListModel();
                		queryList.toJSON();         
                		queryList.fetch({success:function(){
                        window.queryListStore = queryList;
                        var q_dummy_var = JSON.parse(JSON.stringify(queryList));                
                        var q_objs_arr = [];
                                       
                        $.each(q_dummy_var, function(i, obj) {
                        	
                        	//console.log('Query Id Value: '+obj['srcID']);        	
                        	obj['q_checked'] = '<input type="checkbox" value="'+obj['qID']+'" name="q_checked[]">';
                        	
                        	var q_control = obj['control'];
                        	if(q_control != 1){
                        		obj['q_control'] = '<a href="#" class="btn btn-small btn-inverse h16" ><img src="imgs/play_32.png" name="'+obj['qID']+'"class="img_class" id="q_play'+obj['qID']+'" /></a>';
                        	}else{
                        		obj['q_control'] = '<a href="#" class="btn btn-small btn-inverse h16" ><img src="imgs/stop_32.png" name="'+obj['qID']+'" class="img_class_stop" id="q_stop'+obj['qID']+'" /></a>';
                        	}
                        	
                        	if(obj['status'] == 'R') obj['status'] = 'Running';
                        	if(obj['status'] == 'S') obj['status'] = 'Stop';
                        	        	
                        	q_objs_arr.push(obj);
                        });
                        
                    	//console.log("Query List: "+JSON.stringify(q_objs_arr));    	
                    	
                    	$('#query_table').dataTable({  
                   		 "bProcessing": true,
                   		 "aaSorting": [[ 1, "asc" ]],
                   		 "aaData": q_objs_arr,
                   		 "bDestroy": true,
                   		 "aoColumns": [
                   		    { "sTitle": '<input type="checkbox" name="q_select_all" id="q_select_all">',   "mDataProp": "q_checked","bSortable":false },
                   		    { "sTitle": "Id",  "mDataProp": "qID" },
                   		    { "sTitle": "Name", "mDataProp": "queryName" },
                   		    /*{ "sTitle": "DS Set",  "mDataProp": "dsmasterId" },*/
                   		    { "sTitle": "Resolution",  "mDataProp": "boundingbox" },
                   		    { "sTitle": "Status",  "mDataProp": "status" },
                   		    { "sTitle": "Control",  "mDataProp": "q_control" }   		    
                   		  ]

                    	});
                    	
                    	
                    	}});
                     /* End script for Query list*/	                
		            }
            		
            	}else{
            		alert("Error:"+JSON.stringify(responseText));            		
            	}
            }
    	 });
    	}
    },    
       
    
    deleteDataSources: function(){
    	
    	var num_checked = $( "#ds_table td input:checked" ).length;					
		if( num_checked == 0 ){
			alert("Please select at least one checkbox !");
			return false;
		}
		
		if(confirm("Are you sure to delete !")){    	
    	var checkValues = $('#ds_table td input:checked').map(function() {return this.value;}).get().join(',');
	  	
    	
	  	var chk_array = checkValues.split(',');
	  	
	  	this.dataSourceList.set("selectedDSIds",chk_array);
	  			  	
	  	this.dataSourceList.save(null,
   			{
        	type:"DELETE",	
        	error: function(datasource,responseText,xhr) {
        		if(responseText.responseText = 'Success'){
            		alert("Deleted Successfully !");
            		//$('#ds_tab'+Backbone.history.fragment).addClass('active');
            		var newFragment = Backbone.history.getFragment($(this).attr('button'));
            		 if (Backbone.history.fragment == newFragment) {
            			 /* Start script for ds list*/	     
            		var dataSourceList= new adminDataSourceListModel();
                	dataSourceList.toJSON();         
                	dataSourceList.fetch({success:function(){
                    	
                    var ds_dummy_var = JSON.parse(JSON.stringify(dataSourceList));                
                    var ds_objs_arr = [];
                                   
                    $.each(ds_dummy_var, function(i, obj) {
                    	
                    	//console.log('DS Control Value: '+obj['control']);
                    	var ds_control = obj['control'];
                    	if(obj['access'] == 'Public')
                    		obj['ds_access'] = '<a href="#" class="change_access" id="change_access_link" name="'+obj['srcID']+'">'+obj['access']+'</a>';
                    	else
                    		obj['ds_access'] = obj['access'];
                    	
                    	obj['ds_checked'] = '<input type="checkbox" value="'+obj['srcID']+'" name="ds_checked[]">';
                    	
                    	if(ds_control != 1){        		
                    		obj['ds_control'] = '<a href="#" class="btn btn-small btn-inverse h16" ><img src="imgs/play_32.png" name="'+obj['srcID']+'"  class="img_class" id="ds_play_'+obj['srcID']+'" /></a>';
                    	}else{        		
                    		obj['ds_control'] = '<a href="#" class="btn btn-small btn-inverse h16" ><img src="imgs/stop_32.png" name="'+obj['srcID']+'" class="img_class_stop" id="ds_stop'+obj['srcID']+'" /></a>';
                    	}
                    	ds_objs_arr.push(obj);
                    });
                    
                	//console.log("DataSource List: "+JSON.stringify(ds_objs_arr));
                	
                	
                	$('#ds_table').dataTable({  
               		 "bProcessing": true,
               		 "aaSorting": [[ 1, "asc" ]],
               		 "aaData": ds_objs_arr,
               		 "bDestroy": true,
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
                	
                	
                	}});
                	/* End script for ds list*/	  
            		 }
            	  }else{
            		alert("Error:"+JSON.stringify(responseText));            		
            	}
            }
    	 });
	  	
		}
    },    
   
    
    /*activateSelectedUsers: function(){
    	
    	var num_checked = $( "#users_table td input:checked" ).length;					
		if( num_checked == 0 ){
			alert("Please select at least one checkbox !");
			return false;
		}
    	
    	var checkValues = $('#users_table td input:checked').map(function() {return this.value;}).get().join(',');
	  	checkValues += ',A';
    	
	  	var chk_array = checkValues.split(',');
	  	//updateUserStatusModel = new updateUserStatusModel();
	  	this.user.set("selectedUserIds",chk_array);
	  			  	
		this.user.save(null,
   			{
            type:"PUT",        		
        	error: function(user,responseText,xhr) {
            	
            	if(responseText.responseText = 'Success'){
            		alert("Updated Successfully !");
            		
                    var newFragment = Backbone.history.getFragment($(this).attr('button'));
                    if (Backbone.history.fragment == newFragment) {
                        Backbone.history.fragment = null;
		                Backbone.history.navigate(newFragment, true);
		            }
            		
            	}else{
            		alert("Error:"+JSON.stringify(responseText));            		
            	}            	
            }
    	 });
    },    
    
    deActivateSelectedUsers: function(){
    	
    	var num_checked = $( "#users_table td input:checked" ).length;					
		if( num_checked == 0 ){
			alert("Please select at least one checkbox !");
			return false;
		}
    	
    	var checkValues = $('#users_table td input:checked').map(function() {return this.value;}).get().join(',');
	  	checkValues += ',I';
    	//alert(checkValues);
	  	var chk_array = checkValues.split(',');
	  	//updateUserStatusModel = new updateUserStatusModel();
	  	this.updateUserStatusModel.set("selectedUserIds",chk_array);
	  			  	
		this.updateUserStatusModel.save(null,
   			{
        		
        	error: function(updateUserStatusModel,responseText,xhr) {
        		if(responseText.responseText = 'Success'){
            		alert("Updated Successfully !");
            		
                    var newFragment = Backbone.history.getFragment($(this).attr('button'));
                    if (Backbone.history.fragment == newFragment) {
                        Backbone.history.fragment = null;
		                Backbone.history.navigate(newFragment, true);
		            }
            		//window.location = 'http://localhost:8080/eventshoplinux/#adminHome';
            		//window.location.reload();
            	}else{
            		alert("Error:"+JSON.stringify(responseText));            		
            	}
        	}
    	 });
    },    
    
     selectAllUsers: function(e){    	
    	var c = $(e.target).prop('checked'); 
        if(c==true){
          $('#users_table input:checkbox').prop('checked','checked');  
            
        }else{ 
            $('#users_table input:checkbox').prop('checked',c);         
        }
    },*/
    
    selectAllDS: function(e){    	
    	var c = $(e.target).prop('checked'); 
        if(c==true){
          $('#ds_table input:checkbox').prop('checked','checked');  
            
        }else{ 
            $('#ds_table input:checkbox').prop('checked',c);         
        }   	
    },
    
    selectAllQuery: function(e){    	
    	var c = $(e.target).prop('checked'); 
        if(c==true){
          $('#query_table input:checkbox').prop('checked','checked');  
            
        }else{ 
            $('#query_table input:checkbox').prop('checked',c);         
        } 
    },    
       
    
    callInitialScripts: function(){
    	    	
    	//Start script for Users list
    	/*this.usersList.toJSON(); 
		window.usersListStore = this.usersList;        
        this.usersList.fetch({success:function(){  
	        var dummy_var = JSON.parse(JSON.stringify(usersListStore)); 
	        var objs_arr = [];
	        $.each(dummy_var, function(i, obj) {
	        	//alert('User Id Value: '+obj['id']);        	
	        	obj['users_checked'] = '<input type="checkbox" value="'+obj['id']+'" name="users_checked[]">';
               
	        	if(obj['status'] == 'A') obj['status'] = 'Active';
	        	if(obj['status'] == 'I') obj['status'] = 'Inactive';
	         	objs_arr.push(obj);
	        });
            
	        $('#users_table').dataTable({  
	    		 "bProcessing": true,
	    		 "aaSorting": [[ 1, "desc" ]],
	    		 "aaData": objs_arr,
	    		 "aoColumns": [
	    		    { "sTitle": '<input type="checkbox" name="select_all" id="select_all">',   "mDataProp": "users_checked","bSortable":false },
	    		    { "sTitle": "User Id",  "mDataProp": "id" },
	    		    { "sTitle": "User Name", "mDataProp": "userName" },
	    		    { "sTitle": "Email",  "mDataProp": "emailId" },
	     		    { "sTitle": "Role",  "mDataProp": "roleType" },
	    		    { "sTitle": "Status",  "mDataProp": "status" },
	    		    { "sTitle": "Last Login",  "mDataProp": "userLastAccessed" }
	    		  ]
	    	});   
	      
        }});*/ // users done
        
    	//Start script for DS list    	
    /*	this.dataSourceList.toJSON();  
    	window.dataSourceListStore = this.dataSourceList;
		
    	this.dataSourceList.fetch({success:function(){    		        
    		var ds_dummy_var = JSON.parse(JSON.stringify(dataSourceListStore));  
    		var ds_objs_arr = [];
    		$.each(ds_dummy_var, function(i, obj) {
        	
        	//console.log('DS Control Value: '+obj['control']);
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
        
    	//console.log("DataSource List: "+JSON.stringify(ds_objs_arr));
    	
    	
    /*	$('#ds_table').dataTable({  
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
    	
    	
    	}});
 		
    	
		//Start script for Query list
		
		var queryList= new Queries();
		queryList.toJSON();         
		queryList.fetch({success:function(){
        window.queryListStore = queryList;
        var q_dummy_var = JSON.parse(JSON.stringify(queryList));                
        var q_objs_arr = [];
                       
        $.each(q_dummy_var, function(i, obj) {
                	
        	//console.log('Query Id Value: '+obj['srcID']);        	
        	obj['q_checked'] = '<input type="checkbox" value="'+obj['qID']+'" name="q_checked[]">';
        	
        	var q_control = obj['control'];
        	if(q_control != 1){
        		obj['q_control'] = '<a href="javascript:void(0);" class="btn btn-small btn-inverse h16" ><img src="imgs/play_32.png" name="'+obj['qID']+'" id="q_play_'+obj['qID']+'" class="img_class"  /></a>';
        	}else{
        		obj['q_control'] = '<a href="javascript:void(0);" class="btn btn-small btn-inverse h16" ><img src="imgs/stop_32.png" name="'+obj['qID']+'" id="q_stop_'+obj['qID']+'"  class="img_class_stop"  /></a>';
        	}
        	
        	if(obj['status'] == 'A') obj['status'] = 'Running';
        	if(obj['status'] == 'I') obj['status'] = 'Stop';
        	        	
        	q_objs_arr.push(obj);
        });
        
    	//console.log("Query List: "+JSON.stringify(q_objs_arr));    	
    	
    	$('#query_table').dataTable({  
   		 "bProcessing": true,
   		 "aaSorting": [[ 1, "asc" ]],
   		 "aaData": q_objs_arr,
   		 "aoColumns": [
   		    { "sTitle": '<input type="checkbox" name="q_select_all" id="q_select_all">',   "mDataProp": "q_checked","bSortable":false },
   		    { "sTitle": "Id",  "mDataProp": "qID" },
   		    { "sTitle": "Name", "mDataProp": "queryName" },
   		    /*{ "sTitle": "DS Set",  "mDataProp": "dsmasterId" },*/
   		  /*  { "sTitle": "Resolution",  "mDataProp": "boundingbox" },
   		    { "sTitle": "Status",  "mDataProp": "status" },
   		    { "sTitle": "Control",  "mDataProp": "q_control" }   		    
   		  ]

    	});
    	
    	
    	}});*/
		
		   	
    },
    
    showQueryLists: function(){
    	
    	//Start script for Query List			
	
			var num_checked = $( "#query_table td input:checked" ).length;					
			if( num_checked == 0 ){
				alert("Please select at least one checkbox to view query profile");
				return false;
			}
		
		
			
			  var n = $( "#query_table td input:checked" ).length;
			  var html_block_arr = new Array();
			  var checkValues = $('#query_table td input:checked').map(function() {return this.value;}).get().join(',');
			  //alert(checkValues);
			  var chk_array = checkValues.split(',');
			    //console.log('User List:'+JSON.stringify(usersListStore));
			  	
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
			  	  } else {
			  	    for (key in haystack) {
			  	      if (haystack[key] == needle) {
			  	        return true;
			  	      }
			  	    }
			  	  }

			  	  return false;
			  	}
			  	//End custom function
			  
			  var temp = JSON.parse(JSON.stringify(queryListStore));
			  	//var html_block_arr = new Array();
			  var t = 0;
			  	$.each(temp, function(i, obj) {
			  		
		        	//console.log('User Id Value: '+obj['id']);
		        	if(in_array(obj['qID'],chk_array)){
		        		//alert(obj['qID']+" is Selected");
		        		
		        		if(t == 0){
							var active = 'active';
						}else{					
							var active = '';					
						}
		        		t++;
		        		var qID = obj['qID'];
		        		var queryName = obj['queryName'];
		        		var qds_set = '';
		        		var q_resolution = obj['boundingbox'];		        		
		        		var q_status = obj['status'];
		        		
		        		if(q_status == 'A') obj['status'] = 'Running';
		            	if(q_status == 'I') obj['status'] = 'Stop';
		        		
		        		html_block_arr += '<div class="item '+active+'"><div class="row"><div class="span12 adjWidth"><table align="center" cellpadding="11" ><tr><td><table align="center" cellpadding="5" cellspacing="5"><tr><td>Query Id</td><td>:</td><td>'+qID+'</td></tr><tr><td>Query Name</td><td>:</td><td>'+queryName+'</td></tr><tr><td>Resolution</td><td>:</td><td>'+q_resolution+'</td></tr><tr><td>Status</td><td>:</td><td>'+q_status+'</td></tr></table></td><td>&nbsp;</td></tr></table></div></div></div>';
		        	}
		        	
		        });
			  	
			  	//console.log("HTML:"+html_block_arr);
			  
			  
			  
			$('#view_query_block').html(html_block_arr);
						
			$('#view_querylist_block').css('display','block');
			 $("#query-carousel").carousel("pause").removeData();
			 $("#query-carousel").carousel(); 
	
		
		//End script for Query List
    	
    }
    /*
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
			  
	            populateDsQueries(dataSourceListStore,count); 
			   	
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
									
	            		function populateDsQueries(dataSourceListStore,count) {
	            			
	            			  var ds_list_dummy_var = JSON.parse(JSON.stringify(dataSourceListStore));
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
		          				/* ds multi select*/
								/*var dsUsersList = new getDsUsersModel({id:srcID});
								dsUsersList.toJSON();
								
								dsUsersList.fetch({success:function(){ 
									var mul_sel_arr = new Array();
    			
									var dummy_var = JSON.parse(JSON.stringify(dsUsersList));
    			    			
									for (e in dummy_var) {
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
			  	                   
									
								}
								});
								/* ds multi select */
		          				/*ds_html_block_arr1 += '<div class="item '+active+'"><input type="hidden" value="'+srcID+'" name="current_ds_id"><div class="row"><div class="span12 adjWidth"><table align="center" cellpadding="11" ><tr><td><table align="center" cellpadding="5" cellspacing="5"><tr><td>Data Source Id</td><td>:</td><td>'+srcID+'</td></tr><tr><td>Data Souce Name</td><td>:</td><td>'+srcName+'</td></tr><tr><td>Description</td><td>:</td><td>'+desc+'</td></tr><tr><td>URL</td><td>:</td><td>'+url+'</td></tr><tr><td>Format</td><td>:</td><td>'+format+'</td></tr><tr><td>Type</td><td>:</td><td>'+type+'</td></tr><tr><td>Creator</td><td>:</td><td>'+creater+'</td></tr><tr><td>Archive</td><td>:</td><td>'+archive+'</td></tr><tr><td>Unit</td><td>:</td><td>'+unit+'</td></tr><tr><td>Created Date</td><td>:</td><td>'+createdDate+'</td></tr></table></td><td><table align="center" cellpadding="5" cellspacing="5"><tr><td>View Map :</td></tr><tr><td><textarea rows="2" cols="10"></textarea></td></tr><tr><td>Resolution :</td></tr><tr><td><textarea rows="2" cols="10"></textarea></td></tr></table></td></tr></table></div></div></div>';
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
    
  /* showUserProfiles:function(){
        //Start script for Users list                   
        var num_checked = $( "#users_table td input:checked" ).length;                  
        if( num_checked == 0 ){
            alert("Please select at least one checkbox to view user profile");
            return false;
        }                   
        var n = $( "#users_table td input:checked" ).length;

        var html_block_arr = new Array();             
        //var checkValues = $('#users_table td input:checked').map(function() {return '{userId:'+this.value+'}';}).get().join(',')
        var checkValues = $('#users_table td input:checked').map(function() {return this.value;}).get().join(',');

        function in_array (needle, haystack, argStrict) {
          var key = '',
          strict = !! argStrict;
          if (strict) {
              for (key in haystack) {
                      if (haystack[key] === needle) {
                        return true;
                      }
                }
          } else {
              for (key in haystack) {
                if (haystack[key] == needle) {
                    return true;
                }
              }
          }
          return false;
    }
    //End custom function
      //Start code to get list of checked users datasource and query lists
    var chk_array = checkValues.split(',');

    //End code to get list of checked users datasource and query list
    var count=chk_array.length;
    
    populateDsQueries(usersListStore,count); 
    //console.log("selected fellows"+JSON.stringify(selectedUsersModel));
    //End code to get list of checked users datasource and query list
    var new_arr = new Array();           
    function populateDsQueries(ds_query_list,count) {

                     var ds_arr_obj = '';
                     var query_arr_obj = '';
                    
                    var temp = JSON.parse(JSON.stringify(usersListStore));
                    var html_block_arr = new Array();
                    var mul_select_options = new Array();
                    var m=0;
                    $.each(temp, function(i, obj) {
                                                
                        if(in_array(obj['id'],chk_array)){
                            
                            
                            if(m == 0){
                                var active = ' active';
                            }else{                  
                                var active = '';                    
                            }
                            m++;
                            var id = obj['id'];
                            var name = obj['userName'];
                            var email = obj['emailId'];
                            var auth = obj['authentication'];
                            var role = obj['roleId'];
                            var status = obj['status'];
                            var a_checked = '';
                            var i_checked = '';
                            if(status == 'A') a_checked = 'checked="checked"';
                            if(status == 'I') i_checked = 'checked="checked"';
                            
                            var a_selected = '';
                            var n_selected = '';
                            if(role == '1') a_selected = 'selected="selected"';
                            if(role == '2') n_selected = 'selected="selected"';
                                                                                    
                            html_block_arr += '<div class="item'+active+'"><div class="row"><div class="span12 adjWidth"><table align="center" cellpadding="11" ><tr><td><table align="center" cellpadding="5" cellspacing="5"><tr><td>Id</td><td>:</td><td>'+id+'</td></tr><tr><td>Name</td><td>:</td><td><input type="text" value="'+name+'" id="name_'+id+'"/></td></tr><tr><td>Email</td><td>:</td><td><input type="text" id="email_'+id+'" value="'+email+'"/></td></tr><tr><td>Role</td><td>:</td><td><select id="role_'+id+'"><option value="1" '+a_selected+'>Admin</option><option value="2" '+n_selected+'>Normal User</option></select></td></tr><tr><td>Status</td><td>:</td><td><input type="radio"  id="status_'+id+'"  name="status_'+id+'" '+a_checked+' value="A"> Active&nbsp; <input id="status_'+id+'" name="status_'+id+'" type="radio" '+i_checked+' name="status" value="I"> Inactive</td></tr></table></td><td><table align="center" cellpadding="5" cellspacing="5"><tr><td>DS List :</td></tr><tr><td><textarea rows="2" cols="10">'+ds_arr_obj+'</textarea></td></tr><tr><td>Query List :</td></tr><tr><td><textarea rows="2" cols="10">'+query_arr_obj+'</textarea></td></tr></table></td></tr><tr><td colspan="2" align="center"><input type="button" class="btn btn-small btn-inverse" name="'+id+'" value="update" id="update"  /></td><tr></table></div></div></div>';  
                            //alert(html_block_arr);
                        }
                        
                        
                        
                    }); 
                    $('#view_users_block').html("");
                    $('#view_users_block').html(html_block_arr);
                    
                }                 
                //End code to get list of checked users
            
            
            //$( "#users_table input[type=checkbox]" ).on( "click", countChecked );
            
            
            $('#view_profile_block').css('display','block');
            $("#home-carousel").carousel("pause").removeData();
            $("#home-carousel").carousel();
            
        //End Script for Users  
    }
*/
  });
  
 return adminHomeView;
});
