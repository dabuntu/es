define([
'underscore',
'backbone'
], function(_, Backbone) {
    var alertListModel = Backbone.Model.extend({
    
        defaults: {
            /*aID:"",
            alertName : "",
            alertType : "",
            alertTheme : "",
            alertSrc : "",
            alertSrcMin : "",
            alertSrcMax : "",
            alertStatus : "",
            uId : ""*/
        },
        urlRoot: "/eventshoplinux/webresources/alert/alerts",
            
    initialize: function(){
        
    }

    });
//    alert("it comes here 2");
  return alertListModel;

});