<%@ taglib uri="http://www.zkoss.org/dsp/web/core" prefix="c" %>

div.cwf-labeledelement {
	display: inline-table;
}

.cwf-labeledelement-top,
.cwf-labeledelement-bottom {
	display: block;
}

.cwf-labeledelement-left,
.cwf-labeledelement-right {
	display: table;
	width: 100%;
}

.cwf-labeledelement-left>*,
.cwf-labeledelement-right>* {
	display: table-cell;
	white-space: nowrap
}

.cwf-labeledelement-left>*:nth-child(2),
.cwf-labeledelement-right>*:nth-child(1) {
	width: 100%;
}
.cwf-labeledelement-left>.cwf-labeledelement-lbl {
	padding-right: 5px;
}

.cwf-labeledelement-right>.cwf-labeledelement-lbl {
	padding-left: 5px;
}

.cwf-labeledelement-top>.cwf-labeledelement-lbl {
	padding-bottom: 2px;
}

.cwf-labeledelement-bottom>.cwf-labeledelement-lbl {
	padding-top: 2px;
}

.cwf-labeledelement-start>.cwf-labeledelement-lbl {
	vertical-align: top;
	text-align: left;
}

.cwf-labeledelement-center>.cwf-labeledelement-lbl {
	vertical-align: middle;
	text-align: center; 
}

.cwf-labeledelement-end>.cwf-labeledelement-lbl {
	vertical-align: bottom;
	text-align: right; 
}
