<%@page import="linfca.*, 
		java.sql.*,
        org.jdom.Element" 
        errorPage="" %> 
<html>
<head>
</head>
<body onLoad="javascript:document.form1.submit()">

<%
		String redirect = null;
		
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
				redirect = request.getContextPath() + "/controle/computador/selecionar-computador.jsp";
			} else {
				Element sair = outXML.getChild("sair");
				String codLancamento = ((Element)sair.getChild("cod-lancamento")).getTextTrim();
				request.setAttribute("cod-lancamento", codLancamento);
				redirect = request.getContextPath() + "/controle/computador/lancar-computador-exec.jsp";
			}
		}
%>

  <form name="form1" method="post" action="<%=redirect%>">
  </form>
</body>
</html>