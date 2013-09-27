zk.$package('cwf.ext');
/**
 * Menu extension
 */
cwf.ext.MenuEx = zk.$extends(zul.menu.Menu, {

	$define : {
		color : function(color) {
			this.rerender();
		}
	},

	doClick_ : function(evt) {
		evt.opts.toServer = true;
		this.$supers('doClick_', arguments);
	},

	_getArrowWidth : function() {
		return this.getZclass() == 'z-menuitem' ? 0 : 20;
	},

	getZclass : function() {
		return this._zclass ? this._zclass : 'z-menu';
	},

	bind_: function(){
		this.$supers(cwf.ext.MenuEx, 'bind_', arguments);
		var a = this.getAnchor_();
		var b = this.getButton_();

		if (a)
			jq(a).css('color', this._color);

		if (b)
			jq(b).css('color', this._color);
	}
});
