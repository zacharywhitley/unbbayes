package br.com.digitaxi.pessoa.uf.junit;

import junit.framework.TestCase;
import br.com.digitaxi.util.persistence.HibernateUtil;
import br.com.digitaxi.pessoa.uf.Uf;
import br.com.digitaxi.pessoa.uf.persistence.UfDao;

public class TesteUf extends TestCase {

	private static Uf instance = new Uf();
	private static UfDao dao = new UfDao();
	private static short id = 0;
	
	public TesteUf(){
		
	}
	protected void setUp() {}
	protected void tearDown() {}
	
	public void test0BeginTransaction(){
		HibernateUtil.beginTransaction();
	}
	
	public void test1Cadastro(){
		instance.setNome("Distrito Federal");
		id = dao.persist(instance);
		assertTrue(id != 0);
	}
	
	public void test2Consulta(){
		Uf ufCadastrada = dao.findById(id);
		boolean flag = ufCadastrada != null &&
			instance.getNome().equals(ufCadastrada.getNome());
		assertTrue(flag);
	}
	
	public void test3Deleta(){
		dao.delete(instance);
	}
	
	public void test99CommitTransaction(){
		HibernateUtil.commitTransaction();
	}
}