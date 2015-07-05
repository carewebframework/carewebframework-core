zk.$package('cwf.ext');

/**
 * Associates a label with one or more components.
 */
cwf.ext.LabeledElement = zk.$extends(zul.Widget, {
	_label: '',
	
	_position: 'top',
	
	_align: 'start',

	$define: {
		label: _zkf = function () {
			if (this.desktop)
				this.rerender();
		},
		
		position: _zkf,
		
		align: _zkf
	},

	getZclass: function() {
		return this._zclass == null ? 'cwf-labeledelement' : this._zclass;
	},
	
	/**
	 * Returns the encoded label.
	 * @return String
	 * @see zUtl#encodeXML
	 */
	encodedLabel_: function () {
		return zUtl.encodeXML(this.getLabel());
	}
});
