<%@page import="linfca.*, 
		linfca.cadastro.*,
        java.sql.*, 
        org.jdom.Element, 
        java.util.Iterator" 
        errorPage="/design/erro.jsp" %> 
<%@include file="/util.jsp" %> 
<%@include file =  "/design/cabecalho.jsp"%>

<% 
   String destino = path + "/gerencia/equipamento/lancar-equipamento-exec.jsp";

%>

        <tr>
          <td>
		  <BR>
            <table width="100%" border="0" cellspacing="5" cellpadding="0" align="center">
            
              <tr>
              
                <td>
                  <P class="header">Olá, <%=request.getAttribute("nome")%> (<%=request.getAttribute("identificacao")%>), escolha o computador que irá utilizar:</P><br>
		</td>
				
	</tr>
			  
	  
              <tr>
              
                <td>
                    <% 
	                 Feature  listarComputadorDisponivel = new ListarGenericoFeature();
					 Element in = new Element("in");
					 Element whereXML = new Element("where");
					 
					 Element descTipo = new Element("cod_tipo_equipamento");
					 descTipo.setText("Computador");
					 whereXML.getChildren().add(descTipo);
					
					 descTipo = new Element("cod_tipo_situacao");
					 descTipo.setText("Disponível");
					 whereXML.getChildren().add(descTipo);					 
					 in.getChildren().add(whereXML);					 
				         Element tiposXML = listarComputadorDisponivel.process(in);
				         Iterator tipos = tiposXML.getChildren().iterator();
					 if (! tipos.hasNext()) {
					 %>
                    Nenhum computador disponível 
			              <% } else {  %>
					<table cellspacing="5" cellpadding="5" align="left" valign="top" width="100%">
					  <% while (tipos.hasNext()) { 
						Element sala = (Element) tipos.next();
						String nomeSala = sala.getChildTextTrim("nome-sala");
  			      	  %>
	      				   <td>
							<table cellspacing="5" cellpadding="5" align="left" valign="top">
							<tr><h4>Sala <%= nomeSala %></h4></tr>
							
					  <%
						Iterator comps = sala.getChildren("equipamento").iterator();
  						while (comps.hasNext()) {
							Element c = (Element) comps.next();

							%>
			 				<tr>
				<a href="<%= destino %>?cod-usuario=<%=request.getAttribute("cod-usuario")%>&cod-equipamento=<%= c.getChildTextTrim("cod-equipamento") %>"><%= c.getChildTextTrim("nome-equipamento") %></a>
							</tr>
					<%	}  %>

						    </table>
       					  </td>
					<%  } %>
					</table>
                           <% } %>
				  </td>                                 
                
              </tr>
			</table>
		  </td>
		</tr>
		
<%@include file =  "/design/rodape.jsp"%>