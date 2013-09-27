zk.$package('wonderbar.ext');
/**
 * WonderbarDefaults
 */
wonderbar.ext.WonderbarDefaults = zk.$extends(zul.Widget, {

    getZclass: function () {
        return this._zclass != null ? this._zclass : 'cwf-wonderbar-defaults';
    },

    redraw: function (out) {
    }
});

