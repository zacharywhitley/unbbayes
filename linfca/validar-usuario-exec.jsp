<%@page import="linfca.*, 
		linfca.gerencia.usuario.*, 
		linfca.gerencia.lancamento.*,
		javax.servlet.RequestDispatcher,
        org.jdom.Element" 
        errorPage="/design/erro_index.jsp" %> 
<html>
<head>
</head>
<body>

<%		
		Element in = new Element("in");
		Element identificacao = new Element("identificacao");
                String idStr = request.getParameter("identificacao").substring(4, 11);
		
		identificacao.setText(idStr);
		in.getChildren().add(identificacao);		
		
		Feature feature = new ValidarUsuarioFeature();
		Element outXML = feature.process(in);
		
		String codUsuario = outXML.getChildTextTrim("cod-usuario");
		String nome = outXML.getChildTextTrim("nome");	
		
		Element inLancamento = new Element("in");
		Element codUsuarioXML = new Element("cod-usuario");
		codUsuarioXML.setText(codUsuario);
		inLancamento.getChildren().add(codUsuarioXML);
		Feature mostrar = new MostrarTipoLancamentoFeature();
		outXML = mostrar.process(inLancamento);	
		
		request.setAttribute("identificacao", idStr);
		request.setAttribute("nome", nome);
		if (outXML.getChild("entrar") != null) {
			request.setAttribute("cod-usuario", codUsuario);			
		} else {
			Element sair = outXML.getChild("sair");
			String codLancamento = sair.getChildTextTrim("cod-lancamento-uso");
			request.setAttribute("cod-lancamento-uso", codLancamento);
		}
		RequestDispatcher dispatcher = request.getRequestDispatcher("/gerencia/equipamento/lancar-equipamento-exec.jsp");
		dispatcher.forward(request, response);
%>

</body>
</html>