<%@ taglib uri="http://www.zkoss.org/dsp/web/core" prefix="c" %>

div.cwf-labeledelement {
	display: inline-block;
}

.cwf-labeledelement-top,
.cwf-labeledelement-bottom {
	display: block;
}

.cwf-labeledelement-left,
.cwf-labeledelement-right {
	display: table;
}

.cwf-labeledelement-left>*,
.cwf-labeledelement-right>* {
	display: table-cell;
}

.cwf-labeledelement-left>div {
	padding-right: 5px;
}

.cwf-labeledelement-right>div {
	padding-left: 5px;
}

.cwf-labeledelement-top>div {
	padding-bottom: 2px;
}

.cwf-labeledelement-bottom>div {
	padding-top: 2px;
}

.cwf-labeledelement-start>div {
	vertical-align: top;
	text-align: left;
}

.cwf-labeledelement-center>div {
	vertical-align: middle;
	text-align: center; 
}

.cwf-labeledelement-end>div {
	vertical-align: bottom;
	text-align: right; 
}
