/* WonderbarItem.js
*/
function (out, ac) {
	if (!ac) {
		return;
	}

	var zcls = this.getZclass();
	var cls = zcls + (this._first ? ' ' + zcls + '-first' : '') + (this._last ? ' ' + zcls + '-last' : '');

	out.push('<li class="', cls, '" ', this.domAttrs_({domClass: 1}), '><a>');

	if (this.choiceNumber > 0) {
		out.push('<span class="', zcls, '-numbered">', this.choiceNumber, ') </span>');
	}

	out.push(wonderbar.ext.Wonderbar.encodeXML(this.label));

	if (this.category && this._first) {
		out.push('<div class="', zcls, '-category">', wonderbar.ext.Wonderbar.encodeXML(this.category), '</div>');
	}

	out.push('</a></li>');
}