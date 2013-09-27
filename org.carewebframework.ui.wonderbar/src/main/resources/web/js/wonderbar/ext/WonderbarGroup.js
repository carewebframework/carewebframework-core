zk.$package('wonderbar.ext');
/**
 * WonderbarGroup
 */
wonderbar.ext.WonderbarGroup = zk.$extends(zul.Widget, {

	label: null,
    group: true,

    getZclass: function () {
        return this._zclass != null ? this._zclass : 'cwf-wonderbar-group';
    }
});

