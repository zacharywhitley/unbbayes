<%@page 
	import="java.sql.*,
	linfca.*,
	linfca.cadastro.*,
	org.jdom.Element"
	errorPage="/design/erro.jsp" %>
	
<%@include file="/util.jsp" %> 
<%@include file =  "/design/cabecalho_gerencia.jsp"%>

<%
	String nomeTabela = "tipo_usuario";
	String codElemento = request.getParameter("cod_elemento");
	Element elementoXML = null;

	if (codElemento != null) {
		Element in = new Element("in");
		Element nomeTabelaXML = new Element("nome-tabela");
		nomeTabelaXML.setText(nomeTabela);
		in.getChildren().add(nomeTabelaXML);	

		Element codElementoXML = new Element("cod-elemento");
		codElementoXML.setText(codElemento);
		in.getChildren().add(codElementoXML);
		
		Feature  detalharElemento = new DetalharGenericoFeature();
		elementoXML = detalharElemento.process(in);	
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
		  <P class="header">Entre com as informações do <%=nomeTabela%> a ser salvo:</P>
<br>
		</td>
	  </tr>
	  <tr>
		<td><p>Descrição</p></td> 
	  </tr>
	  <tr>
		<td>
		        <INPUT maxLength=30 name="string_desc_tipo_usuario"
				 value="<% if (elementoXML != null) { %><%=elementoXML.getChildTextTrim("desc_tipo_usuario")%><% } %>">
		</td>
	  </tr>
	  <INPUT type="hidden" name="nome_tabela" value="<%=nomeTabela%>">
	  <% if (elementoXML != null) { %>
			<INPUT type="hidden" name="int_cod_tipo_usuario" value="<%=codElemento%>">
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