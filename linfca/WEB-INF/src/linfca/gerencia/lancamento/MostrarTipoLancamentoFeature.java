
package linfca.gerencia.lancamento;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import linfca.Controller;
import linfca.Feature;

import org.jdom.Element;

public class MostrarTipoLancamentoFeature implements Feature {

	/**
	 * <pre>
	 * <in>
	 *    <cod-usuario>Fulano</cod-usuario>
	 * </in>
	 * 
	 * <out>
	 *    <entrar/>
	 * 
	 *      |
	 * 
	 * 	  <sair>
	 *		<cod-lancamento-uso>4</cod-lancamento-uso>
	 *    </sair>
	 * </out> 
	 * </pre>
	 * @see Feature#process(Element)
	 */
	public Element process(Element in) throws Exception {
		Element out = new Element("out");
		Connection con = Controller.getInstance().makeConnection();
		PreparedStatement ps = con.prepareStatement(
			"SELECT cod_lancamento_uso" +
			" FROM lancamento_uso " +
			" WHERE cod_usuario = ? AND dt_hora_fim_lancamento_uso IS NULL"
		);
		
		int codUsuario = Integer.parseInt(in.getChildTextTrim("cod-usuario"));
		ps.setInt(1, codUsuario);
		ResultSet rs = ps.executeQuery();
		if (rs.next()) {
			Element sair = new Element("sair");
			Element codLancamento = new Element("cod-lancamento-uso");
			codLancamento.setText("" + rs.getLong("cod_lancamento_uso"));
			sair.getChildren().add(codLancamento);
			
			/*
			Element codEquipamento = new Element("cod-equipamento");
			codEquipamento.setText("" + rs.getLong("cod_equipamento"));
			sair.getChildren().add(codEquipamento);
			*/

			out.getChildren().add(sair);
		} else {
			Element entrar = new Element("entrar");
			out.getChildren().add(entrar);
		}
		return out;
	}

}
