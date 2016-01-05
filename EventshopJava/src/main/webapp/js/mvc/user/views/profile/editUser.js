define([
  'jquery',
  'underscore',
  'backbone',
  'mvc/user/models/user',
  'text!templates/user/profile/editUserTpl.html'
 ], function($, _, Backbone,user,userTpl){
 	var userView = Backbone.View.extend({
 		el: $("#tabs-3"),
 		 events: {
             "click #saveUser": "saveUser",
 			 "click #cancelSave":"cancelSave" 			
        },
 		initialize: function(){
 			this.render();
 		},
 	render: function(){
 		
      this.$el.html(userTpl);
    }


 	});
 	return userView;
 });