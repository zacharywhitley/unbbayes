<%@page import="linfca.*, 
        java.sql.*, 
        org.jdom.Element, 
        java.util.Iterator" 
        errorPage="" %> 
<%@include file="/util.jsp" %> 
<%@include file =  "/design/cabecalho.jsp"%>

        <tr>
          <td align="right" valign="top"><img height="86" src="<%=path%>/design/imagens/logo_computador.gif" width="174" border="0" hspace="20" alt="Sele��o de Computador"></td>
          <td>
		  <BR>
            <FORM name="selecionar" action="<%=path%>/controle/computador/lancar-computador-exec.jsp" method="post">
            
            <table width="100%" border="0" cellspacing="5" cellpadding="0" align="center">
            
              <tr>
              
                <td colspan=2>
                  <P class="header">Escolha o computador que ir� utilizar:</P><br>
				</td>
				
			  </tr>
			  
              <tr>
              
                <td width="30%">
                  <P>Computador</P></td>
                <td width="70%"></td>
                
			  </tr>
			  
              <tr>
              
                <td width="30%">                
                  <select name="cod-computador" id="tipo_computador_disponivel" size="15">
                  <% 
		             Feature  listarComputadorDisponivel = new ListarComputadorDisponivelFeature();
			         Element tiposXML = listarComputadorDisponivel.process(null);
			         Iterator tipos = tiposXML.getChildren().iterator();
			         while (tipos.hasNext()) {
		  	            Element tipo = (Element) tipos.next();
		          %>
                  <option value="<%= ((Element)tipo.getChild("cod-computador")).getText() %>"> 
                  <%= ((Element)tipo.getChild("nome-sala")).getText() + " - " + 
                      ((Element)tipo.getChild("descricao-computador")).getText() %> </option>
                  <% }	%>
                </td>
                  
                <td width="70%"></td>
                
              </tr>
                
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