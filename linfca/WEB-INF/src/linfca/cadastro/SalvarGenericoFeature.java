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
 *    <inserir/> | <atualizar/>
 * 
 * 	  <CAMPOS A SEREM SALVOS>*
 * 
 * </NOME DA TABELA NO BANCO DE DADOS>
 * 
 * O formato dos campos a serem salvos tem que respeitar a seguinte forma:
 * 
 * "[PREFIXO]_[CAMPO]"
 * 
 * [PREFIXO] é o tipo do Java do campo.
 * [CAMPO] é o campo escrito de forma identica do banco de dados.
 * 
 * ex de campo:
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
		
		if (in.getChild("inserir") != null) {
			in.removeChild("inserir");
			return inserir(in);
		} else if (in.getChild("atualizar") != null) {
			in.removeChild("atualizar");
//			return atualizar(in);			
		} else {
			throw new RuntimeException("Sem instrucoes de inserir ou atualizar!");			
		}
		
		return null;		
	}
	
	private Element inserir(Element in) throws Exception {
		List campos = in.getChildren();		
		StringBuffer sb = new StringBuffer();
		sb.append("INSERT INTO ");
		sb.append(nomeTabela);
		sb.append(" (");
		sb.append(((Element)campos.get(0)).getName());		
		for (int i = 1; i < campos.size(); i++) {
			String nomeCampo = ((Element)campos.get(i)).getName();
			int inicio = nomeCampo.indexOf(')') + 1;
			sb.append(", " + nomeCampo.substring(inicio));
		}
		sb.append(") VALUES (");
		for (int i = 1; i < campos.size(); i++) {
			sb.append("? ");
		}				
		sb.append(")");


		Connection con = Controller.getInstance().makeConnection();		
		PreparedStatement ps = con.prepareStatement(sb.toString());
		prepare(ps, campos);		
		return null;		
	}
	
	private void prepare(PreparedStatement ps, List campos) {
		for (int i = 0; i < campos.size(); i++) {
		}		
	}
			
}
