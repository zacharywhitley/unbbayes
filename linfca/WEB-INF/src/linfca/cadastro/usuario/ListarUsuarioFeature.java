
package linfca.cadastro.usuario;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Iterator;

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
	 *       <nome-completo>Mané Peregrino</nome-completo>
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
			
			Element nomeXML = new Element("nome-completo");
			nomeXML.setText(nome + " " + sobrenome);			
			usuario.getChildren().add(nomeXML);
			
			out.getChildren().add(usuario);
			
		}
		
		Iterator usuarios = out.getChildren().iterator();
		while (usuarios.hasNext()) {
			Element usuario = (Element) usuarios.next();
			System.out.println("CodUsuarioF: " + ((Element)usuario.getChild("cod-usuario")).getText());
			System.out.println("IdentificacaoF: " + ((Element)usuario.getChild("identificacao")).getText());
			System.out.println("NomeF: " + ((Element)usuario.getChild("nome-completo")).getText());
			//System.out.println("SobreNomeF: " + ((Element)usuario.getChild("sobrenome")).getText());
		}
		
		rs.close();
		ps.close();
		con.close();
		
		return out;
	}

}
