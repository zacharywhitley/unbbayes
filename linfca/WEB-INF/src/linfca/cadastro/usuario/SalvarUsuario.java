
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

import java.util.Date;

public class SalvarUsuario implements Feature {

	/**
	 * <pre>
	 * <in>
	 *    <cod-usuario>1</cod-usuario>?
	 *    <cod-tipo-usuario>1</cod-tipo-usuario>
	 *    <cod-tipo-sexo>1</cod-tipo-sexo>
	 *    <identificacao>9912345</identificacao>
	 *    <cpf>12345678912</cpf>
	 *    <nome>Man�</nome>
	 *    <sobrenome>Peregrino</sobrenome>
	 *    <senha>supersecreta</senha>
	 *    <confirmacao-senha>supersecreta</confirmacao-senha>
	 *    <data-nascimento>1980/04/20</data-nascimento>
	 *    <email>mp@provedor.com.br</email>
	 *    <endereco>SQN 410 Bl. B Apto. 101</endereco>
	 *    <foto>L�KH�AOFOHASFOHWQOHQWRLK�HJL�JA...</foto>
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
		String email           = in.getChild("email").getTextTrim();
		String endereco        = in.getChild("endereco").getTextTrim();
		String foto            = in.getChild("foto").getTextTrim();
		String dataNascimentoS = in.getChild("data-nascimento").getTextTrim();
		
		Date dataNascimento = null;
		DateFormat df = DateFormat.getDateInstance();
	    try {
	    	dataNascimento = df.parse(dataNascimentoS);
	    } catch(ParseException e) {
	    	throw new RuntimeException("N�o foi capaz de criar a data: " + 
										dataNascimento);
	    }
		
		// cria o elemento de sa�da
		Element out = new Element("out");
		
		if ( in.getChild("cod-usuario") != null ) {
			
			String codUsuario = in.getChild("cod-usuario").getTextTrim();			
			System.out.println("codUsuario = " + codUsuario);						
			
			if (atualizarUsuario(codUsuario, codTipoUsuario, codTipoSexo, 
					identificacao, cpf, nome, sobrenome, senha, email, 
					endereco, foto, dataNascimento, con)) {
				System.out.println("Atualizou Usu�rio: " + nome);
				out.getChildren().add(new Element("ok"));
			} else {
				System.out.println("N�o Atualizou Usu�rio: " + nome);
				throw new RuntimeException("N�o foi poss�vel atualizar os dados " +
											"do usu�rio de nome " + nome + "!");
			}
			
			
		} else {			
			
			if (inserirUsuario(codTipoUsuario, codTipoSexo, identificacao, cpf,
					nome, sobrenome, senha, email, endereco, foto, 
					dataNascimento, con)) {
				System.out.println("Inseriu Usu�rio: " + nome);
				out.getChildren().add(new Element("ok"));
			} else {
				System.out.println("N�o Inseriu Usu�rio: " + nome);
				throw new RuntimeException("N�o foi poss�vel inserir " +
											"usu�rio de nome " + nome + "!");
			}
			
		}
					
		con.close();
		
		// retorna o elemento de sa�da
		return out;
	}
		
	private boolean inserirUsuario(String codTipoUsuario, String codTipoSexo, 
			String identificacao, String cpf, String nome, String sobrenome, 
			String senha, String email, String endereco, String foto, 
			Date dataNascimento, Connection con) throws SQLException {
		
		PreparedStatement ps = null;
		ResultSet rs = null;
		
		StringBuffer sql = new StringBuffer();
		sql.append("INSERT INTO ");
		sql.append("  Usuario ");
		sql.append("  (cod_tipo_usuario, cod_tipo_sexo, identificacao, cpf, ");
		sql.append("   nome, sobrenome, senha, email, endereco, foto, ");
		sql.append("   data_nascimento) ");
		sql.append("VALUES ");
		sql.append("  (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) ");
		
		ps = con.prepareStatement(sql.toString());
		
		ps.setInt(1, Integer.parseInt(codTipoUsuario));
		ps.setInt(2, Integer.parseInt(codTipoSexo));
		ps.setString(3, identificacao);
		ps.setString(4, cpf);
		ps.setString(5, nome);
		ps.setString(6, sobrenome);
		ps.setString(7, senha);
		ps.setString(8, email);
		ps.setString(9, endereco);
		ps.setBytes(10, Base64.decode(foto).getBytes());
		ps.setDate(11, new java.sql.Date(dataNascimento.getTime()));

		return (ps.executeUpdate() > 0);
		
	}
	
	private boolean atualizarUsuario(String codUsuario, String codTipoUsuario,
			String codTipoSexo, String identificacao, String cpf, String nome, 
			String sobrenome, String senha, String email, String endereco, 
			String foto, Date dataNascimento, Connection con) 
			throws SQLException {
		
		PreparedStatement ps = null;
		ResultSet rs = null;
		
		StringBuffer sql = new StringBuffer();
		sql.append("UPDATE ");
		sql.append("  Usuario ");
		sql.append("SET ");
		sql.append("  cod_tipo_usuario = ?, cod_tipo_sexo = ?, ");
		sql.append("  identificacao = ?, cpf = ?, nome = ?, sobrenome = ?, ");
		sql.append("  senha = ?, email = ?, endereco = ?, foto = ?, ");
		sql.append("   data_nascimento = ? ");
		sql.append("WHERE ");
		sql.append("  cod_usuario = ? ");
		
		ps = con.prepareStatement(sql.toString());
		
		ps.setInt(1, Integer.parseInt(codTipoUsuario));
		ps.setInt(2, Integer.parseInt(codTipoSexo));
		ps.setString(3, identificacao);
		ps.setString(4, cpf);
		ps.setString(5, nome);
		ps.setString(6, sobrenome);
		ps.setString(7, senha);
		ps.setString(8, email);
		ps.setString(9, endereco);
		ps.setBytes(10, Base64.decode(foto).getBytes());
		ps.setDate(11, new java.sql.Date(dataNascimento.getTime()));
		ps.setString(12, codUsuario);

		return (ps.executeUpdate() > 0);
		
	}

}
