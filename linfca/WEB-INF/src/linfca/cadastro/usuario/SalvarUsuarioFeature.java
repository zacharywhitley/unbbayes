
package linfca.cadastro.usuario;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;

import linfca.Controller;
import linfca.Feature;
import linfca.util.Base64;
import org.jdom.Element;
import java.security.*;

import java.sql.Date;

public class SalvarUsuarioFeature implements Feature {

	/**
	 * <pre>
	 * <in>
	 *    <cod-usuario>1</cod-usuario>?
	 *    <cod-tipo-usuario>1</cod-tipo-usuario>
	 *    <cod-tipo-sexo>1</cod-tipo-sexo>
	 *    <identificacao>9912345</identificacao>
	 *    <cpf>12345678912</cpf>
	 *    <nome>Mané</nome>
	 *    <sobrenome>Peregrino</sobrenome>
	 *    <senha>supersecreta</senha>
	 *    <confirmacao-senha>supersecreta</confirmacao-senha>
	 *    <dia>20</dia>
	 *    <mes>04</mes>
	 *    <ano>1980</ano>
	 *    <telefone>61-3681244</telefone>
	 *    <email>mp@provedor.com.br</email>
	 *    <endereco>SQN 410 Bl. B Apto. 101</endereco>
	 *    <foto>LÇKHÇAOFOHASFOHWQOHQWRLKÇHJLÇJA...</foto>
	 * </in>
	 * 
	 * <out>
	 * 	  </ok>
	 * </out> 
	 * </pre>
	 * @see Feature#process(Element)
	 */
	public Element process(Element in) throws Exception {
		
		Connection con = Controller.getInstance().makeConnection();
		

		String codTipoUsuario  = in.getChild("cod-tipo-usuario").getTextTrim();
		String codTipoSexo     = in.getChild("cod-tipo-sexo").getTextTrim();
		String identificacao   = in.getChild("identificacao").getTextTrim();
		String cpf             = in.getChild("cpf").getTextTrim();
		String nome            = in.getChild("nome").getTextTrim();
		String sobrenome       = in.getChild("sobrenome").getTextTrim();
		String senha           = in.getChild("senha").getTextTrim();
		String telefone        = in.getChild("telefone").getTextTrim();
		String email           = in.getChild("email").getTextTrim();
		String endereco        = in.getChild("endereco").getTextTrim();
		String foto            = in.getChild("foto").getTextTrim();
		String dia             = in.getChild("dia").getTextTrim();
		String mes             = in.getChild("mes").getTextTrim();
		String ano             = in.getChild("ano").getTextTrim();
		System.out.println(ano + '-' + mes + '-' + dia);
		Date dataNascimento    = Date.valueOf(ano + '-' + mes + '-' + dia);
		System.out.println("Data salvar: " + dataNascimento.toString());
		
		MessageDigest md = MessageDigest.getInstance("MD5");
		byte [] senhaEncode = md.digest(senha.getBytes());
		byte [] senhaEncode64 = Base64.encode(senhaEncode);

		// cria o elemento de saída
		Element out = new Element("out");
		
		if ( in.getChild("cod-usuario") != null ) {
			
			String codUsuario = in.getChild("cod-usuario").getTextTrim();			
			System.out.println("codUsuario = " + codUsuario);						
			
			if (atualizarUsuario(codUsuario, codTipoUsuario, codTipoSexo, 
					identificacao, cpf, nome, sobrenome, senhaEncode64, email, 
					endereco, foto, dataNascimento, telefone, con)) {
				System.out.println("Atualizou Usuário: " + nome);
				out.getChildren().add(new Element("ok"));
			} else {
				System.out.println("Não Atualizou Usuário: " + nome);
				throw new RuntimeException("Não foi possível atualizar os dados " +
											"do usuário de nome " + nome + "!");
			}
			
			
		} else {			
			
			if (inserirUsuario(codTipoUsuario, codTipoSexo, identificacao, cpf,
					nome, sobrenome, senhaEncode64, email, endereco, foto, 
					dataNascimento, telefone, con)) {
				System.out.println("Inseriu Usuário: " + nome);
				out.getChildren().add(new Element("ok"));
			} else {
				System.out.println("Não Inseriu Usuário: " + nome);
				throw new RuntimeException("Não foi possível inserir " +
											"usuário de nome " + nome + "!");
			}
			
		}
					
		con.close();
		
		// retorna o elemento de saída
		return out;
	}
		
	private boolean inserirUsuario(String codTipoUsuario, String codTipoSexo, 
			String identificacao, String cpf, String nome, String sobrenome, 
			byte [] senha, String email, String endereco, String foto, 
			Date dataNascimento, String telefone, Connection con)
			throws SQLException {
		
		PreparedStatement ps = null;
//		ResultSet rs = null;
		
		StringBuffer sql = new StringBuffer();
		sql.append("INSERT INTO ");
		sql.append("  Usuario ");
		sql.append("  (cod_tipo_usuario, cod_tipo_sexo, identificacao, cpf, ");
		sql.append("   nome, sobrenome, senha, email, endereco, foto, ");
		sql.append("   data_nascimento, telefone) ");
		sql.append("VALUES ");
		sql.append("  (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) ");
		
		ps = con.prepareStatement(sql.toString());
		
		ps.setInt(1, Integer.parseInt(codTipoUsuario));
		System.out.println("codTipoSexo: " + codTipoSexo);
		ps.setInt(2, Integer.parseInt(codTipoSexo));
		ps.setString(3, identificacao);
		ps.setString(4, cpf);
		ps.setString(5, nome);
		ps.setString(6, sobrenome);
		ps.setBytes(7, senha);
		ps.setString(8, email);
		ps.setString(9, endereco);
		ps.setString(10, foto);
		ps.setDate(11, new java.sql.Date(dataNascimento.getTime()));
		ps.setString(12, telefone);

		return (ps.executeUpdate() > 0);
		
	}
	
	private boolean atualizarUsuario(String codUsuario, String codTipoUsuario,
			String codTipoSexo, String identificacao, String cpf, String nome, 
			String sobrenome, byte [] senha, String email, String endereco, 
			String foto, Date dataNascimento, String telefone, Connection con) 
			throws SQLException {
		
		PreparedStatement ps = null;
//		ResultSet rs = null;
		
		StringBuffer sql = new StringBuffer();
		sql.append("UPDATE ");
		sql.append("  Usuario ");
		sql.append("SET ");
		sql.append("  cod_tipo_usuario = ?, cod_tipo_sexo = ?, ");
		sql.append("  identificacao = ?, cpf = ?, nome = ?, sobrenome = ?, ");
		sql.append("  senha = ?, email = ?, endereco = ?, foto = ?, ");
		sql.append("   data_nascimento = ?, telefone = ? ");
		sql.append("WHERE ");
		sql.append("  cod_usuario = ? ");
		
		ps = con.prepareStatement(sql.toString());
		
		ps.setInt(1, Integer.parseInt(codTipoUsuario));
		ps.setInt(2, Integer.parseInt(codTipoSexo));
		ps.setString(3, identificacao);
		ps.setString(4, cpf);
		ps.setString(5, nome);
		ps.setString(6, sobrenome);
		ps.setBytes(7, senha); 
		ps.setString(8, email);
		ps.setString(9, endereco);
		ps.setString(10, foto);		
		ps.setDate(11, new java.sql.Date(dataNascimento.getTime()));
		ps.setString(12, telefone);
		ps.setString(13, codUsuario);

		return (ps.executeUpdate() > 0);
		
	}

}
