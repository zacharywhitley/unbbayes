<%@page import="linfca.*, 
        java.sql.*, 
        org.jdom.Element, 
        java.util.Iterator" 
        errorPage="" %> 
<%@include file="/util.jsp" %> 
<%@include file =  "/design/cabecalho.jsp"%>

        <tr>
          <td align="right" valign="top"><img height="86" src="<%=path%>/design/imagens/logo_computador.gif" width="174" border="0" hspace="20" alt="Seleção de Computador"></td>
          <td>
		  <BR>
            <FORM name="selecionar" action="<%=path%>/controle/computador/lancar-computador-exec.jsp" method="post">
            
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
                    <select name="cod-computador" id="cod-computador" size="25">
                      <%
			         while (tipos.hasNext()) {
		  	            Element tipo = (Element) tipos.next();
		          %>
                      <option value="<%= ((Element)tipo.getChild("cod-computador")).getText() %>"> 
                      <%= ((Element)tipo.getChild("nome-sala")).getText() + " - " + 
                      ((Element)tipo.getChild("descricao-computador")).getText() %> </option>
                      <% }	%>
                    </select>
                    <% } %>
                  </h3></td>
                  
                <td width="70%"><img src="<%=path%>/design/imagens/mapa_linf.gif" border="0" hspace="20" alt="Mapa do Linf"></td>
                
              </tr>
              
              <input type="hidden" name="cod-usuario" value="<%=request.getAttribute("cod-usuario")%>">
                
              <tr>
                <td colspan=2>
                  <P><INPUT type="submit" value="Processar">
                  </P><br><br>				  
				</td>
				</form>
			  </tr>
			</table>
		  </td>
		</tr>
		
<%@include file =  "/design/rodape.jsp"%>