/* wonderbar.js
*/
function (out) {
	var zcls = this.getZclass(),
		uuid = this.uuid;
	out.push('<i id="', uuid, '" class="', zcls, '" ', this.domAttrs_({text:true}),'>');
	out.push('<input id="', uuid, '-real" class="', zcls, '-inp"', this.textAttrs_(), '>');
	out.push('<i id="', uuid, '-arrow" class="', zcls, '-arrow ', zcls, '-arrow-down"></i>');
	out.push('</i>');
}