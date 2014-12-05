/*
 * Bootstrap mold.
 */
function (out, skipper) {
	function genIcon(icon, type) {
		out.push('<span id="', uuid, '-', type, '" class="panel-icon glyphicon glyphicon-', icon, '" />')
	};
	
	var uuid = this.uuid,
		title = this.getTitle(),
		caption = this.caption,
		sclass = this.getSclass() || 'panel-primary',
		contentStyle = this.getContentStyle(),
		contentSclass = this.getContentSclass();

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
		
		if (this._closable) {
			genIcon('remove', 'close');
		}
		
		if (this._maximizable) {
			genIcon(this._maximized ? 'resize-small' : 'resize-full', 'max');
		}
		
		if (this._minimizable) {
			genIcon('plus', 'min');
		}
		
		if (title) {
			out.push('<span class="panel-title">', zUtl.encodeXML(title), '</span>');
		}
		
		out.push('</div>');
	} 
	
	out.push('<span id="', uuid, '-cave" class="');
	
	if (contentSclass)
		out.push(contentSclass, ' ');
	
	out.push('panel-content" ');
	
	if (contentStyle)
		out.push(' style="', contentStyle, '"');
	
	out.push('>');

	if (!skipper)
		for (var w = this.firstChild; w; w = w.nextSibling)
			if (w != caption)
				w.redraw(out);
	
	out.push('</span></div>');
}