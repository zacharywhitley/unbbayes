
package linfca.cadastro.usuario;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.DateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.StringTokenizer;

import linfca.Controller;
import linfca.Feature;
import org.jdom.Element;

public class DetalharUsuarioFeature implements Feature {

	/**
	 * <pre>
	 * <in>
	 *    <cod-usuario>1</cod-usuario>
	 * </in>
	 * 
	 * <out>
	 * 	  <cod-usuario>1</cod-usuario>
	 *    <cod-tipo-usuario>1<cod-tipo-usuario>
	 *    <cod-tipo-sexo>1</cod-tipo-sexo>
	 *    <cod-curso>2</cod-curso> 
	 *    <cod-semestre>5</cod-semestre>
	 *    <identificacao>9912345</identificacao>
	 *    <cpf>12345678912</cpf>
	 *    <nome>Mané</nome>
	 *    <sobrenome>Peregrino</sobrenome>
	 *    <dia>20</dia>
	 *    <mes>04</mes>
	 *    <ano>1980</ano>
	 *    <telefone>61-3662244</telefone>
	 *    <email>mp@provedor.com.br</email>
	 *    <endereco>SQN 410 Bl. B Apto. 101</endereco>
	 *    <foto>LÇKHÇAOFOHASFOHWQOHQWRLKÇHJLÇJA...</foto>
	 * </out> 
	 * </pre>
	 * @see Feature#process(Element)
	 */
	public Element process(Element in) throws Exception {
		
		Connection con = Controller.getInstance().makeConnection();
		
		String codUsuario  = in.getChild("cod-usuario").getTextTrim();
		
		PreparedStatement ps = con.prepareStatement("select * from usuario where cod_usuario = ?");
		
		ps.setInt(1, Integer.parseInt(codUsuario));
		
		ResultSet rs = ps.executeQuery();
		
		Element out = new Element("out");			
		
		while (rs.next()) {						
			
			int codTipoUsuario = rs.getInt("cod_tipo_usuario");
			int codTipoSexo = rs.getInt("cod_tipo_sexo");
			String identificacao = rs.getString("identificacao");
			String cpf = rs.getString("cpf");
			String nome = rs.getString("nome");
			String sobrenome = rs.getString("sobrenome");
			Date dtNascimento = rs.getDate("data_nascimento");
			String telefone = rs.getString("telefone");
			String email = rs.getString("email");
			String endereco = rs.getString("endereco");
			String foto = rs.getString("foto");
			int codSemestre = rs.getInt("cod_semestre");
			int codCurso = rs.getInt("cod_curso");
			
			StringTokenizer st = new StringTokenizer(dtNascimento.toString(), "-");			
			String ano = st.nextToken();
			String mes = st.nextToken();
			String dia = st.nextToken();
			
			Element codUsuarioXML = new Element("cod-usuario");
			Element codTipoUsuarioXML = new Element("cod-tipo-usuario");
			Element codTipoSexoXML = new Element("cod-tipo-sexo");
			Element codSemestreXML = new Element("cod-semestre");
			Element codCursoXML = new Element("cod-curso");
			Element identificacaoXML = new Element("identificacao");
			Element cpfXML = new Element("cpf");
			Element nomeXML = new Element("nome");
			Element sobrenomeXML = new Element("sobrenome");
			Element diaXML = new Element("dia");
			Element mesXML = new Element("mes");
			Element anoXML = new Element("ano");
			Element telefoneXML = new Element("telefone");
			Element emailXML = new Element("email");
			Element enderecoXML = new Element("endereco");
			Element fotoXML = new Element("foto");
			
			codUsuarioXML.setText(codUsuario);
			codTipoUsuarioXML.setText("" + codTipoUsuario);
			codTipoSexoXML.setText("" + codTipoSexo);
			codSemestreXML.setText("" + codSemestre);
			codCursoXML.setText("" + codCurso);
			identificacaoXML.setText(identificacao);
			cpfXML.setText(cpf);
			nomeXML.setText(nome);
			sobrenomeXML.setText(sobrenome);
			diaXML.setText(dia);
			mesXML.setText(mes);
			anoXML.setText(ano);
			telefoneXML.setText(telefone);
			emailXML.setText(email);
			enderecoXML.setText(endereco);
			fotoXML.setText(foto);
			
			out.getChildren().add(codUsuarioXML);
			out.getChildren().add(codTipoUsuarioXML);
			out.getChildren().add(codTipoSexoXML);
			out.getChildren().add(codSemestreXML);
			out.getChildren().add(codCursoXML);
			out.getChildren().add(identificacaoXML);
			out.getChildren().add(cpfXML);
			out.getChildren().add(nomeXML);
			out.getChildren().add(sobrenomeXML);
			out.getChildren().add(diaXML);
			out.getChildren().add(mesXML);
			out.getChildren().add(anoXML);
			out.getChildren().add(telefoneXML);
			out.getChildren().add(emailXML);
			out.getChildren().add(enderecoXML);
			out.getChildren().add(fotoXML);
			
		}
		
		rs.close();
		ps.close();
		con.close();
		
		return out;
	}

}
