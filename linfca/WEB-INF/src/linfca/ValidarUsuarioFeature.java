package linfca;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Date;

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
	 *    <entrar/> 
	 *      | 
	 * 	  <sair>
	 * 		 <cod-lancamento>4</cod-lancamento>
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
		
		PreparedStatement ps = con.prepareStatement("SELECT * FROM usuario WHERE identificacao = ? AND senha = ?");
		ps.setString(1,login);
		ps.setString(2, senha);
		ResultSet rs = ps.executeQuery();
	
		if (rs.next()) {
			ps = con.prepareStatement(
				"SELECT cod_lancamento" +
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
				out.getChildren().add(sair);
			} else {
				Element entrar = new Element("entrar");
				out.getChildren().add(entrar);
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
