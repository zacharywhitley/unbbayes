<%@page 
	import="java.sql.*,
	linfca.*,
	linfca.cadastro.curso.*,
	org.jdom.Element"
	errorPage="/design/erro.jsp" %>
	
<%@include file="/util.jsp" %> 
<%@include file =  "/design/cabecalho.jsp"%>

<%
	String codCurso = request.getParameter("cod_curso");
	Element cursoXML = null;

	if (codCurso != null) {	
		Element in = new Element("in");
		Element codCursoXML = new Element("cod-curso");
		codCursoXML.setText(codCurso);
		in.getChildren().add(codCursoXML);
		
		Feature  detalharCurso = new DetalharCursoFeature();
		cursoXML = detalharCurso.process(in);	
	}
%>


<tr>
  <td align="right" valign="top"><img height="86" src="<%=path%>/design/imagens/logo_curso.gif" width="174" border="0" hspace="20" alt="Salvar Curso"></td>
  <td>
  <BR>
	<FORM name="login" action="<%=path%>/cadastro/curso/salvar-curso-exec.jsp" METHOD="post">
	<table width="100%" border="0" cellspacing="5" cellpadding="0" align="center">              
	  <tr>
		<td colspan=2>
		  <P class="header">Entre com as informações do curso a ser salvo:</P>
<br>
		</td>
	  </tr>
	  <tr>
		<td width="50%"><P>Código da Opção</P></td>
		<td width="50%"><p>Descrição</p></td> 
	  </tr>
	  <tr>
		<td width="50%">
				<INPUT maxLength=7 name="string_cod_opcao" 
				 value="<% if (cursoXML != null) { %><%=cursoXML.getChildTextTrim("cod-opcao")%><% } %>">
		</td>
		<td width="50%">
		        <INPUT maxLength=40 name="string_desc_curso"
				 value="<% if (cursoXML != null) { %><%=cursoXML.getChildTextTrim("descricao-curso")%><% } %>">
		</td>
	  </tr>
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
<%@include file =  "/design/rodape.jsp"%>


