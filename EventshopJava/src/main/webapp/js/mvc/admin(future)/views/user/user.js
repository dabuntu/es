define([
  'jquery',
  'underscore',
  'backbone',
  'text!templates/admin/user/user.html',
  'mvc/admin/models/user',
  'mvc/admin/collections/users'
 ], function($, _, Backbone,userTpl,User,users){
 	var self;
 	var userView = Backbone.View.extend({
 		el: $("#tabs-1"),
 		initialize: function(){
 			self = this;
 			this.user= new User();
            this.usersList=new users()
 			this.render();

 		},
 		events : {
 			"click #view":"showUserProfiles",
 			"click #select_all":"selectAllUsers",
 			"click #activate":"activateSelectedUsers",
 			"click #deactivate":"deActivateSelectedUsers",
 			 "click #update":"updateUserDetails"
 		},
 	render: function(){
 		this.fetchUser();
      //this.$el.html(userTpl);
    },
    fetchUser: function() {
    	self=this;
    	userList = new users();
    	userList.toJSON();
    	userList.fetch({
    		success: function(){

    			 var users_Arr = JSON.parse(JSON.stringify(userList));
			 	
			 	
			 	var compiledTemplate = _.template(userTpl,{UserArr: users_Arr});
			    self.$el.html(compiledTemplate);
			    self.usertable(users_Arr);
			 
			}		
    	});
    },
    usertable:function(users_Arr){
    	var objs_arr = [];
	    window.usersListStore = users_Arr; 
		
		$.each(users_Arr, function(i, obj) {
	        	//alert('User Id Value: '+obj['id']);        	
	        	obj['users_checked'] = '<input type="checkbox" value="'+obj['id']+'" name="users_checked[]">';
               
	        	if(obj['status'] == 'A') obj['status'] = 'Active';
	        	if(obj['status'] == 'I') obj['status'] = 'Inactive';
	         	objs_arr.push(obj);
	        });
            
	        $('#users_table').dataTable({  
	    		 "bProcessing": true,
	    		 "aaSorting": [[ 1, "desc" ]],
                 "oLanguage": { 
                    "sLengthMenu":"Show _MENU_  Users",
                    "sInfo": "Showing _START_ to _END_ of _TOTAL_ users",
                    "sInfoEmpty": "Showing 0 to 0 of 0 users"
                }, 
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
    },
     showUserProfiles:function(){
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

          
  
    this.populateDsQueries(usersListStore,checkValues); 
    //console.log("selected fellows"+JSON.stringify(selectedUsersModel));
    //End code to get list of checked users datasource and query list
    //var new_arr = new Array();           
                     
    //End code to get list of checked users
            
            
    //$( "#users_table input[type=checkbox]" ).on( "click", countChecked );
            
            
      $('#view_profile_block').css('display','block');
      $("#home-carousel").carousel("pause").removeData();
       $("#home-carousel").carousel();
            
        //End Script for Users  
    },
     updateUserDetails: function(e){
  
    		var user_id = $(e.target).attr('name');

    		
    		 		
    		var name = $('#name_'+user_id).val();
    		var email = $('#email_'+user_id).val();
    		var auth = $('#auth_'+user_id).val();
    		var role = $('#role_'+user_id).val();
       
    		if(role == 0) role = 1;
    		
    		var status = $('#status_'+user_id+':checked').val();    		
    	    		
    		this.user.set({"emailId":email});
    		this.user.set({"authentication":auth});
    		this.user.set({"roleId":role});
    		
    		this.user.set({"status":status});
    		this.user.set({"userName":name});

    		this.user.set({"id":user_id});         
    	  			  	
    		
            this.user.save(null,
                {
                type:"PUT",    
            	error: function(user,responseText,xhr) {
            		if(responseText.responseText = 'Success'){
            	

            			this.usersList = new users();
            	    	this.usersList.toJSON();  
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
    	
    },
    activateSelectedUsers: function(){


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
        //this.user.set("status","Active");
                    
        this.user.save(null,
            {
            url: "/eventshoplinux/webresources/adminservice/users/activate",
            type:"POST", 

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
	  	this.user.set("selectedUserIds",chk_array);
	  			  	
		this.user.save(null,
   			{
                url: "/eventshoplinux/webresources/adminservice/users/activate",
        		type: "POST",
        	error: function(user,responseText,xhr) {
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
   populateDsQueries: function(ds_query_list,checkValues) {

	 //End custom function
	      //Start code to get list of checked users datasource and query lists
	   				var chk_array = checkValues.split(',');
	   				//CALL REST SERVICE userDatasources AND PASS CHECKVALUES it will give an object with 2 arrays one called DS, the other Query
                 
                       //checkedvalues = encodeURIComponent(checkValues);
                    
                    $.ajax({ 
                        url: "/eventshoplinux/webresources/adminservice/userDatasources",
                        type: "GET",
                        contentType:"application/json",        
                        data: {"selectedUserIds" : checkValues},
                        success: function(data){
                         var text_area=[];        
                            $.each(data,function(key,value){
                                text_area.push(value);
                            })
                                    //End code to get list of checked users datasource and query list
                            var count=chk_array.length;
                           
                             var ds_arr_obj = '';
                             var query_arr_obj = '';
                            
                            var temp = JSON.parse(JSON.stringify(usersListStore));
                            var html_block_arr = new Array();
                            var mul_select_options = new Array();
                            var m=0;
                            $.each(temp, function(i, obj) {
                               // console.log("temp : "+JSON.stringify(temp));
                                                        
                                if(self.in_array(obj['id'],chk_array)){                          
                                                              
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
                                    if(status == 'Active') a_checked = 'checked="checked"';
                                    if(status == 'Inactive') i_checked = 'checked="checked"';
                                    
                                    var a_selected = '';
                                    var n_selected = '';
                                    if(role == '1') a_selected = 'selected="selected"';
                                    if(role == '2') n_selected = 'selected="selected"';
                                    $.each(text_area,function(i,key){
                                        if(key.userId==id){
                                          ds_arr_obj=key.DS; 
                                          query_arr_obj=key.Query; 
                                        }
                                    });
                                    
                                    //dslist, querylist??
                                                                                            
                                    html_block_arr += '<div class="item'+active+'"><div class="row"><div class="span12 adjWidth"><table align="center" cellpadding="11" ><tr><td><table align="center" cellpadding="5" cellspacing="5"><tr><td>Id</td><td>:</td><td>'+id+'</td></tr><tr><td>Name</td><td>:</td><td><input type="text" value="'+name+'" id="name_'+id+'"/></td></tr><tr><td>Email</td><td>:</td><td><input type="text" id="email_'+id+'" value="'+email+'"/></td></tr><tr><td>Role</td><td>:</td><td><select id="role_'+id+'"><option value="1" '+a_selected+'>Admin</option><option value="2" '+n_selected+'>Normal User</option></select></td></tr><tr><td>Status</td><td>:</td><td><input type="radio"  id="status_'+id+'"  name="status_'+id+'" '+a_checked+' value="A"> Active&nbsp; <input id="status_'+id+'" name="status_'+id+'" type="radio" '+i_checked+' name="status" value="I"> Inactive</td></tr></table></td><td><table align="center" cellpadding="5" cellspacing="5"><tr><td>DS List :</td></tr><tr><td><textarea disabled rows="2" cols="10">'+ds_arr_obj+'</textarea></td></tr><tr><td>Query List :</td></tr><tr><td><textarea disabled rows="2" cols="10">'+query_arr_obj+'</textarea></td></tr></table></td></tr><tr><td colspan="2" align="center"><input type="button" class="btn btn-small btn-inverse" name="'+id+'" value="Update" id="update"  /></td><tr></table></div></div></div>';  
                                    alert(html_block_arr);
                                }
                                
                                
                                
                            }); 
                            $('#view_users_block').html("");
                            $('#view_users_block').html(html_block_arr);
                         }
                    });

                 



	    	
                    
                },  
                in_array: function(needle, haystack, argStrict) {
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
    },
    
     selectAllUsers: function(e){    	
    	var c = $(e.target).prop('checked'); 
        if(c==true){
          $('#users_table input:checkbox').prop('checked','checked');  
            
        }else{ 
            $('#users_table input:checkbox').prop('checked',c);         
        }
    }

   

 	});
 	return userView;
 });
