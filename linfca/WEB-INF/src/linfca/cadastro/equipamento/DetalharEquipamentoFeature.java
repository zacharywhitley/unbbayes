
package linfca.cadastro.equipamento;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import linfca.Controller;
import linfca.Feature;
import org.jdom.Element;


public class DetalharEquipamentoFeature implements Feature {

	/**
	 * <pre>
	 * <in>
	 *    <cod-equipamento>1</cod-equipamento>
	 * </in>
	 * 
	 * <out>
	 * 	  <cod-equipamento>1</cod-equipamento>
	 *    <cod-tipo-situacao>1</cod-tipo-situacao>
	 *    <cod-sala>1</cod-sala>
	 *    <cod-tipo-equipamento>1</cod-tipo-equipamento>
	 * 	  <nome-equipamento>Equipamento 1</nome-equipamento>
	 * 	  <descricao-equipamento>Pentium 3 500...</descricao-equipamento>
	 *    <numero-patrimonio-equipamento>UNB1234DF</numero-patrimonio-equipamento>
	 * 	  <valor-equipamento>3000.99</valor-equipamento>
	 * </out> 
	 * </pre>
	 * @see Feature#process(Element)
	 */
	public Element process(Element in) throws Exception {
		
		Connection con = Controller.getInstance().makeConnection();
		
		String codEquipamento = in.getChildTextTrim("cod-equipamento");
		
		PreparedStatement ps = con.prepareStatement("select * from equipamento where cod_equipamento = ?");
		
		ps.setInt(1, Integer.parseInt(codEquipamento));
		
		ResultSet rs = ps.executeQuery();
		
		Element out = new Element("out");			
		
		while (rs.next()) {

			Element codXML = new Element("cod-equipamento");
			codXML.setText(codEquipamento);
			out.getChildren().add(codXML);
			
			String codTipoSituacao = "" + rs.getInt("cod_tipo_situacao");
			Element codTipoSituacaoXML = new Element("cod-tipo-situacao");
			codTipoSituacaoXML.setText(codTipoSituacao);
			out.getChildren().add(codTipoSituacaoXML);
			
			String codSala = "" + rs.getInt("cod_sala");
			Element codSalaXML = new Element("cod-sala");
			codSalaXML.setText(codSala);
			out.getChildren().add(codSalaXML);
			
			String desc = rs.getString("desc_equipamento");
			Element descXML = new Element("descricao-equipamento");
			descXML.setText(desc);
			out.getChildren().add(descXML);
			
			String codTipoEquipamento = "" + rs.getInt("cod_tipo_equipamento");
			Element codTipoEquipamentoXML = new Element("cod-tipo-equipamento");
			codTipoEquipamentoXML.setText(codTipoEquipamento);
			out.getChildren().add(codTipoEquipamentoXML);
			
			String nomeEquipamento = rs.getString("nome_equipamento");
			Element nomeEquipamentoXML = new Element("nome-equipamento");
			nomeEquipamentoXML.setText(nomeEquipamento);
			out.getChildren().add(nomeEquipamentoXML);
			
			String numeroPatrimonio = rs.getString("numero_patrimonio_equipamento");
			Element numeroPatrimonioXML = new Element("numero-patrimonio-equipamento");
			numeroPatrimonioXML.setText(numeroPatrimonio);
			out.getChildren().add(numeroPatrimonioXML);
			
			String valorEquipamento = "" + rs.getFloat("valor_equipamento");
			Element valorEquipamentoXML = new Element("valor-equipamento");
			valorEquipamentoXML.setText(valorEquipamento);
			out.getChildren().add(valorEquipamentoXML);
			
		}
		
		rs.close();
		ps.close();
		con.close();
		
		return out;
	}


}
