package linfca.gerencia.usuario;

import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import linfca.Controller;
import linfca.Feature;
import linfca.util.Base64;
import org.jdom.Element;

public class ValidarUsuarioFeature implements Feature {
	
	/**
	 * <pre>
	 * <in>
	 *    <login>Fulano</login>
	 *    <senha>asdf</senha>
	 * </in>
	 * 
	 * <out>
	 *    <cod-usuario>4</cod-usuario>
	 * </out> 
	 * </pre>
	 * 
	 * @throws InvalidUserException caso as informações sejam inválidas.
	 * @see Feature#process(Element)
	 */
	public Element process(Element in) throws Exception {
		Connection con = Controller.getInstance().makeConnection();
		String login = in.getChildTextTrim("identificacao");
		String senha = in.getChildTextTrim("senha");
		Element out = new Element("out");
		
		PreparedStatement ps = con.prepareStatement(
						"SELECT cod_usuario, senha FROM usuario"+
						" WHERE identificacao = ? OR cpf = ?");
		ps.setString(1,login);
		ps.setString(2,login);
		
		ResultSet rs = ps.executeQuery();
		
		if (rs.next()) {
			
			MessageDigest md = MessageDigest.getInstance("MD5");
			if (md.isEqual(md.digest(senha.getBytes()), Base64.decode(rs.getBytes("senha")))) {
				int codUsuario = rs.getInt("cod_usuario");
				Element codUsuarioXML = new Element("cod-usuario");
				codUsuarioXML.setText("" + codUsuario);
				out.getChildren().add(codUsuarioXML);				
			} else {
	   			ps.close();		
				rs.close();		
				con.close();
				throw new InvalidUserException("Usuário Inválido");
			}
			
		} else {
			ps.close();		
			rs.close();		
			con.close();
			throw new InvalidUserException("Usuário Inválido");
		}
		ps.close();		
		rs.close();		
		con.close();	
		return out;
	}
}
