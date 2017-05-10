/*
 * CareWeb Framework JavaScript Library
 */
cwf = {};  // Use as namespace

cwf.printStyles = [];

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
cwf.print = function (source, printStyles, printPreview) {
	var printContent = '';

	if (!(source instanceof Array)) {
		source = [source];
	}

	for (var i = 0; i < source.length; i++) {
		$(source[i]).each(function() {
			printContent += this.innerHTML;
		});
	}

	printStyles = cwf.printStyles.concat(!printStyles ? [] : printStyles instanceof Array ? printStyles : printStyles.split(','));
	printPreview = printPreview || cwf.debug;

	window.cwf_print = function(root) {
		this.$(root).html(printContent);

		if (printStyles) {
			var head = this.$('head');

			for (var i = 0; i < printStyles.length; i++) {
				var item = printStyles[i];
				if (item.startsWith('web/'))
					item = zk.ajaxURI('/web' + item.substring(2), {
						au : true
					});

				if (this.document.createStyleSheet) {
					this.document.createStyleSheet(item);  //IE only - otherwise, won't download stylesheet
				} else {
					var link = this.$('<link>');
					link.attr({
						rel: 'stylesheet',
						type: 'text/css',
						href: item
					});
					head.append(link);
				}
			}
		}
		
		this.focus();

		if (!printPreview) {
			this.print();
			this.setTimeout(this.close, 100);
		}

	};

	window.open('web/org/carewebframework/ui/dialog/printPreview.cwf?owner=' + zk.Desktop.$().id, 'PrintPreview');
};

/*
 * Prints the contents of a given iframe, specified by frameIdentifier (which could be array index or frame name).
 */
cwf.printIframe = function (frameIdentifier) {
	var domElement = frames[frameIdentifier];
	domElement.focus();
	domElement.print();
};


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

