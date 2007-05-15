package br.com.digitaxi.pessoa.pessoa.junit;

import junit.framework.TestCase;
import br.com.digitaxi.controleacesso.usuario.Usuario;
import br.com.digitaxi.pessoa.pessoa.Pessoa;
import br.com.digitaxi.pessoa.pessoa.persistence.PessoaDao;
import br.com.digitaxi.util.persistence.HibernateUtil;

public class TestePessoa extends TestCase {

	private static Pessoa instance = new Pessoa();
	private static PessoaDao dao = new PessoaDao();
	private static int id = 0;
	
	public TestePessoa(){
		
	}
	protected void setUp() {}
	protected void tearDown() {}
	
	public void test0BeginTransaction(){
		HibernateUtil.beginTransaction();
	}
	
	public void test1Cadastro(){
		instance.setNome("teste");
		instance.setDescricao("pessoa de teste");
		instance.setUsuario(new Usuario());
		id = dao.persist(instance);
		assertTrue(id != 0);
	}
	
	public void test2Consulta(){
		Pessoa pessoaCadastrado = dao.findById(id);
		boolean flag = pessoaCadastrado != null &&
			instance.getNome().equals(pessoaCadastrado.getNome());
		assertTrue(flag);
	}
	
	public void test3Deleta(){
		dao.delete(instance);
	}
	
	public void test99CommitTransaction(){
		HibernateUtil.commitTransaction();			
	}
}
