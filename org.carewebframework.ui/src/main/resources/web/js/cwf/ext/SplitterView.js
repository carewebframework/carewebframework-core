zk.$package('cwf.ext');

var SplitterView =
cwf.ext.SplitterView = zk.$extends(zul.Widget, {
	_horizontal: true,

$define: {
	horizontal: function(horizontal) {
		this.rerender();
	},
	resize: function() {
		this.resize();
	}
},
getZclass: function() {
	return this._zclass == null ? 'cwf-splitterview' : this._zclass;
},
//-- super --//
onChildAdded_: function(child) {
	this.$supers('onChildAdded_', arguments);
	child.setHorizontal(this._horizontal);
	this.resize();
},
onChildRemoved_: function(child) {
	this.$supers('onChildRemoved_', arguments);
	if (!this.childReplacing_)
		this.resize();
},
bind_: function() {
	this.$supers(SplitterView, 'bind_', arguments);
	zWatch.listen({onSize: this});
	this.resize(25);
},
unbind_: function() {
	zWatch.unlisten({onSize: this});
	this.$supers(SplitterView, 'unbind_', arguments);
},
_getPaneSize: function(wgt, hor) {
	var n = !wgt ? null : wgt.$n('real');
	return !n ? 0 : hor ? n.offsetWidth : n.offsetHeight;
},
beforeMinFlex_: function(o) {
	var hor = o == 'w';
	var val = 0;
	var k = this.nChildren;
	for (var j = 0; j < k; ++j) {
		var pane = this.getChildAt(j);
		var sz = this._getPaneSize(pane, hor);

		if (sz > val)
			val = sz;
	}
	return val;
},
//@Override, region with vflex/hflex, must wait flex resolved then do resize
afterChildrenFlex_: function() {
	//region's min vflex/hflex resolved and try the border resize
	//@see #_resize
	if (this._isOnSize)
		this._resize(true);
},
/**
 * Re-sizes this layout component.
 */
resize: function(delay) {
	if (this.desktop)
		if (delay >= 0) {
			var _this = this;
			setTimeout(function(){_this.resize();}, delay);
		} else
			this._resize();
},
_resize: function(isOnSize) {
	this._isOnSize = this._isOnSize || isOnSize;
	
	if (this._sizing || !this.isRealVisible())
		return;

	this._sizing = true;
	
	var el = this.$n(),
		view = {
			offset: 0,
			width: el.offsetWidth,
			height: el.offsetHeight
		};

	var k = this.nChildren;

	for (var j = 0; j < k; ++j) {
		var pane = this.getChildAt(j);
		pane.setHorizontal(this._horizontal);

		if (zk(pane.$n()).isVisible()) {
			this._positionPane(pane, view);
		}
	}
	this._isOnSize = false; // reset
	this._sizing = false;
},
_positionPane: function(pane, view) {
	var ambit = pane._ambit(view),
		real = pane.$n('real'),
		$real = zk(real),
		hor = this._horizontal;

	if (!pane._nextSibling()) {
		var rm = (hor ? view.width : view.height) - view.offset;
		ambit.size += rm;
	}
	ambit.size = Math.max(0, ambit.size);

	var w = hor ? ambit.size : view.width,
		h = hor ? view.height : ambit.size;

	w = $real.revisedWidth(w);
	h = $real.revisedHeight(h);

	zk.copy(real.style, {
		left: jq.px(hor ? ambit.offset : 0),
		top: jq.px(hor ? 0 : ambit.offset),
		width: jq.px0(w),
		height: jq.px0(h)
	});

	if (!this._ignoreResize(real)) {
		var cave = pane.$n('cave'),
			$cave = zk(cave),
			cap = pane.$n('cap');

		w = $cave.revisedWidth(w);

		if (cap)
			h = Math.max(0, h - cap.offsetHeight);

		h = $cave.revisedHeight(h);

		zk.copy(cave.style, {
			width: jq.px0(w),
			height: jq.px0(h)
		});

		if (!this._isOnSize)
			zUtl.fireSized(pane);
	}
},
_ignoreResize : function(el) {
	var w = el.offsetWidth,
		h = el.offsetHeight;

	if (el._lastSize && el._lastSize.width == w && el._lastSize.height == h) {
		return true;
	} else {
		el._lastSize = {width: w, height: h};
		return false;
	}
},
//zWatch//
onSize: function() {
	this._resize(true);
}
});

