<%@page import="linfca.gerencia.lancamento.*,
				linfca.*,
				linfca.util.*,
				java.io.*,
				java.util.*,
				java.sql.*,				
				org.jdom.Element" 
       errorPage="" %>
	   
<%@include file="/util.jsp" %> 

<%@include file =  "/design/cabecalho.jsp"%>

        <tr>
          <td align="right" valign="top"><img height="86" src="<%=path%>/design/imagens/logo_lancamento.gif" width="174" border="0" hspace="20" alt="Histórico de Lançamentos"></td>
          <td>
		  <BR>
            <FORM name="listar" action="" method="post">
            
            <table width="100%" border="0" cellspacing="5" cellpadding="0" align="center">
            
              <tr>
              
                <td colspan=2>
                  <P class="header">Histórico de Lançamentos:</P><br>
				</td>
				
			  </tr>
			  
			  <tr>
			  
			    <td colspan=2>
			      <table width="100%" border="1" cellspacing="0" cellpadding="10" align="center">
			      

            
              <tr> 
              
                <td align="center">
                  Usu&aacute;rio
                </td>
                
                <td align="center">
                  Foto
                </td>
                
                <td align="center">
                  Data-Hora In&iacute;cio
                </td>
                
                <td align="center">
                  Data-Hora Fim
                </td>
                
              </tr>
  
  <% 
  		Element in = new Element("in");
		Element dataInicio = new Element("data-hora-inicio");
		if (request.getParameter("data-hora-inicio") != null)  {
			dataInicio.setText(request.getParameter("data-hora-inicio"));
		} else {
			dataInicio.setText(new Timestamp(System.currentTimeMillis() - 1000000L).toString());
		}
	
		in.getChildren().add(dataInicio);
		
		Element dataFim = new Element("data-hora-fim");		
		if (request.getParameter("data-hora-fim") != null)  {
			dataFim.setText(request.getParameter("data-hora-fim"));
		} else {
			dataFim.setText(new Timestamp(System.currentTimeMillis()).toString());
		}
		in.getChildren().add(dataFim);
		
		if (request.getParameter("abertos") != null) {
			in.getChildren().add(new Element("abertos"));
		}
		
		Feature f = new ListarLancamentoFeature();
		Element outXML = f.process(in);
		List children = outXML.getChildren();
		for (int i = 0; i < children.size(); i++) {									
			Element lan = (Element) children.get(i);
			
			byte[] buffer = Base64.decode(Base64.getBinaryBytes(lan.getChildTextTrim("foto-usuario")));
			File foto = File.createTempFile("usuario", null, new File(config.getServletContext().getRealPath("") + "/tmp"));
			FileOutputStream fos = new FileOutputStream(foto);
			fos.write(buffer);
			fos.close();
			foto.deleteOnExit();
  %>
			  
              <tr>
              
                <td align="center">
                  <P><%= lan.getChildTextTrim("nome-usuario")  %></P>
                </td>
                
                <td align="center">
                  <img src="<%=path + "/tmp/" + foto.getName() %>" border="0" hspace="20" alt="Foto do Usuário">
                </td>
                
                <td align="center">
                  <%= lan.getChildTextTrim("data-hora-inicio")  %>
                </td align="center">
                
                <td align="center">
                  <%= lan.getChildTextTrim("data-hora-fim")  %>
                </td>
                
			  </tr>			  
		
  <%     }  %>
  
			      </table>  
			    </td>
			    
			  </tr>  
			  
              <tr>
                <td colspan=2>
                  <P>
                  </P><br><br>
				</td>
				</form>
			  </tr>
			</table>
		  </td>
		</tr>

<%@include file =  "/design/rodape.jsp"%>
