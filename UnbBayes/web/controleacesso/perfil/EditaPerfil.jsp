<%@ taglib uri="struts-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<HTML>
<HEAD>
<style>
	.textbox{
		border:2px inset white;
		background-color: #ffffff;
	}
	.requiredfield { font-family: fraklin gothic medium;
                font-size: 9pt;
                font-weight: bold;
                color: RED;
	}
</style>

<SCRIPT>
	function enviarAssinatura(intUrl) { 
		var url = "";
		switch (intUrl) {
			case 0:
				if(!validatePerfilForm(document.forms[0])) {
					return;
				}			
				url = "${pageContext.request.contextPath}/Perfil/UpdatePerfil.do";
				break;
			case 1:
				url = "${pageContext.request.contextPath}/Perfil/SelecionarPerfil.do";
				break;
			default:
				break;
		}
		if (url!="") {
			document.forms[0].action=url;
			document.forms[0].submit(); 
		}
	}
</SCRIPT>

<html:javascript formName="perfilForm"/>

</HEAD>
<BODY BGCOLOR=#FFFFFF LEFTMARGIN=0 TOPMARGIN=0 MARGINWIDTH=0 MARGINHEIGHT=0 ONLOAD="">

<html:errors/>

<html:form action='Perfil/UpdatePerfil.do' method="POST">
<table width='100%'>
	<tr>
     <TD style="border:3px inset #FFCC00;border-right:0px;border-top:0;border-left:0; text-align:left; font-size:10; color:#414141;">
       <FONT SIZE="4" COLOR=""><bean:message bundle="Perfil" key="controleacesso.perfil.nomeCasoUso" /></font>
     </TD>
	</tr>
</table>
<table>
	<tr>
	  <TD>
	   <TABLE class=fancy width=100% cellpadding=4>
	    <TR>
	     <TD height=25px ><bean:message bundle="Perfil" key="controleacesso.perfil.nome" />:<span class=requiredfield>*</span></TD>
	     <TD height=25px><html:text property="nome" styleId="nome" value='${perfil.nome}' styleClass="textbox" size='48' maxlength="45"/></TD>
	    </TR>
	    <tr>
	     <TD height=25px ><bean:message bundle="Perfil" key="controleacesso.perfil.descricao" />:</TD>
	     <TD height=25px><html:textarea property='descricao' styleId="descricao" value='${perfil.descricao}' styleClass='textbox' rows="4" cols="50"/></TD>
	    </tr>
	   </TABLE>
	  </TD>
	</tr>
<table>
<table>
	<tr>
		<td>
			<input type='button' onClick='enviarAssinatura(0);' name='btnSalvar' value='Editar'>
			&nbsp;
			<input type='button' onClick='enviarAssinatura(1);' name='btnVoltar' value='Voltar'>
		</td>
	</tr>
</table>

<html:hidden property="idPerfil" styleId="idPerfil" value="${perfil.idPerfil}"/>

</html:form>
</BODY>
</HTML>