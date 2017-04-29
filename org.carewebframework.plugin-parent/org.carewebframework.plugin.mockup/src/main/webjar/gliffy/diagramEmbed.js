function embedGliffy() {
	var qs = window.location.search.substring(1).split('&'),
		gliffy_did;
	
	for (var i = 0; i < qs.length; i++) {
		var s = qs[i];
		
		if (s.startsWith('ident=')) {
			gliffy_did = s.substring(6);
			break;
		}
	}
	
	if (gliffy_did) {
	    document.write("<img usemap='#GLIFFY_MAP_" + gliffy_did + "' border='0' src='http://www.gliffy.com/go/view/" + gliffy_did + ".png'/>");
		document.write("<script src='http://www.gliffy.com/go/diagramEmbed/" + gliffy_did + "' type='text/javascript'></script>");
	}
}    