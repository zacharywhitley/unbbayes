<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">
<html>
	<head>
		<style>
			div { position:absolute; }
		</style>
		<script language="JavaScript" src="${pageContext.request.contextPath}/js/crossbrowser.js" type="text/javascript"></script>
		<script language="JavaScript" src="${pageContext.request.contextPath}/js/outlook.js" type="text/javascript"></script>
		<script language="JavaScript">

var o = new createOutlookBar("Bar", 0, 0,
	screenSize.width-5, screenSize.height-5, "#FFFFFF", "black", "");
o.buttonspace=60;

<c:forEach items="${menus}" var="menuPai">

	var panel${menuPai.idMenu} = new createPanel("panel${menuPai.idMenu}","${menuPai.nome}","");
	<c:forEach items="${menuPai.menus}" var="menuFilho">

		panel${menuPai.idMenu}.addButton(
			'${pageContext.request.contextPath}${menuFilho.icon}',
			'${menuFilho.nome}',
			'parent.mainIFrame.location.href="${pageContext.request.contextPath}${menuFilho.url}"');

	</c:forEach>

	panel${menuPai.idMenu}.addButton(
		'${pageContext.request.contextPath}/images/icons/icon_sair.gif',
		'Sair',
		'parent.location.href="${pageContext.request.contextPath}/Logout.do"');
	o.addPanel(panel${menuPai.idMenu});

</c:forEach>

o.draw();

		</script>
		<script language="JavaScript">

//resize OP5 (test screenSize every 100ms)
function resize_op5() {
  if (bt.op5) {
    o.showPanel(o.aktPanel);
    var s = new createPageSize();
    if ((screenSize.width!=s.width) || (screenSize.height!=s.height)) {
      screenSize=new createPageSize();
      setTimeout("o.resize(0,0,screenSize.width,screenSize.height)",100);
    }
    setTimeout("resize_op5()",100);
  }
}

//resize IE & NS (onResize event!)
function myOnResize() {
  if (bt.ie4 || bt.ie5 || bt.ns5) {
    var s = new createPageSize();
    o.resize(0,0,s.width,s.height);
  }
  else
    if (bt.ns4) location.reload();
}

		</script>
	</head>
	<body onLoad="resize_op5();Barvar.showPanel(0);" onResize="myOnResize();"></body>
</html>