
package linfca.gerencia.lancamento;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import linfca.Controller;
import linfca.Feature;
import linfca.cadastro.computador.Computador;
import org.jdom.Element;

public class LancamentoFeature implements Feature {

	/**
	 * <pre>
	 * <in>
	 *    <cod-lancamento>1</cod-lancamento>
	 *    <cod-computador>7</cod-computador>
	 *       |
	 *    (<cod-usuario>1</cod-usuario>
	 * 
	 *     </manutencao>
	 *        |
	 *     </uso>
	 *        |
	 *     </deposito>)
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
		
		String descSituacao = Computador.USO;
		String codComputador = in.getChildTextTrim("cod-computador");
		
		if ( in.getChild("cod-lancamento") != null ) {			
			String codLancamento = in.getChild("cod-lancamento").getTextTrim();			
			System.out.println("codlancamento = " + codLancamento);
			
			descSituacao = Computador.DISPONIVEL;

			StringBuffer sql = new StringBuffer();
			sql.append("UPDATE ");
			sql.append("  Lancamento ");
			sql.append(" SET ");
			sql.append(" dt_hora_fim_lancamento = ? ");
			sql.append("WHERE ");
			sql.append("  cod_lancamento = ? ");
			 
			ps = con.prepareStatement(sql.toString());
						
			ps.setTimestamp(1, dtHora);
			ps.setInt(2, Integer.parseInt(codLancamento));
			
		} else {			
			String codUsuario = in.getChildTextTrim("cod-usuario");			

			System.out.println("codusuario = " + codUsuario);
			System.out.println("codcomputador = " + codComputador);
			
			int codTipoLancamento = 0;
			if ( in.getChild("manutencao") != null ) {
				codTipoLancamento = 
				retornarCodTipoLancamento(Lancamento.MANUTENCAO, con);
			} else if ( in.getChild("uso") != null ) {
				codTipoLancamento = 
				retornarCodTipoLancamento(Lancamento.USO, con);
			} else if ( in.getChild("deposito") != null ) {
				codTipoLancamento = 
				retornarCodTipoLancamento(Lancamento.DEPOSITO, con);
			}	
			
			StringBuffer sql = new StringBuffer();
			sql.append("INSERT INTO ");
			sql.append("  Lancamento ");
			sql.append("  (cod_usuario, cod_computador, cod_tipo_lancamento, ");
			sql.append("   dt_hora_inicio_lancamento) ");
			sql.append("VALUES ");
			sql.append("  (?, ?, ?, ?) ");
			
			ps = con.prepareStatement(sql.toString());
			System.out.println("codtipolancamento = " + codTipoLancamento);
			ps.setLong(1, Long.parseLong(codUsuario));
			ps.setLong(2, Long.parseLong(codComputador));
			ps.setInt(3, codTipoLancamento);
			ps.setTimestamp(4, dtHora);
		}
		
		// cria o elemento de saída
		Element out = new Element("out");
		
		if ( ps.executeUpdate() > 0 ) {
			out.getChildren().add(new Element("ok"));
		} else {
			throw new RuntimeException("Não foi possível processar o lançamento!");
		}	
						
		ps.close();		
		
		atualizarSituacaoComputador(codComputador, descSituacao, con);
		
		con.close();
		
		// retorna o elemento de saída
		return out;
	}
		
	private int retornarCodTipoLancamento(String desc, Connection con) 
				throws SQLException {
		
		PreparedStatement ps = null;
		ResultSet rs = null;
		
		StringBuffer sql = new StringBuffer();
		sql.append("SELECT ");
		sql.append("  cod_tipo_lancamento ");
		sql.append("FROM ");
		sql.append("  Tipo_Lancamento ");
		sql.append("WHERE ");
		sql.append(" desc_tipo_lancamento = ? ");
		
		ps = con.prepareStatement(sql.toString());
		
		ps.setString(1, desc);
		
		rs = ps.executeQuery();
				
		if (rs.next()) {
			System.out.println("deu");	
		} else {
			System.out.println("nao deu");
		}
		
		return rs.getInt("cod_tipo_lancamento");
		
	}
	
	private void atualizarSituacaoComputador(String codComputador, 
				String descSituacao, Connection con) throws SQLException {
		
		PreparedStatement ps = null;
		ResultSet rs = null;
		
		StringBuffer sql = new StringBuffer();
		sql.append("SELECT ");
		sql.append("  cod_tipo_situacao ");
		sql.append("FROM ");
		sql.append("  Tipo_Situacao ");
		sql.append("WHERE ");
		sql.append(" desc_tipo_situacao = ? ");
		
		System.out.println("111111");
		ps = con.prepareStatement(sql.toString());
		System.out.println("asdf");
		
		ps.setString(1, descSituacao);
		
		rs = ps.executeQuery();
		
		rs.next();
		
		sql.delete(0, sql.length());
		sql.append("UPDATE ");
		sql.append("  Computador ");
		sql.append("SET ");
		sql.append("  cod_tipo_situacao = ? ");
		sql.append("WHERE ");
		sql.append(" cod_computador = ? ");
		
		ps = con.prepareStatement(sql.toString());
		
		ps.setInt(1, rs.getInt("cod_tipo_situacao"));
		ps.setInt(2, Integer.parseInt(codComputador));
		
		ps.executeUpdate();
		
	}

}
