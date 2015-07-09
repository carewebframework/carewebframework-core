function (out) {
	function outChildren() {
		out.push('<div id="', this.uuid, '-cave" >');
		for (var w = this.firstChild; w; w = w.nextSibling)
			w.redraw(out);
		out.push('</div>');
	}
	
	function outLabel() {
		var label = this.encodedLabel_();
		var style = this._labelStyle ? ' style="' + this._labelStyle + '"' : '';
		var scls = this._labelSclass ? ' ' + this._labelSclass : '';
		out.push('<div id="', this.uuid, '-lbl" class="', zcls, '-lbl', scls, '"', style, '>', label, '</div>');
	}
	
	var zcls = this.getZclass();
	out.push('<div', this.domAttrs_(), '>');
	var clazz = zcls + "-" + this._position + ' ';
	clazz += zcls + "-" + this._align;
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