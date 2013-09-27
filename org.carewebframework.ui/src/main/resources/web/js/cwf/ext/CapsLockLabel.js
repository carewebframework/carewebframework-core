zk.$package('cwf.ext');
/**
 * Label that warns when caps lock is activated.
 */
cwf.ext.CapsLockLabel = zk.$extends(zul.wgt.Label, {
	_textbox : null,

	$define: {
		textbox : function(v) {
			this.doBind(this._textbox, false);
			this.doBind(v, true);
			this._textbox = v;
			jq(this).hide();
		}
	},

	doBind : function(tb, bind) {
		if (!tb)
			return;

		var t = jq(tb);

		if (bind) {
			t.bind('keypress', this.keypress);
			t.data('cwf_capslocklabel', this);
		} else {
			t.unbind('keypress', this.keypress);
			t.removeData('cwf_capslocklabel');
		}
	},

	keypress : function(e) {
		var character = String.fromCharCode(e.keyCode | e.which);
		var uc = character.toUpperCase();
		var lc = character.toLowerCase();
		var me = jq(this).data('cwf_capslocklabel');

	    if (!me._visible || uc === lc)
	        return;

	    // SHIFT doesn't usually give us a lowercase character. Check for this
	    // and for when we get a lowercase character when SHIFT is enabled.

	    var shifton = e.shiftKey ? true : e.modifiers ? !!(e.modifiers & 4) : false;

	    if ((shifton && lc === character) || (!shifton && uc === character))
	        jq(me).show();
	    else
	        jq(me).hide();
	}

});
