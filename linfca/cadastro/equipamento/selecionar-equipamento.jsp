<%@page import="linfca.*,
		linfca.cadastro.equipamento.*,  
        java.sql.*, 
        org.jdom.Element, 
        java.util.Iterator" 
        errorPage="" %> 
<%@include file="/util.jsp" %> 
<%@include file =  "/design/cabecalho.jsp"%>

        <tr>
          <td align="right" valign="top"><img height="86" src="<%=path%>/design/imagens/logo_equipamento.gif" width="174" border="0" hspace="20" alt="Seleção de Equipamento"></td>
          <td>
		  <BR>
            <table width="100%" border="0" cellspacing="5" cellpadding="0" align="center">
            
              <tr>
              
                <td colspan=2>
                  <P class="header">Escolha o equipamento o qual deseja-se alterar os dados:</P><br>
				</td>				
			  </tr>			  
              <tr>
			  
			  <table width="100%" border="1" cellspacing="0" cellpadding="10" align="center">
					  <tr> 
						<td align="center">
						  <p>Sala</p>
						</td>
						
						<td align="center">
						   <p>Equipamento</p>
						</td>
					  </tr>					  
                  <% 
		             Feature  listarEquipamento = new ListarEquipamentoFeature();
			         Element equipamentosXML = listarEquipamento.process(new Element("in"));
			         Iterator equipamentos = equipamentosXML.getChildren().iterator();
			         while (equipamentos.hasNext()) {
		  	            Element equipamento = (Element) equipamentos.next();
		          %>
				     <tr>
					     <td>
						 		<%= equipamento.getChildTextTrim("nome-sala") %>
						 </td>
						 <td>
						 		<a href="<%=path%>/cadastro/equipamento/salvar-equipamento.jsp?cod_equipamento=<%= equipamento.getChildTextTrim("cod-equipamento") %>"><%= equipamento.getChildTextTrim("nome-equipamento") %></a>
						 </td>
					 </tr>

			 	</table>
			  
                  <option value=""> 
                       </option>
                  <% }	%>
                </td>
                  
              </tr>
			</table>
		  </td>
		</tr>
		
<%@include file =  "/design/rodape.jsp"%>
