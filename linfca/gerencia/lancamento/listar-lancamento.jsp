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

<table width="75%" border="1">
  <tr> 
    <td width="22%">Usu&aacute;rio</td>
    <td width="25%">Foto</td>
    <td width="25%">Data-Hora In&iacute;cio</td>
    <td width="28%">Data-Hora Fim</td>
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
    <td><%= lan.getChildTextTrim("nome-usuario")  %></td>
    <td><img src="<%=path + "/tmp/" + foto.getName() %>" border="0" hspace="20" alt="Foto do Usuário"></td>
    <td><%= lan.getChildTextTrim("data-hora-inicio")  %></td>
    <td><%= lan.getChildTextTrim("data-hora-fim")  %></td>
  </tr>
  <%     }  %>
</table>
<%@include file =  "/design/rodape.jsp"%>
