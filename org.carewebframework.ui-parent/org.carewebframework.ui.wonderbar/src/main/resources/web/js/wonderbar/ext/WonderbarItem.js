zk.$package('wonderbar.ext');
/**
 * WonderbarItem
 */
wonderbar.ext.WonderbarItem = zk.$extends(zul.Widget, {
    keyIndex: null,

    label: null,

    searchTerm: null,

    uniqueKey: null,

    uniquePriority: null,

    value: null,

    category: null,

    selectable: true,

    getZclass: function () {
        return this._zclass != null ? this._zclass : 'cwf-wonderbar-item';
    },

    _sameCategory: function (sib) {
    	var cat = !sib || !sib.category ? null : sib.category;
    	return cat === this.category;
    }

});

