/* WonderbarGroup.js
*/
function (out, ac) {
	if (ac) {
		out.push('<div', this.domAttrs_(), '>', wonderbar.ext.Wonderbar.encodeXML(this.label), '</div>');
	}
}