<%@ taglib uri="http://www.zkoss.org/dsp/web/core" prefix="c" %>

@media print {
    .cwf-splitterview {
		<c:if test="${c:browser('safari')}">
			position: static;
		</c:if>
	}
}

@media screen {
    .cwf-splitterview {
		position: relative;
	}
}

.cwf-splitterview {
	width: 100%;
	height: 100%;
	overflow: hidden;
	background-color: #CDE6F5;
	border: 0;
	padding: 0;
}

.cwf-splitterview-icon {
	overflow: hidden;
	width: 16px;
	height: 16px;
	float: right;
	background: transparent no-repeat 0 0;
	background-image: url(${c:encodeURL('~./org/carewebframework/ui/zk/splitterview-icon.png')});
	margin-left: 2px;
	cursor: pointer;
}

.cwf-splitterpane-vert,
.cwf-splitterpane-horz {
	border: 1px solid #9ECAD8;
	position: absolute;
	overflow: hidden;
	background-color: white;
}

.cwf-splitterpane-vert {
	width: 100%;
}

.cwf-splitterpane-horz {
	height: 100%;
}

.cwf-splitterpane-horz-noborder,
.cwf-splitterpane-vert-noborder {
	border:0;
}

.cwf-splitterpane-horz-splt {
	position: absolute;
	height: 100%;
	width: 4px;
	background: transparent left;
	cursor: col-resize;
	cursor: ew-resize;
	line-height: 0;
	font-size: 0;
	z-index: 100;
}

.cwf-splitterpane-vert-splt {
	position: absolute;
	height: 4px;
	width: 100%;
	background: transparent top;
	cursor: row-resize;
	cursor: ns-resize;
	line-height: 0;
	font-size: 0;
	z-index: 100;
}

.cwf-splitterpane-horz-splt:hover,
.cwf-splitterpane-vert-splt:hover {
	background-color: #C4DCFB;
}

.cwf-splitterpane-splt-ghost {
	background-color: #AAA;
}

.cwf-splitterpane-horz {
	z-index: 12;
}

.cwf-splitterpane-vert {
	z-index: 16;
}

.cwf-splitterpane-horz-header,
.cwf-splitterpane-vert-header {
	color: #0F3B82;
	font-size: ${fontSizeMS};
	font-family: ${fontFamilyT};
	font-weight: bold;
	padding: 5px 3px 4px 5px;
	border-bottom: 1px solid #9ECAD8;
	background: transparent repeat-x 0 0;
	background-image: url(${c:encodeURL('~./org/carewebframework/ui/zk/splitterpane-hm.png')});
	white-space: nowrap;
	overflow: hidden;
	line-height: 15px;
	zoom: 1;
	cursor: default;
}

