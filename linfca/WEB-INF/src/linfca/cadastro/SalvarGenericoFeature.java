package linfca.cadastro;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import linfca.Controller;
import linfca.Feature;
import linfca.util.Base64;
import org.jdom.Element;
import java.security.*;
import java.sql.Date;
import java.util.List;

/**
 * <pre>
 * 
 * 
 * Entrada:
 * <NOME DA TABELA NO BANCO DE DADOS>
 * 	  <CAMPOS A SEREM SALVOS>*
 * 
 *    <where>
 *       <CAMPOS A SEREM USADOS NO WHERE>*
 *    </where>?
 * 
 * </NOME DA TABELA NO BANCO DE DADOS>
 * 
 * O formato dos campos a serem salvos e os a serem usados no where 
 * tem que respeitar a seguinte forma:
 * 
 * "[PREFIXO]_[CAMPO]"
 * 
 * [PREFIXO] é o tipo do Java do campo.
 * [CAMPO] é o campo escrito de forma identica do banco de dados.
 * 
 * ex de campos:
 * 
 * <string_nome>Fulano</string_nome>
 * <int_idade>30</int_idade>
 * <date_data_nascimento>1980-03-13</date_data_nascimento>
 * 
 * 
 * 
 * SAIDA:
 * <out>
 * 		<ok/>
 * </out>
 * 
 * </pre>
 */
public class SalvarGenericoFeature implements Feature {
	private String nomeTabela;

	public Element process(Element in) throws Exception {
		nomeTabela = in.getName();
		
		if (in.getChild("where") == null) {
			in.removeChild("inserir");
			return inserir(in);
		} else {
			in.removeChild("atualizar");
			return alterar(in);			
		}		
	}
	
	private Element inserir(Element in) throws Exception {
		List campos = in.getChildren();		
		StringBuffer sb = new StringBuffer();
		sb.append("INSERT INTO ");
		sb.append(nomeTabela);
		sb.append(" ( ");
		
		String nomeCampo = ((Element)campos.get(0)).getName();
		int inicio = nomeCampo.indexOf('_') + 1;		
		sb.append(nomeCampo.substring(inicio));				
		for (int i = 1; i < campos.size(); i++) {
			nomeCampo = ((Element)campos.get(i)).getName();
			inicio = nomeCampo.indexOf('_') + 1;
			sb.append(", " + nomeCampo.substring(inicio));
		}
		
		sb.append(" ) VALUES (");
		for (int i = 0; i < campos.size() - 1; i++) {			
			sb.append(" ?,");
		}
		sb.append(" ? ) ");			
		
		System.out.println(sb.toString());


		Connection con = Controller.getInstance().makeConnection();
		PreparedStatement ps = con.prepareStatement(sb.toString());
		prepare(ps, campos, 0);
		
		Element out = new Element("out");
		
		if (ps.executeUpdate() > 0) {
			out.getChildren().add(new Element("ok"));
		}		
		return out;		
	}
	
	private Element alterar(Element in) throws Exception {
		List camposWhere = in.getChild("where").getChildren();
		in.removeChild("where");
		List campos = in.getChildren();
		
				
		StringBuffer sb = new StringBuffer();
		sb.append("UPDATE ");
		sb.append(nomeTabela);
		sb.append(" SET ");
		
		String nomeCampo = ((Element)campos.get(0)).getName();
		int inicio = nomeCampo.indexOf('_') + 1;		
		sb.append(nomeCampo.substring(inicio) + " = ? ");				
		for (int i = 1; i < campos.size(); i++) {
			nomeCampo = ((Element)campos.get(i)).getName();
			inicio = nomeCampo.indexOf('_') + 1;
			sb.append(", " + nomeCampo.substring(inicio) + " = ? ");
		}
		
		sb.append(" WHERE ");
		nomeCampo = ((Element)camposWhere.get(0)).getName();
		inicio = nomeCampo.indexOf('_') + 1;		
		sb.append(nomeCampo.substring(inicio) + " = ? ");				
		for (int i = 1; i < camposWhere.size(); i++) {
			nomeCampo = ((Element)camposWhere.get(i)).getName();
			inicio = nomeCampo.indexOf('_') + 1;
			sb.append("AND " + nomeCampo.substring(inicio) + " = ? ");
		}
		
		System.out.println(sb.toString());

		Connection con = Controller.getInstance().makeConnection();
		PreparedStatement ps = con.prepareStatement(sb.toString());		
		prepare(ps, campos, camposWhere);
		
		Element out = new Element("out");
		
		if (ps.executeUpdate() > 0) {
			out.getChildren().add(new Element("ok"));
		}
		
		return out;		
	}
	
	private void prepare(PreparedStatement ps, List campos, int indiceInicial) throws SQLException, NoSuchAlgorithmException {
		
		String nomeCampo = null;
		String nomeTipo = null;
		int fim = 0;
		for (int i = 0; i < campos.size(); i++) {
			Element campo = (Element) campos.get(i);
			nomeCampo = campo.getName();
			fim = nomeCampo.indexOf('_');
			nomeTipo = nomeCampo.substring(0, fim);
			System.out.println(nomeTipo);
			
			if (nomeTipo.equals("string")) {
				ps.setString(i + 1 + indiceInicial, campo.getTextTrim());
			} else	if (nomeTipo.equals("int")) {
				ps.setInt(i + 1 + indiceInicial, Integer.parseInt(campo.getTextTrim()));
			} else if (nomeTipo.equals("date")) {
				ps.setDate(i + 1 + indiceInicial, Date.valueOf(campo.getTextTrim()));
			} else if (nomeTipo.equals("float")) {
				ps.setFloat(i + 1 + indiceInicial, Float.parseFloat(campo.getTextTrim()));
			} else	if (nomeTipo.equals("password")) {
				
				String password = ((Element)campos.get(i)).getTextTrim();
				
				MessageDigest md = MessageDigest.getInstance("MD5");				
				byte [] senhaEncode = md.digest(password.getBytes());
				byte [] senhaEncode64 = Base64.encode(senhaEncode);
				
				ps.setBytes(i + 1 + indiceInicial, senhaEncode64);				
			} else {
				throw new RuntimeException("Tipo não reconhecido no salvar-generico: " + nomeTipo);				
			}			
		}
			
	}
	

	private void prepare(PreparedStatement ps, List campos, List camposWhere) throws SQLException, NoSuchAlgorithmException {
		
		prepare(ps, campos, 0);
		prepare(ps, camposWhere, campos.size());			
	}
			
}
