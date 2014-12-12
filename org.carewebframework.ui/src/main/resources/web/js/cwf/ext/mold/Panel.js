/*
 * Bootstrap mold.
 */
function (out, skipper) {
	function genIcon(type, icon) {
		if (!iconPanel) {
			iconPanel = true;
			out.push('<span class="panel-icons">');
		}
		out.push('<span id="', uuid, '-', type, '" class="panel-icon"><span class="glyphicon ', icon, '"></span></span>')
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
	this._default = true;
	
	out.push('<div', this.domAttrs_(), '>');

	if (caption || title) {
		out.push('<div id="', uuid, '-head" class="panel-heading">');
		out.push('<div id="', uuid, '-cap" class="panel-caption">');
		
		if (caption) {
			caption._zclass = "panel-title";
			caption.redraw(out);
		} else {
			out.push('<span class="panel-title">', zUtl.encodeXML(title), '</span>');
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
		
		out.push('</div></div>');
	} 
	
	out.push('<div id="', uuid, '-body" class="panel-content"');
	
	if (!this._open) {
		out.push(' style="display:none;"');
	}
	
	out.push('>');
	
	if (!skipper) {
		genTbar('tb', this.tbar);
		
		if (pc) {
			pc._zclass = "panel-body";
			pc.redraw(out);
		}
			
		genTbar('bb', this.bbar);
		genTbar('fb', this.fbar);
	}
	
	out.push('</div></div>');
}