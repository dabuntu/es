define([
'underscore',
'backbone'
], function(_, Backbone) {
    var ruleListModel = Backbone.Model.extend({

        defaults: {
        },
        urlRoot: "/eventshoplinux/rest/rulewebservice/rule",

    initialize: function(){

    }

    });
//    alert("it comes here 2");
  return ruleListModel;

});