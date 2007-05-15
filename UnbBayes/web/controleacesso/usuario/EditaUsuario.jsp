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
				if(!validateUsuarioForm(document.forms[0])) {
					return;
				}			
				url = "${pageContext.request.contextPath}/Usuario/UpdateUsuario.do";
				break;
			case 1:
				url = "${pageContext.request.contextPath}/Usuario/SelecionarUsuario.do";
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

<html:javascript formName="usuarioForm"/>

</HEAD>
<BODY BGCOLOR=#FFFFFF LEFTMARGIN=0 TOPMARGIN=0 MARGINWIDTH=0 MARGINHEIGHT=0 ONLOAD="">

<html:errors/>

<bean:define id="usuario" name="usuario" type="br.com.digitaxi.controleacesso.usuario.Usuario" />

<html:form action='Usuario/UpdateUsuario.do' method="POST">
<table width='100%'>
	<tr>
     <TD style="border:3px inset #FFCC00;border-right:0px;border-top:0;border-left:0; text-align:left; font-size:10; color:#414141;">
       <FONT SIZE="4" COLOR=""><bean:message bundle="Usuario" key="controleacesso.usuario.nomeCasoUso" /></font>
     </TD>
	</tr>
</table>
<table>
	<tr>
	  <TD>
	   <TABLE class=fancy width=100% cellpadding=4>
	    <TR>
	     <TD height=25px ><bean:message bundle="Usuario" key="controleacesso.usuario.login" />:<span class=requiredfield>*</span></TD>
	     <TD height=25px><html:text property="login" name="usuario" styleId="login" styleClass="textbox" size='48' maxlength="45"/></TD>
	    </TR>
	    <TR>
	     <TD height=25px ><bean:message bundle="Usuario" key="controleacesso.usuario.senha" />:<span class=requiredfield>*</span></TD>
	     <TD height=25px><html:text property="senha" name="usuario" styleId="senha" styleClass="textbox" size='48' maxlength="20"/></TD>
	    </TR>
	    <TR>
	     <TD height=25px ><bean:message bundle="Usuario" key="controleacesso.usuario.perfil" />:<span class=requiredfield>*</span></TD>
	     <TD height=25px><html:select property="perfil" styleId="perfil" >
							<html:options collection="perfis" property="idPerfil" labelProperty="nome"/>
	     				</html:select>
	     </TD>
	    </TR>
	    <tr>
	     <TD height=25px ><bean:message bundle="Usuario" key="controleacesso.usuario.descricao" />:</TD>
	     <TD height=25px><html:textarea property='descricao' name="usuario" styleId="descricao" styleClass='textbox' rows="4" cols="50"/></TD>
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

<html:hidden property="idUsuario" styleId="idUsuario" value="${usuario.idUsuario}"/>

</html:form>
</BODY>
</HTML>