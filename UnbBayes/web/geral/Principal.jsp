<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>

<!DOCTYPE html PUBLIC
	"-//W3C//DTD HTML 4.01 Transitional//EN"
	"http://www.w3.org/TR/html4/loose.dtd">

<html>
	<head>
		<title><bean:message bundle="Autenticacao" key="controleacesso.autenticacao.FormularioAutenticacao.title" /></title>
		<jsp:include flush="true" page="/geral/Estilo.jsp" />
		<style type="text/css">
		<!--
		body {
			background-image: url(${pageContext.request.contextPath}/images/bg.jpg);
		}
		-->
		</style>
	</head>
	<body>

		<table border="0" align="center" bgcolor="white" cellpadding="0" cellspacing="0">
			<tr height="115px">
				<td colspan="2">
					<jsp:include page="/geral/Cabecalho.jsp" />
				</td>
			</tr>
			<tr height="340px">
				<td width="14%">
					
					<iframe name="menuIFrame"
						id="menuIFrame"
						src="${pageContext.request.contextPath}/Menu/ExecuteMenu.do"
						frameborder="0"
						height="360px"
						width="100%"
						scrolling="no">
					</iframe>					
					
				</td>
				<td width="86%">
					
					<iframe name="mainIFrame"
						id="mainIFrame"
						src=""
						frameborder="0"
						height="360px"
						width="100%">
					</iframe>
					
				</td>
			</tr>
			<tr height="76px">
				<td colspan="2">
					<jsp:include page="/geral/Rodape.jsp" />
				</td>
			</tr>
		</table>

	</body>
</html>