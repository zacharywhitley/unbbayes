<%@page 
	import="java.sql.*,
	linfca.*,
	linfca.cadastro.*,
	org.jdom.Element"
	errorPage="/design/erro.jsp" %>
	
<%@include file="/util.jsp" %> 
<%@include file =  "/design/cabecalho_gerencia.jsp"%>

<%
	String nomeTabela = "sala";
	String codElemento = request.getParameter("cod_sala");
	Element outXML = null;

	if (codElemento != null) {	
		Element in = new Element("in");
		Element nomeTabelaXML = new Element("nome-tabela");
		nomeTabelaXML.setText(nomeTabela);
		in.getChildren().add(nomeTabelaXML);

		Element codElementoXML = new Element("cod-elemento");
		codElementoXML.setText(codElemento);
		in.getChildren().add(codElementoXML);
	
		Feature  detalharFeature = new DetalharGenericoFeature();
		outXML = detalharFeature.process(in);
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
		  <P class="header">Entre com as informações da <%=nomeTabela%> a ser salva:</P>
<br>
		</td>
	  </tr>
	  <tr>
		<td><P>Nome</P></td>
	  </tr>
	  <tr>
		<td>
				<INPUT maxLength=7 name="string_nome_sala" 
				 value="<% if (outXML != null) { %><%=outXML.getChildTextTrim("nome_sala")%><% } %>">
		</td>
	  </tr>
	  <tr>
  		<td><p>Descrição</p></td> 
	  </tr>
	  <tr>
  		<td>
		<input size="50"  maxlength="50" name="string_desc_sala" 
		value="<% if (outXML != null) { %><%=outXML.getChildTextTrim("desc_sala")%><% } %>">
		</td>
	  </tr>
	  <INPUT type="hidden" name="nome_tabela" value="<%=nomeTabela%>">
	  <% if (outXML != null) { %>
			<INPUT type="hidden" name="int_cod_sala" value="<%=codElemento%>">
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