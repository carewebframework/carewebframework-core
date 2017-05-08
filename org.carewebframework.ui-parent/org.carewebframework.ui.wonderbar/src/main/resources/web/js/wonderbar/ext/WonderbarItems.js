zk.$package('wonderbar.ext');
/**
 * WonderbarItem
 */
wonderbar.ext.WonderbarItems = zk.$extends(cwf.Widget, {

    getZclass: function () {
        return this._zclass != null ? this._zclass : 'cwf-wonderbar-items';
    },

    redraw: function (out) {
    }
});

