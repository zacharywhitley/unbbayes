package linfca.gerencia.usuario;

import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import linfca.Controller;
import linfca.Feature;
import linfca.util.Base64;
import org.jdom.Element;

public class ValidarUsuarioFeature implements Feature {
	
	private Connection con;
	
	/**
	 * <pre>
	 * <in>
	 *    <login>9912345</login>
	 * </in>
	 * 
	 * <out>
	 *    <cod-usuario>4</cod-usuario>
	 *    <nome>Fulano</nome>
	 * </out> 
	 * </pre>
	 * 
	 * @throws InvalidUserException caso as informações sejam inválidas.
	 * @see Feature#process(Element)
	 */
	public Element process(Element in) throws Exception {
		con = Controller.getInstance().makeConnection();
		String login = in.getChildTextTrim("identificacao");
		Element out = new Element("out");
		
		PreparedStatement ps = con.prepareStatement(
						"SELECT cod_usuario, nome FROM usuario"+
						" WHERE identificacao = ?");
		ps.setString(1,login);
		
		ResultSet rs = ps.executeQuery();
		
		if (rs.next()) {			
			int codUsuario = rs.getInt("cod_usuario");
			
			if (! dentroSemestre(codUsuario)) {
				ps.close();
				rs.close();		
				con.close();
				throw new InvalidUserException("Usuário sem direito de uso no semestre!");
			}
			
			Element codUsuarioXML = new Element("cod-usuario");
			codUsuarioXML.setText("" + codUsuario);
			String nome = rs.getString("nome");
			Element nomeXML = new Element("nome");
			nomeXML.setText(nome);
			out.getChildren().add(codUsuarioXML);
			out.getChildren().add(nomeXML);				
		} else {
			ps.close();
			rs.close();		
			con.close();
			throw new InvalidUserException("Usuário não cadastrado!");
		}
		ps.close();		
		rs.close();		
		con.close();	
		return out;
	}
	
	
	private boolean dentroSemestre(int codUsuario) throws SQLException {
		Date dataAtual = new Date(System.currentTimeMillis());		
		PreparedStatement ps = con.prepareStatement(
						"SELECT s.data_inicio, s.data_fim FROM usuario u, semestre s"+
						" WHERE u.cod_usuario = ? AND s.cod_semestre = u.cod_semestre");
		ps.setInt(1, codUsuario);
		ResultSet rs = ps.executeQuery();
		
		if (rs.next()) {
			Date dataFim = rs.getDate("s.data_fim");
			return dataAtual.before(dataFim);
		}
		return false;
	}
}
