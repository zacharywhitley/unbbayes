<%@page import="linfca.*, 
		linfca.cadastro.*, 
		java.sql.*, 
		org.jdom.Element, 
		java.util.Iterator" 
		errorPage="" %>
		<%@include file="/util.jsp" %> 

<%

	String codSemestre = request.getParameter("cod_semestre");
	Element semestreXML = null;
/*
	if (codSemestre != null) {
	
		Element in = new Element("in");
		Element codSemestreXML = new Element("cod-semestre");
		codSemestreXML.setText(codSemestre);
		in.getChildren().add(codSemestreXML);
		
		Feature  detalharSemestre = new DetalharSemestreFeature();
		semestreXML = detalharSemestre.process(in);
	
	}
*/
%>

<%@include file =  "/design/cabecalho.jsp"%>
        <tr>
          <td align="right" valign="top"><img height="86" src="<%=path%>/design/imagens/logo_usuario.gif" width="174" border="0" hspace="20" alt="Salvar Semestre"></td>
          <td>
		  <BR>
            <FORM name="login" action="<%=path%>/cadastro/semestre/salvar-semestre-exec.jsp" METHOD="post" ENCTYPE="multipart/form-data">
            <table width="100%" border="0" cellspacing="5" cellpadding="0" align="center">              
              <tr>
                <td colspan=2>
                  <P class="header">Entre com a descrição do semestre a ser salvado:</P><br>
				</td>
			  </tr>
              <tr>
                <td width="50%"><P>Semestre</P></td>
                <td width="50%"></td> 
			  </tr>
              <tr>
                <td width="50%">
                		<INPUT maxLength=7 name="string_desc_semestre" 
                		 value="<% if (semestreXML != null) { %><%=semestreXML.getChild("desc-semestre").getTextTrim()%><% } %>">
                </td>
                <td width="50%">
                  <P>O semestre deve estar no formato SS/AAAA. Ex: 01/2002</P>
                </td>
              </tr>
              <% if (semestreXML != null) { %>
                    <INPUT type="hidden" name="int_cod_semestre" value="<%=codSemestre%>">
              <% } %>
              <INPUT type="hidden" name="int_cod_semestre" value="7">
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