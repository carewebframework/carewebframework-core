zk.$package('cwf.ext');
/**
 * Icon picker
 */
cwf.ext.IconPicker = zk.$extends(zul.inp.Bandbox, {
	getZclass: function () {
		var zcs = this._zclass;
		return zcs != null ? zcs: 'z-bandbox';
	}
	
});
