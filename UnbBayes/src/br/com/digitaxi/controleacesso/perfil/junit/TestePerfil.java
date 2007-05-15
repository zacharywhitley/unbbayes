package br.com.digitaxi.controleacesso.perfil.junit;

import java.util.Collection;
import java.util.Iterator;

import junit.framework.TestCase;
import br.com.digitaxi.util.persistence.HibernateUtil;
import br.com.digitaxi.controleacesso.perfil.Perfil;
import br.com.digitaxi.controleacesso.perfil.persistence.PerfilDao;

public class TestePerfil extends TestCase {

	//private static Perfil instance = new Perfil();
	private static PerfilDao dao = new PerfilDao();
	//private static short id = 0;
	
	public TestePerfil(){
		
	}
	protected void setUp() {}
	protected void tearDown() {}
	
	public void test0BeginTransaction(){
		HibernateUtil.beginTransaction();
	}
	
	/*public void test1Cadastro(){
		instance.setNome("Distrito Federal");
		id = dao.persist(instance);
		assertTrue(id != 0);
	}
	
	public void test2Consulta(){
		Perfil ufCadastrada = dao.findById(id);
		boolean flag = ufCadastrada != null &&
			instance.getNome().equals(ufCadastrada.getNome());
		assertTrue(flag);
	}
	
	public void test3Deleta(){
		dao.delete(instance);
	}*/
	
	public void test4Listagem(){
		Collection col = dao.list();
		Iterator it = col.iterator();
		while (it.hasNext()) {
			Perfil p = (Perfil)it.next();
			System.out.println(p.getDescricao());
			System.out.println(p.getNome());
		}		
		if (col.size()!=0) {
			assertTrue(true);			
		} else {
			assertTrue(false);
		}
	}
	
	public void test5ListagemParcial(){
		Collection col = dao.list(1,3);
		Iterator it = col.iterator();
		while (it.hasNext()) {
			Perfil p = (Perfil)it.next();
			System.out.println(p.getDescricao());
			System.out.println(p.getNome());
		}		
		if (col.size()!=0) {
			assertTrue(true);			
		} else {
			assertTrue(false);
		}
	}
	
	public void test99CommitTransaction(){
		HibernateUtil.commitTransaction();
	}
}