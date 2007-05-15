<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>

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
	<body onload="document.forms[0].login.focus();">

		<table border="0" align="center" bgcolor="white" cellpadding="0" cellspacing="0">
			<tr height="115px">
				<td>
					<jsp:include page="/geral/Cabecalho.jsp" />
				</td>
			</tr>
			<tr height="340px">
				<td>
					
					<center>
					<fieldset style="width:300px;">
						<legend><bean:message bundle="Autenticacao" key="controleacesso.autenticacao.FormularioAutenticacao.fieldset" /></legend>
						<html:form action="/Autenticacao/ExecuteAutenticacao.do" method="POST">
							<table border="0">
								<tr>
									<td><bean:message bundle="Autenticacao" key="controleacesso.autenticacao.FormularioAutenticacao.input.login" /></td>
									<td><html:text property="login" styleId="login" /></td>
								</tr>
								<tr>
									<td><bean:message bundle="Autenticacao" key="controleacesso.autenticacao.FormularioAutenticacao.input.senha" /></td>
									<td><html:password property="senha" styleId="senha" /></td>
								</tr>
								<tr>
									<td colspan="2">
										<html:submit value="Enviar" />
										<html:reset value="Limpar" />
									</td>
								</tr>
								<tr>
									<td colspan="2">

										<logic:messagesPresent message="true">
											<html:messages id="message" message="true">
												<bean:write name="message"/>
											</html:messages>
										</logic:messagesPresent> 

									</td>
								</tr>
							</table>
						</html:form>
					</fieldset>
					</center>

				</td>
			</tr>
			<tr height="76px">
				<td>
					<jsp:include page="/geral/Rodape.jsp" />
				</td>
			</tr>
		</table>

	</body>
</html>