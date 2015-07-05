function (out) {
	function outSpan() {
		out.push('<span>');
		for (var w = this.firstChild; w; w = w.nextSibling)
			w.redraw(out);
		out.push('</span>');
	}
	
	function outLabel() {
		var label = this.encodedLabel_();
		var class1 = this.getZclass() + "-" + this._position;
		var class2 = this.getZclass() + "-" + this._align;
		out.push('<div class="' + class1 + ' ' + class2 +'">', label, '</div>');
	}
	
	out.push('<div', this.domAttrs_(), '>');
	
	if (this._position == 'top' || this._position == 'left') {
		outLabel.apply(this);
		outSpan.apply(this);
	} else {
		outSpan.apply(this);
		outLabel.apply(this);
	}
	
	out.push('</div>');
}