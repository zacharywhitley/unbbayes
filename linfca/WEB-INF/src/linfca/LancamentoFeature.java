
package linfca;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import org.jdom.Element;

public class LancamentoFeature implements Feature {

	/**
	 * <pre>
	 * <in>
	 *    <cod-lancamento>1</cod-lancamento>
	 *       |
	 *    (<cod-usuario>1</cod-usuario>
	 *     <cod-computador>7</cod-computador>
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
		
		if ( in.getChild("cod-lancamento") != null ) {
			
			String codLancamento = in.getChild("cod-lancamento").getTextTrim();			
			
			StringBuffer sql = new StringBuffer();
			sql.append("UPDATE ");
			sql.append("  Lancamento AS L ");
			sql.append("SET ");
			sql.append("  (dt_hora_fim_lancamento) ");
			sql.append("VALUES ");
			sql.append("  (?) ");			
			sql.append("WHERE ");
			sql.append("  L.cod_lancamento = ? ");
			
			ps = con.prepareStatement(sql.toString());
						
			ps.setTimestamp(1, dtHora);
			ps.setInt(2, Integer.parseInt(codLancamento));	
			
		} else {
			
			String codUsuario = in.getChild("cod-usuario").getTextTrim();
			String codComputador = in.getChild("cod-computador").getTextTrim();
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
			sql.append("  Lancamento AS L ");
			sql.append("  (cod_usuario, cod_computador, cod_tipo_lacamento, ");
			sql.append("   dt_hora_inicio_lancamento) ");
			sql.append("VALUES ");
			sql.append("  (?, ?, ?, ?) ");
			
			System.out.println(sql.toString());
			
			ps = con.prepareStatement(sql.toString());
									
			ps.setInt(1, Integer.parseInt(codUsuario));
			ps.setInt(2, Integer.parseInt(codComputador));
			ps.setInt(3, codTipoLancamento);
			ps.setTimestamp(4, dtHora);
			
		}
		
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
		
	private int retornarCodTipoLancamento(String desc, Connection con) 
				throws SQLException {
		
		PreparedStatement ps = null;
		ResultSet rs = null;
		
		StringBuffer sql = new StringBuffer();
		sql.append("SELECT ");
		sql.append("  cod_tipo_lancamento ");
		sql.append("FROM ");
		sql.append("  Lancamento AS L ");
		sql.append("WHERE ");
		sql.append("  L.desc_tipo_lancamento = ? ");
		
		ps = con.prepareStatement(sql.toString());
		
		ps.setString(1, desc);
		
		rs = ps.executeQuery();
		
		return rs.getInt("cod_tipo_lancamento");
		
	}

}
