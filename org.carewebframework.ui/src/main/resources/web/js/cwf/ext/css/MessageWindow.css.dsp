<%@ taglib uri="http://www.zkoss.org/dsp/web/core" prefix="c" %>

.cwf-messagewindow {
	position: fixed; 
	display: none; 
	background: transparent;
	width: 100%;
	max-height: 100%;
	z-index: 9999;
	pointer-events: none;
}

.cwf-messagewindow-real {
	position: absolute;
	background: lightyellow; 
	border: solid gray 2px;
	border-top: none;
	border-radius: 0 0 5px 5px;
	overflow: auto;
	width: auto;
	max-width: 70%;
	min-width: 200px;
	pointer-events: auto;
}

.cwf-messagewindow-cave {
}

.cwf-messagewindow-cap {
	height: 15px;
	width: 100%;
	float: right;
	opacity: .6;
	filter: alpha(opacity=60);
}

.cwf-messagewindow-btn {
	background-image: url(${c:encodeThemeURL('~./org/carewebframework/ui/zk/messagewindow-close.png')});
	background-repeat: no-repeat;
	cursor: pointer;
	display: block;
	width: 15px;
	height: 15px;
	position: absolute;
	right: 3px;
	top: 2px;
	z-index: 15;
	zoom: 1;
}

.cwf-messagewindow-title {
	padding-left: 4px;
	font-size: 85%;	
}

.cwf-messagewindow-msg {
	clear: both;
	padding: 5px;
}

.cwf-messagewindow-text {
	font-size: 1em;
	white-space: pre-wrap;
	word-wrap: break-word;
}

