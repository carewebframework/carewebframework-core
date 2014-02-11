zk.$package('cwf.ext');
/**
 * Message Window Component
 */
cwf.ext.MessageWindow = zk.$extends(zul.wgt.Div, {
	_duration : 8000,

	$define : {
		duration : null
	},

	_show : function(options) {
		var cmp = jq(this);
		cmp.show();
		var zcls = this.getZclass();
		var real = jq(this.$n('real'));
		var cave = jq('<div>').addClass(zcls + '-cave');
		var cap = jq('<div>').addClass('z-toolbar ' + zcls + '-cap').appendTo(
				cave);
		
		if (options.caption)
			jq('<span>').addClass(zcls + '-title').text(options.caption).appendTo(cap);
		
		if (options.action) {
			jq('<span>').addClass(zcls + '-btn').addClass(zcls + '-btn-action').appendTo(cap).bind('click', options.action);
		}
		
		var btn = jq('<span>').addClass(zcls + '-btn').addClass(zcls + '-btn-close').appendTo(cap);
		var msg = jq('<div>').addClass(zcls + '-msg').appendTo(cave);

		if (options.tag)
			cave.data('tag', options.tag);

		if (options.color)
			cave.css('background-color', options.color);

		if (jq.trim(options.message).substring(0, 6) === '<html>')
			msg.append(options.message);
		else
			msg.addClass(zcls + '-text').text(options.message);

		cave.appendTo(real);
		cwf.ext.MessageWindow._slide(cave, true);
		var _this = this;
		btn.bind('click', function() {
			_this._close(cave);
		});
		cave.data('timeout', setTimeout(function() {
			_this._close(cave, true);
		}, options.duration ? options.duration : this._duration));
	},

	_clear : function(tag) {
		var real = jq(this.$n('real'));
		var i;

		for (i = real.children().length - 1; i >= 0; i--) {
			var cave = jq(real.children().get(i));

			if (!tag || cave.data('tag') === tag)
				this._close(cave);
		}
	},

	_close : function(cave, animate) {
		if (!cave.jquery)
			cave = jq(cave);

		var tmout = cave.data('timeout');

		if (tmout)
			clearTimeout(tmout);

		cwf.ext.MessageWindow._slide(cave, false, animate ? null : 1, function() {
			cwf.ext.MessageWindow._remove(cave);});
	},

	bind_: function () {
		this.$supers(cwf.ext.MessageWindow, 'bind_', arguments);
		zWatch.listen({onSize: this});
	},
	
	unbind_ : function() {
		zWatch.unlisten({onSize: this});
		this._clear();
		this.$supers(cwf.ext.MessageWindow, 'unbind_', arguments);
	},
	
	getZclass : function() {
		return this._zclass == null ? 'cwf-messagewindow' : this._zclass;
	}
	
}, {
	_remove : function(cave) {
		var real = cave.parent();
		var cmp = real.parent();
		cave.remove();

		if (real && real.children().length == 0)
			cmp.hide();
	},

	_slide : function(cave, down, duration, complete) {
		if (!cave.jquery)
			cave = jq(cave);
		
		var start = down ? 0 : cave.outerHeight();
		var end = down ? cave.outerHeight() : 0;
		cave.outerHeight(start);
		cave.animate({height: end}, duration || 'slow', complete);
	}
});
