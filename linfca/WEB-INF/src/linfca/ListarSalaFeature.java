
package linfca;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.jdom.Element;

public class ListarSalaFeature implements Feature {

	/**
	 * <pre>
	 * <in/>
	 * 
	 * <out>
	 * 	  <sala>
	 * 		<cod-sala>1</cod-sala>
	 * 		<nome-sala>Laboratorio 1</nome-sala>
	 * 		<descricao-sala>DESCRICAO</descricao-sala>
	 * 	  </sala>*
	 * </out> 
	 * </pre>
	 * @see Feature#process(Element)
	 */
	public Element process(Element in) throws Exception {
		Connection con = Controller.getInstance().makeConnection();
		PreparedStatement ps = con.prepareStatement("select * from sala");
		ResultSet rs = ps.executeQuery();
		Element out = new Element("out");		
		while (rs.next()) {
			Element sala = new Element("sala");
			
 			int codigo = rs.getInt("cod_sala");
			Element codigoXML = new Element("cod-sala");
			codigoXML.setText("" + codigo);			
			sala.getChildren().add(codigoXML);

			String descricao = rs.getString("desc_sala");
			Element descricaoXML = new Element("descricao-sala");
			descricaoXML.setText(descricao);
			sala.getChildren().add(descricaoXML);

			String nome = rs.getString("nome_sala");
			Element nomeXML = new Element("nome-sala");
			nomeXML.setText(nome);
			sala.getChildren().add(nomeXML);
			out.getChildren().add(sala);
		}
		rs.close();
		ps.close();
		con.close();
		return out;
	}

}
