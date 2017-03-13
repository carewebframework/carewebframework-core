'use strict';

define('cwf-login', ['jquery', 'lodash', 'css!cwf-login-css.css', 'css!bootstrap-css.css'], function($) {
	return {
	
		init: function(timeout, logoutUrl, required, disabled) {
			this._timeout = timeout;
			this._logoutUrl = logoutUrl;
			this._required = required;
			this._logo = $('#cwf-title-img').attr('src');
			this._infoTitle = $('#cwf-info-title').text();
			this._infoContent = $('#cwf-info-content').text();
			
			$('body').on('click keydown', this.resetTimeout.bind(this));
			$('#cwf-form').on('submit', this.submitHandler.bind(this));
			$('#cwf-domain').on('change', this.domainHandler.bind(this));
			$('#cwf-alternate').one('click', this.alternateHandler.bind(this));
			this.domainHandler();
			this.resetTimeout();
			
			if (disabled) {
				$('#cwf-login-root').addClass('cwf-login-disabled');
				$('#cwf-error').text(disabled);
			}
			
			$('body').show();
		},
		
		resetTimeout: function() {
			var self = this;
			
			if (this._timer) {
				clearTimeout(this._timer);
			}
			
			this._timer = setTimeout(function() {
				$(location).attr('href', self._logoutUrl);				
			}, this._timeout);
		},
		
		submitHandler: function(event) {
			var username = this.validateInput('#cwf-username', event),
				password = this.validateInput('#cwf-password', event);
				
			if (!username || !password) {
				return;
			}
			
			var domain = $('#cwf-domain').val();
			username = domain ? domain + '\\' + username : username;
			$('#cwf-username-real').val(username);
		},
		
		domainHandler: function() {
			var domain = $('#cwf-domain option:selected'),
				logo = domain.attr('data-logo') || this._logo,
				info = domain.attr('data-info') || this._infoContent,
				header = domain.attr('data-header') || this._infoTitle;
			
			$('#cwf-title-img').attr('src', logo);
			$('#cwf-domain-name').text(domain.text());
			$('#cwf-info-title').text(header);
			$('#cwf-info-content').text(info);
		},
		
		alternateHandler: function(event) {
			$(event.target).parent().hide();
			$('#cwf-domains').show();
		},
		
		validateInput: function(id, event) {
			if (event.isDefaultPrevented()) {
				return;
			}
			
			var input = $(id),
				value = input.val();
			
			if (!value) {
				event.preventDefault();
				this.showError(this._required);
				input.focus();
			}
			
			return value;
		},
		
		showError: function(message) {
			$('#cwf-error').text(message);
		}
	};
});