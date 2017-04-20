zk.$package('angular.ext');
/**
 * AngularComponent
 */
angular.ext.AngularComponent = zk.$extends(zul.Widget, {

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
				src = this._src;
			
			System.import('cwf-angular-bootstrap').then(function(bootstrap) {
				System.import(src).then(function(module) {
					var appContext = new bootstrap.AppContext(module, id);
					appContext.bootstrap();
				});
			});
		}
	}
});

