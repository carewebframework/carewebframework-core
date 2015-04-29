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
	max-width: 75%;
}

.cwf-messagewindow-cave {
	min-width: 200px;
    padding: 1px!important;
    background-color: lightblue;
}

.cwf-messagewindow-cap {
    border-bottom: 1px solid;
    min-height: 18px;
    zoom: 0.8;
}

.cwf-messagewindow-btns {
    float: right;
}

.cwf-messagewindow-btn {
	opacity: .6;
	filter: alpha(opacity=60);
	cursor: pointer;
	z-index: 15;
}

.cwf-messagewindow-btn:hover {
	opacity: 1;
	filter: alpha(opacity=100);
}

.cwf-messagewindow-title {
	padding-left: 4px;
}

.cwf-messagewindow-msg {
	padding: 5px;
}

.cwf-messagewindow-text {
	white-space: pre-wrap;
	word-wrap: break-word;
}
