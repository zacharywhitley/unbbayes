package linfca.gerencia.controle;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Date;

import org.jdom.Element;

import java.security.*;

import linfca.Controller;
import linfca.Feature;
import linfca.util.Base64;

public class ValidarUsuarioFeature implements Feature {
	
	/**
	 * <pre>
	 * <in>
	 *    <login>Fulano</login>
	 *    <senha>asdf</senha>
	 * </in>
	 * 
	 * <out>
	 *    <entrar/> 
	 *      | 
	 * 	  <sair>
	 * 		 <cod-lancamento>4</cod-lancamento>
	 * 		 <cod-computador>3></cod-computador>	
	 *    </sair>
	 *      |
	 *    <false/>
	 * </out> 
	 * </pre>
	 * @see Feature#process(Element)
	 */
	public Element process(Element in) throws Exception {
		Connection con = Controller.getInstance().makeConnection();
		String login = in.getChild("identificacao").getTextTrim();
		String senha = in.getChild("senha").getTextTrim();		
		Element out = new Element("out");
		
		PreparedStatement ps = con.prepareStatement("SELECT * FROM usuario WHERE identificacao = ? OR cpf = ?");
		ps.setString(1,login);
		ps.setString(2,login);
		
		ResultSet rs = ps.executeQuery();
	
		if (rs.next()) {
			
			MessageDigest md = MessageDigest.getInstance("MD5");
			if (md.isEqual(md.digest(senha.getBytes()), Base64.decode(rs.getBytes("senha")))) {
				
				ps = con.prepareStatement(
					"SELECT cod_lancamento, cod_computador" +
					" FROM lancamento" +
					" WHERE cod_usuario = ? AND dt_hora_fim_lancamento IS NULL"
				);
				long codUsuario = rs.getLong("cod_usuario");
				ps.setLong(1, codUsuario);
				rs = ps.executeQuery();
				if (rs.next()) {
					Element sair = new Element("sair");
					Element codLancamento = new Element("cod-lancamento");
					codLancamento.setText("" + rs.getLong("cod_lancamento"));
					sair.getChildren().add(codLancamento);

					Element codComputador = new Element("cod-computador");
					codComputador.setText("" + rs.getLong("cod_computador"));
					sair.getChildren().add(codComputador);

					out.getChildren().add(sair);
				} else {
					Element entrar = new Element("entrar");
					out.getChildren().add(entrar);
				}
				
			} else {
				System.out.println("Senha não confere!!!");
				out.getChildren().add(new Element("false"));
			}
			
		} else {
			out.getChildren().add(new Element("false"));
		}
		
		ps.close();		
		rs.close();		
		con.close();		
		return out;
	}
}
