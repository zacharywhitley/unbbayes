
package linfca;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import org.jdom.Element;

/**
 * Lista os computadores disponiveis agrupados por sala.
 */
public class ListarComputadoresDisponiveisFeature implements Feature {

	/**
	 * <pre>
	 * <in/>
	 * 
	 * <out>
	 * 	  <computador>
	 *      <nome-sala>Laboratorio 3</nome-sala>
	 * 		<cod-computador>34</cod-computador>
	 * 		<descricao-computador>Computador 25</descricao-computador>
	 * 	  </computador>*
	 * </out> 
	 * </pre>
	 * @see Feature#process(Element)
	 */
	public Element process(Element in) throws Exception {
		Connection con = Controller.getInstance().makeConnection();		
		PreparedStatement ps = con.prepareStatement(
			"select s.nome_sala, c.cod_computador, c.desc_computador" +
			" from computador c LEFT JOIN lancamento l" +
			" ON c.cod_computador = l.cod_computador, sala s" +
			" where c.cod_sala = s.cod_sala AND l.dt_hora_fim_lancamento IS NULL" +
			" group by c.cod_sala"
		);
		ResultSet rs = ps.executeQuery();
		Element out = new Element("out");
		while (rs.next()) {
			Element computador = new Element("computador");
			String nomeSala = rs.getString("s.nome_sala");
			long codigo = rs.getLong("c.cod_computador");
			String descricao = rs.getString("c.desc_computador");
			
			Element nomeSalaXML = new Element("nome-sala");
			nomeSalaXML.setText(nomeSala);
			computador.getChildren().add(nomeSalaXML);
			Element codigoXML = new Element("cod-computador");
			codigoXML.setText("" + codigo);			
			computador.getChildren().add(codigoXML);
			Element descricaoXML = new Element("descricao-computador");
			descricaoXML.setText(descricao);
			computador.getChildren().add(descricaoXML);
			out.getChildren().add(computador);
		}
		rs.close();
		ps.close();		
		con.close();
		return out;
	}
}
