
package linfca;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.jdom.Element;

public class ListarTipoSexoFeature implements Feature {

	/**
	 * <pre>
	 * <in/>
	 * 
	 * <out>
	 * 	  <tipo-sexo>
	 * 		<cod-tipo-sexo>1</cod-tipo-sexo>
	 * 		<descricao-tipo-sexo>Feminino</descricao-tipo-sexo>
	 * 	  </tipo-sexo>*
	 * </out> 
	 * </pre>

	 * @see Feature#process(Element)
	 */
	public Element process(Element in) throws Exception {
		Connection con = Controller.getInstance().makeConnection();
		PreparedStatement ps = con.prepareStatement("select * from tipo_sexo");
		ResultSet rs = ps.executeQuery();
		Element out = new Element("out");		
		while (rs.next()) {
			Element tipoSexo = new Element("tipo-sexo");
			int codigo = rs.getInt("cod_tipo_sexo");
			String descricao = rs.getString("desc_tipo_sexo");
			Element codigoXML = new Element("cod-tipo-sexo");
			codigoXML.setText("" +codigo);			
			tipoSexo.getChildren().add(codigoXML);
			Element descricaoXML = new Element("descricao-tipo-sexo");
			descricaoXML.setText(descricao);
			tipoSexo.getChildren().add(descricaoXML);
			out.getChildren().add(tipoSexo);
		}
		rs.close();
		ps.close();
		con.close();
		return out;
	}
}
