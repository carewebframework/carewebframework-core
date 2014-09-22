<%@ taglib uri="http://www.zkoss.org/dsp/web/core" prefix="c" %>
<%@ taglib uri="http://www.zkoss.org/dsp/zk/core" prefix="z" %>
<%@ taglib uri="http://www.carewebframework.org/tld/core" prefix="cw" %>

<html xmlns="http://www.w3.org/1999/xhtml">
	<head>
		<meta http-equiv="content-type" content="text/html;charset=UTF-8" />
		<title>${c:l('logout.window.title')}</title>
		<%--
		<style>
		${z:outDeviceCSSContent('ajax')}
		</style>
		--%>
		${z:outDeviceStyleSheets('ajax')}
	</head>
	<body class="webkit webkit1 breeze">
		<c:set var="warning" value="${cw:getCookie('logoutWarningMessage')}"/>
		<c:set var="target" value="${cw:getCookie('logoutTargetURI')}"/>

		<div style="position: relative; min-width: 200px; max-width: 33%; margin-top: 10%; margin-left: auto; margin-right: auto" 
			class="z-window z-window-noborder z-window-modal z-window-shadow">
			
			<div class="z-window-header z-window-header-move">${c:l('logout.window.title')}</div>
			
			<div class="z-window-content">
				<div style="text-align: center;" class="z-div">
					<span style="font-weight: bold; color: red;" class="z-label">${empty warning ? c:l('logout.message.default') : warning}</span>
					<div style="height: 20px;" class="z-separator z-separator-horizontal-bar">&nbsp;</div>
					<button type="button" class="z-button" onClick="location.href='${empty target ? c:encodeURL('/') : c:encodeURL(target)}'">${c:l('logout.form.button.login.label')}</button>
				</div>
			</div>
		</div>
	</body>
</html>
