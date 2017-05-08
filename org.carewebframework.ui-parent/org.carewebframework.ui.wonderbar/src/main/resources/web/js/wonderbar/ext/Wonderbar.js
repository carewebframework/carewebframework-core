zk.$package('wonderbar.ext');
/**
 * Wonderbar
 */
wonderbar.ext.Wonderbar = zk.$extends(cwf.inp.InputWidget, {
    _arrow: null,

    $arrow: null,

    _input: null,

    $input: null,

    _value: '',

    _isOpen: false,

    _minLength: 2,

    _openOnFocus: false,

    _skipTab: false,

    _fromServer: false,

    _clientMode: false,

    _lastTerm: null,

    _lastHits: null,

    _selectFirst: true,

    _matchMode: 0,

    _suppressOpen: false,

    _truncItem: null,

    _maxResults: 100,

    defaults: [],

    onSize: function () {
        var width = this.getWidth();
        if (!width || width.indexOf('%') != -1)
            this.getInputNode().style.width = '';
    },

    getZclass: function () {
        return this._zclass != null ? this._zclass : 'cwf-wonderbar';
    },

    bind_: function () {
        this.$supers(wonderbar.ext.Wonderbar, 'bind_', arguments);
        this._arrow = this.$n('arrow');
        this._arrow._wb = this;
        this.$arrow = jq(this._arrow);
        this._input = this.$n('real');
        this._input._wb = this;
        this.$input = jq(this._input);
        this.$arrow.bind('mousedown', this._onArrowClick);
        this.$input.bind('focus', this._onInputFocus);
        this._initAutoComplete();
    },

    unbind_: function () {
    	this._autocomplete('destroy');
        this.$input.unbind('focus', this._onInputFocus);
        this.$arrow.unbind('mousedown', this._onArrowClick);
        this.$supers(wonderbar.ext.Wonderbar, 'unbind_', arguments);
    },

	onChildAdded_: function (child) {
		this.$supers('onChildAdded_', arguments);

		if (child.$instanceof(wonderbar.ext.WonderbarItems))
			this.items = child;
		else if (child.$instanceof(wonderbar.ext.WonderbarDefaults))
			this.defaults = child;
	},

	onChildRemoved_: function (child) {
		this.$supers('onChildRemoved_', arguments);

		if (child == this.items)
			this.items = null;
		else if (child == this.defaults)
			this.defaults = [];
	},

	fireOnSelect: function (item, event) {
		var keys = event ? {shiftKey: event.shiftKey, altKey: event.altKey, ctrlKey: event.ctrlKey}: {};
		this.fire("onWonderbarSelect", zk.copy({reference: item}, keys));
	},

	search: function (term) {
		term = term || this._lastTerm;
		this._autocomplete('search', term);
	},

	serverSource: function (term) {
		if (this._fromServer) {
    		this._lastHits = null;
			this._fromServer = false;
			return this._getItems();
		} else {
			this.fire("onWonderbarSearch", {term: term});
			return this._lastHits;
		}
	},

    clientSource: function (term) {
		this._lastHits = null;
        var patterns = wonderbar.ext.Wonderbar.tokenize(term);
        var uniqueMap = {};

        // this grep will return a list of the matched items
        // into hits
        var hits = [];
        var prev = null;

        for (var item = this.items.firstChild; item; item = item.nextSibling) {
        	if (!item.selectable) {
        		prev = item;
        		continue;
        	}
            var text = item.searchTerm || item.value || item.label;

            if (!wonderbar.ext.Wonderbar.matches(patterns, text, this._matchMode)) {
                continue;
            }

            // if a uniquekey is specified,
            if (item.uniquekey) {
                var uniqueItem = uniqueMap[item.uniquekey];
                var uniquePriority = uniqueItem && uniqueItem.uniquePriority ? uniqueItem.uniquePriority : 0;
                var priority = item.uniquePriority ? item.uniquePriority : 0;

                if (priority >= uniquePriority) { // the newly matched item trumps previous one
                	uniqueMap[item.uniqueKey] = item;
                } else {
                	continue;
                }
            }

            // we matched
            if (prev) {
            	hits.push(prev);
            	prev = null;
            }

            hits.push(item);
        };

        if (uniqueMap.length > 0) {
        	hits = jq.grep(hits, function(item) {
        		return !item.uniqueKey || uniqueMap[item.uniqueKey] === item;
        	});
        }

        if (hits.length > this._maxResults) {
        	hits = hits.slice(0, this._maxResults);
        	hits.push(this._truncItem);
        }

        return hits;
    },

    _unselect: function() {
    	var inp = zk(this._input);
    	var range = inp.getSelectionRange();

    	if (range[0] != range[1]) {
    		inp.setSelectionRange(range[1], range[1]);
    	}
    },

    _source: function(term) {
    	this._unselect();
    	term = term.trim();
    	var same = this._lastHits && term === this._lastTerm;
    	var useDefaults = term.length < this._minLength;

    	if (!same) {
    		this._lastTerm = term;
    		this._lastHits = useDefaults ? this._getDefaults() :
    			this._clientMode ? this.clientSource(term) : this.serverSource(term);
    	}

		var item = useDefaults ? this._findNumberedChoice(term, this._lastHits) : null;

		if (!item && this._selectFirst) {
			item = this._getFirstSelectable(this._lastHits);

			if (item && item.choiceNumber > 0) {
				item = null;
			}
		}

		if (item) {
			this._focusItem(item, 100);
		}

    	return this._lastHits;
    },

    _focusItem: function(item, timeout) {
		var menu = this._getMenu();

		setTimeout(function() {
			var comp = menu.find('#' + item.uuid);
			
			if (comp && comp.offset()) {
				menu.menu('focus', null, comp);
			}
		}, timeout);
    },

    _getFirstSelectable: function(hits) {
    	if (hits != null) {
	    	for (var i = 0; i < hits.length; i++) {
	    		var item = hits[i];
	
	    		if (item.selectable) {
	    			return item;
	    		}
	    	}
    	}

    	return null;
    },

    _getDefaults: function() {
    	return this._getChildren(this.defaults);
    },

    _getItems: function() {
    	return this._getChildren(this.items);
    },

    _getChildren: function(wgt) {
    	var list = [];

    	if (wgt) {
	    	for (var item = wgt.firstChild; item; item = item.nextSibling) {
	    		list.push(item);
	    	}
    	}

    	return list;
    },

    _autocomplete: function (fnc, args) {
        return this.$input.autocomplete(fnc, args);
    },

    _open: function () {
    	if (this._isOpen)
    		return;

        var $wb = jq(this);

        if ($wb.is(':visible')) {
            this.search();
        } else {
            this._close();
        }
    },

    _selectItem: function (item, fire) {
    	var v = item ? item.value || item.label : null;
    	this.setText(v);
    	this._close();

    	if (fire) {
    		this._fireOnSelect(item);
    	}
    },

    _close: function () {
    	if (this._isOpen) {
    		this._autocomplete('close');
    	}
    },

    _toggle: function () {
        if (this._isOpen) {
            this._close();
        } else {
            this._open();
        }
    },

    _onArrowClick: function (event) {
        var wb = this._wb;
        wb._focus(true);
        setTimeout(function() {
            wb._toggle();
        }, 20);
        return false;
    },

    _onInputFocus: function (event) {
    	var wb = this._wb;

    	if (wb._openOnFocus && !wb._suppressOpen)
    		wb._open();

    	wb._suppressOpen = false;
    },

    _focus: function (noOpen) {
        this._suppressOpen = noOpen;
        this.$input.focus();
    },

    _getMenu: function () {
    	return this._autocomplete('widget');
    },

    _updateArrow: function (open) {
        this.$arrow.removeClass(open ? 'cwf-wonderbar-arrow-down' : 'cwf-wonderbar-arrow-up');
        this.$arrow.addClass(open ? 'cwf-wonderbar-arrow-up' : 'cwf-wonderbar-arrow-down');
        this._isOpen = open;
    },

    _findNumberedChoice: function (val, items) {
    	var numValue = parseInt(val, 10);

        // get int value of what user keyed
        if (!isNaN(numValue) && isFinite(numValue) && numValue > 0) {
            for (var i = 0; i < items.length; i++) {
            	var item = items[i];

                if (item.choiceNumber == numValue) {
                    return item;
                }
            }
        }

        return null;
    },

    _serverResponse: function (term) {
    	this._fromServer = true;
    	this._lastHits = null;
    	this._autocomplete('search', term);
    },

    _initAutoComplete: function () {
        var wb = this;
        var $wb = jq(this);

        if (this.$input.data('ui-autocomplete'))
        	this._autocomplete('destroy');

        this._autocomplete({
            delay: 50,
            minLength: 0,
            autoFocus: false,

            source: function(request, response) {
                response(wb._source(request.term));
            },

            focus: function (event, ui) {
                return false;
            },

            open: function (event, ui) {
                wb._updateArrow(true);
                // this code puts the popup above the textbox if it
                // is too long
                var $autopop = null;
                jq('.ui-autocomplete').each(function (index, val) {
                    var $val = jq(val);
                    if ($val.css('display') == 'block') {
                        $autopop = $val;
                    }
                });
                if ($autopop) {
                    $autopop.css('height', ''); // clear any
                    // previously
                    // hardcoded
                    // heights
                    var ah = $autopop.height();
                    var bh = jq(document.body).height();
                    // check if the popup is extending off the
                    // bottom of the screen
                    if ($autopop.position().top + ah > bh) {
                        var atopo = parseInt($autopop.css('top'), 10);
                        var ih = $wb.height() + 5;
                        var ntop = atopo - ah - ih;

						if (ntop < 0 || ntop > bh) {

                            // see what has more room... above or
                            // below
                            var arm = Math.abs(atopo) - ih;
                            var brm = bh - (atopo) - 4;
                            if (brm > arm) {
                                // just squish it but leave it below
                                $autopop.css('height', brm + 'px');
                            } else {
                                // squish it and put it above
                                var nh = arm;
                                ntop = atopo - nh - ih;
                                $autopop.css('height', nh + 'px');
                                $autopop.css('top', ntop);
                            }
                        } else {
                            // move it above since it fits nicely
                            $autopop.css('top', ntop);
                        }
                    }
                }
            },

            close: function (event, ui) {
                wb._updateArrow(false);
            },

            select: function (event, ui) {
                if (!wb._skipTab || event.keyCode != 9) {
                    if (ui.item && ui.item.selectable) {
                        var text = ui.item.value || ui.item.label;
                        wb.setText(text);
                        wb._input.value = text;
                        wb.fireOnSelect(ui.item, event);
                    }
                }
                return false;
            }
        });
    }

}, {
    closeAll: function () {
        jq('.ui-autocomplete-input').autocomplete('close');
    },

    encodeXML: function (text) {
    	if (text) {
    		if (text.match('^<html>')) {
    			text = text.slice(6);

    			if (text.match('</html>$')) {
    				text = text.slice(0, -7);
    			}
    			return text;
    		} else {
    			return zUtl.encodeXML(text);
    		}

    	} else {
    		return "";
    	}
    },

    matches: function (pattern, value, mode) {
    	var patterns = wonderbar.ext.Wonderbar.tokenize(pattern, !mode);
    	var values = wonderbar.ext.Wonderbar.tokenize(value);
        var start = 0;

        for (var p = 0; p < patterns.length; p++) {
        	pattern = patterns[p];

        	if (pattern.charAt(0) != '^') {
        		pattern = patterns[p] = '^' + jq.ui.autocomplete.escapeRegex(pattern);
        	}

            var matched = false;

            for (var i = start; i < values.length; i++) {
            	value = values[i];

                if (value != null)
	            	if (matched = value.match(pattern)) {
	                    values[i] = null;
	                    start = !mode ? 0 : i;

	                    if (mode == 2) {
	                        mode = 3;
	                    }
	                    break;
	                } else if (mode == 3) {
	                    return false;
	                }
            }

            if (!matched) {
                return false;
            }
        }

        return true;
    },

    tokenize: function (text, sortByLength) {
    	if (text instanceof Array)
    		return text;

    	var pcs = text.toLowerCase().trim().split(/\W/);
    	var list = [];

    	for (var i = 0; i < pcs.length; i++) {
    		var pc = pcs[i].trim();

    		if (pc.length != 0)
    			list.push(pc);
    	}

    	if (sortByLength) {
    		list.sort(function (s1, s2) {
    			var len1 = s1 ? s1.length : -1;
    			var len2 = s2 ? s2.length : -1;
    			return len2 - len1;
    		});
    	}
    	return list;
    }
});

//Overriding _renderItem to handle ZK item renderers.

jq.ui.autocomplete.prototype.cwf_renderItem = jq.ui.autocomplete.prototype._renderItem;

jq.ui.autocomplete.prototype._renderItem = function (ul, item) {
    if (item.redraw) {
    	var out = [];
    	item.redraw(out, true);
    	return jq(out.join('')).appendTo(ul);
    } else {
        return this.cwf_renderItem(ul, item);
    }
};

//Overriding _renderMenu to detect category change.

jq.ui.autocomplete.prototype.cwf_renderMenu = jq.ui.autocomplete.prototype._renderMenu;

jq.ui.autocomplete.prototype._renderMenu = function (ul, items) {
	var prev = null;

	for (var i = 0; i < items.length; i++) {
		var item = items[i];

		if (item._sameCategory) {
			if (prev) {
				var same = item._sameCategory(prev);
				prev._last = !same;
				item._first = !same;
			} else {
				item._first = true;
				item._last = false;
			}

			prev = item;
		} else {
			prev = null;
		}
	}

	if (prev) {
		prev._last = false;
	}

	return this.cwf_renderMenu(ul, items);
};

