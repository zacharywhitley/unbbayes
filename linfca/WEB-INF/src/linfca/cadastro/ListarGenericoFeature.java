package linfca.cadastro;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.Date;
import java.util.StringTokenizer;

import linfca.Controller;
import linfca.Feature;
import org.jdom.Element;

public class ListarGenericoFeature implements Feature {
	
	/**
	 * <pre>
	 * <in>
 	 * 		<nome-tabela>curso</nome-tabela>
 	 * 		<campo>campo1</campo>
	 * </in>
	 * 
	 * <out>
	 * 	  <elemento>
	 * 		 <cod-elemento>1</cod-elemento>
	 * 		 <campo>Elemento</campo>*  // definido pelo metodo inserirCampo
	 * 	  </elemento>*
	 * </out> 
	 * </pre>
	 * @see Feature#process(Element)
	 */
	public Element process(Element in) throws Exception {
		String nomeTabela = in.getChildTextTrim("nome-tabela");
		
		Connection con = Controller.getInstance().makeConnection();
		StringBuffer sql = new StringBuffer("select cod_" + nomeTabela);
		
		List listaCampos = in.getChildren("campo");
		
		for (int i = 0; i < listaCampos.size(); i++) {
			Element el = (Element) listaCampos.get(i);
			sql.append(", " + el.getTextTrim());
		}
		
		sql.append(" from " + nomeTabela);
		
		PreparedStatement ps = con.prepareStatement(sql.toString());
		ResultSet rs = ps.executeQuery();
		
		Element out = new Element("out");		
		while (rs.next()) {
			Element elementoXML = new Element("elemento");
					
			int codigo = rs.getInt("cod_" + nomeTabela);
			Element codigoXML = new Element("cod-elemento");
			codigoXML.setText("" + codigo);
		
			elementoXML.getChildren().add(codigoXML);
			for (int i = 0; i < listaCampos.size(); i++) {
				Element el = (Element) listaCampos.get(i);
				String campo = el.getTextTrim();
				String valor = rs.getString(campo);
				Element campoXML = new Element(campo);				
				campoXML.setText(valor);
				elementoXML.getChildren().add(campoXML);
			}
			out.getChildren().add(elementoXML);			
		}
		
		rs.close();
		ps.close();
		con.close();
		
		return out;
	}
}
