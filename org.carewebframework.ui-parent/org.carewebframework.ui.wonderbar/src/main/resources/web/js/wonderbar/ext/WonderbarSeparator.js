zk.$package('wonderbar.ext');
/**
 * WonderbarSeparator
 */
wonderbar.ext.WonderbarSeparator = zk.$extends(zul.Widget, {
    separator: true,

    getZclass: function () {
        return this._zclass != null ? this._zclass : 'cwf-wonderbar-separator';
    }
});

