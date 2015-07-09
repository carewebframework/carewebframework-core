function (out) {
	function outChildren() {
		out.push('<span>');
		for (var w = this.firstChild; w; w = w.nextSibling)
			w.redraw(out);
		out.push('</span>');
	}
	
	function outLabel() {
		var label = this.encodedLabel_();
		var style = this._labelStyle ? ' style="' + this._labelStyle + '"' : '';
		var clazz = this._labelSclass ? ' class="' + this._labelSclass + '"' : '';
		out.push('<div id="', this.uuid, '-lbl', '"', clazz, style, '>', label, '</div>');
	}
	
	out.push('<div', this.domAttrs_(), '>');
	var clazz = this.getZclass() + "-" + this._position + ' ';
	clazz += this.getZclass() + "-" + this._align;
	out.push('<span class="', clazz, '">')
	
	if (this._position == 'top' || this._position == 'left') {
		outLabel.call(this);
		outChildren.call(this);
	} else {
		outChildren.call(this);
		outLabel.call(this);
	}
	
	out.push('</span></div>');
}