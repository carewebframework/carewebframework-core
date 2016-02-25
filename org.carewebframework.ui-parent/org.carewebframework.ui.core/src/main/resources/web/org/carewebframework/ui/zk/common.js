/*
 * CareWeb Framework JavaScript Library
 */
cwf = {};  // Use as namespace

cwf.printStyles = new Array();

/*
 * Registers a style sheet to be used for print preview.
 */
cwf.registerPrintStyle = function (printStyle) {
	cwf.printStyles.push(printStyle);
};

/*
 * Extracts the html based on the jquery selectors in source, formats it with print styles,
 * and presents it for printing.
 */
zAu.cmd0.cwf_print = cwf.print =
function (source, printStyles, printPreview) {
	var printContent = '';

	if (!(source instanceof Array)) {
		source = [source];
	}

	for (var i = 0; i < source.length; i++) {
		jq(source[i]).each(function() {
			printContent += this.innerHTML;
		});
	}

	printStyles = cwf.printStyles.concat(!printStyles ? [] : printStyles instanceof Array ? printStyles : printStyles.split(','));
	printPreview = printPreview || cwf.debug;

	window.cwf_print = function(root) {
		this.jq(root).html(printContent);

		if (printStyles) {
			var head = this.jq('head');

			for (var i = 0; i < printStyles.length; i++) {
				var item = printStyles[i];
				if (item.startsWith('~./'))
					item = zk.ajaxURI('/web' + item.substring(2), {
						au : true
					});

				if (this.document.createStyleSheet) {
					this.document.createStyleSheet(item);  //IE only - otherwise, won't download stylesheet
				} else {
					var link = this.jq('<link>');
					link.attr({
						rel: 'stylesheet',
						type: 'text/css',
						href: item
					});
					head.append(link);
				}
			}
		}

		if (printPreview)
			this.focus();
		else {
			this.jq(this).load(function() {
				this.print();
				this.close();
			});
		};
	};

	window.open(zk.ajaxURI('/web/org/carewebframework/ui/zk/printPreview.zul?owner=' + zk.Desktop.$().id, {au:true}), 'PrintPreview');
};

/*
 * Prints the contents of a given iframe, specified by frameIdentifier (which could be array index or frame name).
 */
cwf.printIframe = function (frameIdentifier) {
	var domElement = frames[frameIdentifier];
	domElement.focus();
	domElement.print();
};

/*
 * Returns the height and width of an element.
 */
cwf.dim = function (id) {
	var domElement = document.getElementById(id);
	return domElement.clientHeight + ',' + domElement.clientWidth;
};

/*
 * Adds the specified pixel offset to the current left and top position of the window
 * with the specified id.
 */
cwf.offset_window = function(id, offset) {
	add_offset = function(value) {
		return (parseFloat(value) + offset) + 'px';
	};
	do_element = function(elid) {
		var domElement = document.getElementById(elid);

		if (domElement) {
			domElement.style.left = add_offset(domElement.style.left);
			domElement.style.top = add_offset(domElement.style.top);
		}
	};
	do_element(id);
	do_element(id + '!shadow');
};

/*
 * Blocks propagating a click event on a detail element in a grid row.  This is used
 * in the selection grid to prevent affecting the selection when the detail is expanded
 * or collapsed.
 */
cwf.selectiongrid_onclick = function (event) {
	var target = event.target.$n();
	var cls = target.getAttribute('class');
	cls = cls ? cls : target.getAttribute('className');

	if (cls && cls.startsWith('z-detail'))
		event.stop();
};

/**
 * Replaces ZK's date parsing to allow for other formats, like mm/dd/yyyy and T+2Y.
 */
cwf.parseDate = function(s) {
	var ms = Date.parse(s);

	if (ms == null) {											    	// Parse failure
		return null;
	} else {
		var r = new Date();												// Parse success
		r.setTime(ms);
		return r;
	}
};

/**
 * Hook the zUtl.go function to clear the confirm close message if we are logging out.
 */
cwf.old_zutl_go = zUtl.go;

zUtl.go = function (url, opts) {
	if (url && opts && url.indexOf('/logout') != -1 && !opts.target)
		zk.confirmClose = null;

	cwf.old_zutl_go(url, opts);
};

/**
 * Mods to input widgets.
 */
cwf.slideDownDuration = 50;		// Change default slide down duration from 400ms to 50ms

cwf.slideDown_ = function(pp) {
	zk(pp).slideDown(this, {duration: cwf.slideDownDuration, afterAnima: this._afterSlideDown});
};

zk.afterLoad('zul.inp', function() {
	zul.inp.ComboWidget.prototype.slideDown_ = cwf.slideDown_;
	zul.inp.Combobox.prototype.slideDown_ = cwf.slideDown_;
	zul.inp.Bandbox.prototype.slideDown_ = cwf.slideDown_;

	zul.inp.InputWidget.onChangingDelay = 150;	// Change the delay between keystrokes before calling the onChanging event from 350ms to 150ms

	// Fix combobox dropdown positioning anomaly (ZK-2997) when server push enabled.
	cwf.old_cbx_onresponse = zul.inp.Combobox.prototype.onResponse;
	
	zul.inp.Combobox.prototype.onResponse = function() {
		this._shallSyncPopupPosition = false;
		cwf.old_cbx_onresponse.apply(this, arguments);
	}
});

