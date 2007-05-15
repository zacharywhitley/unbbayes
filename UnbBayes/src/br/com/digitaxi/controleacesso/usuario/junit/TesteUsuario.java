package br.com.digitaxi.controleacesso.usuario.junit;

import junit.framework.TestCase;
import br.com.digitaxi.controleacesso.perfil.Perfil;
import br.com.digitaxi.controleacesso.usuario.Usuario;
import br.com.digitaxi.controleacesso.usuario.persistence.UsuarioDao;
import br.com.digitaxi.util.persistence.HibernateUtil;
import br.com.digitaxi.util.security.CryptographyUtil;

public class TesteUsuario extends TestCase {

	private static Usuario instance = new Usuario();
	private static UsuarioDao dao = new UsuarioDao();
	private static int id = 0;
	
	public TesteUsuario(){
		
	}
	protected void setUp() {}
	protected void tearDown() {}
	
	public void test0BeginTransaction(){
		HibernateUtil.beginTransaction();
	}
	
	public void test1Cadastro(){
		instance.setLogin("teste");
		instance.setSenha(CryptographyUtil.encodeToMD5("abc123"));
		instance.setDescricao("usuario de teste");
		instance.setPerfil(new Perfil((short)5, null));
		id = dao.persist(instance);
		assertTrue(id != 0);
	}
	
	public void test2Consulta(){
		Usuario usuarioCadastrado = dao.findById(id);
		boolean flag = usuarioCadastrado != null &&
			instance.getLogin().equals(usuarioCadastrado.getLogin()) &&
			instance.getSenha().equals(usuarioCadastrado.getSenha());
		assertTrue(flag);
	}
	
	public void test3Deleta(){
		dao.delete(instance);
	}
	
	public void test99CommitTransaction(){
		HibernateUtil.commitTransaction();			
	}
}
