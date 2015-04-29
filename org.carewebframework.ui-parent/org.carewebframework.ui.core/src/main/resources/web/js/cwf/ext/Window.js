zk.$package('cwf.ext');
/**
 * Window override to support bootstrap mold.
 */
cwf.ext.Window = zk.$extends(zul.wnd.Window, {

	_closableIconClass: null,
	_maximizableIconClass: null,
	_maximizedIconClass: null,
	_minimizableIconClass: null,
	_default: null,
	
	_isDefault: function() {
		return this._default;
	},
	
	domClass_: function (no) {
		var zclass = this._zclass,
			sclass = this._sclass;
		
		if (this._default && (!zclass || zclass == 'z-window')) {
			this._zclass = 'panel';
			this._sclass = sclass || 'panel-primary';
		}
		
		var result = this.$supers('domClass_', arguments);
		this._zclass = zclass;
		this._sclass = sclass;
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
	}
	
});
