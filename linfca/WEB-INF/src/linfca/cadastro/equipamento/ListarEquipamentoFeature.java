
package linfca.cadastro.equipamento;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import org.jdom.Element;
import linfca.Controller;
import linfca.Feature;

/**
 * Lista os equipamentos agrupados por sala.
 */
public class ListarEquipamentoFeature implements Feature {

	/**
	 * <pre>
	 * <in>
	 *    <desc-tipo-equipamento>Computador</desc-tipo-equipamento>?
	 *    <desc-tipo-situacao>Disponível</desc-tipo-situacao>?
	 * </in>
	 * 
	 * <out>
	 * 	  <equipamento>
	 *      <nome-sala>Laboratorio 3</nome-sala>
	 * 		<cod-equipamento>34</cod-equipamento>
	 * 		<nome-equipamento>Equipamento 25</nome-equipamento>
	 * 	  </equipamento>*
	 * </out> 
	 * </pre>
	 * @see Feature#process(Element)
	 */
	public Element process(Element in) throws Exception {
		Connection con = Controller.getInstance().makeConnection();
		
		String descTipoEquipamento = in.getChildTextTrim("desc-tipo-equipamento");
		String descTipoSituacao    = in.getChildTextTrim("desc-tipo-situacao");
		
		StringBuffer sql = new StringBuffer();
		sql.append("SELECT ");
		sql.append("  DISTINCT E.cod_equipamento, S.nome_sala, E.nome_equipamento ");
		sql.append("FROM ");
		sql.append("  Equipamento E, Sala S ");
		
		if (descTipoSituacao != null) {
			sql.append("  , Tipo_Situacao TS ");
		}
		
		if (descTipoEquipamento != null) {
			sql.append("  , Tipo_Equipamento TE ");
		}
				
		sql.append("WHERE ");
		sql.append("  E.cod_sala = S.cod_sala ");
				
		if (descTipoSituacao != null) {
			sql.append("  AND E.cod_tipo_situacao = TS.cod_tipo_situacao ");
			sql.append("  AND TS.desc_tipo_situacao = ? ");
		}
		
		if (descTipoEquipamento != null) {
			sql.append("  AND E.cod_tipo_equipamento = TE.cod_tipo_equipamento ");
			sql.append("  AND TE.desc_tipo_equipamento = ? ");
		}
		
		sql.append("ORDER BY ");
		sql.append("  E.cod_sala, E.nome_equipamento ");
		
		PreparedStatement ps = con.prepareStatement(sql.toString());
		
		int index = 1;
		
		if (descTipoSituacao != null) {
			ps.setString(index++, descTipoSituacao);
		}
		
		if (descTipoEquipamento != null) {
			ps.setString(index, descTipoEquipamento);
		}
		
		ResultSet rs = ps.executeQuery();
		
		Element out = new Element("out");
		while (rs.next()) {
			Element equipamento = new Element("equipamento");
			String nomeSala = rs.getString("S.nome_sala");
			long codigo = rs.getLong("E.cod_equipamento");
			String nome = rs.getString("E.nome_equipamento");
			
			Element nomeSalaXML = new Element("nome-sala");
			nomeSalaXML.setText(nomeSala);
			equipamento.getChildren().add(nomeSalaXML);
			Element codigoXML = new Element("cod-equipamento");
			codigoXML.setText("" + codigo);			
			equipamento.getChildren().add(codigoXML);
			Element nomeXML = new Element("nome-equipamento");
			nomeXML.setText(nome);
			equipamento.getChildren().add(nomeXML);
			out.getChildren().add(equipamento);
		}
		
		rs.close();
		ps.close();		
		con.close();
		
		return out;
	}
}
