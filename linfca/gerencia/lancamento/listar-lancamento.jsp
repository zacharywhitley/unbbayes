<%@page import="linfca.gerencia.lancamento.*,
				linfca.*,
				linfca.util.*,
				java.io.*,
				org.jdom.Element" 
       errorPage="" %>
	   
<html>
<head>
<title>Listar Lançamentos</title>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
</head>
<body>
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
		dataInicio.setText(request.getParameter("data-hora-inicio"));
		in.getChildren().add(dataInicio);
		
		Element dataFim = new Element("data-hora-fim");		
		dataFim.setText(request.getParameter("data-hora-fim"));
		in.getChildren().add(dataFim);
		
		if (request.getParameter("abertos")) {
			in.getChildren().add("abertos");
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
</body>
</html>
