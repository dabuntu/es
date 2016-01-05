define([ 'jquery', 
         'underscore', 
         'backbone',
		 'text!templates/user/login/loginTemplate.html',
		 'mvc/user/models/login', 
], function($, _, Backbone, loginTemplate, login) {
	var self;
	var loginView = Backbone.View.extend({
		el : $("#page"),

		events : {
			"click #login" : "loginValidate",
			"click #signup" : "openRegisterPage"
		},

		initialize : function(opts) {
			self = this;
			self.app_router = opts.router;
			var checkAdmin = getCookie("checkAdmin");

			if ((checkAdmin != null) && (checkAdmin == 0)) {
				//this.app_router.navigate('admin', {trigger:true});
				window.location.hash = 'homePage';

			} else if ((checkAdmin != null) && (checkAdmin == 1)) {
				//window.location.hash = 'adminHome';
				// Future work

			}
		},

		render : function() {
			this.$el.html(loginTemplate);
		},

		loginValidate : function() {
			var loginId = document.getElementById('loginId').value;
			var loginPwd = document.getElementById('loginPWD').value;

			this.model = new login();
			this.model.set({
				"userName" : loginId
			});
			this.model.set({
				"password" : loginPwd
			});

			this.model.save(null, {
				success : function(model, responseText) {

					//alert("responseText" + JSON.stringify(responseText));                	

					if ((responseText.id != "-1") && (responseText.id != 0)
							&& (responseText.id != 'null')) {

						function setCookie(c_name, value, exdays) {
							var exdate = new Date();
							exdate.setDate(exdate.getDate() + exdays);
							var c_value = escape(value)
									+ ((exdays == null) ? "" : "; expires="
											+ exdate.toUTCString());
							document.cookie = c_name + "=" + c_value;
						}
						setCookie("userName", responseText.userName, 1);
						setCookie("id", responseText.id, 1);
						setCookie("roleId", responseText.roleId, 1);
						setCookie("checkAdmin", "0", 1);
						self.app_router.navigate('homePage', {
							trigger : true
						});
					} else {
						//alert("Invalid UserName And Password.");
						document.getElementById('invalid').setAttribute("style", "display:block;color:red");
					}
				},
				error : function(model, responseText) {
					//alert("Error."+responseText);                                             
				}

			});

		},

		openRegisterPage : function() {
			self.app_router.navigate('registerUserPage', {
				trigger : true
			});
		}

	});
	function getCookie(c_name) {
		var c_value = document.cookie;
		var c_start = c_value.indexOf(" " + c_name + "=");
		if (c_start == -1) {
			c_start = c_value.indexOf(c_name + "=");
		}
		if (c_start == -1) {
			c_value = null;
		} else {
			c_start = c_value.indexOf("=", c_start) + 1;
			var c_end = c_value.indexOf(";", c_start);
			if (c_end == -1) {
				c_end = c_value.length;
			}
			c_value = unescape(c_value.substring(c_start, c_end));
		}
		return c_value;
	}
	return loginView;

});
