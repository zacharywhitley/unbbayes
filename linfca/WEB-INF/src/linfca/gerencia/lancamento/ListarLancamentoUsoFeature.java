package linfca.gerencia.lancamento;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.Calendar;

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
			sql.append(" AND (l.dt_hora_fim_lancamento_uso < ? OR l.dt_hora_fim_lancamento_uso IS NULL)");
		}
		sql.append(" order by l.dt_hora_inicio_lancamento_uso desc");
		
		PreparedStatement ps = con.prepareStatement(sql.toString());
						
		Timestamp inicioIn = Timestamp.valueOf(in.getChildTextTrim("data-hora-inicio"));			
		
		ps.setTimestamp(1, inicioIn);
		if (! aberto) {
			Timestamp fimIn = Timestamp.valueOf(in.getChildTextTrim("data-hora-fim"));
			ps.setTimestamp(2, fimIn);
		}
	
		ResultSet rs = ps.executeQuery();
		
		Element out = new Element("out");
		while (rs.next()) {
			
			Element lancamento = new Element("lancamento");
			int codigo = rs.getInt("l.cod_lancamento_uso");
			Timestamp inicio = rs.getTimestamp("l.dt_hora_inicio_lancamento_uso");
			Calendar calInicio = Calendar.getInstance();
			calInicio.setTime(inicio);					
				
			Timestamp fim = rs.getTimestamp("l.dt_hora_fim_lancamento_uso");
			Calendar calFim = Calendar.getInstance();
			if (fim != null) {
				calFim.setTime(fim);
			}
			
			String nome = rs.getString("u.nome");
			String foto = rs.getString("u.foto");

			Element codigoXML = new Element("cod-lancamento-uso");
			codigoXML.setText("" +codigo);

			Element dataInicioXML = new Element("data-hora-inicio-uso");
			dataInicioXML.setText(calInicio.get(Calendar.DAY_OF_MONTH) + "/" + (calInicio.get(Calendar.MONTH)+1) + 
					"/" + calInicio.get(Calendar.YEAR) + " " + calInicio.get(Calendar.HOUR_OF_DAY) + ":" + 
					calInicio.get(Calendar.MINUTE));
			
			if (fim != null) {
				Element dataFimXML = new Element("data-hora-fim-uso");
				dataFimXML.setText(calFim.get(Calendar.DAY_OF_MONTH) + "/" + (calFim.get(Calendar.MONTH)+1) + "/" + 
					calFim.get(Calendar.YEAR) + " " + calFim.get(Calendar.HOUR_OF_DAY) + ":" + calFim.get(Calendar.MINUTE));
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
