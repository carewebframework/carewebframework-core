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
		out.push('<div id="', this.uuid, '-lbl', '"', style, '>', label, '</div>');
	}
	
	out.push('<div', this.domAttrs_(), '>');
	var cls = this.getZclass() + "-" + this._position + ' ';
	cls += this.getZclass() + "-" + this._align;
	out.push('<span class="' + cls + '">')
	
	if (this._position == 'top' || this._position == 'left') {
		outLabel.call(this);
		outChildren.call(this);
	} else {
		outChildren.call(this);
		outLabel.call(this);
	}
	
	out.push('</span></div>');
}