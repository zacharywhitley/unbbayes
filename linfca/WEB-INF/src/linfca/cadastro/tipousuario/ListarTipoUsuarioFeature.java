package linfca.cadastro.tipousuario;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import linfca.Controller;
import linfca.Feature;

import org.jdom.Element;

public class ListarTipoUsuarioFeature implements Feature {

	/**
	 * <pre>
	 * <in/>
	 * 
	 * <out>
	 * 	  <tipo-usuario>
	 * 		<cod-tipo-usuario>1</cod-tipo-usuario>
	 * 		<descricao-tipo-usuario>Aluno</descricao-tipo-usuario>
	 * 	  </tipo-usuario>*
	 * </out> 
	 * </pre>
	 * @see Feature#process(Element)
	 */
	public Element process(Element in) throws Exception {
		Connection con = Controller.getInstance().makeConnection();
		PreparedStatement ps = con.prepareStatement("select * from tipo_usuario");
		ResultSet rs = ps.executeQuery();
		Element out = new Element("out");		
		while (rs.next()) {
			Element tipoUsuario = new Element("tipo-usuario");
			int codigo = rs.getInt("cod_tipo_usuario");
			String descricao = rs.getString("desc_tipo_usuario");
			Element codigoXML = new Element("cod-tipo-usuario");
			codigoXML.setText("" + codigo);			
			tipoUsuario.getChildren().add(codigoXML);
			Element descricaoXML = new Element("descricao-tipo-usuario");
			descricaoXML.setText(descricao);
			tipoUsuario.getChildren().add(descricaoXML);
			out.getChildren().add(tipoUsuario);
		}
		rs.close();
		ps.close();
		con.close();
		return out;
	}
}