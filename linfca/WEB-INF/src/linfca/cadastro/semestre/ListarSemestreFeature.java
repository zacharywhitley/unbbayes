
package linfca.cadastro.semestre;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import linfca.Controller;
import linfca.Feature;

import org.jdom.Element;

public class ListarSemestreFeature implements Feature {

	/**
	 * <pre>
	 * <in/>
	 * 
	 * <out>
	 * 	  <semestre>
	 * 		<cod-semestre>1</cod-semestre>
	 * 		<descricao-semestre>Aluno</descricao-semestre>
	 * 	  </semestre>*
	 * </out> 
	 * </pre>
	 * @see Feature#process(Element)
	 */
	public Element process(Element in) throws Exception {
		Connection con = Controller.getInstance().makeConnection();
		PreparedStatement ps = con.prepareStatement("select * from semestre");
		ResultSet rs = ps.executeQuery();
		Element out = new Element("out");		
		while (rs.next()) {
			Element semestre = new Element("semestre");
			int codigo = rs.getInt("cod_semestre");
			String descricao = rs.getString("desc_semestre");
			Element codigoXML = new Element("cod-semestre");
			codigoXML.setText("" + codigo);			
			semestre.getChildren().add(codigoXML);
			Element descricaoXML = new Element("descricao-semestre");
			descricaoXML.setText(descricao);
			semestre.getChildren().add(descricaoXML);
			out.getChildren().add(semestre);
		}
		rs.close();
		ps.close();
		con.close();
		return out;
	}
}
