
define([
'jquery',
'underscore',
'backbone',
'mvc/admin/models/user'
], function($, _, Backbone, user) {
	var users = Backbone.Collection.extend({
		model: user,
		url: "/eventshoplinux/webresources/adminservice/users",
		parse: function(response) {
			if (response != null) {
				return response;
			}else {
				return response;
			}
		}
	});
	return users;
});