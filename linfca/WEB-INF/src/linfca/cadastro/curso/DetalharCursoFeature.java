
package linfca.cadastro.curso;

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

public class DetalharCursoFeature implements Feature {

	/**
	 * <pre>
	 * <in>
	 *    <cod-curso>1</cod-curso>
	 * </in>
	 * 
	 * <out>
	 * 	  <cod-curso>1</cod-curso>
	 *    <cod-opcao>1<cod-opcao>
	 *    <descricao-curso>1</descricao-curso>
	 * </out> 
	 * </pre>
	 * @see Feature#process(Element)
	 */
	public Element process(Element in) throws Exception {
		
		Connection con = Controller.getInstance().makeConnection();
		
		String codCurso = in.getChildTextTrim("cod-curso");
		
		PreparedStatement ps = con.prepareStatement("select * from curso where cod_curso = ?");
		
		ps.setInt(1, Integer.parseInt(codCurso));
		
		ResultSet rs = ps.executeQuery();
		
		Element out = new Element("out");			
		
		while (rs.next()) {						
			String codOpcao = rs.getString("cod_opcao");
			String desc = rs.getString("desc_curso");
			
			Element codCursoXML = new Element("cod-curso");
			Element codOpcaoXML = new Element("cod-opcao");
			Element descXML = new Element("descricao-curso");
			
			codCursoXML.setText(codCurso);
			codOpcaoXML.setText(codOpcao);
			descXML.setText(desc);
			
			out.getChildren().add(codCursoXML);
			out.getChildren().add(codOpcaoXML);
			out.getChildren().add(descXML);
		}
		
		rs.close();
		ps.close();
		con.close();
		
		return out;
	}

}
