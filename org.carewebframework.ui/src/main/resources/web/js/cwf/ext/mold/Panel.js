/*
 * Bootstrap mold.
 */
function (out, skipper) {
	function genIcon(type, icon) {
		if (!iconPanel) {
			iconPanel = true;
			out.push('<span class="panel-icons">');
		}
		out.push('<span id="', uuid, '-', type, '" class="panel-icon"><span class="glyphicon ', icon, '" /></span>')
	};
	
	function genTbar(type, tbar) {
		if (tbar) {
			out.push('<div id="', uuid, '-', type, '" class="panel-footer" style="padding:0;">');
			tbar.redraw(out);
			out.push('</div>');
		}
	};
	
	var uuid = this.uuid,
		title = this.getTitle(),
		caption = this.caption,
		sclass = this.getSclass() || 'panel-primary',
		pc = this.panelchildren,
		iconPanel;

	this._closableIconClass = 'glyphicon-remove';
	this._maximizableIconClass = 'glyphicon-resize-full';
	this._maximizedIconClass = 'glyphicon-resize-small';
	this._minimizableIconClass = 'glyphicon-minus';
	this._collapseOpenIconClass = 'glyphicon-chevron-up';
	this._collapseCloseIconClass = 'glyphicon-chevron-down';
	
	out.push('<div class="panel ', sclass, '" ', this.domAttrs_({domClass:1}), '>');

	if (caption || title) {
		out.push('<div id="', uuid, '-cap" class="panel-heading">');
		
		if (caption) {
			var vis = caption.isVisible(),
				title2 = vis ? caption.getLabel() : null;
			
			if (title2) {
				if (title) {
					title += ' - ' + title2;
				} else {
					title = title2;
				}
			}
			
			var w = caption.firstChild;
			
			if (w) {
				out.push('<div class="panel-caption ', caption.getSclass(), '" ', caption.domAttrs_({domClass:1}), '>');
	
				for (; w; w = w.nextSibling)
					w.redraw(out);
				
				out.push('</div>');
			}
		}
		
		if (this._collapsible) {
			genIcon('exp', this._open ? this._collapseOpenIconClass:
				this._collapseCloseIconClass);
		}
		
		if (this._minimizable) {
			genIcon('min', this._minimizableIconClass);
		}
		
		if (this._maximizable) {
			genIcon('max', this._maximized ? this._maximizedIconClass:
				this._maximizableIconClass);
		}
		
		if (this._closable) {
			genIcon('close', this._closableIconClass);
		}
		
		if (iconPanel) {
			out.push('</span>');
		}
		
		if (title) {
			out.push('<span class="panel-title">', zUtl.encodeXML(title), '</span>');
		}
		
		out.push('</div>');
	} 
	
	out.push('<div id="', uuid, '-body" class="panel-content"');
	
	if (!this._open) 
		out.push(' style="display:none;"');
	
	out.push('>');
	
	if (!skipper) {
		genTbar('tb', this.tbar);
		
		if (pc) {
			out.push('<div class="panel-body');
			var sc = pc.getSclass();
			
			if (sc) {
				out.push(' ', sc);
			}
			
			out.push('" ', this.domAttrs_({domClass:1}), '>');
			
			for (var w = pc.firstChild; w; w = w.nextSibling)
				w.redraw(out);
			
			out.push('</div>');
		}
			
		genTbar('bb', this.bbar);
		genTbar('fb', this.fbar);
	}
	
	out.push('</div>');
}