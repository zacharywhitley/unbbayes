package linfca.geral;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import linfca.Controller;
import linfca.Feature;
import linfca.XMLUtil;

import org.jdom.Element;

/**
 * @author administrador
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class BuscarAlunoFeature implements Feature {
	
	private boolean primeiro = true;
	private StringBuffer sql = new StringBuffer("select * from usuario u");

	/**
	 * <pre>
	 * <in>
	 *    ?<no-laboratorio/>
	 *    ?<identificacao>9912345</identificacao>
	 *    ?<cpf>12345678912</cpf>
	 *    ?<nome>Mané</nome>
	 *    ?<sobrenome>Peregrino</sobrenome>
	 *    ?<telefone>61-3681244</telefone>
	 *    ?<email>mp@provedor.com.br</email>
	 *    ?<endereco>SQN 410 Bl. B Apto. 101</endereco>
	 * </in>
	 * 
	 * <out>
	 *    <out>
	 * 	  <usuario>
	 * 		 <cod-usuario>1</cod-usuario>
	 *       <identificacao>9912345</identificacao>
	 *       <nome-completo>Mané Peregrino</nome-completo>
	 * 	  </usuario>*
	 * </out>
	 * </pre>
	 * @see linfca.Feature#process(org.jdom.Element)
	 */
	public Element process(Element in) throws Exception {
		Connection con = Controller.getInstance().makeConnection();
		
		if (in.getChild("no-laboratorio") != null) {
			sql.append(", lancamento_uso l");
			trataPrimeiro();
			sql.append("u.cod_usuario = l.cod_usuario AND l.dt_hora_fim_lancamento_uso IS NULL AND ");
		}
		
		trata(in, "identificacao");
		trata(in, "cpf");
		trata(in, "nome");
		trata(in, "sobrenome");
		trata(in, "telefone");
		trata(in, "email");
		trata(in, "endereco");
		
		if (! primeiro) {
			int len = sql.length();
			sql.setLength(len-4);
		}

		sql.append(" order by u.nome");
		
//		System.out.println(sql);	
		PreparedStatement ps = con.prepareStatement(sql.toString());
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
		
		/*
		Iterator usuarios = out.getChildren().iterator();
		while (usuarios.hasNext()) {
			Element usuario = (Element) usuarios.next();
			System.out.println("CodUsuarioF: " + ((Element)usuario.getChild("cod-usuario")).getText());
			System.out.println("IdentificacaoF: " + ((Element)usuario.getChild("identificacao")).getText());
			System.out.println("NomeF: " + ((Element)usuario.getChild("nome-completo")).getText());
			//System.out.println("SobreNomeF: " + ((Element)usuario.getChild("sobrenome")).getText());
		}
		*/
		
		rs.close();
		ps.close();
		con.close();
		
		return out;
	}
	
	private void trata(Element in, String col) {
		if (in.getChild(col) != null) {
			trataPrimeiro();
			sql.append("u." + col + " like '%" + in.getChildTextTrim(col) + "%' AND ");
		}
	}

	private void trataPrimeiro() {
		if (primeiro) {
			sql.append(" WHERE ");			
			primeiro = false;
		}
	}
	
	public static void main(String args[]) throws Exception {
		Element in = new Element("in");
		Element temp;
		temp = new Element("no-laboratorio");
//		temp.setText("468");
		in.getChildren().add(temp);
		Feature f = new BuscarAlunoFeature();
		Element out = f.process(in);
		XMLUtil.print(out);
	}
}
