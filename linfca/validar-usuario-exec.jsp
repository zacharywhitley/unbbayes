<%@page import="linfca.*, 
		linfca.gerencia.usuario.*, 
		java.sql.*,
		javax.servlet.RequestDispatcher,
        org.jdom.Element" 
        errorPage="" %> 
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
		if (outXML.getChild("false") != null) {
			throw new ServletException("Informação de autenticação inválida!");
		} else {
			Connection con = Controller.getInstance().makeConnection();
			PreparedStatement ps = con.prepareStatement(
									"select cod_usuario, nome from usuario" +
									" where identificacao = ? or cpf = ?"
									);
			ps.setString(1, idStr);
			ps.setString(2, idStr);
			ResultSet rs = ps.executeQuery();
			rs.next();				
			if (outXML.getChild("entrar") != null) {
				request.setAttribute("nome-usuario", rs.getString("nome"));
				request.setAttribute("cod-usuario", "" + rs.getLong("cod_usuario"));
				RequestDispatcher dispatcher = request.getRequestDispatcher("/controle/computador/selecionar-computador.jsp");					
				dispatcher.forward(request, response);
			} else {
				Element sair = outXML.getChild("sair");
				String codLancamento = sair.getChildTextTrim("cod-lancamento-uso");
				request.setAttribute("cod-lancamento-uso", codLancamento);
				
				String codEquipamento = sair.getChildTextTrim("cod-equipamento");
				request.setAttribute("cod-equipamento", codEquipamento);

				RequestDispatcher dispatcher = request.getRequestDispatcher("/gerencia/computador/lancar-computador-exec.jsp");
				dispatcher.forward(request, response);
			}
		}
%>
</body>
</html>