
package linfca.cadastro.equipamento;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.jdom.Element;
import linfca.Controller;
import linfca.Feature;
import linfca.XMLUtil;

/**
 * Lista os equipamentos agrupados por sala.
 */
public class ListarEquipamentoFeature implements Feature {
	
	private Connection con;

	/**
	 * <pre>
	 * <in>
	 *    <desc-tipo-equipamento>Computador</desc-tipo-equipamento>?
	 *    <desc-tipo-situacao>Disponível</desc-tipo-situacao>?
	 * </in> 
	 * 
	 * <out>
	 *      <sala>
	 * 		  <nome-sala>Laboratorio 3</nome-sala>
	 *        <equipamento>
	 * 			<cod-equipamento>34</cod-equipamento>
	 *	 		<nome-equipamento>Equipamento 25</nome-equipamento>
	 *	 	  </equipamento>*
	 * 		</sala>*
	 * </out> 
	 * 
	 * ||
	 * 
	 * <in/>
	 * 
	 * <out>
	 *   <equipamento>
	 * 		<cod-equipamento>34</cod-equipamento>
	 *		<nome-equipamento>Equipamento 25</nome-equipamento>
	 *	 </equipamento>*
	 * </out>
	 * </pre>
	 * @see Feature#process(Element)
	 */
	public Element process(Element in) throws Exception {
		con = Controller.getInstance().makeConnection();
		
		if (in == null || in.getChildren().size() == 0) {
			return listarTodos();
		}
	
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
		String sala = null;
		Element salaXML = null;
		while (rs.next()) {
			Element equipamento = new Element("equipamento");
			String nomeSala = rs.getString("S.nome_sala");
			if (! nomeSala.equals(sala)) {
				if (salaXML != null) {
					out.getChildren().add(salaXML);
				}
				sala = nomeSala;
				salaXML = new Element("sala");
				Element nome = new Element("nome-sala");
				nome.setText(sala);
				salaXML.getChildren().add(nome);				
			}
			long codigo = rs.getLong("E.cod_equipamento");
			String nome = rs.getString("E.nome_equipamento");			
			
			Element codigoXML = new Element("cod-equipamento");
			codigoXML.setText("" + codigo);			
			equipamento.getChildren().add(codigoXML);
			Element nomeXML = new Element("nome-equipamento");
			nomeXML.setText(nome);
			equipamento.getChildren().add(nomeXML);
			salaXML.getChildren().add(equipamento);
		}
		if (salaXML != null) {		
			out.getChildren().add(salaXML);
		}
		
		rs.close();
		ps.close();		
		con.close();
		
		return out;
	}
	
	private Element listarTodos() throws SQLException {
		PreparedStatement ps = con.prepareStatement("select * from equipamento group by cod_tipo_equipamento");
		ResultSet rs = ps.executeQuery();
		Element out = new Element("out");
		while (rs.next()) {
			Element equip = new Element("equipamento");
			int codEquip = rs.getInt("cod_equipamento");
			String nome = rs.getString("nome_equipamento");
			
			Element codXML = new Element("cod-equipamento");
			codXML.setText("" + codEquip);
			
			Element nomeXML = new Element("nome-equipamento");
			nomeXML.setText(nome);
			
			equip.getChildren().add(codXML);
			equip.getChildren().add(nomeXML);
			
			out.getChildren().add(equip);
		}
		return out;
	}
	
	public static void main(String args[]) throws Exception {
		Element in = new Element("in");
		Element temp;
		temp = new Element("desc-tipo-equipamento");
		temp.setText("Computador");
		in.getChildren().add(temp);
		temp = new Element("desc-tipo-situacao");
		temp.setText("Disponível");
		in.getChildren().add(temp);
		Feature f = new ListarEquipamentoFeature();
		Element out = f.process(in);
		XMLUtil.print(out);
	}
}
