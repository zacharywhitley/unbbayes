
package linfca.cadastro.semestre;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.DateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.StringTokenizer;

import linfca.Controller;
import linfca.Feature;
import org.jdom.Element;

public class DetalharSemestreFeature implements Feature {

	/**
	 * <pre>
	 * <in>
	 *    <cod-semestre>1</cod-semestre>
	 * </in>
	 * 
	 * <out>
	 * 	  <cod-semestre>1</cod-semestre>
	 *    <descricao-semestre>02/2002</descricao-semestre>
	 *    <data-inicio>2002-10-14</data-inicio>
	 * 	  <data-fim>
	 * </out> 
	 * </pre>
	 * @see Feature#process(Element)
	 */
	public Element process(Element in) throws Exception {
		
		Connection con = Controller.getInstance().makeConnection();
		
		String codSemestre  = in.getChild("cod-semestre").getTextTrim();
		
		PreparedStatement ps = con.prepareStatement("select * from semestre where cod_semestre = ?");
		
		ps.setInt(1, Integer.parseInt(codSemestre));
		
		ResultSet rs = ps.executeQuery();
		
		Element out = new Element("out");			
		
		while (rs.next()) {						
			
			String descricao = rs.getString("desc_semestre");
			Date dtInicio = rs.getDate("data_inicio");
			Date dtFim = rs.getDate("data_fim");			
			
			/*
			StringTokenizer st = new StringTokenizer(dtNascimento.toString(), "-");			
			String ano = st.nextToken();
			String mes = st.nextToken();
			String dia = st.nextToken();
			*/
			
			Element codSemestreXML = new Element("cod-semestre");
			Element descSemestreXML = new Element("desc-semestre");
			Element dataInicioXML = new Element("data-inicio");
			Element dataFimXML = new Element("data-fim");
						
			codSemestreXML.setText(codSemestre);
			descSemestreXML.setText(descricao);
			dataInicioXML.setText(dtInicio.toString());
			dataFimXML.setText(dtFim.toString());			
			
			out.getChildren().add(codSemestreXML);
			out.getChildren().add(descSemestreXML);
			out.getChildren().add(dataInicioXML);
			out.getChildren().add(dataFimXML);
		}
		
		rs.close();
		ps.close();
		con.close();
		
		return out;
	}

}