/**
 * Disable back navigation when backspace key is pressed.
 */
cwf.cancelNavigation = function(event) {
	if (event.keyCode == 8) {
		var tp = event.srcElement || event.target;
		tp = tp.tagName.toLowerCase();

		if (tp != 'input' && tp != 'textarea')
			event.preventDefault();
	}
};

/**
 * Adds a disable mask over a widget.
 *
 * @param uuid Widget or widget id.
 * @param msg Caption for mask.
 * @param popid Optional id of popup menu
 * @param hint Optional hint text
 * @param anchor Optional subid of mask anchor
 */
zAu.cmd0.cwf_addMask =
cwf.addMask = function(uuid, msg, popid, hint, anchor) {
	cwf.removeMask(uuid);
	cwf.zkMask_(uuid, true);
	
	if (uuid.uuid) {
		uuid = uuid.uuid;
	}

	var w = jq('<div />').attr('id', uuid + '-cwfMask').addClass('cwf-mask'),
		ctx;

	anchor = anchor ? uuid + '-' + anchor : uuid;
	
	jq('<div />').text(msg).appendTo(w);
	jq('#' + anchor).append(w);
	
	if (popid && (ctx = zk.Widget.$(popid))) {
		w.bind('contextmenu', function(e) {
			ctx.open(w, [e.pageX, e.pageY], null, {sendOnOpen:true});
			e.stop({dom:true});
		});
	}
	
	if (hint)
		w.attr('title', hint);
};

/**
 * Remove disable mask from a widget.
 *
 * @param uuid Widget or widget id.
 */
zAu.cmd0.cwf_removeMask =
cwf.removeMask = function(uuid) {
	cwf.zkMask_(uuid, false);
	
	if (uuid.uuid)
		uuid = uuid.uuid;

	jq('#' + uuid + '-cwfMask').remove();
};

/**
 * Hide or show ZK mask, if any.
 */
cwf.zkMask_ = function(uuid, hide) {
	var wgt = uuid.uuid ? uuid : zkWidget.$(uuid);

	if (wgt.__mask) {
		jq(wgt.__mask.mask).css('display', hide ? 'none' : 'block');
	}
};

/**
 * Add or remove a badge.
 * 
 * @param selector Selector for element to receive badge.
 * @param text Text for the badge.  If none, any existing badge will be removed.
 */
cwf.setBadge = function(selector, text) {
	var w$ = jq(selector),
		b$ = w$.find('.badge');
	
	if (text) {
		b$ = b$.length ? b$ : jq('<span class="badge" />').appendTo(w$);
		b$.text(text);
	} else {
		b$.remove();
	}
}

/**
 * Prevent backspace key from invoking browser back function.
 */
jq(document).ready(function() {
	jq(document).keydown(function(event) {cwf.cancelNavigation(event);});
});

/**
 * Fire a local event at the server.
 *
 * @param eventName The event name.
 * @param eventData The event data.
 */
cwf.fireLocalEvent = function(eventName, eventData) {
	cwf.fireEvent(eventName, eventData, true);
};

/**
 * Fire a remote event at the server.
 *
 * @param eventName The event name.
 * @param eventData The event data.
 */
cwf.fireRemoteEvent = function(eventName, eventData) {
	cwf.fireEvent(eventName, eventData, false);
};

/**
 * Fire a local or remote event at the server.
 *
 * @param eventName The event name.
 * @param eventData The event data.
 * @param asLocal If true, fire as local event; otherwise as remote.
 */
cwf.fireEvent = function(eventName, eventData, asLocal) {
	var params = {
		eventName: eventName,
		eventData: eventData,
		asLocal: asLocal
	};

	zAu.send(new zk.Event(null, 'onGenericEvent', params));
};

/**
 * Simple stopwatch.
 */
cwf.stopwatch = function(tag, evt, fnc) {
	this.tag = tag || '';
	this.evt = evt || 'STATUS.TIMING';
	this.fnc = fnc || cwf.stopwatch.format;
};

cwf.stopwatch.prototype.start = function() {
	this.begin = new Date();
	this.elapsed = null;
};

cwf.stopwatch.prototype.stop = function() {
	this.elapsed = new Date() - this.begin;
	cwf.fireLocalEvent(this.evt, this.fnc(this));
};

cwf.stopwatch.format = function(sw) {
	var tm = sw.elapsed;
	var units = 'ms';

	if (tm >= 1000) {
		tm /= 1000;
		units = 's';

		if (tm >= 60) {
			tm /= 60;
			units = 'm';

			if (tm >= 60) {
				tm /= 60;
				units = 'h';
			}
		}
	}

	return sw.tag + tm + ' ' + units;
};

/**
 * onKeyPress event handler for constraining input.
 */
cwf.constrainInput = function(keyPressEvent, regex) {
	var key = String.fromCharCode(keyPressEvent.which);
	
   	if (!regex.test(key)) {
   		keyPressEvent.stop();
   	}
};
