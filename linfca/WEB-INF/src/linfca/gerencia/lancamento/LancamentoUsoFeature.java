
package linfca.gerencia.lancamento;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import linfca.Controller;
import linfca.Feature;
import linfca.cadastro.tiposituacao.*;
import linfca.cadastro.tiposituacao.TipoSituacao;
import org.jdom.Element;

public class LancamentoUsoFeature implements Feature {

	/**
	 * <pre>
	 * <in>
	 *    (<cod-lancamento-uso>1</cod-lancamento-uso>
	 *     <cod-equipamento>7</cod-equipamento>)
	 *       |
	 *    (<cod-usuario>1</cod-usuario>
	 *     <desc-tipo-situacao>Dispon�vel</desc-tipo-situacao>)
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
		
		PreparedStatement ps = null;		
		Timestamp dtHora = new Timestamp(System.currentTimeMillis());
		
		String descTipoSituacao = null;
		String codEquipamento = in.getChildTextTrim("cod-equipamento");
		
		if ( in.getChild("cod-lancamento-uso") != null ) {
			
			String codLancamento = in.getChildTextTrim("cod-lancamento-uso");
			
			descTipoSituacao = TipoSituacao.DISPONIVEL;

			StringBuffer sql = new StringBuffer();
			sql.append("UPDATE ");
			sql.append("  Lancamento_Uso ");
			sql.append(" SET ");
			sql.append(" dt_hora_fim_lancamento_uso = ? ");
			sql.append("WHERE ");
			sql.append("  cod_lancamento_uso = ? ");
			 
			ps = con.prepareStatement(sql.toString());
						
			ps.setTimestamp(1, dtHora);
			ps.setInt(2, Integer.parseInt(codLancamento));
			
		} else {
			
			descTipoSituacao = TipoSituacao.USO;
			
			String codUsuario = in.getChildTextTrim("cod-usuario");
			
			StringBuffer sql = new StringBuffer();
			sql.append("INSERT INTO ");
			sql.append("  Lancamento_Uso ");
			sql.append("  (cod_usuario, cod_equipamento, ");
			sql.append("   dt_hora_inicio_lancamento_uso) ");
			sql.append("VALUES ");
			sql.append("  (?, ?, ?) ");
			
			ps = con.prepareStatement(sql.toString());

			ps.setLong(1, Long.parseLong(codUsuario));
			ps.setLong(2, Long.parseLong(codEquipamento));
			ps.setTimestamp(3, dtHora);
		}
		
		// cria o elemento de sa�da
		Element out = new Element("out");
		
		if ( ps.executeUpdate() > 0 ) {
			out.getChildren().add(new Element("ok"));
		} else {
			throw new RuntimeException("N�o foi poss�vel processar o lan�amento!");
		}	
						
		ps.close();		
		
		atualizarSituacaoComputador(codEquipamento, descTipoSituacao, con);
		
		con.close();
		
		// retorna o elemento de sa�da
		return out;
	}
	
	private void atualizarSituacaoComputador(String codEquipamento, 
				String descTipoSituacao, Connection con) throws SQLException {
		
		PreparedStatement ps = null;
		ResultSet rs = null;
		
		StringBuffer sql = new StringBuffer();
		sql.append("SELECT ");
		sql.append("  cod_tipo_situacao ");
		sql.append("FROM ");
		sql.append("  Tipo_Situacao ");
		sql.append("WHERE ");
		sql.append(" desc_tipo_situacao = ? ");
		
		ps = con.prepareStatement(sql.toString());
		
		ps.setString(1, descTipoSituacao);
		
		rs = ps.executeQuery();
		
		if (! rs.next()) {
			throw new RuntimeException("Descri��o Tipo Situa��o N�o Encontrada no BD - " + descTipoSituacao);
		}
		
		sql.delete(0, sql.length());
		sql.append("UPDATE ");
		sql.append("  Equipamento ");
		sql.append("SET ");
		sql.append("  cod_tipo_situacao = ? ");
		sql.append("WHERE ");
		sql.append(" cod_equipamento = ? ");
		
		ps = con.prepareStatement(sql.toString());
		
		ps.setInt(1, rs.getInt("cod_tipo_situacao"));
		ps.setInt(2, Integer.parseInt(codEquipamento));
		
		ps.executeUpdate();
		
	}

}
