<%@page import="linfca.*, 
		linfca.gerencia.usuario.*, 
            linfca.cadastro.*,
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

		Element tipoUsuario = new Element("tipo-usuario");
		tipoUsuario.setText(TipoUsuario.GERENTE);

		in.getChildren().add(identificacao);	
		in.getChildren().add(senha);
		in.getChildren().add(tipoUsuario);
		
		Feature feature = new ValidarUsuarioFeature();
		Element outXML = feature.process(in);
		
		if (outXML.getChild("ok") != null) {
			request.getSession().setAttribute("tipo-usuario", TipoUsuario.GERENTE);
		} else {
		// aqui nunca deve entrar, pois gera exception na feature			 			
		}
		
		RequestDispatcher dispatcher = request.getRequestDispatcher("/gerencia/index.jsp");
		dispatcher.forward(request, response);
%>
</body>
</html>