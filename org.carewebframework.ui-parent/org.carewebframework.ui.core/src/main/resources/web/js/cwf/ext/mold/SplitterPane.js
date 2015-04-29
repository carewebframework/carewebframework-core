/**
 * Mold: SplitterPane.js
 */
function (out) {
	var uuid = this.uuid,
		zcls = this.getZclass();
	out.push('<div id="', uuid, '"><div id="', uuid, '-real" ',
			this.domAttrs_({id: 1}), '>');

	this.titleRenderer_(out);
	out.push('<div id="', uuid, '-cave" class="', zcls, '-body">');

	for (var child = this.firstChild; child; child = child.nextSibling)
		child.redraw(out);

	out.push('</div></div><div id="', uuid, '-split" class="', zcls, '-splt"></div></div>');
}