zk.$package('cwf.ext');
/**
 * Window override to support bootstrap mold.
 */
cwf.ext.Window = zk.$extends(zul.wnd.Window, {

	_closableIconClass: null,
	_maximizableIconClass: null,
	_maximizedIconClass: null,
	_minimizableIconClass: null,
	_minimizedIconClass: null,
	
	$define: {
		minimized: function (minimized, fromServer) {
			if (this._maximized)
				this.setMaximized(false);

			var min = jq(this.$n('min')),
				down = this.getMinimizableIconClass_(),
				up = this.getMinimizedIconClass_();
			
			if (min) {
				if (minimized) {
					min.children('.' + down).removeClass(down).addClass(up);
				} else {
					min.children('.' + up).removeClass(up).addClass(down);
				}
				if (!fromServer) {
					this._visible = false;
					this.zsync();
					var s = this.$n().style,
						p = this._getPosByParent(this, s.left, s.top); 
					this.fire('onMinimize', {
						left: p[0],
						top: p[1],
						width: s.width,
						height: s.height,
						minimized: minimized
					});
				}
			}
		}
	},
	
	_getPosByParent: function(wgt, left, top) {
		var pos = wgt._position,
			left = zk.parseInt(left),
			top = zk.parseInt(top),
			x = 0, y = 0;
		if (pos == 'parent') {
			var vp = zk(wgt.$n()).vparentNode();
			if (vp) {
				var ofs = zk(vp).revisedOffset();
				x = ofs[0];
				y = ofs[1];
			}
		}
		return [jq.px(left - x), jq.px(top - y)];
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
	
	getMinimizedIconClass_: function () {
		return this._minimizedIconClass || this.getMinimizableIconClass_();
	}
	
});
