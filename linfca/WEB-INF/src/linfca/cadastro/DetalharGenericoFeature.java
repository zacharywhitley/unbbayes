package linfca.cadastro;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;

import linfca.Controller;
import linfca.Feature;
import linfca.XMLUtil;

import org.jdom.Element;

/**
 * @author Michael S. Onishi
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class DetalharGenericoFeature implements Feature {

	/**
	 * <pre>
	 * <in>
	 *    <nome-tabela>Curso</nome-tabela>
	 *    <cod-elemento>23</cod-elemento>
	 * </in>
	 * 
	 * <out>
	 *	  <campo>Elemento</campo>*  // todos os campos da tabela do banco de dados
	 * </out> 
	 * </pre>
	 * @see Feature#process(Element)
	 */
	public Element process(Element in) throws Exception {
		String nomeTabela = in.getChildTextTrim("nome-tabela");
		
		Connection con = Controller.getInstance().makeConnection();
		
		String codElemento = in.getChildTextTrim("cod-elemento");
		
		PreparedStatement ps = con.prepareStatement("select * from " + nomeTabela + " where cod_" + nomeTabela + " = ?");
		
		ps.setInt(1, Integer.parseInt(codElemento));
		
		ResultSet rs = ps.executeQuery();
		
		Element out = new Element("out");
		
		ResultSetMetaData meta = rs.getMetaData();
		int numCols = meta.getColumnCount();
				
		while (rs.next()) {
			for (int i = 1; i <= numCols; i++) {
				String nomeColuna = meta.getColumnName(i).toLowerCase();
				String valor = rs.getString(nomeColuna);
				Element elementoXML = new Element(nomeColuna);
				elementoXML.setText(valor);
				out.getChildren().add(elementoXML);
			}
		}
		
		rs.close();
		ps.close();
		con.close();
		
		return out;
	}
	
	public static void main(String arg[]) throws Exception {
		Feature f = new DetalharGenericoFeature();
		Element in = new Element("in");
		Element nomeTabela = new Element("nome-tabela");
		nomeTabela.setText("sala");
		in.getChildren().add(nomeTabela);
		
		Element codigo = new Element("cod-elemento");
		codigo.setText("" + 21);
		in.getChildren().add(codigo);
				
		Element out = f.process(in);
		XMLUtil.print(out);
	}
}
