zk.$package('cwf.ext');

var SplitterPane =
cwf.ext.SplitterPane = zk.$extends(zul.Widget, {
	_horizontal: true, // horizontal orientation
	_splittable: true,
	_border: 'normal',
	_minsize: 0,

	$define: {
		/**
		 * Sets the border (either none or normal).
		 *
		 * @param String border the border. If null or '0', 'none' is assumed.
		 */
		/**
		 * Returns the border.
		 * <p>
		 * The border actually controls what CSS class to use: If border is null, it
		 * implies 'none'.
		 *
		 * <p>
		 * If you also specify the CSS class ({@link #setSclass}), it overwrites
		 * whatever border you specify here.
		 *
		 * <p>
		 * Default: 'normal'.
		 * @return String
		 */
		border: function(border) {
			if (!border || '0' == border)
				this._border = border = 'none';

			if (this.desktop)
				(this.$n('real') || {})._lastSize = null;

			this.updateDomClass_();
		},
		/**
		 * Sets the title.
		 * @param String title
		 */
		/**
		 * Returns the title.
		 * <p>Default: null.
		 * @return String
		 */
		title: function(title) {
			this.rerender();
		},
		/**
		 * Sets whether to enable the split functionality.
		 * @param boolean splittable
		 */
		/**
		 * Returns whether the split functionality is enabled.
		 * <p>
		 * Default: false.
		 * @return boolean
		 */
		splittable: function(splittable) {
			this._getSplitter();
		},
		/**
		 * Sets the orientation (true = horizontal, false = vertical).
		 * @param boolean horizontal
		 */
		/**
		 * Returns the orientation (true = horizontal, false = vertical).
		 * <p>
		 * Default: true.
		 * @return boolean
		 */
		horizontal: function(horizontal) {
			this._destroySplitter();
			//this.parent.rerender();
		},
		/**
		 * Sets the minimum size of the resizing element.
		 * @param int minsize
		 */
		/**
		 * Returns the minimum size of the resizing element.
		 * <p>
		 * Default: 0.
		 * @return int
		 */
		minsize: null
	},
	domClass_: function(no) {
		var scls = this.$supers('domClass_', arguments);
		if (!no || !no.zclass) {
			var added = 'normal' == this.getBorder() ? '' : this.getZclass() + '-noborder';
			if (added) scls += (scls ? ' ': '') + added;
		}
		return scls;
	},
	getZclass: function() {
		return (this._zclass == null ? 'cwf-splitterpane' : this._zclass) + (this._horizontal ? '-horz' : '-vert');
	},
	setWidth: function(width, noresize) {
		this._setDims(width, null, noresize);
		return this;
	},
	setHeight: function(height, noresize) {
		this._setDims(null, height, noresize);
		return this;
	},
	_setDims: function(width, height, noresize) {
		this._width = width;
		this._height = height;
		var real = this.$n('real');

		if (real) {
			real.style.width = width || '';
			real.style.height = height || '';
			real._lastSize = null;

			if (!noresize)
				this._resizeParent();
		}
	},
	_isRelative: function() {
		var sz = this._horizontal ? this._width : this._height;
		return sz && sz.indexOf('%') > 0;
	},
	_setRawSize: function(sz, noresize) {
		if (this._isRelative()) {
			var psz = this.parent.$n()[this._horizontal ? 'offsetWidth' : 'offsetHeight'];
			sz = this._toPercent(sz, psz);
		} else {
			sz = jq.px(sz);
		}

		if (this._horizontal)
			this.setWidth(sz, noresize);
		else
			this.setHeight(sz, noresize);
	},
	setVisible: function(visible) {
		if (this._visible != visible) {
			this.$supers('setVisible', arguments);
			var real = this.$n('real');
			if (real) {
				if (this._visible) {
					jq(real).show();
				} else {
					jq(real).hide();
				}
				this._resizeParent();
			}
		}
		return this;
	},
	updateDomClass_: function() {
		if (this.desktop) {
			var real = this.$n('real');
			if (real) {
				real.className = this.domClass_();
				this._resizeParent();
			}
		}
	},
	updateDomStyle_: function() {
		if (this.desktop) {
			var real = this.$n('real');
			if (real) {
				zk(real).clearStyles().jq.css(jq.parseStyle(this.domStyle_()));
				this._resizeParent();
			}
		}
	},
	onChildAdded_: function(child) {
		this.$supers('onChildAdded_', arguments);
		// reset
		(this.$n('real') || {})._lastSize = null;
		if (this.parent && this.desktop) {
			if (this.parent.isRealVisible({dom: true}))
				this._resizeParent();
		}
	},
	onChildRemoved_: function(child) {
		this.$supers('onChildRemoved_', arguments);

		// reset
		(this.$n('real') || {})._lastSize = null;
		if (this.parent && this.desktop && !this.childReplacing_) {
			if (this.parent.isRealVisible({dom: true}))
				this._resizeParent();
		}
	},
	_nextSibling: function() {
		for (var sib = this; sib = sib.nextSibling;) {
			if (sib.isVisible())
				return sib;
		}

		return null;
	},
	_initSplitter: function(recreate) {
		var split = this._getSplitter();

		if (recreate || !split)
			this._destroySplitter();

		if (split && !this._drag)
			this._drag = new zk.Draggable(this, split, {
				constraint: this._horizontal ? 'horizontal': 'vertical',
				ghosting: SplitterPane._ghosting,
				snap: SplitterPane._snap,
				zIndex: 12000,
				overlay: true,
				initSensitivity: 0,
				ignoredrag: SplitterPane._ignoredrag,
				endeffect: SplitterPane._endeffect
			});
	},
	_destroySplitter: function() {
		if (this._drag) {
			this._drag.destroy();
			this._drag = null;
		}
	},
	_getSplitter: function() {
		var split = this.$n('split'),
			visible = this._splittable && this._nextSibling();

		if (split)
			jq(split)[visible ? 'show' : 'hide']();

		return visible ? split : null;
	},
	_resizeSplitter: function(ambit) {
		var split = this._getSplitter();

		if (split) {
			var dims;

			if (this._horizontal) {
				dims = {
					left: jq.px0(ambit.offset + ambit.size - (split.offsetWidth / 2)),
					top: jq.px0(0)
				};
			} else {
				dims = {
					left: jq.px0(0),
					top: jq.px0(ambit.offset + ambit.size - (split.offsetHeight / 2))
				};
			}
			zk.copy(split.style, dims);
		}

		return ambit;
	},
	_resizeParent: function() {
		if (this.parent)
			this.parent.resize();
	},
	bind_: function(){
		this.$supers(SplitterPane, 'bind_', arguments);

		if (!this.isVisible())
			this.$n().style.display = 'none';
		
		this._initSplitter(true);
	},
	unbind_: function() {
		this._destroySplitter();
		this.$supers(SplitterPane, 'unbind_', arguments);
	},
	// Returns the ambit of this pane.
	_ambit: function(view) {
		var pn = this.parent.$n(),
			real = this.$n('real'),
			hor = this._horizontal,
			size = (hor ? this.getWidth() : this.getHeight()) || '',
			psize = hor ? pn.offsetWidth : pn.offsetHeight,
			rsize = hor ? real.offsetWidth : real.offsetHeight,
			pct;
		var ambit = {
			offset: view.offset,
			size: (pct = this._fromPercent(size, psize)) != null ? pct : rsize
		};
		if (zk.ie == 9 && hor && !this._width)
			ambit.size++; // B50-ZK-641: text wrap in IE

		view.offset += ambit.size;
		return this._resizeSplitter(ambit);
	},
	_fromPercent: function(v, s) {
		var pct = !v ? 0 : v.indexOf('%');
		return pct < 1 ? null : Math.max(
				Math.floor(s * zk.parseInt(v.substring(0, pct)) / 100), 0);
	},
	_toPercent: function(v, s) {
		return (s == 0 ? 0 : v * 100.0 / s) + '%';
	},
	titleRenderer_: function(out) {
		if (this._title) {
			var uuid = this.uuid,
				zcls = this.getZclass();

			out.push('<div id="', uuid, '-cap" class="', zcls, '-header">', zUtl.encodeXML(this._title), '</div>');
		}
	},
	getFirstChild: function() {
		return this.firstChild;
	}
},
{	_ignoredrag: function(dg, pointer, evt) {
		var target = evt.domTarget,
			wgt = dg.control,
			hor = wgt._horizontal,
			split = wgt.$n('split');

		if (!target || target != split || !wgt._splittable)
			return true;

		dg._control2 = wgt._nextSibling();

		var	real = wgt.$n('real'),
			sib = dg._control2,
			sreal = sib.$n('real'),
			$real = zk(real),
			ofs = $real.cmOffset(),
			pbw = $real.padBorderWidth(),
			pbh = $real.padBorderHeight(),
			maxs = hor ? real.offsetWidth + sreal.offsetWidth :
				real.offsetHeight + sreal.offsetHeight;

		dg._rootoffs = {
			maxs: maxs - sib._minsize - ((hor ? pbw : pbh) * 2),
			mins: wgt._minsize,
			top: ofs[1] + pbh,
			left : ofs[0] + pbw,
			right : real.offsetWidth,
			bottom: real.offsetHeight,
			pbh: pbh,
			pbw: pbw
		};
		return false;
	},
	_endeffect: function(dg, evt) {
		var wgt = dg.control,
			real = wgt.$n('real'),
			sib = dg._control2,
			sreal = sib.$n('real');

		if (wgt._horizontal) {
			var w = real.offsetWidth;
			wgt._setRawSize(dg._point[0], true);
			sib._setRawSize(sreal.offsetWidth - real.offsetWidth + w, true);
		} else {
			var h = real.offsetHeight;
			wgt._setRawSize(dg._point[1], true);
			sib._setRawSize(sreal.offsetHeight - real.offsetHeight + h, true);
		}
		wgt.$n().style.zIndex = '';
		dg._rootoffs = dg._point = dg._control2 = null;
		wgt.parent.resize();
		wgt.fire('onSize', zk.copy({
			width: real.style.width,
			height: real.style.height
		}, evt.data));
		sib.fire('onSize', zk.copy({
			width: sreal.style.width,
			height: sreal.style.height
		}, evt.data));
		jq(document.body).css('overflow', '');
	},
	_snap: function(dg, pointer) {
		var wgt = dg.control,
			x = pointer[0],
			y = pointer[1],
			b = dg._rootoffs, w, h;
		if (wgt._horizontal) {
			if (x > b.maxs + b.left)
				x = b.maxs + b.left;
			if (x < b.mins + b.left)
				x = b.mins + b.left;
			w = x - b.left;
			h = y;
		} else {
			if (y > b.maxs + b.top)
				y = b.maxs + b.top;
			if (y < b.mins + b.top)
				y = b.mins + b.top;
			w = x;
			h = y - b.top;
		}
		dg._point = [w + b.pbw, h + b.pbh];
		return [x, y];
	},
	_ghosting: function(dg, ofs, evt) {
		var el = dg.node,
			ghost = jq(el).clone();
		ghost[0].id = 'zk_layoutghost';
		ghost.addClass('cwf-splitterpane-splt-ghost')
			.css('width', jq.px(el.offsetWidth))
			.css('height', jq.px(el.offsetHeight))
			.css('top', jq.px(ofs[1]))
			.css('left', jq.px(ofs[0]));
		jq(document.body).prepend(ghost).css('overflow','hidden');
		return ghost[0];
	}
});
