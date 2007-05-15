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

<script src="../js/numericas-1.0.js"></script>

<SCRIPT>
	function enviarAssinatura(intUrl) { 
		var url = "";
		switch (intUrl) {
			case 0:
				if(!validateMenuForm(document.forms[0])) {
					return;
				}				
				url = "${pageContext.request.contextPath}/Menu/CreateMenu.do";
				break;
			case 1:
				url = "${pageContext.request.contextPath}/Menu/SelecionarMenu.do";
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

<html:javascript formName="menuForm"/>

</HEAD>
<BODY BGCOLOR=#FFFFFF LEFTMARGIN=0 TOPMARGIN=0 MARGINWIDTH=0 MARGINHEIGHT=0 ONLOAD="">

<html:errors/>

<html:form action='Menu/CreateMenu.do' method="POST" >
<table width='100%'>
	<tr>
     <TD style="border:3px inset #FFCC00;border-right:0px;border-top:0;border-left:0; text-align:left; font-size:10; color:#414141;">
       <FONT SIZE="4" COLOR=""><bean:message bundle="Menu" key="controleacesso.menu.nomeCasoUso" /></font>
     </TD>
	</tr>
</table>
<table>
	<tr>
	  <TD>
	   <TABLE class=fancy width=100% cellpadding=4>
	    <TR>
	     <TD height=25px ><bean:message bundle="Menu" key="controleacesso.menu.nome" />:<span class=requiredfield>*</span></TD>
	     <TD height=25px><html:text property="nome" styleId="nome" value='' styleClass="textbox" size='48' maxlength="45"/></TD>
	    </TR>
	    <TR>
	     <TD height=25px ><bean:message bundle="Menu" key="controleacesso.menu.perfil" />:<span class=requiredfield>*</span></TD>
	     <TD height=25px><html:select property="perfil" styleId="perfil" >
							<html:options collection="perfis" property="idPerfil" labelProperty="nome"/>
	     				</html:select>
	     </TD>
	    </TR>
	    <TR>
	     <TD height=25px ><bean:message bundle="Menu" key="controleacesso.menu.menuPai" />:</TD>
	     <TD height=25px><html:select property="menupai" styleId="menupai" >
							<option value=""></option>
							<html:options collection="menus" property="idMenu" labelProperty="nome"/>
	     				</html:select>
	     </TD>
	    </TR>
	    <tr>
	     <TD height=25px ><bean:message bundle="Menu" key="controleacesso.menu.url" />:</TD>
	     <TD height=25px><html:text property="url" styleId="url" value='' styleClass="textbox" size='48' maxlength="45"/></TD>
	    </tr>
	    <tr>
	     <TD height=25px ><bean:message bundle="Menu" key="controleacesso.menu.icon" />:</TD>
	     <TD height=25px><html:text property="icon" styleId="icon" value='' styleClass="textbox" size='48' maxlength="45"/></TD>
	    </tr>
	    <tr>
	     <TD height=25px ><bean:message bundle="Menu" key="controleacesso.menu.prioridade" />:<span class=requiredfield>*</span></TD>
	     <TD height=25px><html:text property="prioridade" styleId="prioridade" value='' styleClass="textbox" size='48' maxlength="3" onkeyup="cNumber('prioridade',this.value);" /></TD>
	    </tr>
	    <tr>
	     <TD height=25px ><bean:message bundle="Menu" key="controleacesso.menu.descricao" />:</TD>
	     <TD height=25px><html:textarea property='descricao' styleId="descricao" value='' styleClass='textbox' rows="4" cols="50"/></TD>
	    </tr>
	   </TABLE>
	  </TD>
	</tr>
<table>
<table>
	<tr>
		<td>
			<input type='button' onClick='enviarAssinatura(0);' name='btnSalvar' value='Incluir'>
			&nbsp;
			<input type='button' onClick='enviarAssinatura(1);' name='btnVoltar' value='Voltar'>
		</td>
	</tr>
</table>

</html:form>
</BODY>
</HTML>
