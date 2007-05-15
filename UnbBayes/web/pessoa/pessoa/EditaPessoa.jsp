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
				if(!validatePessoaForm(document.forms[0])) {
					return;
				}			
				url = "${pageContext.request.contextPath}/Pessoa/UpdatePessoa.do";
				break;
			case 1:
				url = "${pageContext.request.contextPath}/Pessoa/SelecionarPessoa.do";
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

<html:javascript formName="pessoaForm"/>

</HEAD>
<BODY BGCOLOR=#FFFFFF LEFTMARGIN=0 TOPMARGIN=0 MARGINWIDTH=0 MARGINHEIGHT=0 ONLOAD="">

<html:errors/>

<html:form action='Pessoa/UpdatePessoa.do' method="POST">
<table width='100%'>
	<tr>
     <TD style="border:3px inset #FFCC00;border-right:0px;border-top:0;border-left:0; text-align:left; font-size:10; color:#414141;">
       <FONT SIZE="4" COLOR=""><bean:message bundle="Pessoa" key="pessoa.pessoa.nomeCasoUso" /></font>
     </TD>
	</tr>
</table>
<table>
	<tr>
	  <TD>
	   <TABLE class=fancy width=100% cellpadding=4>
	    <TR>
	     <TD height=25px ><bean:message bundle="Pessoa" key="pessoa.pessoa.nome" />:<span class=requiredfield>*</span></TD>
	     <TD height=25px><html:text property="nome" styleId="nome" value='${pessoa.nome}' styleClass="textbox" size='48' maxlength="20"/></TD>
	    </TR>
	    <TR>
	     <TD height=25px ><bean:message bundle="Pessoa" key="pessoa.pessoa.cpf" />:<span class=requiredfield>*</span></TD>
	     <TD height=25px><html:text property="cpf" styleId="cpf" value='${pessoa.cpf}' styleClass="textbox" size='48' maxlength="20"/></TD>
	    </TR>
	    <TR>
	     <TD height=25px ><bean:message bundle="Pessoa" key="pessoa.pessoa.usuario" />:<span class=requiredfield>*</span></TD>
	     <TD height=25px><html:select property="usuario" styleId="usuario" >
							<option value=""></option>
							<html:options collection="usuarios" property="idUsuario" labelProperty="login"/>
	     				</html:select>
	     </TD>
	    </TR>
	    <TR>
	     <TD height=25px ><bean:message bundle="Pessoa" key="pessoa.pessoa.telefoneRes" />:<span class=requiredfield>*</span></TD>
	     <TD height=25px><html:text property="telefoneRes" styleId="telefoneRes" value='${pessoa.telefoneRes}' styleClass="textbox" size='48' maxlength="20"/></TD>
	    </TR>
	    <TR>
	     <TD height=25px ><bean:message bundle="Pessoa" key="pessoa.pessoa.celular" />:<span class=requiredfield>*</span></TD>
	     <TD height=25px><html:text property="celular" styleId="celular" value='${pessoa.celular}' styleClass="textbox" size='48' maxlength="20"/></TD>
	    </TR>
	    <TR>
	     <TD height=25px ><bean:message bundle="Pessoa" key="pessoa.pessoa.telefoneCom" />:<span class=requiredfield>*</span></TD>
	     <TD height=25px><html:text property="telefoneCom" styleId="telefoneCom" value='${pessoa.telefoneCom}' styleClass="textbox" size='48' maxlength="20"/></TD>
	    </TR>
	    <TR>
	     <TD height=25px ><bean:message bundle="Pessoa" key="pessoa.pessoa.email" />:<span class=requiredfield>*</span></TD>
	     <TD height=25px><html:text property="email" styleId="email" value='${pessoa.email}' styleClass="textbox" size='48' maxlength="20"/></TD>
	    </TR>
	    <tr>
	     <TD height=25px ><bean:message bundle="Pessoa" key="pessoa.pessoa.descricao" />:</TD>
	     <TD height=25px><html:textarea property='descricao' styleId="descricao" value='${pessoa.descricao}' styleClass='textbox' rows="4" cols="50"/></TD>
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

<html:hidden property="idPessoa" styleId="idPessoa" value="${pessoa.idPessoa}"/>

</html:form>
</BODY>
</HTML>