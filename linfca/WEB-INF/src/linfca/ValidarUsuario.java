package linfca;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jdom.Element;

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
				if (out.getChild("entrar") != null) {
					req.setAttribute("nome-usuario", rs.getString("nome"));
					req.setAttribute("cod-usuario", "" + rs.getLong("cod_usuario"));
					RequestDispatcher dispatcher = req.getRequestDispatcher(req.getContextPath() + 
											"/controle/computador/selecionar-computador.jsp");
					dispatcher.forward(req, res);				
				} else {
					Element sair = out.getChild("sair");
					String codLancamento = ((Element)sair.getChild("cod-lancamento")).getTextTrim();
					req.setAttribute("cod-lancamento", codLancamento);
					RequestDispatcher dispatcher = req.getRequestDispatcher(req.getContextPath() + 
											"/controle/computador/lancar-computador-exec.jsp");
					dispatcher.forward(req, res);
				}
			}
		} catch (Exception e) {
			throw new ServletException(e);
		}
	}
}
