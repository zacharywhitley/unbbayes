
package linfca;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.jdom.Element;

public class ListarTipoSituacaoFeature implements Feature {

	/**
	 * <pre>
	 * <in/>
	 * 
	 * <out>
	 * 	  <tipo-situacao>
	 * 		<cod-tipo-situacao>1</cod-tipo-situacao>
	 * 		<descricao-tipo-situacao>Feminino</descricao-tipo-situacao>
	 * 	  </tipo-situacao>*
	 * </out> 
	 * </pre>
	 * @see Feature#process(Element)
	 */
	public Element process(Element in) throws Exception {
		Connection con = Controller.getInstance().makeConnection();
		PreparedStatement ps = con.prepareStatement("select * from tipo_situacao");
		ResultSet rs = ps.executeQuery();
		Element out = new Element("out");		
		while (rs.next()) {
			Element tipoSituacao = new Element("tipo-situacao");
			int codigo = rs.getInt("cod_tipo_situacao");
			String descricao = rs.getString("desc_tipo_situacao");
			Element codigoXML = new Element("cod-tipo-situacao");
			codigoXML.setText("" +codigo);			
			tipoSituacao.getChildren().add(codigoXML);
			Element descricaoXML = new Element("descricao-tipo-situacao");
			descricaoXML.setText(descricao);
			tipoSituacao.getChildren().add(descricaoXML);
			out.getChildren().add(tipoSituacao);
		}
		rs.close();
		ps.close();
		con.close();
		return out;
	}

}
