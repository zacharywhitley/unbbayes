package linfca.gerencia.lancamento;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;

import linfca.Controller;
import linfca.Feature;
import org.jdom.Element;

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
		Timestamp inicioIn = Timestamp.valueOf(in.getChildTextTrim("data-hora-inicio"));
		Timestamp fimIn = Timestamp.valueOf(in.getChildTextTrim("data-hora-fim"));
		
		ps.setTimestamp(1, inicioIn);
		ps.setTimestamp(2, fimIn);
	
		ResultSet rs = ps.executeQuery();
		Element out = new Element("out");
		while (rs.next()) {
			Element lancamento = new Element("lancamento");
			int codigo = rs.getInt("l.cod_lancamento");
			Timestamp inicio = rs.getTimestamp("l.dt_hora_inicio_lancamento");
			String nome = rs.getString("u.nome");
			String foto = rs.getString("u.foto");

			Element codigoXML = new Element("cod-lancamento");
			codigoXML.setText("" +codigo);

			Element dataInicioXML = new Element("data-hora-inicio");
			dataInicioXML.setText(inicio.toString());
			
			Element nomeXML = new Element("nome-usuario");
			nomeXML.setText(nome);
			
			Element fotoXML = new Element("foto-usuario");
			fotoXML.setText(foto);
			
			lancamento.getChildren().add(codigoXML);
			lancamento.getChildren().add(dataInicioXML);
			lancamento.getChildren().add(nomeXML);
			lancamento.getChildren().add(fotoXML);
			out.getChildren().add(lancamento);
		}
		rs.close();
		ps.close();
		con.close();
		return out;		
	}

}
