<%@page import="linfca.*,
		linfca.cadastro.*,		
        java.sql.*, 
        org.jdom.Element, 
        java.util.*" 
        errorPage="/design/erro.jsp" %> 
<%@include file="/util.jsp" %>
<%@include file =  "/design/cabecalho_gerencia.jsp"%>

<%
	String nomeFeature = "sala";
	ArrayList lista = new ArrayList();
	lista.add("nome_sala");
%>

        <tr>
          <td align="right" valign="top"><img height="86" src="<%=path%>/design/imagens/logo_<%=nomeFeature%>.gif" width="174" border="0" hspace="20" alt="Seleção de <%=nomeFeature%>"></td>
          <td>
		  <BR>
            <FORM name="selecionar" action="<%=path%>/cadastro/<%=nomeFeature%>/salvar-<%=nomeFeature%>.jsp" method="post">
            <table width="100%" border="0" cellspacing="5" cellpadding="0" align="center">
              <tr>
              
                <td colspan=2>
                  <P class="header">Escolha a <%=nomeFeature%> que se deseja alterar os dados:</P><br>
				</td>
				
			  </tr>
			  
              <tr>
              
                <td>
                  <P><%=nomeFeature.toUpperCase() %></P>
                </td>
                
			  </tr>
			  
              <tr>
              
                <td>
                  <% 
		             ListarGenericoFeature listar = new ListarGenericoFeature();
					 for (int i = 0; i < lista.size(); i++) {
						 listar.inserirCampo(lista.get(i).toString());
					 }
					 
					 Element in = new Element("in");
					 Element nomeXML = new Element("nome-tabela");
					 nomeXML.setText(nomeFeature);
					 in.getChildren().add(nomeXML);
					 				 
			         Element elementoXML = listar.process(in);
			         Iterator elementos = elementoXML.getChildren().iterator();
			         while (elementos.hasNext()) {
		  	            Element elemento = (Element) elementos.next();
		          %>
                  <a href="<%=path%>/cadastro/<%=nomeFeature%>/salvar-<%=nomeFeature%>.jsp?cod_<%=nomeFeature%>=<%=((Element)elemento).getChildTextTrim("cod-elemento") %>">
                  <% 
				    for (int i = 0; i < lista.size(); i++) {					
				  %>
				  		<%= elemento.getChildTextTrim(lista.get(i).toString()) + " " %>											
				 <% }  %>
				 				  
				  </a><br>
                  <% }	%>
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