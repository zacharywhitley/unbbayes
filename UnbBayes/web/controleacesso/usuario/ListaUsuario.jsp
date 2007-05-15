<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
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
<SCRIPT TYPE="text/javascript">

	var flagPaginacao = true;

    function setAllCheckBoxes() {
    	
    	if ((document.getElementById("selectAll") == null) || (document.forms[0].selecao == null))
    		return;

		for(var i = 0; i < document.forms[0].selecao.length; i++) {
			if (document.forms[0].selecao[i].checked != document.getElementById("selectAll").checked) {
				document.forms[0].selecao[i].checked = document.getElementById("selectAll").checked;
			}
		}
			
	    habilitaSelecao();
    }

	function habilitaSelecao() {
		var flag = false;
    	if (document.forms[0].selecao.length == null) {
			if (document.forms[0].selecao.checked) {
				flag = true;
			}
    	} else {
			for(var i = 0; i < document.forms[0].selecao.length; i++) {
				if (document.forms[0].selecao[i].checked) {
					flag = true;
				}
			}
    	}
    	
    	// Verifica se pode habilitar/desabilitar botão de checagem de todos
    	var flagTodos = verificaTodosSelecionados();
    	if (flagTodos) {
    		document.forms[0].selectAll.checked = true;
    	} else {
    		document.forms[0].selectAll.checked = false;
    	}
    	
    	// Verifica se pode habilitar o botão de exclusão
    	if (flagPaginacao) {
			if (flag) {
				document.getElementById("btnExcluir").disabled=false;
				document.getElementById("btnExcluir").focus();			
			} else {
				document.getElementById("btnExcluir").disabled=true;
			}
		}
	}
	
	function verificaTodosSelecionados() {
		var flagTodos = true;
		for(var i = 0; i < document.forms[0].selecao.length; i++) {
			if (!document.forms[0].selecao[i].checked) {
				flagTodos = false;
				return flagTodos;
			}
		}
		return flagTodos;
	}

function limpaFormularios() {
	for(var i = 0; i < document.forms.length; i++) {
		for(var e = 0; e < document.forms[i].length; e++){
			if(document.forms[i].elements[e].type == "text") {
				document.forms[i].elements[e].value="";
			}
			if(document.forms[i].elements[e].type == "select-one") {
				document.forms[i].elements[e].value="";
			}
		}
	}
}

	function enviarAssinatura(intUrl) { 
		var url = "";
		switch (intUrl) {
			case 0:
				url = "${pageContext.request.contextPath}/Usuario/PrepareCreateUsuario.do";
				break;
			case 1:
				if (confirm('Deseja apagar os registros selecionados?')) {
					url = "${pageContext.request.contextPath}/Usuario/ExcluirUsuario.do";
				} else {
					return;
				}
				break;
			case 2:
				url = "${pageContext.request.contextPath}/Usuario/PrepareUpdateUsuario.do";
				break;
			case 3:
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

</HEAD>
<BODY BGCOLOR=#FFFFFF LEFTMARGIN=0 TOPMARGIN=0 MARGINWIDTH=0 MARGINHEIGHT=0 ONLOAD="">

<html:form action='Usuario/PrepareSelecionarUsuario.do' method="POST" >

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
	     <TD height=25px ><bean:message bundle="Usuario" key="controleacesso.usuario.login" />:</TD>
	     <TD height=25px><html:text property="loginPesquisa" styleId="loginPesquisa" styleClass="textbox" size='48' maxlength="45"/></TD>
	    </TR>
	   </TABLE>
	  </TD>
	</tr>
<table>

<table>
	<tr>
		<td>
			<input type='button' onClick='enviarAssinatura(3);' name='btnPesquisar' value='Pesquisar'>
			&nbsp;
			<input type='button' onClick='javascript:limpaFormularios();' name='limparBtn' value='Limpar'>
		</td>
	</tr>
</table>

<table width='100%'>
	<tr>
		<td >
			<table width='98%' border='0' cellspacing='1' cellpadding='1' style='BORDER-RIGHT: #ff9900 1px solid; BORDER-TOP: #ff9900 1px solid; BORDER-LEFT: #ff9900 1px solid; BORDER-BOTTOM: #ff9900 1px solid'>
			<tr>
				<th bgcolor="ff9900" width='5%' align="left">
		    		<input type='checkbox' name='selectAll' id='selectAll' value='0' onclick='setAllCheckBoxes();'/>		
				</th>
				<th bgcolor="ff9900" width='45%'><bean:message bundle="Usuario" key="controleacesso.usuario.login" /></th>
				<th bgcolor="ff9900" width='50%'><bean:message bundle="Usuario" key="controleacesso.usuario.perfil" /></th>
			</tr>
			<%int i=0;%>
			<c:forEach items="${col}" var="item">
			<%if ((i%2)==0) {%>
			<tr>
				<td align="left">
					<INPUT type="checkbox" name="selecao" value='${item.idUsuario}' onclick="habilitaSelecao();"/> 
				</td>
				<td>
					<a style='cursor:pointer;' onclick="document.forms[0].idUsuario.value='${item.idUsuario}';enviarAssinatura(2);" >${item.login}</a>
				</td>
				<td >
					<a style='cursor:pointer;' onclick="document.forms[0].idUsuario.value='${item.idUsuario}';enviarAssinatura(2);" >${item.perfil.nome}</a>
				</td>
			</tr>
			<%} else {%>
			<tr>
				<td bgcolor="ffdd88" align="left">
					<INPUT type="checkbox" name="selecao" value='${item.idUsuario}' onclick="habilitaSelecao();"/> 
				</td>
				<td bgcolor="ffdd88">
					<a style='cursor:pointer;' onclick="document.forms[0].idUsuario.value='${item.idUsuario}';enviarAssinatura(2);" >${item.login}</a>
				</td>
				<td bgcolor="ffdd88">
					<a style='cursor:pointer;' onclick="document.forms[0].idUsuario.value='${item.idUsuario}';enviarAssinatura(2);" >${item.perfil.nome}</a>
				</td>
			</tr>
			<%}%>
			<%i++;%>
			</c:forEach>
			</table>
		</td>
	</tr>
</table>

<table>
	<tr>
		<td>
			<input type='button' onClick='enviarAssinatura(0);' name='btnIncluir' id='btnIncluir' value='Incluir'>
			&nbsp;
			<input type='button' onClick='enviarAssinatura(1);' name='btnExcluir' id='btnExcluir' value='Excluir Selecionados' disabled="disabled">
		</td>
	</tr>
</table>

<html:hidden property="idUsuario"/>

</html:form>

</BODY>
</HTML>
