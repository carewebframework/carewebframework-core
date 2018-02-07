'use strict';

define('cwf-shell', ['cwf-core', 'fujion-core', 'cwf-shell-css'], function(cwf, fujion) { 
	
	cwf.widget = cwf.widget || {};
	
	/**
	 * Widgets for shell and extended shell.
	 */
	cwf.widget.Shell = fujion.widget.Div.extend({});
	
	cwf.widget.ShellEx = cwf.widget.Shell.extend({});
	
	return cwf.widget;
});