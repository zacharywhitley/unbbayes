package linfca;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import org.jdom.Element;

import java.awt.image.RescaleOp;
import javax.servlet.*;
import javax.servlet.http.*;

public class ValidarUsuario extends HttpServlet {
	public void service (HttpServletRequest req, HttpServletResponse res) throws ServletException {
		Element in = new Element("in");
		Element identificacao = new Element("identificacao");
		String idStr = req.getParameter("identificacao");
		identificacao.setText(idStr);		
		Element senha = new Element("senha");
		senha.setText(req.getParameter("senha"));

		in.getChildren().add(identificacao);		
		in.getChildren().add(senha);
		
		Feature feature = new ValidarUsuarioFeature();
		try {
			Element out = feature.process(in);
			if (out.getChild("false") != null) {
				throw new ServletException("Informação inválida!");
			} else {
				Connection con = Controller.getInstance().makeConnection();
				PreparedStatement ps = con.prepareStatement(
										"select cod_usuario, nome from usuario" +
										" where identificacao = ?"
										);
				ps.setString(1, idStr);
				ResultSet rs = ps.executeQuery();
				rs.next();
				req.setAttribute("nome-usuario", rs.getString("nome"));
				if (out.getChild("entrar") != null) {
					req.setAttribute("cod-usuario", "" + rs.getLong("cod_usuario"));
					RequestDispatcher dispatcher = req.getRequestDispatcher(req.getContextPath() + 
											"/controle/computador/selecionar-computador.jsp");
					dispatcher.forward(req, res);				
				} else {
																				
				}
			}
		} catch (Exception e) {
			throw new ServletException(e);
		}
	}
}
