
package linfca;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import org.jdom.Element;

public class LancamentoFeature implements Feature {

	/**
	 * <pre>
	 * <in>
	 *    <cod-usuario>1</cod-usuario>
	 * 
	 *    (<cod-computador>7</cod-computador>
	 * 
	 *     </manutencao>
	 *       |
	 *     </uso>
	 *       |
	 *     </deposito>)?
	 * 
	 * </in>   
	 * 
	 * <out>
	 * 	  </ok>
	 *       |
	 *    </false>
	 * </out> 
	 * </pre>
	 * @see Feature#process(Element)
	 */
	public Element process(Element in) throws Exception {
		
		Connection con = Controller.getInstance().makeConnection();
		
		String codUsuario = in.getChild("cod-usuario").getTextTrim();		
		
		if ( in.getChild("cod-computador") != null ) {
			
			StringBuffer sql = new StringBuffer();
			sql.append("UPDATE ");
			sql.append("  Lancamento AS L ");
			sql.append("SET ");
			sql.append("  (dt_hora_fim_lancamento) ");
			sql.append("VALUES ");
			sql.append("  (?) ");			
			sql.append("WHERE ");
			sql.append("  L.cod_computador = ? AND ");
			sql.append("  L.dt_hora_fim_lancamento IS NULL ");
			
		} else {
			
			
			
		}
		
		if ( in.getChild("manutencao") != null ) {
			
			
			
		}
		
		StringBuffer sql = new StringBuffer();
		sql.append("SELECT ");
		sql.append("  S.nome_sala, C.cod_computador, C.desc_computador ");
		sql.append("FROM ");
		sql.append("  Computador AS C, Sala S, Tipo_Situacao AS TS ");
		sql.append("WHERE ");
		sql.append("  C.cod_sala = S.cod_sala AND ");
		sql.append("  C.cod_tipo_situacao = TS.cod_tipo_situacao AND ");
		sql.append("  TS.desc_tipo_situacao = ? ");
		sql.append("ORDER BY ");
		sql.append("  C.cod_sala, C.desc_computador ");
		
		PreparedStatement ps = con.prepareStatement(sql.toString());
		
		ps.setString(1, Computador.DISPONIVEL);
		
		// cria o elemento de saída
		Element out = new Element("out");
		
		if ( ps.executeUpdate() > 0 ) {
			out.getChildren().add(new Element("ok"));
		} else {
			out.getChildren().add(new Element("false"));
		}
		
		ps.close();		
		con.close();
		
		// retorna o elemento de saída
		return out;
	}

}
