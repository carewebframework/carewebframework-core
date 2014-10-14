<%@ taglib uri="http://www.zkoss.org/dsp/web/core" prefix="c" %>

.cwf-messagewindow {
	text-align: center;
	display: none;
	position: fixed; 
	background: transparent;
	z-index: 9999;
	top: 0;
	right: 0;
	left: 0;
	height: 0;
}

.cwf-messagewindow-real {
	text-align: left;
	display: inline-block;
	background: lightyellow; 
	border: solid gray 2px;
	border-top: none;
	border-radius: 0 0 5px 5px;
	max-width: 75%;
}

.cwf-messagewindow-cave {
	min-width: 200px;
}

.cwf-messagewindow-cap {
	height: 15px;
	width: 100%;
	float: right;
	padding-right: 0;
	padding-left: 0;
	opacity: .6;
	filter: alpha(opacity=60);
}

.cwf-messagewindow-btn {
	background-repeat: no-repeat;
	cursor: pointer;
	display: block;
	width: 14px;
	height: 15px;
	position: absolute;
	right: 3px;
	top: 0;
	z-index: 15;
	zoom: 1;
}

.cwf-messagewindow-btn:hover {
	background-position: -16px;
}

.cwf-messagewindow-btn-close {
	background-image: url(${c:encodeThemeURL('~./org/carewebframework/ui/zk/messagewindow-close.png')});
	right: 3px
}

.cwf-messagewindow-btn-action {
	background-image: url(${c:encodeThemeURL('~./org/carewebframework/ui/zk/messagewindow-action.png')});
	right: 20px;
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

