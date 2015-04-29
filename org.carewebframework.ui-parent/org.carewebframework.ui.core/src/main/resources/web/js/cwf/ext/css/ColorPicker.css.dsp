<%@ taglib uri="http://www.zkoss.org/dsp/web/core" prefix="c" %>

.cwf-colorpicker .z-grid {
	border: none;	
}

.cwf-colorpicker .z-row-content {
	padding: 0;	
	line-height: inherit;
	background-color: white;
}

.cwf-colorpicker-colorcell {
    width: 15px;
    height: 15px;
    text-align: center;
    display: inline-block;
}

.cwf-colorpicker-colorcell:hover {
	border: 2px solid white;
}

.cwf-colorpicker-colorcell-nocolor {
	background-image: url(${c:encodeURL('~./org/carewebframework/ui/zk/no-choice.png')});
	background-repeat: no-repeat;
}
