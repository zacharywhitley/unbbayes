package linfca.gerencia.lancamento;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;

import linfca.Controller;
import linfca.Feature;
import org.jdom.Element;

public class ListarLancamentoUsoFeature implements Feature {
	
	/**
	 * <pre>
	 * <in>
	 *     <data-hora-inicio>data</data-hora-inicio>
	 *     ?<data-hora-fim>data</data-hora-fim>
	 *     ?<abertos/>
	 * </in>
	 * 
	 * 
	 * <out>
	 * 	  <lancamento>
	 * 		<cod-lancamento-uso>1</cod-lancamento-uso>
	 *      <nome-usuario>Fulano</nome-usuario>
	 *      <foto-usuario>AFEW$GD#%...</foto-usuario>
	 * 		<data-hora-inicio-uso>10/02/2002 10:15</data-hora-inicio-uso>
	 *      <data-hora-fim-uso>12/02/2002 09:15</data-hora-fim-uso>
	 * 	  </lancamento>*
	 * </out> 
	 * </pre>
	 * @see Feature#process(Element)
	 */
	public Element process(Element in) throws Exception {
		boolean aberto = (in.getChild("abertos") != null);
		
		Connection con = Controller.getInstance().makeConnection();
		
		StringBuffer sql = new StringBuffer();
		sql.append("select l.cod_lancamento_uso, l.dt_hora_inicio_lancamento_uso,");
		sql.append(" l.dt_hora_fim_lancamento_uso, u.nome, u.foto");
		sql.append(" from lancamento_uso l, usuario u");		
		sql.append(" where l.dt_hora_inicio_lancamento_uso > ?");
		sql.append(" AND l.cod_usuario = u.cod_usuario");
		if (aberto) {
			sql.append(" AND l.dt_hora_fim_lancamento_uso IS NULL");
		} else {
			sql.append(" AND l.dt_hora_fim_lancamento_uso < ?");
		}
		sql.append(" order by l.dt_hora_inicio_lancamento_uso desc");
		
		PreparedStatement ps = con.prepareStatement(sql.toString());
						
		Timestamp inicioIn = Timestamp.valueOf(in.getChildTextTrim("data-hora-inicio"));
		Timestamp fimIn = Timestamp.valueOf(in.getChildTextTrim("data-hora-fim"));
		
		ps.setTimestamp(1, inicioIn);
		if (! aberto) {
			ps.setTimestamp(2, fimIn);
		}
	
		ResultSet rs = ps.executeQuery();
		
		Element out = new Element("out");
		while (rs.next()) {
			
			Element lancamento = new Element("lancamento");
			int codigo = rs.getInt("l.cod_lancamento");
			Timestamp inicio = rs.getTimestamp("l.dt_hora_inicio_lancamento");
			Timestamp fim = rs.getTimestamp("l.dt_hora_fim_lancamento");
			String nome = rs.getString("u.nome");
			String foto = rs.getString("u.foto");

			Element codigoXML = new Element("cod-lancamento-uso");
			codigoXML.setText("" +codigo);

			Element dataInicioXML = new Element("data-hora-inicio-uso");
			dataInicioXML.setText(inicio.getDay() + "/" + inicio.getMonth() + 
					"/" + inicio.getYear() + " " + inicio.getHours() + ":" + 
					inicio.getMinutes());
			
			if (fim != null) {
				Element dataFimXML = new Element("data-hora-fim-uso");
				dataFimXML.setText(fim.getDay() + "/" + fim.getMonth() + "/" + 
					fim.getYear() + " " + fim.getHours() + ":" + fim.getMinutes());
				lancamento.getChildren().add(dataFimXML);
			}
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
