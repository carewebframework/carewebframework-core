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
	text-align: center;
    width: 100%;
    height: 100%;
    display: none;
}

.cwf-timeout-lock-panel>div {
	width: 50%;
	min-width: 200px;
	display: inline-block
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

.cwf-promptdialog .z-checkbox {
	display: block;
	text-align: center;
}

.cwf-mask {
    cursor: default;
    position: absolute;
    right: 0;
    left: 0;
    top: 0;
    bottom: 0;
    z-index: 99;
    background: lightgray;
    opacity: .7;
    overflow: hidden;
    text-align: center;
    color: blue;
    font-size: 12px;
}

.cwf-mask > div {
	border: 1px solid;
	background: white;
	padding: 2px;
    position: absolute;
    top: 50%;
    left: 50%;
    -webkit-transform: translate(-50%, -50%);  
    transform: translate(-50%, -50%);   
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

.cwf-treerow-hidebtn .z-tree-icon {
	visibility: hidden;
}

.cwf-picker .z-panelchildren {
	max-height: 300px;
	overflow: auto;
	padding: 5px;
}

.cwf-picker .z-hlayout {
	padding-right: 20px;
}

.cwf-picker-cell {
    cursor: pointer;
	padding: 1px;
	margin-bottom: 1px;
	border: 1px solid transparent;
}

.cwf-picker-cell:hover {
    border: 1px solid black;
}

.cwf-manifest-viewer td {
	white-space: pre-wrap;
	word-wrap: break-word;
}

.cwf-manifest-viewer .z-listcell {
    border: none !important;
	border-bottom: 1px solid lightgray !important;
}

.cwf-manifest-viewer .z-listcell-content {
	max-height: 200px;
	overflow: auto;
	padding: 2px !important;
}

.cwf-menubar {
	min-height: 20px;
}

.cwf-menupopup-empty {
	display: none!important;
}

.cwf-menupopup-noimages .z-menu-image {
	display: none;	
}

.cwf-menu .z-menu-separator {
	display: inline;
	padding-left: 1px;
	margin-left: -1px;
}

.cwf-menuitem .z-menu-content {
	padding-right: 5px;
}

.cwf-menuitem .z-menu-icon,
.cwf-menuitem .z-menu-separator {
	display: none!important;
}

/* Bootstrap additions */

.panel {
	margin: 0;
}

.panel-primary>.panel-heading .z-menu-content * {
	color: white;
	text-shadow: none;
}

.panel-primary>.panel-heading .z-menu-content:hover, 
.panel-primary>.panel-heading .z-menu-selected>.z-menu-content {
	background: #286090;
}

.panel-caption>.panel-title {
    display: inline-block;
}

.panel-content {
	margin: 0;
	padding: 0;
}

.panel-icons {
	float: right;
	margin: -5px -5px;
}

.panel-icon {
	cursor: pointer;
	margin-left: 5px;
	opacity: 0.7;
}

.panel-icon:hover {
	opacity: 1;
}

.panel-sm .panel-content,
.panel-sm .panel-caption {
	padding: 2px;
}

.panel-sm .panel-title,
.panel-sm .panel-icons {
	font-size: 90%;
}

.panel-sm .panel-icons,
.panel-xs .panel-icons {
	margin: 0;
}

.panel-xs .panel-content,
.panel-xs .panel-caption {
	padding: 1px;
}

.panel-xs .panel-title,
.panel-xs .panel-icons {
	font-size: 80%;
}

.glyphicon-chevron-left-double:before {
 	content: "\e079\e079";
 	letter-spacing: -4px;
}

.glyphicon-chevron-right-double:before {
	content: "\e080\e080";
 	letter-spacing: -4px;
}

/* ZK overrides */

.z-window-modal-header {
	font-weight: bold;
}

.z-toolbar-start {
	clear: none;
	float: none;
}

.z-west-splt, .z-east-splt {
    width: 6px;
    background: none;
}

.z-north-splt, .z-south-splt {
    height: 6px;
    background: none;
}

.z-errbox-center {
	word-wrap: break-word;
}

.z-caption .z-toolbar a {
    color: blue;
}

.z-window-header {
	font-weight: bold;
}

.z-menuitem-content {
	text-decoration: none!important;
}

.z-menupopup-separator {
	display: none;
}

.z-apply-loading {
	pointer-events: none;
}

.z-errorbox-content {
	word-break: break-word;
}

.z-radio input[type=radio],
.z-checkbox input[type=checkbox] {
	margin: 0 2px 0 0;
}

.z-radio,
.z-checkbox {
	padding-right: 15px;
}

body {
	-moz-user-select: none;
	-webkit-user-select: none;
	-ms-user-select: none;
	padding: 1px;
	margin: 0;
}

/* JQuery-UI overrides */

.ui-front {
	z-index: 2000;
}

.ui-selectable-helper {
	z-index: 2000;
}

