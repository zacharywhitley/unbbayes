package linfca.gerencia.lancamento;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import org.jdom.Element;

import linfca.*;

public class ListarLancamentoFeature implements Feature {
	
	/**
	 * <pre>
	 * <in>
	 *     <data-hora-inicio>data</data-hora-inicio>
	 *     <data-hora-fim>data</data-hora-fim>	
	 * </in>
	 * 
	 * 
	 * <out>
	 * 	  <lancamento>
	 * 		<cod-lancamento>1</cod-lancamento>
	 *      <nome-usuario>Fulano</nome-usuario>
	 *      <foto-usuario>AFEW$GD#%...</foto-usuario>
	 * 		<data-hora-inicio>data e hora</data-hora-inicio> 
	 * 	  </lancamento>*
	 * </out> 
	 * </pre>

	 * @see Feature#process(Element)
	 */
	public Element process(Element in) throws Exception {
		Connection con = Controller.getInstance().makeConnection();
		PreparedStatement ps = 
		       con.prepareStatement(
					"select l.cod_lancamento, l.dt_hora_inicio_lancamento, " +
					" u.nome, u.foto" +
					" from lancamento l, usuario u" +
					" where l.dt_hora_inicio_lancamento > ?" +
					" AND l.dt_hora_fim_lancamento < ?" +
					" order by l.dt_hora_inicio_lancamento desc"
			   );
		ResultSet rs = ps.executeQuery();
		Element out = new Element("out");
		while (rs.next()) {
			Element tipoSexo = new Element("tipo-sexo");
			int codigo = rs.getInt("cod_tipo_sexo");
			String descricao = rs.getString("desc_tipo_sexo");
			Element codigoXML = new Element("cod-tipo-sexo");
			codigoXML.setText("" +codigo);			
			tipoSexo.getChildren().add(codigoXML);
			Element descricaoXML = new Element("descricao-tipo-sexo");
			descricaoXML.setText(descricao);
			tipoSexo.getChildren().add(descricaoXML);
			out.getChildren().add(tipoSexo);
		}
		rs.close();
		ps.close();
		con.close();
		return out;		
	}

}
