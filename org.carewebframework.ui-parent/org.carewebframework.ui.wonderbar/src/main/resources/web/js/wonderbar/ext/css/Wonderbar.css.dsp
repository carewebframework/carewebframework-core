<%@ taglib uri="http://www.zkoss.org/dsp/web/core" prefix="c" %>

/* I'd like to only set this for cwf-wonderbar, but the UL element where this is needed does not have any cwf* class */
.ui-autocomplete {
	overflow-y: auto;
	overflow-x: hidden;
	max-height: 450px;
}

.cwf-wonderbar {
	display: inline;
}

.cwf-wonderbar-disd {
	 opacity: .6;
	 -moz-opacity: .6;
	 filter: alpha(opacity=60);
}

.cwf-wonderbar-disd,
.cwf-wonderbar-disd * {
	color: #AAAAAA !important;
}
.cwf-wonderbar-inp {
	background: #FFFFFF repeat-x 0 0;
	border: 1px solid #E6E6E6;
	border-top-color: #B2B2B2;
	border-radius: 2px;
	-moz-border-radius: 2px;
	-webkit-border-radius: 2px;
	<c:if test="${zk.safari > 0}">
		margin: 0;
	</c:if>
	outline: none;
	font-family: ${fontFamilyC};
	font-size: ${fontSizeM};
	font-weight: normal;
	padding: 2px;
	padding-right: 16px;
	width: 250px;
}

.cwf-wonderbar-arrow-down {
	background-image: url(${c:encodeThemeURL('~./org/carewebframework/ui/wonderbar/arrow-down.png')});
}

.cwf-wonderbar-arrow-up {
	background-image: url(${c:encodeThemeURL('~./org/carewebframework/ui/wonderbar/arrow-up.png')});
}

.cwf-wonderbar-arrow {
	background-color: transparent;
	background-repeat: no-repeat;
	background-position: 0 3px;
    height: 16px;
    width: 16px;
    display: inline-block;
    cursor: pointer;
    margin-left: -16px;
}


.cwf-wonderbar-focus input {
	background: #FFFFFF repeat-x 0 0;
	border: 1px solid #D5EAFD;
	border-top: 1px solid #94B9DA;
}

.cwf-wonderbar-text-invalid {
	background: #FFF repeat-x 0 0;
	border: 1px solid #DD7777;
}

.cwf-wonderbar-readonly,
.cwf-wonderbar-text-disd {
	background: #F0F0F0;
	border: 1px solid #E6E6E6;
	border-top: 1px solid #B2B2B2;
}

.cwf-wonderbar-readonly {
	background: #FAFAFA;
	cursor: default;
}

<%-- Inplace editing--%>
.cwf-wonderbar-inplace {
	border: 0;
	padding: 3px 1px;
	background: none;
}

<c:if test="${zk.opera > 0}">
.cwf-wonderbar-inplace {
	padding: 3px 2px;
}
</c:if>

<c:if test="${zk.ie > 0}">
.cwf-wonderbar-inplace {
	padding: 3px 2px;
}
</c:if>
