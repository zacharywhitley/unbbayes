<%@page import="linfca.*, 
        java.sql.*, 
        org.jdom.Element, 
        java.util.Iterator" 
        errorPage="" %> 
<%@include file="/util.jsp" %> 
<%@include file =  "/design/cabecalho.jsp"%>

<% 
   String destino = path + "/controle/computador/lancar-computador-exec.jsp";

%>

        <tr>
          <td align="right" valign="top"><img height="86" src="<%=path%>/design/imagens/logo_computador.gif" width="174" border="0" hspace="20" alt="Seleção de Computador"></td>
          <td>
		  <BR>
            <table width="100%" border="0" cellspacing="5" cellpadding="0" align="center">
            
              <tr>
              
                <td colspan=2>
                  <P class="header">Escolha o computador que irá utilizar:</P><br>
				</td>
				
			  </tr>
			  
              <tr>
              
                <td width="30%">
                  <P>Computador</P></td>
                <td width="70%"></td>
                
			  </tr>
			  
              <tr>
              
                <td width="30%"> <h3> 
                    <% 
		             Feature  listarComputadorDisponivel = new ListarComputadorDisponivelFeature();
			         Element tiposXML = listarComputadorDisponivel.process(null);
			         Iterator tipos = tiposXML.getChildren().iterator();
					 if (! tipos.hasNext()) {
					 %>
                    Nenhum computador disponível 
                    <% } else {  %>
					<table>
					   <tr>
					      <td>Sala</td>
						  <td>Computador</td>
					   </tr>
                      <%
				         while (tipos.hasNext()) {
			  	            Element tipo = (Element) tipos.next();
			          %>
					   <tr>
					      <td><%= tipo.getChildTextTrim("nome-sala") %></td>
					      <td><a href="<%= destino %>?cod-usuario=<%=request.getAttribute("cod-usuario")%>&cod-computador=<%= tipo.getChildTextTrim("cod-computador") %>"><%= tipo.getChildTextTrim("descricao-computador") %></a></td>
					   </tr>						
                      <% }	%>
					</table>
                     <% } %>
                  </h3>
				  </td>                  
                <td width="70%"><img src="<%=path%>/design/imagens/mapa_linf.gif" border="0" hspace="20" alt="Mapa do Linf"></td>
                
              </tr>
			</table>
		  </td>
		</tr>
		
<%@include file =  "/design/rodape.jsp"%>