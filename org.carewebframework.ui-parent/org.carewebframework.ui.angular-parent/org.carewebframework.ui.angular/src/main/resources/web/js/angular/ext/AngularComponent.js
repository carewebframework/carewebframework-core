zk.$package('angular.ext');
/**
 * AngularComponent
 */
angular.ext.AngularComponent = zk.$extends(zul.Widget, {

	_ngInvoke : [],
	
	_src : null,

	$define: {
		src : function(v) {
			if (this.desktop){
				this.rerender();
			}
		}
	},
	
    getZclass: function () {
        return this._zclass != null ? this._zclass : 'cwf-angular';
    },

	bind_: function () {
	    this.$supers(angular.ext.AngularComponent, 'bind_', arguments);
	    
		if (this._src) {
			var id = '#'+ this.uuid,
				src = this._src,
				self = this,
				ngFlush = this.ngFlush.bind(this);
			
			System.import('cwf-angular-bootstrap').then(function(bootstrap) {
				System.import(src).then(function(module) {
					self._appContext = new bootstrap.AppContext(module, id);
					self._appContext.bootstrap().then(ngFlush);
				});
			});
		}
	},
	
	unbind_: function () {
	    this.$supers(angular.ext.AngularComponent, 'unbind_', arguments);
	    this._destroy();
	},
	
	_destroy: function () {
		this._appContext ? this._appContext.destroy() : null;
		this._appContext = null;
	},
	
	isLoaded: function() {
		return this._appContext && this._appContext.isLoaded();
	},
	
	ngInvoke: function(functionName, args) {
		if (this.isLoaded()) {
			return this._appContext.invoke(functionName, args);
		} else {
			this._ngInvoke.push({functionName: functionName, args: args});
		}
	},
	
	ngFlush: function () {
		while (this._ngInvoke.length) {
			var invk = this._ngInvoke.shift();
			this.ngInvoke(invk.functionName, invk.args);
		}
	}
});

