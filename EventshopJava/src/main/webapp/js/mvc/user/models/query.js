define([
'underscore',
'backbone'
], function(_, Backbone) {
    var queryListModel = Backbone.Model.extend({
    
        defaults: {

        },
        url: "/eventshoplinux/webresources/queryservice/queries",
            
    initialize: function(){
        
    }

    });
      
  return queryListModel;

});
