zk.$package('wonderbar.ext');
/**
 * WonderbarGroup
 */
wonderbar.ext.WonderbarGroup = zk.$extends(cwf.Widget, {

	label: null,
    group: true,

    getZclass: function () {
        return this._zclass != null ? this._zclass : 'cwf-wonderbar-group';
    }
});

