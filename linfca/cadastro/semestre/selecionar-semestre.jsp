<%@page import="linfca.*,
		linfca.cadastro.semestre.*,  
        java.sql.*, 
        org.jdom.Element, 
        java.util.Iterator" 
        errorPage="/design/erro.jsp" %> 
<%@include file="/util.jsp" %> 
<%@include file =  "/design/cabecalho_gerencia.jsp"%>

        <tr>
          <td align="right" valign="top"><img height="86" src="<%=path%>/design/imagens/logo_semestre.gif" width="174" border="0" hspace="20" alt="Seleção de Usuário"></td>
          <td>
		  <BR>
            <FORM name="selecionar" action="<%=path%>/cadastro/semestre/salvar-semestre.jsp" method="post">
            
            <table width="100%" border="0" cellspacing="5" cellpadding="0" align="center">            
              <tr>
              
                <td colspan=2>
                  <P class="header">Escolha o semestre o qual deseja alterar os dados:</P><br>
				</td>
				
			  </tr>
			  
              <tr>
              
                <td>
                  <P>Semestre</P>
                </td>
                
			  </tr>
			  
              <tr>
              
                <td>
                  <% 
		             Feature  listarSemestre = new ListarSemestreFeature();
			         Element semestreXML = listarSemestre.process(null);
			         Iterator semestres = semestreXML.getChildren().iterator();
			         while (semestres.hasNext()) {
		  	            Element semestre = (Element) semestres.next();
		          %>
                  <a href="<%=path%>/cadastro/semestre/salvar-semestre.jsp?cod_semestre=<%=((Element)semestre).getChildTextTrim("cod-semestre") %>">
                  <%= ((Element)semestre).getChildTextTrim("descricao-semestre") %> </a><br>
                  <% }	%>
                </td>
                  
              </tr>
              
              <tr>
              
                <td>&nbsp;
				  
                </td>
                  
              </tr>
              
              <tr>
                <td colspan=2>
                  <P><INPUT type="submit" value="Novo">
                  </P><br><br>				  
				</td>
				</form>
			  </tr>
			</table>
		  </td>
		</tr>
		
<%@include file =  "/design/rodape_gerencia.jsp"%>