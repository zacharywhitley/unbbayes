
package linfca.cadastro.semestre;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Date;
import java.util.StringTokenizer;

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
	 * 		 <cod-semestre>1</cod-semestre>
	 * 		 <descricao-semestre>01/2002</descricao-semestre>
	 *       <dia-inicio>01</dia-inicio>
	 *       <mes-inicio>03</mes-inicio>
	 *       <ano-inicio>2002</ano-inicio>
	 *       <dia-inicio>01</dia-inicio>
	 *       <mes-inicio>07</mes-inicio>
	 *       <ano-inicio>2002</ano-inicio>
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
			
			int codigo = rs.getInt("cod_semestre");			
			String descricao = rs.getString("desc_semestre");
			Date dataInicio = rs.getDate("data-inicio");
			Date dataFim = rs.getDate("data-fim");
			
			StringTokenizer st = new StringTokenizer(dataInicio.toString(), "-");
			String anoInicio = st.nextToken();
			String mesInicio = st.nextToken();
			String diaInicio = st.nextToken();
			
			st = new StringTokenizer(dataFim.toString(), "-");
			String anoFim = st.nextToken();
			String mesFim = st.nextToken();
			String diaFim = st.nextToken();
			
			Element semestre = new Element("semestre");
			Element descricaoXML = new Element("descricao-semestre");
			Element codigoXML = new Element("cod-semestre");
			Element diaInicioXML = new Element("dia-inicio");
			Element mesInicioXML = new Element("mes-inicio");
			Element anoInicioXML = new Element("ano-inicio");
			Element diaFimXML = new Element("dia-fim");
			Element mesFimXML = new Element("mes-fim");
			Element anoFimXML = new Element("ano-fim");
			
			codigoXML.setText("" + codigo);
			descricaoXML.setText(descricao);
			diaInicioXML.setText(diaInicio);
			mesInicioXML.setText(mesInicio);
			anoInicioXML.setText(anoInicio);
			diaFimXML.setText(diaFim);
			mesFimXML.setText(mesFim);
			anoFimXML.setText(anoFim);
			
			semestre.getChildren().add(codigoXML);
			semestre.getChildren().add(descricaoXML);
			semestre.getChildren().add(diaInicioXML);
			semestre.getChildren().add(mesInicioXML);
			semestre.getChildren().add(anoInicioXML);
			semestre.getChildren().add(diaFimXML);
			semestre.getChildren().add(mesFimXML);
			semestre.getChildren().add(anoFimXML);
			
			out.getChildren().add(semestre);
			
		}
		
		rs.close();
		ps.close();
		con.close();
		
		return out;
	}
}
