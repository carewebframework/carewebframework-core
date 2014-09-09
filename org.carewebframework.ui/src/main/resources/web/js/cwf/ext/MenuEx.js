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
		if (this.isTopmost())
			this.$supers('doClick_', arguments);
		else
			this.fireX(new zk.Event(this, 'onClick', evt.data));
	},

	getZclass : function() {
		return this._zclass ? this._zclass : 'z-menu';
	},
	
	bind_: function(){
		this.$supers(cwf.ext.MenuEx, 'bind_', arguments);
		var a = this.getAnchor_();

		if (a && this._color) {
			jq(a).children('.z-menu-text,.z-menuitem-text').css('color', this._color);
			
			if (this.menupopup) {
				this.menupopup._style = '';
				this.menupopup.rerender();
			}
		}

	}
});
