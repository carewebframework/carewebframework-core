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
<body>
<c:set var="warning" value="${cw:getCookie('logoutWarningMessage')}"/>
<c:set var="target" value="${cw:getCookie('logoutTargetURI')}"/>
<div style="width:100%;height:100%;">
	<div id="hPGP0" style="height:20%;" class="z-separator-hor">&nbsp;
	</div>
	<span style="display: none; "></span>
</div>
<div class="z-modal-mask"
	style="z-index: 1800; left: 0px; top: 0px; width: 100%; height: 100%; display: block; "></div>
<div style="width: 400px; position: absolute; top: 100px; left: 50%; margin-left: -200px;  z-index: 1800;"
	class="z-window-highlighted z-window-highlighted-shadow">
	<div class="z-window-highlighted-tl">
		<div class="z-window-highlighted-tr"></div>
	</div>
	<div class="z-window-highlighted-hl">
		<div class="z-window-highlighted-hr">
			<div class="z-window-highlighted-hm">
				<div id="hPGP1-cap"
					class="z-window-highlighted-header z-window-highlighted-header-move">${c:l('logout.window.title')}</div>
			</div>
		</div>
	</div>
	<div class="z-window-highlighted-cl">
		<div class="z-window-highlighted-cr">
			<div class="z-window-highlighted-cm">
				<div id="hPGP1-cave" class="z-window-highlighted-cnt" style="text-align:center;padding:20px">
					<span style="color:red;font-weight:bold;white-space:pre-wrap;"
						class="z-label">${empty warning ? c:l('logout.message.default') : warning}</span>
					<div style="height:20px;" class="z-separator-hor-bar">&nbsp;
					</div>
					<button id="btnLogin" onClick="location.href='${empty target ? c:encodeURL('/') : c:encodeURL(target)}'">${c:l('logout.form.button.login.label')}</button>
				</div>
			</div>
		</div>
	</div>
	<div class="z-window-highlighted-bl">
		<div class="z-window-highlighted-br"></div>
	</div>
</div>
</body>
</html>