zk.$package('wonderbar.ext');
/**
 * WonderbarItem
 */
wonderbar.ext.WonderbarItems = zk.$extends(zul.Widget, {

    getZclass: function () {
        return this._zclass != null ? this._zclass : 'cwf-wonderbar-items';
    },

    redraw: function (out) {
    }
});

