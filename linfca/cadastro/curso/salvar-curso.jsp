<%@page 
	import="java.sql.*,
	linfca.*,
	linfca.cadastro.*,
	org.jdom.Element"
	errorPage="/design/erro.jsp" %>
	
<%@include file="/util.jsp" %> 
<%@include file =  "/design/cabecalho_gerencia.jsp"%>

<%
	String nomeTabela = "curso";
	String codCurso = request.getParameter("cod_elemento");
	Element cursoXML = null;

	if (codCurso != null) {
		Element in = new Element("in");
		Element nomeTabelaXML = new Element("nome-tabela");
		nomeTabelaXML.setText(nomeTabela);
		in.getChildren().add(nomeTabelaXML);	

		Element codCursoXML = new Element("cod-elemento");
		codCursoXML.setText(codCurso);
		in.getChildren().add(codCursoXML);
		
		Feature  detalharCurso = new DetalharGenericoFeature();
		cursoXML = detalharCurso.process(in);	
	}
%>


<tr>
  <td align="right" valign="top"><img height="86" src="<%=path%>/design/imagens/logo_<%=nomeTabela%>.gif" width="174" border="0" hspace="20" alt="Salvar <%=nomeTabela%>"></td>
  <td>
  <BR>
	<FORM name="login" action="<%=path%>/cadastro/salvar-generico-exec.jsp" METHOD="post">
	<table width="100%" border="0" cellspacing="5" cellpadding="0" align="center">              
	  <tr>
		<td colspan=2>
		  <P class="header">Entre com as informa��es do <%=nomeTabela%> a ser salvo:</P>
<br>
		</td>
	  </tr>
	  <tr>
		<td width="50%"><P>C�digo da Op��o</P></td>
		<td width="50%"><p>Descri��o</p></td> 
	  </tr>
	  <tr>
		<td width="50%">
				<INPUT maxLength=7 name="string_cod_opcao" 
				 value="<% if (cursoXML != null) { %><%=cursoXML.getChildTextTrim("cod_opcao")%><% } %>">
		</td>
		<td width="50%">
		        <INPUT maxLength=40 name="string_desc_curso"
				 value="<% if (cursoXML != null) { %><%=cursoXML.getChildTextTrim("desc_curso")%><% } %>">
		</td>
	  </tr>
	  <INPUT type="hidden" name="nome_tabela" value="<%=nomeTabela%>">
	  <% if (cursoXML != null) { %>
			<INPUT type="hidden" name="int_cod_curso" value="<%=codCurso%>">
	  <% } %>
	  <tr>
		<td colspan=2>
		  <P><INPUT type="submit" value="Salvar">&nbsp;&nbsp;<INPUT type="reset" value="Limpar">
		  </P><br><br>				  
		</td>
		</form>
	  </tr>
	</table>
  </td>
</tr>
<%@include file =  "/design/rodape_gerencia.jsp"%>


