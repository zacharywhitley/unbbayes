
package linfca.cadastro.usuario;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import com.oreilly.servlet.Base64Decoder;
import com.oreilly.servlet.Base64Encoder;

import linfca.Controller;
import linfca.Feature;
import linfca.util.Base64;
import org.apache.xerces.validators.datatype.Base64BinaryDatatypeValidator;
import org.jdom.Element;
import sun.misc.BASE64Decoder;

public class ListarUsuarioFeature implements Feature {

	/**
	 * <pre>
	 * <in/>
	 * 
	 * <out>
	 * 	  <usuario>
	 * 		 <cod-usuario>1</cod-usuario>
	 *       <identificacao>9912345</identificacao>
	 *       <nome>Mané</nome>
	 *       <sobrenome>Peregrino</sobrenome>
	 * 	  </usuario>*
	 * </out> 
	 * </pre>
	 * @see Feature#process(Element)
	 */
	public Element process(Element in) throws Exception {
		
		Connection con = Controller.getInstance().makeConnection();
		
		PreparedStatement ps = con.prepareStatement("select * from usuario order by identificacao");
		ResultSet rs = ps.executeQuery();
		
		Element out = new Element("out");			
		
		while (rs.next()) {
			
			Element usuario = new Element("usuario");
			
			int codigo = rs.getInt("cod_usuario");
			String identificacao = rs.getString("identificacao");
			String nome = rs.getString("nome");
			String sobrenome = rs.getString("sobrenome");
			
			Element codigoXML = new Element("cod-usuario");
			codigoXML.setText("" + codigo);			
			usuario.getChildren().add(codigoXML);
			
			Element identificacaoXML = new Element("identificacao");
			identificacaoXML.setText(identificacao);
			usuario.getChildren().add(identificacaoXML);
			
			Element nomeXML = new Element("nome");
			nomeXML.setText(nome);
			System.out.println("NomeF: " + nome);
			usuario.getChildren().add(nomeXML);
			
			Element sobrenomeXML = new Element("sobrenome");
			nomeXML.setText(sobrenome);
			System.out.println("SobreNomeF: " + sobrenome);
			usuario.getChildren().add(sobrenomeXML);			
			
			out.getChildren().add(usuario);
			
		}
		
		rs.close();
		ps.close();
		con.close();
		
		return out;
	}

}
