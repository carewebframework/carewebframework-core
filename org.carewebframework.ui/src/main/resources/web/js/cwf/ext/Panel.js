zk.$package('cwf.ext');
/**
 * Panel override to support bootstrap mold.
 */
cwf.ext.Panel = zk.$extends(zul.wnd.Panel, {

	_closableIconClass: null,
	_maximizableIconClass: null,
	_maximizedIconClass: null,
	_minimizableIconClass: null,
	_collapseOpenIconClass: null,
	_collapseCloseIconClass: null,
		
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
	},
	
	getCollapseOpenIconClass_: function () {
		return this._collapseOpenIconClass || this.$supers('getCollapseOpenIconClass_', arguments);
	},
	
	getCollapseCloseIconClass_: function () {
		return this._collapseCloseIconClass || this.$supers('getCollapseCloseIconClass_', arguments);
	}
	
});
