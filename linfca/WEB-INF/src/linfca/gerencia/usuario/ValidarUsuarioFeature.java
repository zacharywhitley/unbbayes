package linfca.gerencia.usuario;

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
		String login = in.getChildTextTrim("identificacao");
		String senha = in.getChildTextTrim("senha");
		Element out = new Element("out");
		
		PreparedStatement ps = con.prepareStatement("SELECT * FROM usuario WHERE identificacao = ? OR cpf = ?");
		ps.setString(1,login);
		ps.setString(2,login);
		
		ResultSet rs = ps.executeQuery();
	
		if (rs.next()) {
			
			MessageDigest md = MessageDigest.getInstance("MD5");
			if (md.isEqual(md.digest(senha.getBytes()), Base64.decode(rs.getBytes("senha")))) {
				
				ps = con.prepareStatement(
					"SELECT cod_lancamento_uso, cod_equipamento" +
					" FROM lancamento_uso " +
					" WHERE cod_usuario = ? AND dt_hora_fim_lancamento_uso IS NULL"
				);
				long codUsuario = rs.getLong("cod_usuario");
				ps.setLong(1, codUsuario);
				rs = ps.executeQuery();
				if (rs.next()) {
					Element sair = new Element("sair");
					Element codLancamento = new Element("cod-lancamento-uso");
					codLancamento.setText("" + rs.getLong("cod_lancamento_uso"));
					sair.getChildren().add(codLancamento);

					Element codEquipamento = new Element("cod-equipamento");
					codEquipamento.setText("" + rs.getLong("cod_equipamento"));
					sair.getChildren().add(codEquipamento);

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
