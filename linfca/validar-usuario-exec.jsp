<%@page import="linfca.*, 
		linfca.gerencia.usuario.*, 
		linfca.gerencia.lancamento.*,
		javax.servlet.RequestDispatcher,
        org.jdom.Element" 
        errorPage="/design/erro.jsp" %> 
<html>
<head>
</head>
<body>

<%		
		Element in = new Element("in");
		Element identificacao = new Element("identificacao");
		String idStr = request.getParameter("identificacao");
		identificacao.setText(idStr);		
		Element senha = new Element("senha");
		senha.setText(request.getParameter("senha"));

		in.getChildren().add(identificacao);		
		in.getChildren().add(senha);
		
		Feature feature = new ValidarUsuarioFeature();
		Element outXML = feature.process(in);
		
		String codUsuario = outXML.getChildTextTrim("cod-usuario");
		
		Element inLancamento = new Element("in");
		Element codUsuarioXML = new Element("cod-usuario");
		codUsuarioXML.setText(codUsuario);
		inLancamento.getChildren().add(codUsuarioXML);
		Feature mostrar = new MostrarTipoLancamentoFeature();
		outXML = mostrar.process(inLancamento);	
		
		if (outXML.getChild("entrar") != null) {
			request.setAttribute("cod-usuario", codUsuario);
			RequestDispatcher dispatcher = request.getRequestDispatcher("/gerencia/equipamento/selecionar-equipamento.jsp");					
			dispatcher.forward(request, response);
		} else {
			Element sair = outXML.getChild("sair");
			String codLancamento = sair.getChildTextTrim("cod-lancamento-uso");
			request.setAttribute("cod-lancamento-uso", codLancamento);
			
			String codEquipamento = sair.getChildTextTrim("cod-equipamento");
			request.setAttribute("cod-equipamento", codEquipamento);
	
			RequestDispatcher dispatcher = request.getRequestDispatcher("/gerencia/equipamento/lancar-equipamento-exec.jsp");
			dispatcher.forward(request, response);
		}
%>
</body>
</html>