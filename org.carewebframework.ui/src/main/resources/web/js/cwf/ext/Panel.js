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
	_default: null,
		
	_isDefault: function() {
		return this._default;
	},
	
	domClass_: function (no) {
		var zclass = this._zclass;
		
		if (this._default && (!zclass || zclass == 'z-panel')) {
			this._zclass = 'panel';
		}
		
		var result = this.$supers('domClass_', arguments);
		this._zclass = zclass;
		return result;
	},
	
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
