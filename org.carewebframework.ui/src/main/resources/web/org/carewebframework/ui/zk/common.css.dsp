<%@ taglib uri="http://www.zkoss.org/dsp/web/core" prefix="c" %>

.cwf-fixed-font {
	font-family: monospace;
	white-space: pre;
	white-space: pre-wrap;     					/* css-3 */
	white-space: -moz-pre-wrap !important;  	/* Mozilla, since 1999 */
	white-space: -pre-wrap;      				/* Opera 4-6 */
	white-space: -o-pre-wrap;    				/* Opera 7 */
	word-wrap: break-word;       				/* Internet Explorer 5.5+ */
}

.cwf-timeout {
	position: fixed;
	display: none;
	background: none;
	z-index: 9999 !important;
	width: 100%;
	left: 0;
}

.cwf-timeout .z-label {
	font-size: 1em;
	white-space: pre-wrap;
	vertical-align: middle;
}

.cwf-timeout-panel {
	background: yellow;
	border: 1px gray solid;
	border-top: none;
	padding: 2px 2px 2px 10px;
    border-radius: 0 0 5px 5px;
}

.cwf-timeout-lock-panel {
	margin-top: 40px;
	text-align: center;
	padding: 2px;
    width: 100%;
    height: 100%;
    display: none;
}

.cwf-timeout-baseline-countdown,
.cwf-timeout-lock-countdown,
.cwf-timeout-lock-idle,
.cwf-timeout-shutdown-countdown {
	display: inline;
}

.cwf-timeout-lock-idle,
.cwf-timeout-lock-countdown {
	opacity: 1 !important;
	pointer-events: auto !important;
	height: 100%;
	background: lightblue;
}

.cwf-timeout-lock-idle .cwf-timeout-panel {
	visibility: hidden;
}

.cwf-timeout-lock-idle .cwf-timeout-lock-panel,
.cwf-timeout-lock-countdown .cwf-timeout-lock-panel {
	display: block;
}

.cwf-timeout-shutdown-countdown {
	opacity: .75;
    pointer-events: none;
}

.cwf-timeout-shutdown-countdown .cwf-timeout-panel input,
.cwf-timeout-shutdown-countdown .cwf-timeout-panel button {
	display: none;
}

.cwf-timeout-lock-spawned {
	color: blue;
	font-size: 2em;
}

.cwf-datetimebox-tbar {
	background: none;
	border: none;
}

.cwf-datetimebox-tbar .z-a {
	color: blue;
	background: none;
	border: none
}

.cwf-mask * {
    cursor: default;
}

.cwf-mask>div {
    z-index: 99!important;
}

.cwf-xml-tag {
	color: darkviolet;
	font-weight: bold;
}

.cwf-xml-attrname {
	color: darkred;
}

.cwf-xml-attrvalue {
	color: darkblue;
}

.cwf-xml-content {
	color: magenta;
}

.cwf-treerow-hidebtn .z-tree-ico {
	visibility: hidden;
}

.cwf-gridpicker .z-row-inner {
    width: auto!important;
    padding: 0!important;
    margin: 0!important;
}

.cwf-gridpicker .z-row-cnt>* {
    cursor: pointer;
    border: 1px solid lightgray;
    padding: 4px;
}

.cwf-gridpicker .z-row-cnt>*:hover {
    border: 1px solid black;
}

.cwf-gridpicker table {
	width: auto;
}

.cwf-gridpicker .z-grid-body {
    padding-right: 14px;
    overflow: hidden;
}

.cwf-gridpicker tr.z-row-over>td.z-row-inner {
	background: none;
}

.cwf-manifest-viewer td {
	white-space: pre-wrap;
	word-wrap: break-word;
}

.cwf-manifest-viewer .z-listcell {
    border: none !important;
	border-bottom: 1px solid lightgray !important;
}

.cwf-manifest-viewer .z-listcell-cnt {
	max-height: 200px;
	overflow: auto;
	padding: 2px !important;
}

.cwf-menupopup-empty {
	display: none!important;
}

/* ZK 5.x compatibility */

tr.z-row td.z-row-inner, tr.z-row .z-cell, .z-listhead {
	line-height: 11px;
}

div.z-listbox-body .z-listcell {
	padding: 0 5px 0 5px;
}

.z-window-embedded-hm, .z-window-modal-hm, .z-window-highlighted-hm, .z-window-overlapped-hm, .z-window-popup-hm {
	margin-left: -1px;
	margin-right: -1px;
}

.z-grid td.z-detail-outer {
    padding-left: 1px;
}

.z-row-cnt.z-overflow-hidden {
    width: inherit;
}

.z-detail-faker td {
    overflow-x: hidden;
}

.z-menubar-hor .z-menu-body-clk-over .z-menu-inner-m div {
	background: transparent no-repeat right -14px;
	background-image: url(${c:encodeThemeURL('~./zul/img/menu/btn-arrow.gif')});
}
.z-menubar-ver .z-menu-body-clk-over .z-menu-inner-m div {
	background: transparent no-repeat right 0;
	background-image: url(${c:encodeThemeURL('~./zul/img/menu/btn-arrow.gif')});
}

.z-errbox-center {
	word-wrap: break-word;
}

.z-caption .z-toolbar a {
    color: blue;
}

/* JQuery-UI overrides */

.ui-front {
	z-index: 2000;
}

.ui-selectable-helper {
	z-index: 2000;
}

