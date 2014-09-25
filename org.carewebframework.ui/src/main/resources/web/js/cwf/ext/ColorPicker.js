zk.$package('cwf.ext');
/**
 * Color picker
 */
cwf.ext.ColorPicker = zk.$extends(zul.inp.Bandbox, {
	_selcolor : null,
	
	_showtext: null,
	
	$define: {
		selcolor : function(v) {
			this._selcolor = v;
			this._updateStyle();
		},
		
		showtext : function(v) {
			this._showtext = v;
		}
	},
	
	getZclass: function () {
		var zcs = this._zclass;
		return zcs != null ? zcs: 'z-bandbox';
	},
	
	_updateStyle: function() {
		this.__updateStyle(this._showtext, this._selcolor);
		this.__updateStyle(!this._showtext, 'inherit')
	},
	
	__updateStyle: function(w, c) {
		jq(this).find('.z-bandbox-' + (w ? 'button' : 'input')).css('background-color', c);
	},
	
	bind_: function () {
		this.$supers(cwf.ext.ColorPicker, 'bind_', arguments);
		this._updateStyle();
	}
});
