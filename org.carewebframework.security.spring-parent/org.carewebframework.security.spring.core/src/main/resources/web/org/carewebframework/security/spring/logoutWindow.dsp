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
	<body style="text-align: center">
		<c:set var="warning" value="${cw:getCookie('logoutWarningMessage')}"/>
		<c:set var="target" value="${cw:getCookie('logoutTargetURI')}"/>
		<div style="display: inline-block; min-width: 200px; max-width: 33%; margin-top: 10%">
			<div class="panel panel-primary">
				<div class="panel-heading" style="text-align: left">
					<div class="panel-title">${c:l('logout.window.title')}</div>
				</div>
				<div class="panel-body" style="text-align: center">
					<span class="text-danger">${empty warning ? c:l('logout.message.default') : warning}</span>
				</div>
				<div class="panel-footer">
					<div class="btn-toolbar" style="text-align: center">
						<button type="button" class="btn btn-primary" 
							onClick="location.href='${empty target ? c:encodeURL('/') : c:encodeURL(target)}'">${c:l('logout.form.button.login.label')}</button>
					</div>
				</div>
			</div>
		</div>
	</body>
</html>
