zk.$package('cwf.ext');
/**
 * Window override to support bootstrap mold.
 */
cwf.ext.Window = zk.$extends(zul.wnd.Window, {

	_closableIconClass: null,
	_maximizableIconClass: null,
	_maximizedIconClass: null,
	_minimizableIconClass: null,
	
	getClosableIconClass_: function () {
		return this._closableIconClass || this.$supers('getClosableIconClass_', arguments);
	},
	
	getMaximizableIconClass_: function () {
		return this._maximizableIconClass || this.$supers('getMaximizableIconClass_', arguments);
	},
	
	getMaximizedIconClass_: function () {
		return this._maximizedIconClass || this.$supers('getMaximizedIconClass_', arguments);
	},
	
	getMinimizableIconClass_: function () {
		return this._minimizableIconClass || this.$supers('getMinimizableIconClass_', arguments);
	}
});
