'use strict';

define('cwf-login', ['jquery', 'lodash', 'css!cwf-login-css.css', 'css!bootstrap-css.css'], function($) {
	return {
	
		init: function(timeout, logoutUrl) {
			this._timeout = timeout;
			this._logoutUrl = logoutUrl
		},
		
		resetTimeout: function() {
			var self = this;
			
			if (this._timer) {
				this._timer.clearTimeout();
			}
			
			this._timer = setTimeout(function() {
				$(location).attr('href', self._logoutUrl);				
			}, this._timeout);
		}
	};
});