/* wonderbar.js
*/
function (out) {
	var zcls = this.getZclass(),
		uuid = this.uuid;
	out.push('<div id="', uuid, '" class="', zcls, '">');
	out.push('<input id="', uuid, '-real" class="', zcls, '-inp" type="text" ', this.domAttrs_({id: 1, domClass: 1}), '>');
	out.push('<div id="', uuid, '-arrow" class="', zcls, '-arrow ', zcls, '-arrow-down"></div>');
	out.push('</div>');
}